package br.edu.ufvjm.mestre.identity.domain;

public final class InvalidRegistration extends RuntimeException {
    public InvalidRegistration(String message) { super(message); }
}
