package uns.ac.rs.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import uns.ac.rs.controller.exception.InvalidRegistrationCodeException;
import uns.ac.rs.controller.exception.UserAlreadyExistsException;
import uns.ac.rs.entity.RegistrationInfo;
import uns.ac.rs.entity.User;
import uns.ac.rs.repository.RegistrationInfoRepository;
import uns.ac.rs.repository.UserRepository;
import uns.ac.rs.security.PasswordEncoder;

import java.util.Optional;

@ApplicationScoped
public class UserService {

    @Inject
    PasswordEncoder passwordEncoder;
    @Inject
    EmailService emailService;
    @Inject
    UserRepository userRepository;
    @Inject
    RegistrationInfoRepository registrationInfoRepository;

    private static final int REGISTRATION_CODE_LEN = 6;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void saveRegistrationInfo(RegistrationInfo registrationInfo, String plainTextPassword) {
        if (isAlreadyRegistered(registrationInfo)) {
            throw new UserAlreadyExistsException();
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

    private boolean isAlreadyRegistered(RegistrationInfo registrationInfo) {
        return userRepository.findByUsername(registrationInfo.getUsername()).isPresent() ||
            userRepository.findByEmail(registrationInfo.getEmail()).isPresent();
    }

    private void replaceRegistrationInfo(RegistrationInfo newInfo, RegistrationInfo oldInfo) {
        oldInfo.setUsername(newInfo.getUsername());
        oldInfo.setPassword(newInfo.getPassword());
        oldInfo.setRole(newInfo.getRole());
        oldInfo.setFirstName(newInfo.getFirstName());
        oldInfo.setLastName(newInfo.getLastName());
        oldInfo.setCity(newInfo.getCity());
        oldInfo.setCode(newInfo.getCode());
        registrationInfoRepository.persistAndFlush(oldInfo);
    }
}
