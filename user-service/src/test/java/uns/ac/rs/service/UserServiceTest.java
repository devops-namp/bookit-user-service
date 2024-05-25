package uns.ac.rs.service;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uns.ac.rs.controller.exception.EmailAlreadyInUseException;
import uns.ac.rs.controller.exception.UsernameAlreadyInUseException;
import uns.ac.rs.entity.RegistrationInfo;
import uns.ac.rs.entity.Role;
import uns.ac.rs.entity.User;
import uns.ac.rs.repository.RegistrationInfoRepository;
import uns.ac.rs.repository.UserRepository;
import uns.ac.rs.security.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RegistrationInfoRepository registrationInfoRepository;

    @InjectMocks
    @Inject
    UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveRegistrationInfo_nonRepeatingRequest_success() {
        var registrationInfo = generateRegistrationInfo("test", "test@test.com");
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(registrationInfoRepository.findByEmail(registrationInfo.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername("test")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        userService.saveRegistrationInfo(registrationInfo, "testPassword123");

        assertTrue(
        registrationInfo.getCode() != null &&
            registrationInfo.getCode().length() == UserService.REGISTRATION_CODE_LEN &&
            registrationInfo.getPassword().equals("encodedPassword")
        );
        verify(registrationInfoRepository).persistAndFlush(
            argThat(obj -> obj.getUsername().equals("test") && obj.getEmail().equals("test@test.com"))
        );
        verify(emailService).sendRegistrationCodeEmail(any());

        verifyNoMoreInteractions(registrationInfoRepository);
        verifyNoMoreInteractions(emailService);
    }

    @Test
    void testSaveRegistrationInfo_repeatingRequest_success() {
        var newRegistrationInfo = generateRegistrationInfo("test1", "test1@test.com");
        var oldRegistrationInfo = generateRegistrationInfo("test2", "test1@test.com");
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(registrationInfoRepository.findByEmail(newRegistrationInfo.getEmail())).thenReturn(Optional.of(oldRegistrationInfo));
        when(userRepository.findByUsername("test1")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test1@test.com")).thenReturn(Optional.empty());
        userService.saveRegistrationInfo(newRegistrationInfo, "testPassword123");

        assertTrue(
            oldRegistrationInfo.getCode() != null &&
                oldRegistrationInfo.getCode().length() == UserService.REGISTRATION_CODE_LEN &&
                oldRegistrationInfo.getPassword().equals("encodedPassword") &&
                oldRegistrationInfo.getUsername().equals(newRegistrationInfo.getUsername())
        );
        verify(registrationInfoRepository).persistAndFlush(
            argThat(obj -> obj.getUsername().equals("test1") && obj.getEmail().equals("test1@test.com"))
        );
        verify(emailService).sendRegistrationCodeEmail(any());

        verifyNoMoreInteractions(registrationInfoRepository);
        verifyNoMoreInteractions(emailService);
    }

    @Test
    void testSaveRegistrationInfo_usernameAlreadyInUse() {
        var registrationInfo = generateRegistrationInfo("test", "test1@test.com");
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(new User()));

        assertThrows(UsernameAlreadyInUseException.class, () ->
            userService.saveRegistrationInfo(registrationInfo, "testPassword123"));

        verifyNoInteractions(registrationInfoRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void testSaveRegistrationInfo_emailAlreadyInUse() {
        var registrationInfo = generateRegistrationInfo("test1", "test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));

        assertThrows(EmailAlreadyInUseException.class, () ->
            userService.saveRegistrationInfo(registrationInfo, "testPassword123"));

        verifyNoInteractions(registrationInfoRepository);
        verifyNoInteractions(emailService);
    }

    RegistrationInfo generateRegistrationInfo(String username, String email) {
        var registrationInfo = new RegistrationInfo();
        registrationInfo.setUsername(username);
        registrationInfo.setRole(Role.GUEST);
        registrationInfo.setEmail(email);
        registrationInfo.setFirstName("Test");
        registrationInfo.setLastName("Test");
        registrationInfo.setTimestamp(LocalDateTime.now());
        return registrationInfo;
    }
}
