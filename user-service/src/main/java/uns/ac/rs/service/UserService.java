package uns.ac.rs.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import uns.ac.rs.controller.exception.InvalidRegistrationCodeException;
import uns.ac.rs.controller.exception.UserAlreadyExistsException;
import uns.ac.rs.entity.TempUser;
import uns.ac.rs.entity.User;
import uns.ac.rs.repository.TempUserRepository;
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
    TempUserRepository tempUserRepository;

    private static final int REGISTRATION_CODE_LEN = 6;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void saveTempUser(TempUser tempUser, String plainTextPassword) {
        if (isAlreadyRegistered(tempUser)) {
            throw new UserAlreadyExistsException();
        }
        var code = RandomStringUtils.random(REGISTRATION_CODE_LEN, true, true).toUpperCase();

        tempUser.setPassword(passwordEncoder.encode(plainTextPassword));
        tempUser.setCode(code);
        var tempUserOptional = tempUserRepository.findByEmail(tempUser.getEmail());
        if (tempUserOptional.isPresent()) {
            replaceTempUser(tempUser, tempUserOptional.get());
        } else {
            tempUserRepository.persistAndFlush(tempUser);
        }
        emailService.sendRegistrationCodeEmail(tempUser);
    }

    @Transactional
    public void confirmRegistration(String email, String code) {
        var tempUserOptional = tempUserRepository.findByEmail(email);
        if (tempUserOptional.isEmpty() || !tempUserOptional.get().getCode().equals(code)) {
            throw new InvalidRegistrationCodeException();
        }
        var tempUser = tempUserOptional.get();
        userRepository.persistAndFlush(new User(tempUser));
        tempUserRepository.delete(tempUser);
    }

    private boolean isAlreadyRegistered(TempUser tempUser) {
        return userRepository.findByUsername(tempUser.getUsername()).isPresent() ||
            userRepository.findByEmail(tempUser.getEmail()).isPresent();
    }

    private void replaceTempUser(TempUser newTempUser, TempUser oldTempUser) {
        oldTempUser.setUsername(newTempUser.getUsername());
        oldTempUser.setPassword(newTempUser.getPassword());
        oldTempUser.setFirstName(newTempUser.getFirstName());
        oldTempUser.setLastName(newTempUser.getLastName());
        oldTempUser.setCity(newTempUser.getCity());
        oldTempUser.setCode(newTempUser.getCode());
        tempUserRepository.persistAndFlush(oldTempUser);
    }
}
