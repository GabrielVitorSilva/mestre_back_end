package br.edu.ufvjm.mestre.identity.application;

import br.edu.ufvjm.mestre.identity.domain.UserAccount;

public interface AccountStore {
    void create(UserAccount account, String passwordHash);
}
