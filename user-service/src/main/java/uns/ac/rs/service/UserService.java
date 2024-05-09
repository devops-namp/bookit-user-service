package uns.ac.rs.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import uns.ac.rs.repository.UserRepository;

@ApplicationScoped
public class UserService {
    @Inject
    UserRepository userRepository;
}
