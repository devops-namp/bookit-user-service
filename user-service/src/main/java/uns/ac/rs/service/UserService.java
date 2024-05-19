package uns.ac.rs.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import uns.ac.rs.controller.exception.UserAlreadyExistsException;
import uns.ac.rs.entity.User;
import uns.ac.rs.repository.UserRepository;
import uns.ac.rs.security.PasswordEncoder;

import java.util.Optional;

@ApplicationScoped
public class UserService {

    @Inject
    PasswordEncoder passwordEncoder;
    @Inject
    UserRepository userRepository;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void register(User user, String plainTextPassword) {
        if (userRepository.findByUsername(user.getUsername()).isPresent() || userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        user.setPassword(passwordEncoder.encode(plainTextPassword));
        userRepository.persistAndFlush(user);
    }

}
