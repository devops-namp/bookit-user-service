package uns.ac.rs.service;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import uns.ac.rs.repository.RegistrationInfoRepository;

import java.time.LocalDateTime;

@ApplicationScoped
public class SchedulerService {

    @Inject
    RegistrationInfoRepository registrationInfoRepository;

    @Scheduled(every = "5m")
    @Transactional
    void deleteExpiredRegistrationInfo() {
        registrationInfoRepository.deleteExpiredInfo(LocalDateTime.now().minusMinutes(30));
    }
}
