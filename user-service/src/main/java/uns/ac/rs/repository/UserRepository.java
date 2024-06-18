package uns.ac.rs.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import uns.ac.rs.entity.User;

import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    @Transactional
    public Optional<Boolean> findAutoApproveByUsername(String username) {
        return getEntityManager().createQuery("SELECT u.autoApprove FROM User u WHERE u.username = :username", Boolean.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }
}
