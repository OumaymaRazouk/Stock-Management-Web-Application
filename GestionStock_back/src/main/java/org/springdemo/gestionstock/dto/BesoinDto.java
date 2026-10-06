package org.springdemo.gestionstock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdemo.gestionstock.entity.TypeBesoin;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BesoinDto {
    private Long id;
    private LocalDate dateBesoin;
    private TypeBesoin typeBesoin;
    private List<BesoinArticleDto> besoinArticles;
}
