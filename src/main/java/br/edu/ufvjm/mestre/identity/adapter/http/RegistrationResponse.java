package br.edu.ufvjm.mestre.identity.adapter.http;

import br.edu.ufvjm.mestre.identity.domain.UserAccount;
import java.util.UUID;

public record RegistrationResponse(UUID id, String name, String email, String role, String status) {
    static RegistrationResponse from(UserAccount account) {
        return new RegistrationResponse(account.id(), account.name(), account.email(), account.role(), account.status());
    }
}
