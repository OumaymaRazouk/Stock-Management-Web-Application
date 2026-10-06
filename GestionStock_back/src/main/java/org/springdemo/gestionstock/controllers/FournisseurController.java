package org.springdemo.gestionstock.controllers;

import org.springdemo.gestionstock.services.FournisseurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import org.springdemo.gestionstock.dto.FournisseurDto;
import org.springdemo.gestionstock.entity.Article;


import java.util.List;

@RestController
@RequestMapping("/api/fournisseurs")
public class FournisseurController {

    @Autowired
    private FournisseurService fournisseurService;

    @PostMapping
    public ResponseEntity<FournisseurDto> ajouterFournisseur(@RequestBody FournisseurDto fournisseurDto) {
        FournisseurDto fournisseur = fournisseurService.ajouterFournisseur(fournisseurDto);
        return ResponseEntity.ok(fournisseur);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FournisseurDto> modifierFournisseur(@PathVariable Long id, @RequestBody FournisseurDto fournisseurDto) {
        FournisseurDto fournisseur = fournisseurService.modifierFournisseur(id, fournisseurDto);
        return ResponseEntity.ok(fournisseur);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerFournisseur(@PathVariable Long id) {
        fournisseurService.supprimerFournisseur(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FournisseurDto>> listerFournisseurs() {
        List<FournisseurDto> fournisseurs = fournisseurService.listerFournisseurs();
        return ResponseEntity.ok(fournisseurs);
    }

    @GetMapping("/filtrer")
    public ResponseEntity<List<FournisseurDto>> filtrerFournisseurs(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String email) {
        List<FournisseurDto> fournisseurs = fournisseurService.filtrerFournisseurs(nom, email);
        return ResponseEntity.ok(fournisseurs);
    }

    @GetMapping("/{id}/articles")
    public ResponseEntity<List<Article>> listerArticlesFournisseur(@PathVariable Long id) {
        List<Article> articles = fournisseurService.listerArticlesFournisseur(id);
        return ResponseEntity.ok(articles);
    }
    @GetMapping("/{id}")
    public ResponseEntity<FournisseurDto> getFournisseurById(@PathVariable Long id) {
        FournisseurDto fournisseur = fournisseurService.getFournisseurById(id);
        if (fournisseur != null) {
            return ResponseEntity.ok(fournisseur);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}