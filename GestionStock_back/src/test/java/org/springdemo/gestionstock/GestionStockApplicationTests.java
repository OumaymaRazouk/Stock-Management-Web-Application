package org.springdemo.gestionstock;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springdemo.gestionstock.entity.Utilisateur;
import org.springdemo.gestionstock.entity.Acheteur;
import org.springdemo.gestionstock.repository.UtilisateurRepository;
import org.springdemo.gestionstock.repository.AcheteurRepository;
import org.springdemo.gestionstock.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:securitytest;MODE=MSSQLServer;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Transactional
class GestionStockApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired PasswordEncoder encoder;
    @Autowired UtilisateurRepository users;
    @Autowired AcheteurRepository acheteurs;
    @Autowired UserDetailsService details;
    @Autowired org.springdemo.gestionstock.services.AccountService accounts;
    @Autowired org.springdemo.gestionstock.repository.ArticleRepository articles;

    private String createUser(String role) throws Exception {
        String email = UUID.randomUUID() + "@example.invalid";
        String password = UUID.randomUUID().toString();
        Utilisateur user = new Utilisateur();
        user.setEmail(email);
        user.setNom("Test");
        user.setPrenom("Test");
        user.setRole(role);
        user.setMotDePasse(encoder.encode(password));
        users.save(user);
        String response = mvc.perform(post("/api/auth/login").contentType("application/json")
                .content(json.writeValueAsString(Map.of("email", email, "motDePasse", password))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value(role))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("token").asText();
    }

    @Test void rejectsAnonymousAndFakeTokens() throws Exception {
        mvc.perform(get("/api/articles")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/articles").header("Authorization", "Bearer FAKE_TOKEN"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType("application/json")
                .content(json.writeValueAsString(Map.of("email", "missing@example.invalid", "motDePasse", UUID.randomUUID().toString()))))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "COMMERCIAL", "ACHETEUR"})
    void supportsExistingRolesAndRevokesLogout(String role) throws Exception {
        String token = createUser(role);
        assertEquals(43, token.length());
        mvc.perform(get("/api/articles").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token)).andExpect(status().isNoContent());
        mvc.perform(get("/api/articles").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }

    @Test void acheteurCannotEditCatalogueOrManageAccounts() throws Exception {
        String token = createUser("ACHETEUR");
        mvc.perform(post("/api/articles").header("Authorization", "Bearer " + token)
                .contentType("application/json").content("{}")).andExpect(status().isForbidden());
        mvc.perform(get("/api/acheteurs").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test void passwordHashesNeverLeaveAccountEndpoints() throws Exception {
        Acheteur acheteur = new Acheteur();
        acheteur.setEmail(UUID.randomUUID() + "@example.invalid");
        acheteur.setMotDePasse(encoder.encode(UUID.randomUUID().toString()));
        acheteurs.save(acheteur);
        String token = createUser("ADMIN");
        mvc.perform(get("/api/acheteurs/" + acheteur.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.motDePasse").doesNotExist());
        mvc.perform(get("/api/acheteurs").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].motDePasse").doesNotExist());
        Utilisateur user = new Utilisateur();
        user.setMotDePasse(acheteur.getMotDePasse());
        assertFalse(json.writeValueAsString(user).contains("motDePasse"));
        assertEquals("test-only", json.readValue("{\"motDePasse\":\"test-only\"}", Acheteur.class).getMotDePasse());
    }

    @Test void accountCreationCannotInjectAdminRole() throws Exception {
        String token = createUser("ADMIN");
        String email = UUID.randomUUID() + "@example.invalid";
        mvc.perform(post("/api/acheteurs").header("Authorization", "Bearer " + token)
                .contentType("application/json").content(json.writeValueAsString(Map.of(
                        "email", email, "nom", "Test", "prenom", "Test", "motDePasse", UUID.randomUUID().toString(), "role", "ADMIN"))))
                .andExpect(status().isOk());
        assertEquals("ACHETEUR", acheteurs.findByEmail(email).getRole());
    }

    @Test void expiredTokensAreRejected() {
        var user = org.springframework.security.core.userdetails.User.withUsername("test@example.invalid")
                .password("test-only-hash").roles("ADMIN").build();
        var clock = org.mockito.Mockito.mock(java.time.Clock.class);
        var now = java.time.Instant.parse("2026-01-01T00:00:00Z");
        org.mockito.Mockito.when(clock.instant()).thenReturn(now);
        TokenService tokens = new TokenService(Duration.ofHours(1), clock);
        String token = tokens.issue(user);
        assertNotNull(tokens.resolve(token));
        org.mockito.Mockito.when(clock.instant()).thenReturn(now.plusSeconds(3600));
        assertNull(tokens.resolve(token));
    }

    @Test void authenticatesAcheteurTableAndRejectsTokensAfterPasswordChange() throws Exception {
        Acheteur acheteur = new Acheteur();
        String password = UUID.randomUUID().toString();
        acheteur.setEmail(UUID.randomUUID() + "@example.invalid");
        acheteur.setMotDePasse(encoder.encode(password));
        acheteurs.save(acheteur);
        String response = mvc.perform(post("/api/auth/login").contentType("application/json")
                .content(json.writeValueAsString(Map.of("email", acheteur.getEmail(), "motDePasse", password))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("ACHETEUR"))
                .andReturn().getResponse().getContentAsString();
        String token = json.readTree(response).get("token").asText();
        mvc.perform(get("/api/articles").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        acheteur.setMotDePasse(encoder.encode(UUID.randomUUID().toString()));
        acheteurs.save(acheteur);
        mvc.perform(get("/api/articles").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }

    @Test void publicRegistrationIsAlwaysClosed() throws Exception {
        long count = users.count();
        mvc.perform(post("/api/auth/register").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
        String token = createUser("ADMIN");
        mvc.perform(post("/api/auth/register").header("Authorization", "Bearer " + token)
                .contentType("application/json").content("{}")).andExpect(status().isForbidden());
        assertEquals(count + 1, users.count());
    }

    @Test void corsAllowsOnlyConfiguredOrigin() throws Exception {
        mvc.perform(options("/api/articles").header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
        mvc.perform(options("/api/articles").header("Origin", "https://untrusted.example.invalid")
                .header("Access-Control-Request-Method", "GET")).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "ACHETEUR", "COMMERCIAL"})
    void purchaseAndAccountPermissionsMatchRole(String role) throws Exception {
        String token = createUser(role);
        int purchaseStatus = role.equals("COMMERCIAL") ? 403 : 200;
        for (String endpoint : java.util.List.of("/api/commandes", "/api/commandes/bandes", "/api/besoins/commande/preparation")) {
            mvc.perform(get(endpoint).header("Authorization", "Bearer " + token)).andExpect(status().is(purchaseStatus));
        }
        for (String endpoint : java.util.List.of("/api/admin/accounts", "/api/acheteurs")) {
            mvc.perform(get(endpoint).header("Authorization", "Bearer " + token))
                    .andExpect(status().is(role.equals("ADMIN") ? 200 : 403));
        }
        if (!role.equals("ADMIN")) {
            for (String endpoint : java.util.List.of("/api/articles", "/api/fournisseurs", "/api/admin/accounts")) {
                mvc.perform(post(endpoint).header("Authorization", "Bearer " + token)
                        .contentType("application/json").content("{}")).andExpect(status().isForbidden());
            }
        }
    }

    @Test void commercialCanCreateSalesNeedsButCannotManagePurchasesOrEditNeeds() throws Exception {
        String token = createUser("COMMERCIAL");
        var article = new org.springdemo.gestionstock.entity.Article();
        article.setReference("TEST"); article.setQuantiteConditionnement(1); article.setQuantite(10);
        articles.save(article);
        for (String type : java.util.List.of("DEVIS", "NEGOCE", "PRODUCTION")) {
            String payload = json.writeValueAsString(Map.of("dateBesoin", "2026-10-06", "typeBesoin", type,
                    "besoinArticles", java.util.List.of(Map.of("articleId", article.getId(), "quantite", 2))));
            mvc.perform(post("/api/besoins").header("Authorization", "Bearer " + token)
                    .contentType("application/json").content(payload))
                    .andExpect(status().is(type.equals("PRODUCTION") ? 403 : 201));
        }
        mvc.perform(post("/api/commandes/passer").header("Authorization", "Bearer " + token)
                .contentType("application/json").content("[]")).andExpect(status().isForbidden());
        mvc.perform(put("/api/besoins/1").header("Authorization", "Bearer " + token)
                .contentType("application/json").content("{}")).andExpect(status().isForbidden());
        mvc.perform(delete("/api/besoins/1").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
        String buyer = createUser("ACHETEUR");
        mvc.perform(post("/api/besoins").header("Authorization", "Bearer " + buyer)
                .contentType("application/json").content("{}")).andExpect(status().isForbidden());
    }

    @Test void adminCreatesUpdatesAndDeletesCommercialWithoutReturningPassword() throws Exception {
        String token = createUser("ADMIN");
        String email = UUID.randomUUID() + "@example.invalid";
        String password = UUID.randomUUID().toString();
        String input = json.writeValueAsString(Map.of("nom", "Test", "prenom", "Commercial", "email", email,
                "motDePasse", password, "role", "COMMERCIAL"));
        String body = mvc.perform(post("/api/admin/accounts").header("Authorization", "Bearer " + token)
                .contentType("application/json").content(input)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.motDePasse").doesNotExist()).andReturn().getResponse().getContentAsString();
        Long id = json.readTree(body).get("id").asLong();
        String originalHash = users.findById(id).orElseThrow().getMotDePasse();
        assertTrue(encoder.matches(password, originalHash));
        input = json.writeValueAsString(Map.of("nom", "Modifié", "prenom", "Commercial", "email", email, "role", "COMMERCIAL"));
        mvc.perform(put("/api/admin/accounts/UTILISATEUR/" + id).header("Authorization", "Bearer " + token)
                .contentType("application/json").content(input)).andExpect(status().isOk());
        assertEquals(originalHash, users.findById(id).orElseThrow().getMotDePasse());
        mvc.perform(delete("/api/admin/accounts/UTILISATEUR/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        assertFalse(users.existsById(id));
    }

    @Test void legacyUserIsPreservedAndMustBeExplicitlyReassigned() throws Exception {
        String password = UUID.randomUUID().toString();
        Utilisateur legacy = new Utilisateur();
        legacy.setEmail(UUID.randomUUID() + "@example.invalid"); legacy.setNom("Ancien"); legacy.setPrenom("Compte");
        legacy.setMotDePasse(encoder.encode(password)); legacy.setRole("USER"); users.save(legacy);
        String credentials = json.writeValueAsString(Map.of("email", legacy.getEmail(), "motDePasse", password));
        mvc.perform(post("/api/auth/login").contentType("application/json").content(credentials)).andExpect(status().isUnauthorized());
        assertEquals("USER", users.findById(legacy.getId()).orElseThrow().getRole());
        String token = createUser("ADMIN");
        String input = json.writeValueAsString(Map.of("nom", "Ancien", "prenom", "Compte", "email", legacy.getEmail(), "role", "COMMERCIAL"));
        mvc.perform(put("/api/admin/accounts/UTILISATEUR/" + legacy.getId()).header("Authorization", "Bearer " + token)
                .contentType("application/json").content(input)).andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").contentType("application/json").content(credentials))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("COMMERCIAL"));
        assertTrue(users.existsById(legacy.getId()));
    }

    @Test void adminCannotDeleteOrDemoteSelfAndCannotCreateLegacyRole() throws Exception {
        String token = createUser("ADMIN");
        var admin = users.findAll().stream().filter(u -> "ADMIN".equals(u.getRole())).findFirst().orElseThrow();
        mvc.perform(delete("/api/admin/accounts/UTILISATEUR/" + admin.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
        String input = json.writeValueAsString(Map.of("nom", "Test", "prenom", "Test", "email", admin.getEmail(), "role", "COMMERCIAL"));
        mvc.perform(put("/api/admin/accounts/UTILISATEUR/" + admin.getId()).header("Authorization", "Bearer " + token)
                .contentType("application/json").content(input)).andExpect(status().isConflict());
        input = json.writeValueAsString(Map.of("nom", "Test", "prenom", "Test", "email", "new@example.invalid", "role", "USER", "motDePasse", UUID.randomUUID().toString()));
        mvc.perform(post("/api/admin/accounts").header("Authorization", "Bearer " + token)
                .contentType("application/json").content(input)).andExpect(status().isBadRequest());
    }

    @Test void bootstrapCreatesOnlyFirstAdminAndNeverPromotesExistingAccounts() {
        Utilisateur legacy = new Utilisateur();
        legacy.setEmail(UUID.randomUUID() + "@example.invalid"); legacy.setRole("USER");
        legacy.setMotDePasse(encoder.encode(UUID.randomUUID().toString()));
        users.save(legacy);
        var request = new org.springdemo.gestionstock.dto.AccountRequest();
        request.setEmail(UUID.randomUUID() + "@example.invalid"); request.setNom("Initial"); request.setPrenom("Admin");
        request.setRole("ADMIN"); request.setMotDePasse(UUID.randomUUID().toString());
        accounts.bootstrap(request);
        assertEquals("ADMIN", users.findByEmail(request.getEmail()).getRole());
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> accounts.bootstrap(request));
        assertEquals(2, users.count());
        assertEquals("USER", users.findById(legacy.getId()).orElseThrow().getRole());
    }
}
