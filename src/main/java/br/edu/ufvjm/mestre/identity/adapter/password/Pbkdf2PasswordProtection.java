package br.edu.ufvjm.mestre.identity.adapter.password;

import br.edu.ufvjm.mestre.identity.application.PasswordProtection;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import java.util.Map;

public final class Pbkdf2PasswordProtection implements PasswordProtection {
    private static final String ID = "pbkdf2-sha256-i600000-s16-v1";
    private final PasswordEncoder encoder;

    public Pbkdf2PasswordProtection() {
        var pbkdf2 = new Pbkdf2PasswordEncoder("", 16, 600_000,
                Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
        encoder = new DelegatingPasswordEncoder(ID, Map.of(ID, pbkdf2));
    }
    @Override
    public String hash(String password) { return encoder.encode(password); }
    public boolean matches(String password, String hash) { return encoder.matches(password, hash); }
}
