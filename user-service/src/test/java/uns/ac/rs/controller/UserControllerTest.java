package uns.ac.rs.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import uns.ac.rs.controller.request.ConfirmRegistrationRequest;
import uns.ac.rs.controller.request.RegistrationRequest;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class UserControllerTest {

    private static final String EXISTING_USERNAME = "username1";
    private static final String EXISTING_EMAIL = "name@example.com";

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
