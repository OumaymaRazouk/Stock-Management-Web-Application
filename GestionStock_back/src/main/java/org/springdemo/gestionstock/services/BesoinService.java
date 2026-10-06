package org.springdemo.gestionstock.services;

import org.springdemo.gestionstock.dto.*;
import org.springdemo.gestionstock.entity.Article;
import org.springdemo.gestionstock.entity.Besoin;
import org.springdemo.gestionstock.entity.BesoinArticle;
import org.springdemo.gestionstock.entity.TypeBesoin;
import org.springdemo.gestionstock.repository.ArticleRepository;
import org.springdemo.gestionstock.repository.BesoinArticleRepository;
import org.springdemo.gestionstock.repository.BesoinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BesoinService {

    @Autowired
    private BesoinRepository besoinRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private BesoinArticleRepository besoinArticleRepository;

    public BesoinDto createBesoin(BesoinCreateDto dto) {
        Besoin besoin = new Besoin();
        besoin.setDateBesoin(dto.getDateBesoin());
        besoin.setTypeBesoin(dto.getTypeBesoin());

        List<BesoinArticle> besoinArticles = new ArrayList<>();

        for (BesoinArticleCreateDto baDto : dto.getBesoinArticles()) {
            Article article = articleRepository.findById(baDto.getArticleId())
                    .orElseThrow(() -> new RuntimeException("Article non trouvé avec l'ID : " + baDto.getArticleId()));

            BesoinArticle besoinArticle = new BesoinArticle();
            besoinArticle.setArticle(article);
            besoinArticle.setQuantite(baDto.getQuantite());
            besoinArticle.setTypeBesoin(dto.getTypeBesoin());
            besoinArticle.setDateBesoin(dto.getDateBesoin());
            besoinArticle.setBesoin(besoin);
            besoinArticle.setCommandeCreee(false); // par défaut : non commandé

            besoinArticles.add(besoinArticle);
        }

        besoin.setBesoinArticles(besoinArticles);
        Besoin saved = besoinRepository.save(besoin);
        return mapToDto(saved);
    }

    private BesoinDto mapToDto(Besoin besoin) {
        List<BesoinArticleDto> articleDtos = besoin.getBesoinArticles().stream().map(ba -> {
            Article article = ba.getArticle();
            Double prix = article.getPrix();
            Double montantTotal = (prix != null) ? prix * ba.getQuantite() : null;
            Long fournisseurId = (article.getFournisseur() != null) ? article.getFournisseur().getId() : null;
            String fournisseurNom = (article.getFournisseur() != null) ? article.getFournisseur().getNom() : null;

            return new BesoinArticleDto(
                    ba.getId(),
                    ba.getQuantite(),
                    ba.getDateBesoin(),
                    article.getId(),
                    article.getReference(),
                    ba.getTypeBesoin(),
                    fournisseurNom,
                    fournisseurId,
                    prix,
                    montantTotal,
                    ba.getCommandeCreee() != null ? ba.getCommandeCreee() : false,
                    article.getQuantite()
            );
        }).collect(Collectors.toList());

        return new BesoinDto(
                besoin.getId(),
                besoin.getDateBesoin(),
                besoin.getTypeBesoin(),
                articleDtos
        );
    }

    public List<BesoinDto> getAllBesoin() {
        List<Besoin> besoins = besoinRepository.findAll();
        return besoins.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BesoinDto> getBesoinByType(TypeBesoin typeBesoin) {
        return besoinRepository.findByTypeBesoin(typeBesoin).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<BesoinArticleDto> getBesoinArticlesByArticle(Long articleId) {
        return besoinRepository.findAll().stream()
                .flatMap(b -> b.getBesoinArticles().stream())
                .filter(ba -> ba.getArticle().getId().equals(articleId))
                .map(ba -> {
                    Article article = ba.getArticle();
                    Double prix = article.getPrix();
                    Double montantTotal = (prix != null) ? prix * ba.getQuantite() : null;
                    Long fournisseurId = (article.getFournisseur() != null) ? article.getFournisseur().getId() : null;
                    String fournisseurNom = (article.getFournisseur() != null) ? article.getFournisseur().getNom() : null;

                    return new BesoinArticleDto(
                            ba.getId(),
                            ba.getQuantite(),
                            ba.getDateBesoin(),
                            article.getId(),
                            article.getReference(),
                            ba.getTypeBesoin(),
                            fournisseurNom,
                            fournisseurId,
                            prix,
                            montantTotal,
                            ba.getCommandeCreee() != null ? ba.getCommandeCreee() : false ,
                            article.getQuantite()
                    );
                }).collect(Collectors.toList());
    }

    public List<BesoinDto> getBesoinByArticleReference(String reference) {
        return besoinRepository.findAll().stream()
                .filter(b -> b.getBesoinArticles().stream()
                        .anyMatch(ba -> ba.getArticle().getReference().equalsIgnoreCase(reference)))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<BesoinDto> getBesoinsByFournisseur(String nomFournisseur) {
        return besoinRepository.findAll().stream()
                .filter(b -> b.getBesoinArticles().stream()
                        .anyMatch(ba -> ba.getArticle().getFournisseur() != null &&
                                ba.getArticle().getFournisseur().getNom().equalsIgnoreCase(nomFournisseur)))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public void deleteBesoin(Long id) {
        besoinRepository.deleteById(id);
    }

    public List<BesoinDto> getBesoinsSortedByDate() {
        return besoinRepository.findAllByOrderByDateBesoinAsc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<BesoinDto> getBesoinsParDate(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr); // format attendu : "yyyy-MM-dd"

        return besoinRepository.findAll().stream()
                .filter(b -> b.getDateBesoin() != null && b.getDateBesoin().equals(date))
                .sorted(Comparator.comparing(Besoin::getDateBesoin))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<BesoinDto> getBesoinsPriorisesParDate() {
        return besoinRepository.findAll().stream()
                .filter(b -> b.getDateBesoin() != null)
                .sorted(Comparator.comparing(Besoin::getDateBesoin)) // tri ascendant
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ... dans BesoinService.java

    public List<BesoinCommandeDto> genererTableCommande() {
        List<BesoinArticle> besoins = besoinArticleRepository.findByCommandeCreeeFalseOrderByDateBesoinAsc();

        Map<String, Integer> stockMap = new HashMap<>();
        articleRepository.findAll().forEach(article ->
                stockMap.put(article.getReference(), article.getQuantite() != null ? article.getQuantite() : 0)
        );

        Map<String, BesoinCommandeDto> resultMap = new HashMap<>();

        for (BesoinArticle ba : besoins) {
            Article article = ba.getArticle();
            String ref = article.getReference();
            int qBesoin = ba.getQuantite();

            int stockActuel = stockMap.getOrDefault(ref, 0);
            int stockSecurite = article.getStockSecurite();
            int stockDisponible = Math.max(0, stockActuel - stockSecurite);

            int qCommande = 0;
            if (qBesoin <= stockDisponible) {
                stockMap.put(ref, stockActuel - qBesoin);
            } else {
                qCommande = qBesoin - stockDisponible;
                stockMap.put(ref, stockActuel - stockDisponible);

                Double prixUnitaire = article.getPrix();
                Double montantTotal = (prixUnitaire != null) ? prixUnitaire * qCommande : null;

                if (!resultMap.containsKey(ref)) {
                    resultMap.put(ref, new BesoinCommandeDto(
                            ref,
                            qCommande,
                            ba.getDateBesoin(),
                            article.getFournisseur() != null ? article.getFournisseur().getId() : null,
                            article.getId(),
                            article.getFournisseur() != null ? article.getFournisseur().getNom() : null,
                            ba.getId(),
                            prixUnitaire,
                            montantTotal,
                            stockSecurite
                    ));
                } else {
                    BesoinCommandeDto dto = resultMap.get(ref);
                    dto.setQuantiteACommander(dto.getQuantiteACommander() + qCommande);
                    // Met à jour le montant total
                    if (prixUnitaire != null && dto.getMontantTotal() != null) {
                        dto.setMontantTotal(dto.getMontantTotal() + montantTotal);
                    }
                    if (ba.getDateBesoin().isAfter(dto.getDateDernierBesoin())) {
                        dto.setDateDernierBesoin(ba.getDateBesoin());
                    }
                }
            }
        }
        return new ArrayList<>(resultMap.values());
    }

    private BesoinDto convertToDto(Besoin besoin) {
        BesoinDto dto = new BesoinDto();
        dto.setId(besoin.getId());
        dto.setDateBesoin(besoin.getDateBesoin());
        dto.setTypeBesoin(besoin.getTypeBesoin());

        List<BesoinArticleDto> articleDtos = besoin.getBesoinArticles().stream().map(ba -> {
            Article article = ba.getArticle();

            return new BesoinArticleDto(
                    ba.getId(),
                    ba.getQuantite(),
                    ba.getDateBesoin(),
                    article != null ? article.getId() : null,
                    article != null ? article.getReference() : null,
                    ba.getTypeBesoin(),
                    article != null && article.getFournisseur() != null ? article.getFournisseur().getNom() : null,
                    article != null && article.getFournisseur() != null ? article.getFournisseur().getId() : null,
                    article != null ? article.getPrix() : null,
                    article != null && article.getPrix() != null ? article.getPrix() * ba.getQuantite() : null,
                    ba.getCommandeCreee(),
                    article != null ? article.getQuantite() : null // 👈 quantiteEnStock
            );
        }).toList();

        dto.setBesoinArticles(articleDtos);
        return dto;
    }

    private String calculerStatutStock(Integer quantite, int stockSecurite) {
        if (quantite == null || quantite == 0) return "RUPTURE";
        if (quantite <= stockSecurite) return "FAIBLE";
        return "OK";
    }



    @Transactional
    public BesoinDto modifierBesoin(Long id, BesoinCreateDto dto) {
        Besoin besoin = besoinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Besoin non trouvé avec l'ID : " + id));

        // Mise à jour des propriétés de base
        besoin.setDateBesoin(dto.getDateBesoin());
        besoin.setTypeBesoin(dto.getTypeBesoin());

        // Parcours des articles à mettre à jour (sans toucher à ceux non présents)
        for (BesoinArticleCreateDto baDto : dto.getBesoinArticles()) {
            for (BesoinArticle existant : besoin.getBesoinArticles()) {
                if (existant.getArticle().getId().equals(baDto.getArticleId())) {
                    existant.setQuantite(baDto.getQuantite());
                    existant.setDateBesoin(dto.getDateBesoin());
                    existant.setTypeBesoin(dto.getTypeBesoin());
                }
            }
        }

        // Enregistrement
        Besoin saved = besoinRepository.save(besoin);
        return mapToDto(saved);
    }




    public BesoinDto getBesoinById(Long id) {
        Besoin besoin = besoinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Besoin non trouvé avec l'ID : " + id));
        return mapToDto(besoin);
    }
}
