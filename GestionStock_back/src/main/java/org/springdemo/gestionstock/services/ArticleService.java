package org.springdemo.gestionstock.services;

import org.springdemo.gestionstock.dto.ArticleDto;
import org.springdemo.gestionstock.dto.ErrorDto;
import org.springdemo.gestionstock.entity.Article;
import org.springdemo.gestionstock.entity.Fournisseur;
import org.springdemo.gestionstock.repository.ArticleRepository;
import org.springdemo.gestionstock.repository.FournisseurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
public class ArticleService {
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private FournisseurRepository fournisseurRepository;

    public Article createArticle(ArticleDto dto) {
        Article article = new Article();
        article.setReference(dto.getReference());
        article.setDescription(dto.getDescription());
        article.setStockSecurite(dto.getStockSecurite());
        article.setQuantiteConditionnement(dto.getQuantiteConditionnement());
        article.setQuantite(dto.getQuantite());  // Permettra NULL
        article.setPrix(dto.getPrix());
        if (dto.getFournisseurId() != null) {
            Fournisseur fournisseur = fournisseurRepository.findById(dto.getFournisseurId())
                    .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
            article.setFournisseur(fournisseur);

        }

        return articleRepository.save(article);
    }

    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    public Article modifierArticle(Long id, ArticleDto dto) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article avec ID " + id + " introuvable."));

        article.setReference(dto.getReference());
        article.setDescription(dto.getDescription());
        article.setStockSecurite(dto.getStockSecurite());
        article.setQuantiteConditionnement(dto.getQuantiteConditionnement());
        article.setQuantite(dto.getQuantite());
        article.setPrix(dto.getPrix());

        if (dto.getFournisseurId() != null) {
            Fournisseur fournisseur = fournisseurRepository.findById(dto.getFournisseurId())
                    .orElseThrow(() -> new RuntimeException("Fournisseur avec ID " + dto.getFournisseurId() + " introuvable."));
            article.setFournisseur(fournisseur);
        } else {
            article.setFournisseur(null);
        }

        return articleRepository.save(article);
    }
    public Article getArticle(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article avec ID " + id + " introuvable."));
    }

    private String calculerStatutStock(Integer quantite, int stockSecurite) {
        if (quantite == null || quantite == 0) return "RUPTURE";
        if (quantite <= stockSecurite) return "FAIBLE";
        return "OK";
    }


    public void verifierEtNotifierStockCritique() {
        List<Article> articles = articleRepository.findAll();
        for (Article article : articles) {
            String statut = calculerStatutStock(article.getQuantite(), article.getStockSecurite());
            if (!"OK".equals(statut)) {
                // TODO: Remplacer par une vraie notification (email, websocket, etc.)
                System.out.println("ALERTE STOCK " + statut + " pour l'article " + article.getReference());
            }
        }
    }




    public ArticleDto mapToDto(Article article) {
        ArticleDto dto = new ArticleDto(
                article.getId(),
                article.getReference(),
                article.getDescription(),
                article.getStockSecurite(),
                article.getQuantiteConditionnement(),
                article.getQuantite(),
                article.getFournisseur() != null ? article.getFournisseur().getId() : null,
                article.getPrix(),
                calculerStatutStock(article.getQuantite(), article.getStockSecurite())

        );

        return dto;
    }

    public void supprimerArticle(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new RuntimeException("Article non trouvé");
        }
        articleRepository.deleteById(id);
    }
}


