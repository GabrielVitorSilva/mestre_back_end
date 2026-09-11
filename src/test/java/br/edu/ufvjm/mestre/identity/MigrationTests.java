package br.edu.ufvjm.mestre.identity;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class MigrationTests {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("fresh");

    @Test
    void migratesFreshDatabase() throws Exception {
        Flyway flyway = migration(POSTGRES.getJdbcUrl(), null);
        assertEquals(2, flyway.migrate().migrationsExecuted);
        assertEquals(0, flyway.migrate().migrationsExecuted);
        assertTrue(flyway.validateWithResult().validationSuccessful);
        try (var connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT count(*) FROM mestre.user_accounts")) {
            assertTrue(result.next());
            assertEquals(0, result.getInt(1));
        }
    }

    @Test
    void upgradesExistingV1WithoutChangingItsChecksumOrExistingData() throws Exception {
        try (var connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             var statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE upgrade_test");
        }
        String url = "jdbc:postgresql://" + POSTGRES.getHost() + ":" + POSTGRES.getMappedPort(5432) + "/upgrade_test";
        Flyway v1 = migration(url, "1");
        assertEquals(1, v1.migrate().migrationsExecuted);
        Integer checksum = v1.info().current().getChecksum();
        try (var connection = DriverManager.getConnection(url, POSTGRES.getUsername(), POSTGRES.getPassword());
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE mestre.existing_data (value TEXT)");
            statement.execute("INSERT INTO mestre.existing_data VALUES ('preserved')");
        }
        Flyway upgraded = migration(url, null);
        assertEquals(1, upgraded.migrate().migrationsExecuted);
        var appliedV1 = java.util.Arrays.stream(upgraded.info().applied())
                .filter(info -> info.getVersion() != null && "1".equals(info.getVersion().toString()))
                .findFirst().orElseThrow();
        assertEquals(checksum, appliedV1.getChecksum());
        assertTrue(upgraded.validateWithResult().validationSuccessful);
        try (var connection = DriverManager.getConnection(url, POSTGRES.getUsername(), POSTGRES.getPassword());
             var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT value FROM mestre.existing_data")) {
            assertTrue(result.next());
            assertEquals("preserved", result.getString(1));
        }
    }
    private Flyway migration(String url, String target) {
        var config = Flyway.configure().dataSource(url, POSTGRES.getUsername(), POSTGRES.getPassword())
                .defaultSchema("mestre").schemas("mestre").locations("classpath:db/migration");
        if (target != null) config.target(target);
        return config.load();
    }
}
