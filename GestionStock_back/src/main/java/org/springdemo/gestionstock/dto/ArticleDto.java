package org.springdemo.gestionstock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDto {
    private Long id;
    private String reference;
    private String description;
    private int stockSecurite;
    private int quantiteConditionnement;
    private Integer quantite;
    private Long fournisseurId;// Optionnel pour le Fournisseur
    private Double prix;
    private String statutStock; // "OK", "FAIBLE", "RUPTURE"



    public void validate() {
        if (reference == null || reference.trim().isEmpty()) {
            throw new IllegalArgumentException("La référence est requise");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("La description est requise");
        }
        if (stockSecurite < 0) {
            throw new IllegalArgumentException("Le stock de sécurité doit être positif");
        }
        if (quantiteConditionnement <= 0) {
            throw new IllegalArgumentException("La quantité de conditionnement doit être positive");
        }
        if (fournisseurId == null) {
            throw new IllegalArgumentException("L'ID du fournisseur est requis");
        }
    }
}