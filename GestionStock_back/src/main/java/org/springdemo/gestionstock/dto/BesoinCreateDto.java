package org.springdemo.gestionstock.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import org.springdemo.gestionstock.entity.TypeBesoin;

@Data
public class BesoinCreateDto {
    private LocalDate dateBesoin;
    private TypeBesoin typeBesoin;
    private List<BesoinArticleCreateDto> besoinArticles;
}
