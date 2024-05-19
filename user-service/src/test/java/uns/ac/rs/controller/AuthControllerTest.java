package uns.ac.rs.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import uns.ac.rs.controller.request.LoginRequest;

import static io.restassured.RestAssured.given;
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

}
