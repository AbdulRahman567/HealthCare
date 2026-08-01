package com.healthcare.hms.support;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for MySQL-backed Spring Boot integration tests.
 *
 * <p>Defaults to a Testcontainers MySQL. To run against an existing local/CI MySQL without
 * Docker, set {@code hms.it.db.url} / {@code hms.it.db.username} / {@code hms.it.db.password}
 * system properties (e.g. {@code -Dhms.it.db.url=jdbc:mysql://127.0.0.1:3306/...}).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractMySqlIntegrationTest {

    private static final String LIVE_URL = System.getProperty("hms.it.db.url");
    private static final String LIVE_USER = System.getProperty("hms.it.db.username");
    private static final String LIVE_PASSWORD = System.getProperty("hms.it.db.password");

    @Container
    @SuppressWarnings("resource")
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("hms_db")
            .withUsername("hms_user")
            .withPassword("hms_password");

    @DynamicPropertySource
    static void registerDatasourceProperties(final DynamicPropertyRegistry registry) {
        if (LIVE_URL != null) {
            registry.add("spring.datasource.url", () -> LIVE_URL);
            registry.add("spring.datasource.username", () -> LIVE_USER != null ? LIVE_USER : "hms_user");
            registry.add("spring.datasource.password", () -> LIVE_PASSWORD != null ? LIVE_PASSWORD : "hms_password");
        } else {
            registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
            registry.add("spring.datasource.username", MYSQL::getUsername);
            registry.add("spring.datasource.password", MYSQL::getPassword);
        }
    }
}
