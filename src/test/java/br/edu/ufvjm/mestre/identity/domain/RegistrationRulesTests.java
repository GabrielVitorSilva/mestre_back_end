package br.edu.ufvjm.mestre.identity.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class RegistrationRulesTests {
    @Test
    void createsOnlyActiveStudentsWithServerIdentityAndNormalizesEmail() {
        var first = UserAccount.registerStudent("  Aluna Exemplo  ", "  Aluna+SI@EXAMPLE.COM  ");
        var second = UserAccount.registerStudent("Aluna Exemplo", "aluna+si@example.com");
        assertNotEquals(first.id(), second.id());
        assertEquals("Aluna Exemplo", first.name());
        assertEquals("aluna+si@example.com", first.email());
        assertEquals("STUDENT", first.role());
        assertEquals("ACTIVE", first.status());
        assertEquals("a.b+tag@example.com", RegistrationRules.email("A.B+tag@example.com"));
    }
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\n", "Ana\tSilva", "\u0000"})
    void rejectsInvalidNames(String name) {
        assertThrows(InvalidRegistration.class, () -> RegistrationRules.name(name));
    }
    @Test
    void nameLimitCountsUnicodeCodePoints() {
        assertEquals("á".repeat(120), RegistrationRules.name("á".repeat(120)));
        assertEquals("😀".repeat(120), RegistrationRules.name("😀".repeat(120)));
        assertThrows(InvalidRegistration.class, () -> RegistrationRules.name("a".repeat(121)));
        assertThrows(InvalidRegistration.class, () -> RegistrationRules.name("\uD800"));
    }
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"a", "a@", "@example.com", "a@@example.com", "a@example", "a..b@example.com",
            ".a@example.com", "a.@example.com", "a b@example.com", "a@-example.com", "a@example-.com",
            "a@example..com", "a@example.com.", "á@example.com", "a@exámple.com", "a@example.com\nother"})
    void rejectsInvalidEmails(String email) {
        assertThrows(InvalidRegistration.class, () -> RegistrationRules.email(email));
    }
    @Test
    void emailLimitsAreExplicit() {
        String max = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(61);
        assertEquals(254, max.length());
        assertEquals(max, RegistrationRules.email(max));
        assertThrows(InvalidRegistration.class, () -> RegistrationRules.email(max + "d"));
        assertThrows(InvalidRegistration.class, () -> RegistrationRules.email("a".repeat(65) + "@example.com"));
        assertThrows(InvalidRegistration.class, () -> RegistrationRules.email("a@" + "b".repeat(64) + ".com"));
    }
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"short", "12345678901234", "               ", "senha longa com\nquebra", "\uD800123456789012345"})
    void rejectsInvalidPasswordsWithoutEchoingInput(String password) {
        var failure = assertThrows(InvalidRegistration.class, () -> RegistrationRules.password(password));
        assertTrue(failure.getMessage().startsWith("A senha deve"));
    }
    @Test
    void passwordBoundariesAllowSpacesAndUnicodeWithoutCompositionRules() {
        assertDoesNotThrow(() -> RegistrationRules.password("a".repeat(15)));
        assertDoesNotThrow(() -> RegistrationRules.password("😀".repeat(128)));
        assertDoesNotThrow(() -> RegistrationRules.password("  uma frase longa  "));
        assertThrows(InvalidRegistration.class, () -> RegistrationRules.password("a".repeat(129)));
    }
}
