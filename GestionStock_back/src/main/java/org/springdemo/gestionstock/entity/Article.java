package org.springdemo.gestionstock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reference;

    private String description;

    private int stockSecurite;

    private int quantiteConditionnement;
    private Integer quantite;
    private Double prix;
    @ManyToOne
    @JoinColumn(name = "fournisseur_id", nullable = true)
    private Fournisseur fournisseur;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<BesoinArticle> besoinArticles = new ArrayList<>();

    @OneToMany(mappedBy = "article", orphanRemoval = true)
    @JsonIgnore
    private List<BandeCommande> bandesCommande;

    @ManyToMany
    @JoinTable(
            name = "article_fournisseurs",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "fournisseur_id")
    )
    private List<Fournisseur> fournisseurs = new ArrayList<>();


}