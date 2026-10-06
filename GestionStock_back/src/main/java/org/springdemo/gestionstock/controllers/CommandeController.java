package org.springdemo.gestionstock.controllers;


import org.springdemo.gestionstock.dto.ArticleCommandeDto;
import org.springdemo.gestionstock.dto.BandeCommandeDto;
import org.springdemo.gestionstock.dto.CommandeDto;
import org.springdemo.gestionstock.entity.BandeCommande;
import org.springdemo.gestionstock.repository.BandeCommandeRepository;
import org.springdemo.gestionstock.services.CommandeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commandes")
public class CommandeController {

    @Autowired
    private CommandeService commandeService;
    @Autowired
    private BandeCommandeRepository bandeCommandeRepository;

    @PostMapping("/passer")
    public ResponseEntity<?> passerCommande(
            @RequestBody List<ArticleCommandeDto> articlesACommander,
            @RequestParam Long fournisseurId) {
        commandeService.passerCommande(articlesACommander, fournisseurId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<CommandeDto>> getAllCommandes() {
        List<CommandeDto> commandes = commandeService.getAllCommandes();
        return ResponseEntity.ok(commandes);
    }

    @GetMapping("/{commandeId}/bandes")
    public ResponseEntity<List<BandeCommandeDto>> getBandesCommande(@PathVariable Long commandeId) {
        List<BandeCommandeDto> bandes = commandeService.getBandesCommande(commandeId);
        return ResponseEntity.ok(bandes);
    }

    @GetMapping("/bandes")
    public ResponseEntity<List<BandeCommandeDto>> getAllBandesCommande() {
        List<BandeCommandeDto> bandes = commandeService.getAllBandesCommande();
        return ResponseEntity.ok(bandes);
    }

    @GetMapping("/bandes/{id}")
    public ResponseEntity<BandeCommandeDto> getBandeCommandeById(@PathVariable Long id) {
        BandeCommande bande = bandeCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bande de commande non trouvée"));
        BandeCommandeDto dto = commandeService.mapBandeToDto(bande);
        return ResponseEntity.ok(dto);
    }
}