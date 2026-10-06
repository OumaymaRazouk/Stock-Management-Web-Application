package org.springdemo.gestionstock.services;

import org.springdemo.gestionstock.dto.ArticleCommandeDto;
import org.springdemo.gestionstock.dto.BandeCommandeDto;
import org.springdemo.gestionstock.dto.CommandeDto;
import org.springdemo.gestionstock.entity.*;
import org.springdemo.gestionstock.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service

public class CommandeService {

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private BandeCommandeRepository bandeCommandeRepository;

    @Autowired
    private FournisseurRepository fournisseurRepository;

    @Autowired
    private BesoinArticleRepository besoinArticleRepository;
    @Autowired
    private ArticleService articleService;

    public void passerCommande(List<ArticleCommandeDto> articlesACommander, Long fournisseurId) {
        Fournisseur fournisseur = fournisseurRepository.findById(fournisseurId)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));

        Commande commande = new Commande();
        commande.setDateCommande(LocalDate.now());
        commande.setStatusCommande("EN_ATTENTE");
        commande.setFournisseur(fournisseur);

        List<BandeCommande> bandes = new ArrayList<>();

        for (ArticleCommandeDto dto : articlesACommander) {
            Article article = articleRepository.findById(dto.getArticleId())
                    .orElseThrow(() -> new RuntimeException("Article non trouvé"));

            BandeCommande bande = new BandeCommande();

            if (dto.getBesoinArticleId() != null) {
                BesoinArticle besoinArticle = besoinArticleRepository.findById(dto.getBesoinArticleId())
                        .orElseThrow(() -> new RuntimeException("BesoinArticle non trouvé"));
                bande.setBesoinArticle(besoinArticle);

                int quantiteACommander = dto.getQuantiteACommander();
                if (quantiteACommander == 0) {
                    quantiteACommander = besoinArticle.getQuantite();
                }
                int quantiteConditionnement = article.getQuantiteConditionnement();
                int nbConditionnements = (int) Math.ceil((double) quantiteACommander / quantiteConditionnement);
                int quantiteFinale = nbConditionnements * quantiteConditionnement;

                bande.setQuantite(quantiteFinale);
                bande.setQuantiteConditionnement(quantiteConditionnement);
                bande.setDateArrivage(dto.getDateArrivage());
                bande.setArticle(article);

                besoinArticle.setCommandeCreee(true);
                besoinArticleRepository.save(besoinArticle);

            } else {
                throw new RuntimeException("BesoinArticleId obligatoire pour la création de la commande");
            }
            bande.setCommande(commande);
            bandes.add(bande);
        }
        commande.setBandesCommande(bandes);

        commandeRepository.save(commande);// CascadeType.ALL va sauvegarder les bandes
        for (BandeCommande bande : bandes) {
            Article article = bande.getArticle();
            Integer stockActuel = article.getQuantite() != null ? article.getQuantite() : 0;
            int nouveauStock = stockActuel - bande.getQuantite();
            article.setQuantite(Math.max(0, nouveauStock));
            articleRepository.save(article);
        }

        articleService.verifierEtNotifierStockCritique();
    }

    public List<CommandeDto> getAllCommandes() {
        List<Commande> commandes = commandeRepository.findAll();
        return commandes.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private CommandeDto mapToDto(Commande commande) {
        List<BandeCommandeDto> bandesDto = commande.getBandesCommande().stream()
                .map(bande -> {
                    Double prixUnitaire = bande.getArticle().getPrix();
                    Double montantBande = prixUnitaire != null ? prixUnitaire * bande.getQuantite() : 0.0;
                    Article article = bande.getArticle();
                    Fournisseur fournisseur = article.getFournisseur();

                    return new BandeCommandeDto(
                            bande.getId(),
                            article.getReference(),
                            bande.getQuantite(),
                            bande.getDateArrivage(),
                            prixUnitaire,
                            montantBande,
                            bande.getCommande().getId(),
                            bande.getCommande().getDateCommande(),
                            fournisseur != null ? fournisseur.getNom() : null,
                            bande.getQuantiteConditionnement(),
                            article.getDescription(),
                            fournisseur != null ? fournisseur.getTelephone() : null,
                            fournisseur != null ? fournisseur.getEmail() : null
                    );
                }).collect(Collectors.toList());


        // Calculer le montant total de la commande
        Double montantTotal = bandesDto.stream()
                .mapToDouble(bande -> bande.getMontantBande() != null ? bande.getMontantBande() : 0.0)
                .sum();

        return new CommandeDto(
                commande.getId(),
                commande.getDateCommande(),
                commande.getStatusCommande(),
                commande.getFournisseur() != null ? commande.getFournisseur().getNom() : null,
                montantTotal,
                bandesDto
        );
    }

    public List<BandeCommandeDto> getBandesCommande(Long commandeId) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        return commande.getBandesCommande().stream()
                .map(this::mapBandeToDto)
                .collect(Collectors.toList());
    }

    public List<BandeCommandeDto> getAllBandesCommande() {
        List<BandeCommande> bandes = bandeCommandeRepository.findAll();
        return bandes.stream()
                .map(this::mapBandeToDto)
                .collect(Collectors.toList());
    }

    public BandeCommandeDto mapBandeToDto(BandeCommande bande) {
        Article article = bande.getArticle();
        Fournisseur fournisseur = article.getFournisseur();
        Double prixUnitaire = article.getPrix();
        Double montantBande = prixUnitaire != null ? prixUnitaire * bande.getQuantite() : 0.0;

        return new BandeCommandeDto(
                bande.getId(),                                 // Long id
                article.getReference(),                        // String articleReference
                bande.getQuantite(),                           // int quantite
                bande.getDateArrivage(),                       // LocalDate dateArrivage
                prixUnitaire,                                  // Double prixUnitaire
                montantBande,                                  // Double montantBande
                bande.getCommande() != null ? bande.getCommande().getId() : null, // Long commandeId
                bande.getCommande() != null ? bande.getCommande().getDateCommande() : null, // LocalDate dateCommande
                fournisseur != null ? fournisseur.getNom() : null,                // String fournisseurNom
                bande.getQuantiteConditionnement(),             // int quantiteConditionnement
                article.getDescription(),                       // String designationArticle
                fournisseur != null ? fournisseur.getTelephone() : null, // String fournisseurTelephone
                fournisseur != null ? fournisseur.getEmail() : null      // String fournisseurEmail
        );
    }
}