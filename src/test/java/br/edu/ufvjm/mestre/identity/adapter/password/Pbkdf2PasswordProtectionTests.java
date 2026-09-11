package br.edu.ufvjm.mestre.identity.adapter.password;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Pbkdf2PasswordProtectionTests {
    private final Pbkdf2PasswordProtection passwords = new Pbkdf2PasswordProtection();

    @Test
    void usesRandomSaltAndVersionedParametersAndVerifiesPassword() {
        String input = "uma senha ficticia";
        String first = passwords.hash(input);
        String second = passwords.hash(input);
        assertNotEquals(input, first);
        assertNotEquals(first, second);
        assertTrue(first.startsWith("{pbkdf2-sha256-i600000-s16-v1}"));
        assertTrue(passwords.matches(input, first));
        assertFalse(passwords.matches("outra senha ficticia", first));
    }
    @Test
    void preservesSpacesUnicodeAndCharactersBeyondBcryptLimit() {
        String input = "  " + "😀".repeat(125) + "x";
        String hash = passwords.hash(input);
        assertTrue(passwords.matches(input, hash));
        assertFalse(passwords.matches(input.strip(), hash));
        assertFalse(passwords.matches("  " + "😀".repeat(125) + "y", hash));
    }
}
