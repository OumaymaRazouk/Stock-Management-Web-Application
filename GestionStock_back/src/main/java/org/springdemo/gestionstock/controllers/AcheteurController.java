package org.springdemo.gestionstock.controllers;

import org.springdemo.gestionstock.dto.AccountRequest;
import org.springdemo.gestionstock.entity.Acheteur;
import org.springdemo.gestionstock.services.AccountService;
import org.springdemo.gestionstock.services.AcheteurService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;

/** Compatibility endpoints for the existing buyer screens; admin-only in SecurityConfig. */
@RestController
@RequestMapping("/api/acheteurs")
public class AcheteurController {
    private final AcheteurService buyers;
    private final AccountService accounts;
    public AcheteurController(AcheteurService buyers, AccountService accounts) {
        this.buyers = buyers; this.accounts = accounts;
    }
    @PostMapping
    public ResponseEntity<?> createAcheteur(@RequestBody Acheteur buyer) {
        accounts.create(request(buyer, "ACHETEUR"));
        return ResponseEntity.ok(Collections.singletonMap("message", "Acheteur créé avec succès"));
    }
    @GetMapping
    public List<Acheteur> getAllAcheteurs() { return buyers.findAll(); }
    @GetMapping("/{id}")
    public ResponseEntity<Acheteur> getAcheteurById(@PathVariable Long id) {
        var buyer = buyers.findById(id);
        return buyer == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(buyer);
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAcheteur(@PathVariable Long id, @RequestBody Acheteur request, Authentication auth) {
        var current = buyers.findById(id);
        if (current == null) return ResponseEntity.notFound().build();
        accounts.update("ACHETEUR", id, request(request, current.getRole()), auth.getName());
        return ResponseEntity.ok(Collections.singletonMap("message", "Acheteur modifié avec succès"));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAcheteur(@PathVariable Long id, Authentication auth) {
        accounts.delete("ACHETEUR", id, auth.getName());
        return ResponseEntity.ok(Collections.singletonMap("message", "Acheteur supprimé avec succès"));
    }
    private AccountRequest request(Acheteur buyer, String role) {
        AccountRequest r = new AccountRequest();
        r.setNom(buyer.getNom()); r.setPrenom(buyer.getPrenom()); r.setEmail(buyer.getEmail());
        r.setMotDePasse(buyer.getMotDePasse()); r.setRole(role);
        return r;
    }
}
