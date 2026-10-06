package org.springdemo.gestionstock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class BesoinCommandeDto {
    private String referenceArticle;
    private int quantiteACommander;
    private LocalDate dateDernierBesoin;

    private Long fournisseurId;
    private Long articleId;
    private String fournisseurNom;
    private Long besoinArticleId; // Ajout du champ manquant


    private Double prixUnitaire;
    private Double montantTotal;
    private Integer stockSecurite;
    // Getters et setters (générés par Lombok @Data)

    // Optionnel : si tu veux définir manuellement les setters (par exemple pour mise à jour)
    public void setReferenceArticle(String referenceArticle) {
        this.referenceArticle = referenceArticle;
    }

    public void setQuantiteACommander(int quantiteACommander) {
        this.quantiteACommander = quantiteACommander;
    }

    public void setDateDernierBesoin(LocalDate dateDernierBesoin) {
        this.dateDernierBesoin = dateDernierBesoin;
    }

    public void setFournisseurId(Long fournisseurId) {
        this.fournisseurId = fournisseurId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public void setFournisseurNom(String fournisseurNom) {
        this.fournisseurNom = fournisseurNom;
    }

    public void setBesoinArticleId(Long besoinArticleId) {
        this.besoinArticleId = besoinArticleId;
    }
}
