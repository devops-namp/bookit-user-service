package uns.ac.rs.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import uns.ac.rs.entity.TempUser;

import java.util.Optional;

@ApplicationScoped
public class TempUserRepository implements PanacheRepository<TempUser> {
    public Optional<TempUser> find(String email, String code) {
        return find("email = ?1 and code = ?2", email, code).firstResultOptional();
    }
}
