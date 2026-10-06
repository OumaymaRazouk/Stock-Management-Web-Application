package org.springdemo.gestionstock.dto;

import lombok.Data;

@Data
public class BesoinArticleCreateDto {
    private Long articleId;
    private int quantite;
}
