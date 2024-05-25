package uns.ac.rs.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import uns.ac.rs.controller.request.ChangePasswordRequest;
import uns.ac.rs.controller.request.LoginRequest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;

@QuarkusTest
public class AuthControllerTest {

    @Test
    void testLogin_success() {
        var loginRequest = new LoginRequest("username1", "MojaSifra123");
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
            .when().post("/auth/login")
            .then()
            .statusCode(200)
            .body("$", hasKey("token"));
    }

    @Test
    void testLogin_userDoesNotExist() {
        var loginRequest = new LoginRequest("username123", "MojaSifra123");
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
            .when().post("/auth/login")
            .then()
            .statusCode(400);
    }

    @Test
    void testLogin_incorrectPassword() {
        var loginRequest = new LoginRequest("username1", "MojaSifra1234");
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
            .when().post("/auth/login")
            .then()
            .statusCode(400);
    }

    @TestSecurity(user = "username1", roles = {"GUEST"})
    @Test
    public void testChangePassword_success() {
        var request = new ChangePasswordRequest("username1", "MojaSifra123", "newPass123");

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/auth/password")
            .then()
            .statusCode(200);
        request = new ChangePasswordRequest("username1", "newPass123", "MojaSifra123");

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/auth/password")
            .then()
            .statusCode(200);
    }

    @TestSecurity(user = "username2", roles = {"GUEST"})
    @Test
    public void testChangePassword_unauthorized() {
        var request = new ChangePasswordRequest("username1", "MojaSifra123", "newPass123");

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/auth/password")
            .then()
            .statusCode(403);
    }

    @TestSecurity(user = "username1", roles = {"GUEST"})
    @Test
    public void testChangePassword_passwordDoesNotMatch() {
        var request = new ChangePasswordRequest("username1", "wrongPass", "newPass123");

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/auth/password")
            .then()
            .statusCode(400)
            .body(containsString("Password does not match"));
    }
}
