package uns.ac.rs.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uns.ac.rs.controller.request.ChangePasswordRequest;
import uns.ac.rs.controller.request.LoginRequest;
import uns.ac.rs.entity.Role;
import uns.ac.rs.entity.User;
import uns.ac.rs.repository.UserRepository;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;

@QuarkusTest
public class AuthControllerTest {

    @Inject
    UserRepository userRepository;

    @BeforeEach
    @Transactional
    public void setup() {
        userRepository.deleteAll();

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
    }

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
