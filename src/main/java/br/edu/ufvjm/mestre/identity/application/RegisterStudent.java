package br.edu.ufvjm.mestre.identity.application;

import br.edu.ufvjm.mestre.identity.domain.RegistrationRules;
import br.edu.ufvjm.mestre.identity.domain.UserAccount;

public final class RegisterStudent {
    private final AccountStore accounts;
    private final PasswordProtection passwords;

    public RegisterStudent(AccountStore accounts, PasswordProtection passwords) {
        this.accounts = accounts;
        this.passwords = passwords;
    }

    public UserAccount execute(String name, String email, String password) {
        UserAccount account = UserAccount.registerStudent(name, email);
        RegistrationRules.password(password);
        String hash = passwords.hash(password);
        accounts.create(account, hash);
        return account;
    }
}
