package br.edu.ufvjm.mestre.identity.adapter.http;

import com.fasterxml.jackson.annotation.JsonAnySetter;

// Deliberately not a record: its generated toString would expose the password.
public final class RegistrationRequest {
    @JsonAnySetter
    public void rejectUnknownField(String field, Object value) {
        throw new IllegalArgumentException("Campo não permitido no cadastro.");
    }
    private String name;
    private String email;
    private String password;
    public String getName() { return name; }
    public void setName(Object name) { this.name = stringValue(name); }
    public String getEmail() { return email; }
    public void setEmail(Object email) { this.email = stringValue(email); }
    public String getPassword() { return password; }
    public void setPassword(Object password) { this.password = stringValue(password); }
    private static String stringValue(Object value) {
        if (value == null || value instanceof String) return (String) value;
        throw new IllegalArgumentException("Os campos de cadastro devem ser textos.");
    }
}
