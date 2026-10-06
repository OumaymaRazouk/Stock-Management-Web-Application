package org.springdemo.gestionstock.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class BesoinArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantite;
    private LocalDate dateBesoin;


    // 🔗 Relation avec Besoin
    @ManyToOne
    @JoinColumn(name = "besoin_id")
    @JsonIgnoreProperties("besoinArticles")
    private Besoin besoin;

    @Getter
    @Setter
    private Boolean commandeCreee = false;
    // 🔗 Relation avec Article
    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;

    // 🔗 Relation avec TypeBesoin
    @Enumerated(EnumType.STRING)
    private TypeBesoin typeBesoin;


}

