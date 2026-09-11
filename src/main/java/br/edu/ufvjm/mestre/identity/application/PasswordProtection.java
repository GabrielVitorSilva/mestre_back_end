package br.edu.ufvjm.mestre.identity.application;

public interface PasswordProtection {
    String hash(String password);
}
