package uns.ac.rs.controller;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uns.ac.rs.controller.request.ConfirmRegistrationRequest;
import uns.ac.rs.controller.request.ProfileUpdateRequest;
import uns.ac.rs.controller.request.RegistrationRequest;
import uns.ac.rs.entity.RegistrationInfo;
import uns.ac.rs.entity.Role;
import uns.ac.rs.entity.User;
import uns.ac.rs.repository.RegistrationInfoRepository;
import uns.ac.rs.repository.UserRepository;
import uns.ac.rs.resources.PostgresResource;
import uns.ac.rs.service.EmailService;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
public class UserControllerTest {

    private static final String EXISTING_USERNAME = "username1";
    private static final String EXISTING_EMAIL = "name@example.com";

    @InjectMock
    EmailService emailService;

    @Inject
    UserRepository userRepository;

    @Inject
    RegistrationInfoRepository registrationInfoRepository;

    @BeforeEach
    @Transactional
    public void setup() {
        userRepository.deleteAll();
        registrationInfoRepository.deleteAll();

        User user1 = new User();
        user1.setUsername("username1");
        user1.setPassword("y4t6TyLTziBF7p9CT75tfqTGmMiNMAs5dyzMZKL2e9g=");
        user1.setEmail("name@example.com");
        user1.setFirstName("Test");
        user1.setLastName("User");
        user1.setCity("Test City");
        user1.setRejectedReservationsCount(0);
        user1.setRole(Role.GUEST);
        user1.setAutoApprove(false);
        userRepository.persist(user1);

        User user2 = new User();
        user2.setUsername("username2");
        user2.setPassword("y4t6TyLTziBF7p9CT75tfqTGmMiNMAs5dyzMZKL2e9g=");
        user2.setEmail("name2@example.com");
        user2.setFirstName("Test");
        user2.setLastName("User");
        user2.setCity("Test City");
        user2.setRejectedReservationsCount(0);
        user2.setRole(Role.HOST);
        user2.setAutoApprove(false);
        userRepository.persist(user2);

        RegistrationInfo registrationInfo = new RegistrationInfo();
        registrationInfo.setTimestamp(LocalDateTime.now());
        registrationInfo.setUsername("some_user");
        registrationInfo.setRole(Role.GUEST);
        registrationInfo.setEmail("request@test.com");
        registrationInfo.setCode("123ABC");

        registrationInfoRepository.persist(registrationInfo);

        Mockito.doNothing().when(emailService).sendRegistrationCodeEmail(Mockito.any());
    }

    @Test
    void testRegister_success() {
        var registrationRequest = generateRegistrationRequest("abc", "abc@test.com");

        given()
            .contentType(ContentType.JSON)
            .body(registrationRequest)
            .when().post("/users")
            .then()
            .statusCode(201);
    }

    @Test
    void testRegister_invalidPassword_tooShort() {
        var registrationRequest = generateRegistrationRequest("abc", "abc@test.com");
        registrationRequest.setPassword("abc12");

        given()
            .contentType(ContentType.JSON)
            .body(registrationRequest)
            .when().post("/users")
            .then()
            .statusCode(400);
    }

    @Test
    void testRegister_invalidPassword_noDigits() {
        var registrationRequest = generateRegistrationRequest("abc", "abc@test.com");
        registrationRequest.setPassword("abcsdADrg%");

        given()
            .contentType(ContentType.JSON)
            .body(registrationRequest)
            .when().post("/users")
            .then()
            .statusCode(400);
    }

    @Test
    void testRegister_invalidPassword_noLetters() {
        var registrationRequest = generateRegistrationRequest("abc", "abc@test.com");
        registrationRequest.setPassword("1223%$23#");

        given()
            .contentType(ContentType.JSON)
            .body(registrationRequest)
            .when().post("/users")
            .then()
            .statusCode(400);
    }

    @Test
    void testRegister_usernameAlreadyExists() {
        var registrationRequest = generateRegistrationRequest(EXISTING_USERNAME, "email@test.com");

        given()
            .contentType(ContentType.JSON)
            .body(registrationRequest)
            .when().post("/users")
            .then()
            .statusCode(400);
    }

    @Test
    void testRegister_emailAlreadyExists() {
        var registrationRequest = generateRegistrationRequest("abc", EXISTING_EMAIL);

        given()
            .contentType(ContentType.JSON)
            .body(registrationRequest)
            .when().post("/users")
            .then()
            .statusCode(400);
    }

    @Test
    void testConfirmRegistration_success() {
        var request = new ConfirmRegistrationRequest("request@test.com", "123ABC");

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/users/confirm")
            .then()
            .statusCode(201);
    }

    @Test
    void testConfirmRegistration_requestDoesNotExist() {
        var request = new ConfirmRegistrationRequest("request123@test.com", "123ABC");

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/users/confirm")
            .then()
            .statusCode(404);
    }

    @Test
    void testConfirmRegistration_incorrectCode() {
        var request = new ConfirmRegistrationRequest("request@test.com", "123EFG");

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/users/confirm")
            .then()
            .statusCode(404);
    }

    @Test
    @TestSecurity(user = "username1", roles = {"GUEST"})
    public void testUpdateProfile_success() {
        var request = new ProfileUpdateRequest("username1", "name@example.com", "Name", "Surname", "BG");

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/users/username1")
            .then()
            .statusCode(200)
            .body("username", equalTo("username1"))
            .body("email", equalTo("name@example.com"))
            .body("firstName", equalTo("Name"))
            .body("lastName", equalTo("Surname"))
            .body("city", equalTo("BG"))
            .body("token", equalTo(null));
    }

    @Test
    @TestSecurity(user = "username2", roles = {"GUEST"})
    public void testUpdateProfile_unauthorized() {
        var request = new ProfileUpdateRequest("username1", "name@example.com", "Name", "Surname", "BG");

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/users/username1")
            .then()
            .statusCode(403);
    }

    @Test
    @TestSecurity(user = "username2", roles = {"GUEST"})
    public void testUpdateProfile_usernameAlreadyInUse() {
        var request = new ProfileUpdateRequest("username1", "name123@example.com", "Name", "Surname", "BG");

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/users/username2")
            .then()
            .statusCode(400)
            .body(containsString("Username already in use"));
    }

    @Test
    @TestSecurity(user = "username2", roles = {"GUEST"})
    public void testUpdateProfile_emailAlreadyInUse() {
        var request = new ProfileUpdateRequest("username3", "name@example.com", "Name", "Surname", "BG");

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/users/username2")
            .then()
            .statusCode(400)
            .body(containsString("Email already in use"));
    }

    private RegistrationRequest generateRegistrationRequest(String username, String email) {
        return new RegistrationRequest(
            username,
            "myPassword123",
            email,
            "Name",
            "Surname",
            "Novi Sad",
            "HOST"
        );
    }

}
