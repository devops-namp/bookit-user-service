package uns.ac.rs.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uns.ac.rs.entity.User;
import uns.ac.rs.repository.UserRepository;
import uns.ac.rs.resources.PostgresResource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
public class UserRMControllerIntegrationTest {
    @Inject
    UserRepository userRepository;

    @BeforeEach
    @Transactional
    public void setup() {
        userRepository.deleteAll();

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("testuser@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setCity("Test City");
        user.setRejectedReservationsCount(0);
        user.setAutoApprove(false);
        userRepository.persist(user);
    }

    @Test
    public void testSetAutoapproveFalse() {
        given()
                .contentType(ContentType.JSON)
                .pathParam("username", "testuser")
                .when()
                .post("/users/autoapproveFalse/{username}")
                .then()
                .statusCode(201);

        given()
                .pathParam("username", "testuser")
                .when()
                .get("/users/getAutoapproveStatus/{username}")
                .then()
                .statusCode(200)
                .body(is("false"));
    }

    @Test
    public void testSetAutoapproveTrue() {
        given()
                .contentType(ContentType.JSON)
                .pathParam("username", "testuser")
                .when()
                .post("/users/autoapproveTrue/{username}")
                .then()
                .statusCode(201);

        given()
                .pathParam("username", "testuser")
                .when()
                .get("/users/getAutoapproveStatus/{username}")
                .then()
                .statusCode(200)
                .body(is("true"));
    }

    @Test
    public void testGetAutoapproveStatus() {
        given()
                .pathParam("username", "testuser")
                .when()
                .get("/users/getAutoapproveStatus/{username}")
                .then()
                .statusCode(200)
                .body(is("false"));
    }

    @Test
    public void testGetRejectCount() {
        Map<String, Integer> expectedCounts = new HashMap<>();
        expectedCounts.put("testuser", 0);

        given()
                .contentType(ContentType.JSON)
                .body(Arrays.asList("testuser"))
                .when()
                .post("/users/getRejectCount")
                .then()
                .statusCode(200)
                .body("testuser", is(0));
    }
}
