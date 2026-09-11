package br.edu.ufvjm.mestre;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class MestreApplicationTests {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("mestre")
            .withUsername("mestre")
            .withPassword("mestre_test_password");

    @LocalServerPort
    private int port;

    @Autowired
    private Flyway flyway;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void flywayAppliesTheTechnicalSchemaMigration() {
        assertTrue(Arrays.stream(flyway.info().applied())
                .anyMatch(migration -> "establish application schema".equals(migration.getDescription())));
    }

    @Test
    void healthIsAvailableWithoutDetails() throws Exception {
        var response = get("/actuator/health");

        assertEquals(200, response.statusCode());
        var health = JSON.readTree(response.body());
        assertEquals("UP", health.path("status").asText());
        assertFalse(health.has("details"));
    }

    @Test
    void additionalActuatorEndpointsAreNotExposed() throws Exception {
        assertEquals(404, get("/actuator").statusCode());
        assertEquals(404, get("/actuator/info").statusCode());
        assertEquals(404, get("/actuator/metrics").statusCode());
        assertEquals(404, get("/actuator/env").statusCode());
    }

    private HttpResponse<String> get(String path) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET().build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
