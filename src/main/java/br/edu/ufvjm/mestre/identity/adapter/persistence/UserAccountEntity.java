package br.edu.ufvjm.mestre.identity.adapter.persistence;

import br.edu.ufvjm.mestre.identity.domain.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "user_accounts", schema = "mestre")
public class UserAccountEntity {
    @Id
    private UUID id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 254)
    private String email;
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    @Column(nullable = false, length = 20)
    private String role;
    @Column(nullable = false, length = 20)
    private String status;

    protected UserAccountEntity() {}

    static UserAccountEntity from(UserAccount account, String passwordHash) {
        var entity = new UserAccountEntity();
        entity.id = account.id();
        entity.name = account.name();
        entity.email = account.email();
        entity.passwordHash = passwordHash;
        entity.role = account.role();
        entity.status = account.status();
        return entity;
    }
}
