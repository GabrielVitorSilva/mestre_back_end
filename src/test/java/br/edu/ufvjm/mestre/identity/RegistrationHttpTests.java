package br.edu.ufvjm.mestre.identity;

import br.edu.ufvjm.mestre.identity.application.AccountStore;
import br.edu.ufvjm.mestre.identity.application.EmailAlreadyRegistered;
import br.edu.ufvjm.mestre.identity.domain.UserAccount;
import br.edu.ufvjm.mestre.identity.adapter.password.Pbkdf2PasswordProtection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Import(RegistrationHttpTests.TestWiring.class)
@ExtendWith(OutputCaptureExtension.class)
class RegistrationHttpTests {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String PASSWORD = "Senha ficticia privada 123";
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");
    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired ControlledStore controlled;
    @Autowired @Qualifier("accountStore") AccountStore realStore;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void reset() {
        controlled.barrier = null;
        controlled.failure = null;
        controlled.arrivals.set(0);
        jdbc.update("DELETE FROM mestre.user_accounts");
    }
    @AfterEach
    void releaseGate() { controlled.barrier = null; controlled.failure = null; }

    @Test
    void validRegistrationPersistsOnlyActiveStudentAndProtectedPassword(CapturedOutput output) throws Exception {
        var response = post(body("  Aluna Exemplo  ", "  ALUNA+SI@EXAMPLE.COM  ", PASSWORD));
        assertEquals(201, response.statusCode());
        var result = JSON.readTree(response.body());
        assertEquals(5, result.size());
        UUID id = UUID.fromString(result.path("id").asText());
        assertEquals("Aluna Exemplo", result.path("name").asText());
        assertEquals("aluna+si@example.com", result.path("email").asText());
        assertEquals("STUDENT", result.path("role").asText());
        assertEquals("ACTIVE", result.path("status").asText());
        assertFalse(response.headers().firstValue("Location").isPresent());
        assertEquals(1, count());
        var stored = jdbc.queryForMap("SELECT * FROM mestre.user_accounts WHERE id = ?", id);
        assertEquals(result.path("name").asText(), stored.get("name"));
        assertEquals(result.path("email").asText(), stored.get("email"));
        assertEquals("STUDENT", stored.get("role"));
        assertEquals("ACTIVE", stored.get("status"));
        String hash = (String) stored.get("password_hash");
        assertNotEquals(PASSWORD, hash);
        assertTrue(new Pbkdf2PasswordProtection().matches(PASSWORD, hash));
        assertFalse(response.body().contains(PASSWORD));
        assertFalse(response.body().contains(hash));
        assertFalse(response.body().contains("password"));
        assertFalse(output.getAll().contains(PASSWORD));
        assertFalse(output.getAll().contains(hash));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "null", "[]", "{", "{\"name\":\"Aluna\"}",
            "{\"name\":null,\"email\":\"a@example.com\",\"password\":\"Senha ficticia privada 123\"}",
            "{\"name\":{},\"email\":\"a@example.com\",\"password\":\"Senha ficticia privada 123\"}"})
    void rejectsMalformedOrIncompleteInput(String input) throws Exception {
        assertError(post(input), 400);
        assertEquals(0, count());
    }

    @Test
    void invalidFieldsDoNotPersistOrLeakSecrets(CapturedOutput output) throws Exception {
        for (String name : List.of("", "  ", "a".repeat(121), "Ana\tSilva")) {
            assertError(post(body(name, "a@example.com", PASSWORD)), 400);
        }
        for (String email : List.of("invalid", "a@@example.com", "a..b@example.com", "a@-example.com", "a".repeat(255))) {
            assertError(post(body("Aluna", email, PASSWORD)), 400);
        }
        for (String password : List.of("curta", "a".repeat(14), "a".repeat(129), " ".repeat(15), PASSWORD + "\n")) {
            assertError(post(body("Aluna", "a@example.com", password)), 400);
        }
        assertEquals(0, count());
        assertFalse(output.getAll().contains(PASSWORD));
    }

    @ParameterizedTest
    @ValueSource(strings = {"name", "email", "password"})
    void rejectsNonStringValues(String field) throws Exception {
        var input = new java.util.HashMap<String, Object>(Map.of("name", "Aluna", "email", "a@example.com", "password", PASSWORD));
        for (Object value : List.of(123456789012345L, true, List.of("text"), Map.of("value", "text"))) {
            input.put(field, value);
            assertError(post(JSON.writeValueAsString(input)), 400);
        }
        assertEquals(0, count());
    }

    @Test
    void acceptsDocumentedMaximumLengths() throws Exception {
        String email = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(61);
        assertEquals(201, post(body("😀".repeat(120), email, "😀".repeat(128))).statusCode());
        assertEquals(120, jdbc.queryForObject("SELECT char_length(name) FROM mestre.user_accounts", Integer.class));
        assertEquals(254, jdbc.queryForObject("SELECT char_length(email) FROM mestre.user_accounts", Integer.class));
    }

    @Test
    void equivalentEmailsConflictWithoutExposingExistingAccount(CapturedOutput output) throws Exception {
        var created = post(body("Nome Confidencial", "aluna@example.com", PASSWORD));
        assertEquals(201, created.statusCode());
        var duplicate = post(body("Outra Pessoa", "  ALUNA@EXAMPLE.COM  ", PASSWORD));
        assertError(duplicate, 409);
        assertEquals("REGISTRATION_CONFLICT", JSON.readTree(duplicate.body()).path("code").asText());
        assertFalse(duplicate.body().contains("Nome Confidencial"));
        assertFalse(duplicate.body().contains(JSON.readTree(created.body()).path("id").asText()));
        assertEquals(1, count());
        String hash = jdbc.queryForObject("SELECT password_hash FROM mestre.user_accounts", String.class);
        assertFalse(output.getAll().contains(PASSWORD));
        assertFalse(output.getAll().contains(hash));
    }

    @Test
    void dotsAndProviderSuffixesRemainDistinct() throws Exception {
        for (String email : List.of("ab@example.com", "a.b@example.com", "ab+si@example.com")) {
            assertEquals(201, post(body("Aluna", email, PASSWORD)).statusCode());
        }
        assertEquals(3, count());
    }

    @Test
    void concurrentEquivalentHttpRequestsProduceExactlyOneCreationAndOneConflict() throws Exception {
        controlled.barrier = new CyclicBarrier(2);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> post(body("Primeira", "race@example.com", PASSWORD)));
            var second = executor.submit(() -> post(body("Segunda", " RACE@EXAMPLE.COM ", PASSWORD)));
            var responses = List.of(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS));
            assertEquals(List.of(201, 409), responses.stream().map(HttpResponse::statusCode).sorted().toList());
            assertEquals(2, controlled.arrivals.get(), "Both requests must reach persistence before either inserts");
            assertEquals(1, count());
            assertError(responses.stream().filter(r -> r.statusCode() == 409).findFirst().orElseThrow(), 409);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"role", "roles", "status", "enabled", "emailVerified", "id", "passwordHash"})
    void refusesClientControlledPrivilegeAndIdentityFields(String field) throws Exception {
        String input = JSON.writeValueAsString(Map.of("name", "Aluna", "email", "a@example.com", "password", PASSWORD, field, "ADMIN"));
        assertError(post(input), 400);
        assertEquals(0, count());
    }

    @Test
    void unexpectedFailureHasGenericResponseAndNoPersistence(CapturedOutput output) throws Exception {
        controlled.failure = new IllegalStateException("SQL secret " + PASSWORD);
        var response = post(body("Aluna", "a@example.com", PASSWORD));
        assertError(response, 500);
        assertEquals("INTERNAL_ERROR", JSON.readTree(response.body()).path("code").asText());
        assertFalse(response.body().contains("SQL"));
        assertFalse(response.body().contains("IllegalStateException"));
        assertEquals(0, count());
        assertFalse(output.getAll().contains(PASSWORD));
    }

    @Test
    void unrelatedIntegrityFailuresAreNotMisclassifiedAsDuplicateEmail(CapturedOutput output) {
        var account = UserAccount.registerStudent("Aluna", "first@example.com");
        String hash = new Pbkdf2PasswordProtection().hash(PASSWORD);
        realStore.create(account, hash);
        var primaryKey = assertThrows(RuntimeException.class, () -> realStore.create(account, hash));
        assertFalse(primaryKey instanceof EmailAlreadyRegistered);
        var invalidHash = assertThrows(RuntimeException.class,
                () -> realStore.create(UserAccount.registerStudent("Aluna", "other@example.com"), ""));
        assertFalse(invalidHash instanceof EmailAlreadyRegistered);
        assertEquals(1, count());
        assertFalse(output.getAll().contains(hash));
    }

    private int count() { return jdbc.queryForObject("SELECT count(*) FROM mestre.user_accounts", Integer.class); }
    private String body(String name, String email, String password) {
        return JSON.writeValueAsString(Map.of("name", name, "email", email, "password", password));
    }
    private HttpResponse<String> post(String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/v1/users"))
                .timeout(Duration.ofSeconds(25)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
    private void assertError(HttpResponse<String> response, int status) {
        assertEquals(status, response.statusCode(), response.body());
        var error = JSON.readTree(response.body());
        assertEquals(2, error.size());
        assertTrue(error.has("code"));
        assertTrue(error.has("message"));
        assertFalse(response.body().contains(PASSWORD));
        assertFalse(response.body().contains("password_hash"));
        assertFalse(error.has("trace"));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestWiring {
        @Bean @Primary
        ControlledStore controlledStore(@Qualifier("accountStore") AccountStore real) { return new ControlledStore(real); }
    }
    static class ControlledStore implements AccountStore {
        private final AccountStore real;
        volatile CyclicBarrier barrier;
        volatile RuntimeException failure;
        final AtomicInteger arrivals = new AtomicInteger();
        ControlledStore(AccountStore real) { this.real = real; }
        @Override
        public void create(UserAccount account, String passwordHash) {
            if (failure != null) throw failure;
            CyclicBarrier gate = barrier;
            if (gate != null) {
                arrivals.incrementAndGet();
                try { gate.await(10, TimeUnit.SECONDS); }
                catch (Exception exception) { throw new IllegalStateException("Concurrency gate failed", exception); }
            }
            real.create(account, passwordHash);
        }
    }
}
