package uns.ac.rs.service;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uns.ac.rs.controller.events.AutoApproveEvent;
import uns.ac.rs.controller.exception.*;
import uns.ac.rs.entity.RegistrationInfo;
import uns.ac.rs.entity.Role;
import uns.ac.rs.entity.User;
import uns.ac.rs.repository.RegistrationInfoRepository;
import uns.ac.rs.repository.UserRepository;
import uns.ac.rs.security.PasswordEncoder;
import uns.ac.rs.security.TokenUtils;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    @Mock
    private SecurityIdentity securityIdentity;

    @Mock
    private TokenUtils tokenUtils;

    @InjectMocks
    @Inject
    UserService userService;

    @Mock
    private Emitter<AutoApproveEvent> autoApproveEmmiter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lenient().when(securityIdentity.getPrincipal()).thenReturn(mock(Principal.class));
        userService.autoApproveEmmiter = autoApproveEmmiter;
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

    @Test
    @SneakyThrows
    public void testUpdateProfile_usernameChanged_success() {
        var currentUsername = "testuser";
        var newUsername = "newuser";
        var email = "newemail@test.com";
        var firstName = "First";
        var lastName = "Last";
        var city = "City";

        var user = new User();
        user.setUsername(currentUsername);
        user.setEmail("oldemail@test.com");

        when(securityIdentity.getPrincipal().getName()).thenReturn(currentUsername);

        when(userRepository.findByUsername(currentUsername)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername(newUsername)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(tokenUtils.generateToken(any(), any())).thenReturn("token");

        var result = userService.updateProfile(currentUsername, newUsername, email, firstName, lastName, city);

        assertEquals(newUsername, result.a.getUsername());
        assertEquals(email, result.a.getEmail());
        assertEquals(firstName, result.a.getFirstName());
        assertEquals(lastName, result.a.getLastName());
        assertEquals(city, result.a.getCity());
        assertEquals("token", result.b);

        verify(userRepository).persistAndFlush(user);
    }

    @Test
    public void testUpdateProfile_usernameNotChanged_success() {
        var currentUsername = "testuser";
        var newUsername = "testuser";
        var email = "newemail@test.com";
        var firstName = "First";
        var lastName = "Last";
        var city = "City";

        var user = new User();
        user.setUsername(currentUsername);
        user.setEmail("oldemail@test.com");

        when(securityIdentity.getPrincipal().getName()).thenReturn(currentUsername);
        when(userRepository.findByUsername(currentUsername)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());


        var result = userService.updateProfile(currentUsername, newUsername, email, firstName, lastName, city);

        assertEquals(currentUsername, result.a.getUsername());
        assertEquals(email, result.a.getEmail());
        assertEquals(firstName, result.a.getFirstName());
        assertEquals(lastName, result.a.getLastName());
        assertEquals(city, result.a.getCity());
        assertNull(result.b);

        verifyNoInteractions(tokenUtils);
        verify(userRepository).persistAndFlush(user);
    }


    @Test
    public void testUpdateProfile_userDoesNotExist() {
        var currentUsername = "testuser";

        when(securityIdentity.getPrincipal().getName()).thenReturn(currentUsername);
        when(userRepository.findByUsername(currentUsername)).thenReturn(Optional.empty());

        assertThrows(UserDoesNotExistException.class, () ->
            userService.updateProfile(currentUsername, "new", "new@test.com", "a", "b", "c"));
    }

    @Test
    public void testUpdateProfile_usernameAlreadyInUse() {
        var currentUsername = "testuser";
        var newUsername = "newuser";

        var user = new User();
        user.setUsername(currentUsername);
        user.setEmail("oldemail@test.com");

        when(securityIdentity.getPrincipal().getName()).thenReturn(currentUsername);
        when(userRepository.findByUsername(currentUsername)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername(newUsername)).thenReturn(Optional.of(new User()));

        assertThrows(UsernameAlreadyInUseException.class, () ->
            userService.updateProfile(currentUsername, newUsername, "new@test.com", "a", "b", "c"));
    }

    @Test
    public void testUpdateProfile_emailAlreadyInUse() {
        var currentUsername = "testuser";
        var email = "newemail@test.com";

        var user = new User();
        user.setUsername(currentUsername);
        user.setEmail("oldemail@test.com");

        when(securityIdentity.getPrincipal().getName()).thenReturn(currentUsername);
        when(userRepository.findByUsername(currentUsername)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        assertThrows(EmailAlreadyInUseException.class, () ->
            userService.updateProfile(currentUsername, currentUsername, email, "a", "b", "c"));
    }

    @Test
    public void testChangePassword_success() {
        var username = "testuser";
        var currentPassword = "currentPassword";
        var newPassword = "newPassword";
        var encodedCurrentPassword = "encodedCurrentPassword";
        var encodedNewPassword = "encodedNewPassword";

        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedCurrentPassword);

        when(securityIdentity.getPrincipal().getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(currentPassword)).thenReturn(encodedCurrentPassword);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        userService.changePassword(username, currentPassword, newPassword);

        verify(userRepository).persistAndFlush(user);
    }

    @Test
    public void testChangePassword_userDoesNotExist() {
        var username = "testuser";
        var currentPassword = "currentPassword";
        var newPassword = "newPassword";

        when(securityIdentity.getPrincipal().getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UserDoesNotExistException.class, () -> userService.changePassword(username, currentPassword, newPassword));
    }

    @Test
    public void testChangePassword_passwordDoesNotMatch() {
        var username = "testuser";
        var currentPassword = "currentPassword";
        var newPassword = "newPassword";
        var encodedCurrentPassword = "encodedCurrentPassword";
        var incorrectEncodedCurrentPassword = "incorrectEncodedCurrentPassword";

        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedCurrentPassword);

        when(securityIdentity.getPrincipal().getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(currentPassword)).thenReturn(incorrectEncodedCurrentPassword);

        assertThrows(PasswordDoesNotMatchException.class, () -> userService.changePassword(username, currentPassword, newPassword));
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

    @Test
    void testIncrementCounter_userExists() {
        User user = new User();
        user.setUsername("testUser");
        user.setRejectedReservationsCount(0);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        userService.incrementCounter("testUser");

        assertEquals(1, user.getRejectedReservationsCount());
        verify(userRepository).persist(user);
    }

    @Test
    void testIncrementCounter_userDoesNotExist() {
        when(userRepository.findByUsername("nonExistingUser")).thenReturn(Optional.empty());

        assertThrows(UserDoesNotExistException.class, () -> userService.incrementCounter("nonExistingUser"));
    }

    @Test
    void testGetAutoapprove_userExists() {
        when(userRepository.findAutoApproveByUsername("testUser")).thenReturn(Optional.of(true));

        boolean autoapprove = userService.getAutoapprove("testUser");

        assertTrue(autoapprove);
    }

    @Test
    void testGetAutoapprove_userDoesNotExist() {
        when(userRepository.findAutoApproveByUsername("nonExistingUser")).thenReturn(Optional.empty());

        assertThrows(UserDoesNotExistException.class, () -> userService.getAutoapprove("nonExistingUser"));
    }

    @Test
    void testChangeAutoapprove_userExists() {
        User user = new User();
        user.setUsername("testUser");
        user.setAutoApprove(false);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        userService.changeAutoapprove("testUser", true);

        assertTrue(user.isAutoApprove());
        verify(userRepository).persist(user);
        verify(autoApproveEmmiter, times(1)).send(any(AutoApproveEvent.class));
    }

    @Test
    void testChangeAutoapprove_userDoesNotExist() {
        when(userRepository.findByUsername("nonExistingUser")).thenReturn(Optional.empty());
        assertThrows(UserDoesNotExistException.class, () -> userService.changeAutoapprove("nonExistingUser", true));
    }

    @Test
    void testSendAutoapprove() {
        AutoApproveEvent event = new AutoApproveEvent();
        event.setUsername("testUser");

        when(userRepository.findAutoApproveByUsername("testUser")).thenReturn(Optional.of(true));

        userService.sendAutoapprove(event);

        assertTrue(event.isAutoapprove());
        verify(autoApproveEmmiter).send(event);
    }

    @Test
    void testGetRejectCounts_allUsersExist() {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setRejectedReservationsCount(2);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setRejectedReservationsCount(3);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(userRepository.findByUsername("user2")).thenReturn(Optional.of(user2));

        Map<String, Integer> rejectCounts = userService.getRejectCounts(Arrays.asList("user1", "user2"));

        assertEquals(2, rejectCounts.get("user1"));
        assertEquals(3, rejectCounts.get("user2"));
    }

    @Test
    void testGetRejectCounts_userDoesNotExist() {
        when(userRepository.findByUsername("nonExistingUser")).thenReturn(Optional.empty());

        assertThrows(UserDoesNotExistException.class, () -> userService.getRejectCounts(Arrays.asList("nonExistingUser")));
    }


}
