package org.springdemo.gestionstock.controllers;


import org.springdemo.gestionstock.dto.BesoinArticleDto;
import org.springdemo.gestionstock.dto.BesoinCommandeDto;
import org.springdemo.gestionstock.dto.BesoinCreateDto;

import org.springdemo.gestionstock.dto.BesoinDto;
import org.springdemo.gestionstock.entity.BesoinArticle;
import org.springdemo.gestionstock.entity.TypeBesoin;
import org.springdemo.gestionstock.services.BesoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/besoins")
public class BesoinController {
    @Autowired
    private BesoinService besoinService;

    @PostMapping
    public ResponseEntity<BesoinDto> createBesoin(@RequestBody BesoinCreateDto dto,
                                                 org.springframework.security.core.Authentication auth) {
        boolean commercial = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COMMERCIAL"));
        if (commercial && dto.getTypeBesoin() != TypeBesoin.DEVIS && dto.getTypeBesoin() != TypeBesoin.NEGOCE) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Un commercial peut créer des besoins DEVIS ou NEGOCE uniquement");
        }
        BesoinDto created = besoinService.createBesoin(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<BesoinDto>> getAllBesoins() {
        return ResponseEntity.ok(besoinService.getAllBesoin());
    }

    @GetMapping("/type/{typeBesoin}")
    public ResponseEntity<List<BesoinDto>> getBesoinsByType(@PathVariable TypeBesoin typeBesoin) {
        return ResponseEntity.ok(besoinService.getBesoinByType(typeBesoin));
    }

    @GetMapping("/article/{articleId}")
    public ResponseEntity<List<BesoinArticleDto>> getBesoinsByArticle(@PathVariable Long articleId) {
        return ResponseEntity.ok(besoinService.getBesoinArticlesByArticle(articleId));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<BesoinDto>> getBesoinsByArticleReference(@RequestParam String reference) {
        return ResponseEntity.ok(besoinService.getBesoinByArticleReference(reference));
    }

    @GetMapping("/fournisseur")
    public ResponseEntity<List<BesoinDto>> getBesoinsByFournisseur(@RequestParam String nom) {
        return ResponseEntity.ok(besoinService.getBesoinsByFournisseur(nom));
    }

    @DeleteMapping("/{id}")
    public void deleteBesoin(@PathVariable Long id) {
        besoinService.deleteBesoin(id);
    }

    @GetMapping("/sorted-by-date")
    public ResponseEntity<List<BesoinDto>> getBesoinsSortedByDate() {
        return ResponseEntity.ok(besoinService.getBesoinsSortedByDate());
    }


    @GetMapping("/par-date")
    public ResponseEntity<List<BesoinDto>> getBesoinsParDate(@RequestParam String date) {
        return ResponseEntity.ok(besoinService.getBesoinsParDate(date));
    }
    @GetMapping("/priorises")
    public ResponseEntity<List<BesoinDto>> getBesoinsTriesParDate() {
        return ResponseEntity.ok(besoinService.getBesoinsPriorisesParDate());
    }

    @GetMapping("/commande/preparation")
    public ResponseEntity<List<BesoinCommandeDto>> getPreparationCommande() {
        return ResponseEntity.ok(besoinService.genererTableCommande());
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> modifierBesoin(@PathVariable Long id, @RequestBody BesoinCreateDto dto) {
        try {
            BesoinDto modified = besoinService.modifierBesoin(id, dto);
            return ResponseEntity.ok(modified);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<BesoinDto> getBesoinById(@PathVariable Long id) {
        try {
            BesoinDto besoin = besoinService.getBesoinById(id);
            return ResponseEntity.ok(besoin);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }




}
