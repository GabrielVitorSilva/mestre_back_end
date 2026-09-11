package br.edu.ufvjm.mestre.identity.configuration;

import br.edu.ufvjm.mestre.identity.application.AccountStore;
import br.edu.ufvjm.mestre.identity.application.PasswordProtection;
import br.edu.ufvjm.mestre.identity.application.RegisterStudent;
import br.edu.ufvjm.mestre.identity.adapter.password.Pbkdf2PasswordProtection;
import br.edu.ufvjm.mestre.identity.adapter.persistence.JpaAccountStore;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
public class IdentityConfiguration {
    @Bean
    PasswordProtection passwordProtection() { return new Pbkdf2PasswordProtection(); }
    @Bean
    AccountStore accountStore(EntityManager entityManager, PlatformTransactionManager manager) {
        return new JpaAccountStore(entityManager, new TransactionTemplate(manager));
    }
    @Bean
    RegisterStudent registerStudent(AccountStore accounts, PasswordProtection passwords) {
        return new RegisterStudent(accounts, passwords);
    }
}
