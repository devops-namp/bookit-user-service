package uns.ac.rs.controller;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uns.ac.rs.controller.request.ConfirmRegistrationRequest;
import uns.ac.rs.controller.request.ProfileUpdateRequest;
import uns.ac.rs.controller.request.RegistrationRequest;
import uns.ac.rs.resources.PostgresResource;
import uns.ac.rs.service.EmailService;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
public class UserControllerTest {

    private static final String EXISTING_USERNAME = "username1";
    private static final String EXISTING_EMAIL = "name@example.com";

    @InjectMock
    EmailService emailService;

    @BeforeEach
    public void setup() {
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
