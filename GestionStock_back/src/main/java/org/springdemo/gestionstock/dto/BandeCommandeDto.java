package org.springdemo.gestionstock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class BandeCommandeDto {
    private Long id;
    private String articleReference;
    private int quantite;
    private LocalDate dateArrivage;
    private Double prixUnitaire;
    private Double montantBande;
    private Long commandeId;
    private LocalDate dateCommande;
    private String fournisseurNom;
    private int quantiteConditionnement;
    private String designationArticle;
    private String fournisseurTelephone;
    private String fournisseurEmail;
}