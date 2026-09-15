package br.edu.ufvjm.mestre.identity.configuration;

import br.edu.ufvjm.mestre.identity.adapter.password.Pbkdf2PasswordHasher;
import br.edu.ufvjm.mestre.identity.adapter.persistence.account.JpaUserAccountRepository;
import br.edu.ufvjm.mestre.identity.application.account.UserAccountRepository;
import br.edu.ufvjm.mestre.identity.application.password.PasswordHasher;
import br.edu.ufvjm.mestre.identity.application.registration.RegisterStudentUseCase;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
public class IdentityConfiguration {
    @Bean
    PasswordHasher passwordHasher() { return new Pbkdf2PasswordHasher(); }
    @Bean
    UserAccountRepository userAccountRepository(EntityManager entityManager, PlatformTransactionManager manager) {
        return new JpaUserAccountRepository(entityManager, new TransactionTemplate(manager));
    }
    @Bean
    RegisterStudentUseCase registerStudentUseCase(UserAccountRepository accounts, PasswordHasher passwords) {
        return new RegisterStudentUseCase(accounts, passwords);
    }
}
