package br.edu.ufvjm.mestre.identity.domain;

import java.util.Objects;
import java.util.UUID;

public final class UserAccount {
    private final UUID id;
    private final String name;
    private final String email;

    private UserAccount(UUID id, String name, String email) {
        this.id = Objects.requireNonNull(id);
        this.name = RegistrationRules.name(name);
        this.email = RegistrationRules.email(email);
    }

    public static UserAccount registerStudent(String name, String email) {
        return new UserAccount(UUID.randomUUID(), name, email);
    }
    public UUID id() { return id; }
    public String name() { return name; }
    public String email() { return email; }
    public String role() { return "STUDENT"; }
    public String status() { return "ACTIVE"; }
}
