package uns.ac.rs.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import uns.ac.rs.entity.RegistrationInfo;

import java.time.LocalDateTime;
import java.util.Optional;

@ApplicationScoped
public class RegistrationInfoRepository implements PanacheRepository<RegistrationInfo> {
    public Optional<RegistrationInfo> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public void deleteExpiredInfo(LocalDateTime expiryTime) {
        delete("timestamp < ?1", expiryTime);
    }
}
