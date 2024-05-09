package uns.ac.rs;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class UserTest {
    @Test
    void testHelloEndpoint() {
        given()
          .when().get("/users")
          .then()
             .statusCode(200)
             .body(is("users"));
    }

}