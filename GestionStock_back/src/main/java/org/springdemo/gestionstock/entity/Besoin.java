package org.springdemo.gestionstock.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class Besoin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateBesoin;

    @Enumerated(EnumType.STRING)
    private TypeBesoin typeBesoin;

    @OneToMany(mappedBy = "besoin", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("besoin")
    private List<BesoinArticle> besoinArticles;
}