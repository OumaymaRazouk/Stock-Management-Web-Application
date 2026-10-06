package org.springdemo.gestionstock.dto;

import org.springdemo.gestionstock.entity.TypeBesoin;

import java.time.LocalDate;

public class BesoinArticleDto {

    private Long id;
    private int quantite;
    private LocalDate dateBesoin;
    private Long articleId;
    private String articleReference;
    private TypeBesoin typeBesoin;
    private String fournisseur;
    private Long fournisseurId;
    private Double prix;           // prix unitaire
    private Double montantTotal;   // prix * quantité
    private Boolean commandeCreee; // indique si une commande a été créée
    private Integer quantiteEnStock;
    private String statutStock;// ✅ Quantité actuelle en stock

    // ✅ Constructeur complet avec quantiteEnStock
    public BesoinArticleDto(Long id, int quantite, LocalDate dateBesoin,
                            Long articleId, String articleReference, TypeBesoin typeBesoin,
                            String fournisseur, Long fournisseurId,
                            Double prix, Double montantTotal, Boolean commandeCreee,
                            Integer quantiteEnStock) {
        this.id = id;
        this.quantite = quantite;
        this.dateBesoin = dateBesoin;
        this.articleId = articleId;
        this.articleReference = articleReference;
        this.typeBesoin = typeBesoin;
        this.fournisseur = fournisseur;
        this.fournisseurId = fournisseurId;
        this.prix = prix;
        this.montantTotal = montantTotal;
        this.commandeCreee = commandeCreee;
        this.quantiteEnStock = quantiteEnStock;
    }

    // ✅ Constructeur partiel
    public BesoinArticleDto(Long id, int quantite, LocalDate dateBesoin,
                            Long articleId, String articleReference, TypeBesoin typeBesoin,
                            String fournisseur) {
        this(id, quantite, dateBesoin, articleId, articleReference, typeBesoin,
                fournisseur, null, null, null, false, null);
    }

    // ✅ Constructeur vide
    public BesoinArticleDto() {
    }

    // ✅ Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public LocalDate getDateBesoin() {
        return dateBesoin;
    }

    public void setDateBesoin(LocalDate dateBesoin) {
        this.dateBesoin = dateBesoin;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public String getArticleReference() {
        return articleReference;
    }

    public void setArticleReference(String articleReference) {
        this.articleReference = articleReference;
    }

    public TypeBesoin getTypeBesoin() {
        return typeBesoin;
    }

    public void setTypeBesoin(TypeBesoin typeBesoin) {
        this.typeBesoin = typeBesoin;
    }

    public String getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(String fournisseur) {
        this.fournisseur = fournisseur;
    }

    public Long getFournisseurId() {
        return fournisseurId;
    }

    public void setFournisseurId(Long fournisseurId) {
        this.fournisseurId = fournisseurId;
    }

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    public Double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(Double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public Boolean getCommandeCreee() {
        return commandeCreee;
    }

    public void setCommandeCreee(Boolean commandeCreee) {
        this.commandeCreee = commandeCreee;
    }

    public Integer getQuantiteEnStock() {
        return quantiteEnStock;
    }

    public void setQuantiteEnStock(Integer quantiteEnStock) {
        this.quantiteEnStock = quantiteEnStock;
    }

    public String getStatutStock() {
        return statutStock;
    }

    public void setStatutStock(String statutStock) {
        this.statutStock = statutStock;
    }
}
