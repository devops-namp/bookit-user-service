package uns.ac.rs.resources;


import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

public class PostgresResource implements QuarkusTestResourceLifecycleManager {

    public static final PostgreSQLContainer<?> DATABASE = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_password")
            .withExposedPorts(5432);

    @Override
    public Map<String, String> start() {
        DATABASE.start();
        return Map.of(
                "quarkus.datasource.jdbc.url", DATABASE.getJdbcUrl(),
                "quarkus.datasource.db-kind", "postgresql",
                "quarkus.datasource.username", DATABASE.getUsername(),
                "quarkus.datasource.password", DATABASE.getPassword());
    }

    @Override
    public void stop() {
        DATABASE.stop();
    }
}

