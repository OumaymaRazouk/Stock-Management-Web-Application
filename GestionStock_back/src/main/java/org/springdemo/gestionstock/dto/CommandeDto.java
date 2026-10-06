package org.springdemo.gestionstock.dto;


import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CommandeDto {
    private Long id;
    private LocalDate dateCommande;
    private String statusCommande;
    private String fournisseurNom;
    private Double montantTotal;
    private List<BandeCommandeDto> bandesCommande;

    // Constructeur
    public CommandeDto(Long id, LocalDate dateCommande, String statusCommande,
                       String fournisseurNom, Double montantTotal, List<BandeCommandeDto> bandesCommande) {
        this.id = id;
        this.dateCommande = dateCommande;
        this.statusCommande = statusCommande;
        this.fournisseurNom = fournisseurNom;
        this.montantTotal = montantTotal;
        this.bandesCommande = bandesCommande;
    }
}