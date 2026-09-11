package br.edu.ufvjm.mestre;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MestreApplicationTests {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Test
    void contextLoads() {
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
