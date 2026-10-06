package org.springdemo.gestionstock.services;

import org.springdemo.gestionstock.dto.AccountDto;
import org.springdemo.gestionstock.dto.AccountRequest;
import org.springdemo.gestionstock.entity.Acheteur;
import org.springdemo.gestionstock.entity.Utilisateur;
import org.springdemo.gestionstock.repository.AcheteurRepository;
import org.springdemo.gestionstock.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class AccountService {
    private final UtilisateurRepository users;
    private final AcheteurRepository buyers;
    private final PasswordEncoder encoder;
    private final TransactionTemplate transaction;
    private static final Set<String> ROLES = Set.of("ADMIN", "ACHETEUR", "COMMERCIAL");

    public AccountService(UtilisateurRepository users, AcheteurRepository buyers,
                          PasswordEncoder encoder, org.springframework.transaction.PlatformTransactionManager manager) {
        this.users = users;
        this.buyers = buyers;
        this.encoder = encoder;
        this.transaction = new TransactionTemplate(manager);
    }

    public List<AccountDto> list() {
        List<AccountDto> result = new ArrayList<>();
        users.findAll().forEach(u -> result.add(dto(u)));
        buyers.findAll().forEach(u -> result.add(dto(u)));
        return result;
    }

    // Serializes account mutations through commit for the supported single-instance deployment.
    public synchronized AccountDto create(AccountRequest request) {
        return transaction.execute(status -> {
            validate(request, true);
            checkEmail(request.getEmail(), null, null);
            if ("ACHETEUR".equals(request.getRole())) {
                Acheteur user = new Acheteur();
                user.setNom(request.getNom()); user.setPrenom(request.getPrenom()); user.setEmail(request.getEmail());
                user.setRole(request.getRole()); user.setMotDePasse(encoder.encode(request.getMotDePasse()));
                return dto(buyers.save(user));
            }
            Utilisateur user = new Utilisateur();
            user.setNom(request.getNom()); user.setPrenom(request.getPrenom()); user.setEmail(request.getEmail());
            user.setRole(request.getRole()); user.setMotDePasse(encoder.encode(request.getMotDePasse()));
            return dto(users.save(user));
        });
    }

    public synchronized AccountDto update(String source, Long id, AccountRequest request, String actor) {
        return transaction.execute(status -> {
            validate(request, false);
            checkSource(source);
            checkEmail(request.getEmail(), source, id);
            if ("UTILISATEUR".equals(source)) {
                Utilisateur user = users.findById(id).orElseThrow(this::missing);
                protectAdmin(user.getEmail(), user.getRole(), request.getRole(), actor);
                user.setNom(request.getNom()); user.setPrenom(request.getPrenom()); user.setEmail(request.getEmail());
                user.setRole(request.getRole());
                if (hasPassword(request)) user.setMotDePasse(encoder.encode(request.getMotDePasse()));
                return dto(users.save(user));
            }
            Acheteur user = buyers.findById(id).orElseThrow(this::missing);
            protectAdmin(user.getEmail(), user.getRole(), request.getRole(), actor);
            user.setNom(request.getNom()); user.setPrenom(request.getPrenom()); user.setEmail(request.getEmail());
            user.setRole(request.getRole());
            if (hasPassword(request)) user.setMotDePasse(encoder.encode(request.getMotDePasse()));
            return dto(buyers.save(user));
        });
    }

    public synchronized void delete(String source, Long id, String actor) {
        transaction.executeWithoutResult(status -> {
            checkSource(source);
            if ("UTILISATEUR".equals(source)) {
                Utilisateur user = users.findById(id).orElseThrow(this::missing);
                if (user.getEmail().equals(actor)) throw conflict("Vous ne pouvez pas supprimer votre propre compte");
                protectAdmin(user.getEmail(), user.getRole(), null, actor);
                users.delete(user);
            } else {
                Acheteur user = buyers.findById(id).orElseThrow(this::missing);
                if (user.getEmail().equals(actor)) throw conflict("Vous ne pouvez pas supprimer votre propre compte");
                protectAdmin(user.getEmail(), user.getRole(), null, actor);
                buyers.delete(user);
            }
        });
    }

    /** Explicit bootstrap only: never promotes or replaces any existing account. */
    public synchronized void bootstrap(AccountRequest request) {
        transaction.executeWithoutResult(status -> {
            if (adminCount() != 0) throw conflict("Un administrateur existe déjà : initialisation refusée");
            if (!"ADMIN".equals(request.getRole())) throw conflict("Rôle d'initialisation invalide");
            create(request);
        });
    }

    private void protectAdmin(String email, String oldRole, String newRole, String actor) {
        if ("ADMIN".equals(oldRole) && !"ADMIN".equals(newRole)) {
            if (email.equals(actor)) throw conflict("Vous ne pouvez pas retirer votre propre rôle ADMIN");
            if (adminCount() <= 1) throw conflict("Le dernier administrateur doit être conservé");
        }
    }

    private long adminCount() {
        return users.findAll().stream().filter(u -> "ADMIN".equals(u.getRole())).count()
                + buyers.findAll().stream().filter(u -> "ADMIN".equals(u.getRole())).count();
    }

    private void validate(AccountRequest r, boolean creating) {
        if (r.getRole() == null || !ROLES.contains(r.getRole())) throw bad("Rôle invalide");
        if (r.getNom() == null || r.getNom().isBlank() || r.getPrenom() == null || r.getPrenom().isBlank()
                || r.getEmail() == null || r.getEmail().isBlank()) throw bad("Nom, prénom et email obligatoires");
        if (!r.getEmail().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw bad("Email invalide");
        if (creating && !hasPassword(r)) throw bad("Mot de passe obligatoire");
        if (hasPassword(r) && (r.getMotDePasse().length() < 6
                || r.getMotDePasse().getBytes(StandardCharsets.UTF_8).length > 72)) {
            throw bad("Mot de passe : 6 caractères minimum et 72 octets maximum");
        }
    }

    private boolean hasPassword(AccountRequest r) { return r.getMotDePasse() != null && !r.getMotDePasse().isEmpty(); }
    private void checkSource(String source) {
        if (!Set.of("UTILISATEUR", "ACHETEUR").contains(source)) throw bad("Source de compte invalide");
    }
    private void checkEmail(String email, String source, Long id) {
        var u = users.findByEmail(email);
        var b = buyers.findByEmail(email);
        if ((u != null && !("UTILISATEUR".equals(source) && u.getId().equals(id)))
                || (b != null && !("ACHETEUR".equals(source) && b.getId().equals(id)))) throw conflict("Email déjà utilisé");
    }
    private AccountDto dto(Utilisateur u) { return new AccountDto(u.getId(), "UTILISATEUR", u.getNom(), u.getPrenom(), u.getEmail(), u.getRole()); }
    private AccountDto dto(Acheteur u) { return new AccountDto(u.getId(), "ACHETEUR", u.getNom(), u.getPrenom(), u.getEmail(), u.getRole()); }
    private ResponseStatusException missing() { return new ResponseStatusException(HttpStatus.NOT_FOUND, "Compte introuvable"); }
    private ResponseStatusException bad(String text) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, text); }
    private ResponseStatusException conflict(String text) { return new ResponseStatusException(HttpStatus.CONFLICT, text); }
}
