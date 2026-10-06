package org.springdemo.gestionstock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
public class BandeCommande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantiteConditionnement; // quantité par emballage
    private int quantite; // quantité commandée (recopiée depuis le besoin)
    private LocalDate dateArrivage;

    @ManyToOne
    @JoinColumn(name = "commande_id")
    @JsonIgnore
    private Commande commande;

    @ManyToOne
    private BesoinArticle besoinArticle;

    @ManyToOne
    @JoinColumn(name = "article_id")
    @JsonIgnoreProperties("fournisseur")
    private Article article;
}