package org.springdemo.gestionstock.config;

import org.springdemo.gestionstock.dto.AccountRequest;
import org.springdemo.gestionstock.services.AccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.bootstrap-admin.enabled", havingValue = "true")
public class AdminBootstrap implements ApplicationRunner {
    private final AccountService accounts;
    private final String email;
    private final String password;
    public AdminBootstrap(AccountService accounts, @Value("${BOOTSTRAP_ADMIN_EMAIL}") String email,
                          @Value("${BOOTSTRAP_ADMIN_PASSWORD}") String password) {
        this.accounts = accounts; this.email = email; this.password = password;
    }
    @Override public void run(ApplicationArguments args) {
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new IllegalArgumentException("Email administrateur invalide");
        AccountRequest request = new AccountRequest();
        request.setEmail(email); request.setMotDePasse(password); request.setRole("ADMIN");
        request.setNom("Administrateur"); request.setPrenom("Initial");
        accounts.bootstrap(request);
    }
}
