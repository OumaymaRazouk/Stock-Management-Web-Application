// src/main/java/org/springdemo/gestionstock/dto/ArticleCommandeDto.java
package org.springdemo.gestionstock.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ArticleCommandeDto {
    private Long articleId;
    private LocalDate dateArrivage;
    private Long besoinArticleId; // obligatoire pour retrouver la quantité à commander
    private Integer quantiteACommander;
}