package uns.ac.rs.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Template;
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
    Mailer mailer;
    @Inject
    Template registrationCodeTemplate;
    @Inject
    UserRepository userRepository;
    @Inject
    TempUserRepository tempUserRepository;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void saveTempUser(TempUser tempUser, String plainTextPassword) {
        if (isRegistrationRequestAlreadySent(tempUser.getUsername(), tempUser.getPassword())) {
            throw new UserAlreadyExistsException();
        }
        tempUser.setPassword(passwordEncoder.encode(plainTextPassword));
        var code = RandomStringUtils.random(6, true, true).toUpperCase();
        tempUser.setCode(code);
        tempUserRepository.persistAndFlush(tempUser);

        var instance = registrationCodeTemplate
            .data("name", tempUser.getFirstName())
            .data("code", code);
        mailer.send(Mail.withHtml(tempUser.getEmail(), "[BookIt] Registration code", instance.render()));
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

    private boolean isRegistrationRequestAlreadySent(String username, String email) {
        return userRepository.findByUsername(username).isPresent() || userRepository.findByEmail(email).isPresent() ||
            tempUserRepository.findByEmail(email).isPresent();
    }
}
