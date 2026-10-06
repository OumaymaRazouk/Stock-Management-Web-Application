package org.springdemo.gestionstock.controllers;

import org.springdemo.gestionstock.dto.ArticleDto;
import org.springdemo.gestionstock.entity.Article;
import org.springdemo.gestionstock.services.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/articles")

public class ArticleController {

    @Autowired
    private ArticleService articleService;



    @PutMapping("/{id}")
    public ResponseEntity<?> modifierArticle(@PathVariable Long id, @RequestBody ArticleDto dto) {
        try {
            if (dto.getReference() == null || dto.getReference().isEmpty()) {
                return ResponseEntity.badRequest().body("La référence est obligatoire.");
            }
            Article article = articleService.modifierArticle(id, dto);
            ArticleDto responseDto = articleService.mapToDto(article);
            return ResponseEntity.ok(responseDto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la modification : " + e.getMessage());
        }
    }



    @GetMapping
    public ResponseEntity<List<ArticleDto>> getAllArticles() {
        List<ArticleDto> dtos = articleService.getAllArticles().stream()
                .map(articleService::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }



    @PostMapping
    public ResponseEntity<ArticleDto> createArticle(@RequestBody ArticleDto dto) {
        Article article = articleService.createArticle(dto);
        return ResponseEntity.ok(articleService.mapToDto(article));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ArticleDto> getArticle(@PathVariable Long id) {
        Article article = articleService.getArticle(id);
        return ResponseEntity.ok(articleService.mapToDto(article));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerArticle(@PathVariable Long id) {
        articleService.supprimerArticle(id);
        return ResponseEntity.noContent().build();
    }


}
