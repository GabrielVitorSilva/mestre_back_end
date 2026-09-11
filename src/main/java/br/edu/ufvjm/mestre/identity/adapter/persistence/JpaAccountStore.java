package br.edu.ufvjm.mestre.identity.adapter.persistence;

import br.edu.ufvjm.mestre.identity.application.AccountStore;
import br.edu.ufvjm.mestre.identity.application.EmailAlreadyRegistered;
import br.edu.ufvjm.mestre.identity.domain.UserAccount;
import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.transaction.support.TransactionTemplate;

public final class JpaAccountStore implements AccountStore {
    private final EntityManager entityManager;
    private final TransactionTemplate transaction;

    public JpaAccountStore(EntityManager entityManager, TransactionTemplate transaction) {
        this.entityManager = entityManager;
        this.transaction = transaction;
    }

    @Override
    public void create(UserAccount account, String passwordHash) {
        try {
            // Catch outside the transaction so commit failures are translated after rollback too.
            transaction.executeWithoutResult(status -> {
                entityManager.persist(UserAccountEntity.from(account, passwordHash));
                entityManager.flush();
            });
        } catch (RuntimeException failure) {
            for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation
                        && "23505".equals(violation.getSQLState())
                        && "uk_user_accounts_email".equals(violation.getConstraintName())) {
                    throw new EmailAlreadyRegistered();
                }
            }
            throw failure;
        }
    }
}
