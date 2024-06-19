package uns.ac.rs.service;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.antlr.v4.runtime.misc.Pair;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import uns.ac.rs.controller.exception.*;
import uns.ac.rs.controller.request.CheckReservationsRequest;
import uns.ac.rs.entity.RegistrationInfo;
import uns.ac.rs.entity.Role;
import uns.ac.rs.entity.User;
import uns.ac.rs.repository.RegistrationInfoRepository;
import uns.ac.rs.repository.UserRepository;
import uns.ac.rs.security.PasswordEncoder;
import uns.ac.rs.security.TokenUtils;

import java.time.LocalDateTime;
import java.util.Optional;

@ApplicationScoped
public class UserService {

    @Inject
    SecurityIdentity securityIdentity;
    @Inject
    PasswordEncoder passwordEncoder;
    @Inject
    TokenUtils tokenUtils;
    @Inject
    EmailService emailService;
    @Inject
    UserRepository userRepository;
    @Inject
    RegistrationInfoRepository registrationInfoRepository;

    @Inject
    @RestClient
    AccommodationApiHttpClient httpClient;

    public static final int REGISTRATION_CODE_LEN = 6;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User get(String username) {
        return this.userRepository.findByUsername(username).orElseThrow(UserDoesNotExistException::new);
    }

    @Transactional
    public void saveRegistrationInfo(RegistrationInfo registrationInfo, String plainTextPassword) {
        if (userRepository.findByUsername(registrationInfo.getUsername()).isPresent()) {
            throw new UsernameAlreadyInUseException();
        }
        if (userRepository.findByEmail(registrationInfo.getEmail()).isPresent()) {
            throw new EmailAlreadyInUseException();
        }
        var code = RandomStringUtils.random(REGISTRATION_CODE_LEN, true, true).toUpperCase();

        registrationInfo.setPassword(passwordEncoder.encode(plainTextPassword));
        registrationInfo.setCode(code);
        var existingRegistrationInfo = registrationInfoRepository.findByEmail(registrationInfo.getEmail());
        if (existingRegistrationInfo.isPresent()) {
            replaceRegistrationInfo(registrationInfo, existingRegistrationInfo.get());
        } else {
            registrationInfoRepository.persistAndFlush(registrationInfo);
        }
        emailService.sendRegistrationCodeEmail(registrationInfo);
    }

    @Transactional
    public void confirmRegistration(String email, String code) {
        var registrationInfoOptional = registrationInfoRepository.findByEmail(email);
        if (registrationInfoOptional.isEmpty() || !registrationInfoOptional.get().getCode().equals(code)) {
            throw new InvalidRegistrationCodeException();
        }
        var registrationInfo = registrationInfoOptional.get();
        userRepository.persistAndFlush(new User(registrationInfo));
        registrationInfoRepository.delete(registrationInfo);
    }

    @Transactional
    public Pair<User, String> updateProfile(String currentUsername, String newUsername, String email, String firstName, String lastName, String city) {
        if (!securityIdentity.getPrincipal().getName().equals(currentUsername)) {
            throw new GenericUnauthorizedException();
        }
        var currentUserOptional = userRepository.findByUsername(currentUsername);
        if (currentUserOptional.isEmpty()) {
            throw new UserDoesNotExistException();
        }
        var user = currentUserOptional.get();
        if (!newUsername.equals(currentUsername) && userRepository.findByUsername(newUsername).isPresent()) {
            throw new UsernameAlreadyInUseException();
        }
        if (!email.equals(user.getEmail()) && userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyInUseException();
        }
        setUserProperties(user, newUsername, email, firstName, lastName, city);
        userRepository.persistAndFlush(user);
        String token = null;
        if (!currentUsername.equals(newUsername)) {
            token = generateToken(user);
        }
        return new Pair<>(user, token);
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        if (!securityIdentity.getPrincipal().getName().equals(username)) {
            throw new GenericUnauthorizedException();
        }
        var user = userRepository.findByUsername(username).orElseThrow(UserDoesNotExistException::new);
        if (!user.getPassword().equals(passwordEncoder.encode(currentPassword))) {
            throw new PasswordDoesNotMatchException();
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.persistAndFlush(user);
    }

    @Transactional
    public boolean deleteProfile(String username) {
        if (!securityIdentity.getPrincipal().getName().equals(username)) {
            throw new GenericUnauthorizedException();
        }
        var user = userRepository.findByUsername(username).orElseThrow(UserDoesNotExistException::new);
        var role = user.getRole();
        var result = httpClient.getResource(new CheckReservationsRequest(username, user.getRole().toString()));

        if (!result.isHasFutureReservations()) {
            userRepository.delete(user);
            return role.equals(Role.HOST);
        } else {
            throw new UserHasFutureReservationsException();
        }
    }

    private void setUserProperties(User user, String newUsername, String email, String firstName, String lastName, String city) {
        user.setUsername(newUsername);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCity(city);
    }

    private void replaceRegistrationInfo(RegistrationInfo newInfo, RegistrationInfo oldInfo) {
        oldInfo.setUsername(newInfo.getUsername());
        oldInfo.setPassword(newInfo.getPassword());
        oldInfo.setRole(newInfo.getRole());
        oldInfo.setFirstName(newInfo.getFirstName());
        oldInfo.setLastName(newInfo.getLastName());
        oldInfo.setCity(newInfo.getCity());
        oldInfo.setCode(newInfo.getCode());
        oldInfo.setTimestamp(LocalDateTime.now());
        registrationInfoRepository.persistAndFlush(oldInfo);
    }

    private String generateToken(User user) {
        try {
            return tokenUtils.generateToken(user.getUsername(), user.getRole());
        } catch (Exception ignore) {
            return null;
        }
    }
}
