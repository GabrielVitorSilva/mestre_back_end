package br.edu.ufvjm.mestre.identity.application;

public final class EmailAlreadyRegistered extends RuntimeException {
    public EmailAlreadyRegistered() { super("Não foi possível cadastrar: e-mail já utilizado."); }
}
