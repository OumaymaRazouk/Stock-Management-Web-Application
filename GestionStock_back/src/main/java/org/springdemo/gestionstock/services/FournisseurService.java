package org.springdemo.gestionstock.services;

import org.springdemo.gestionstock.dto.FournisseurDto;
import org.springdemo.gestionstock.entity.Fournisseur;
import org.springdemo.gestionstock.entity.Article;
import org.springdemo.gestionstock.repository.FournisseurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FournisseurService {

    @Autowired
    private FournisseurRepository fournisseurRepository;

    private FournisseurDto toDto(Fournisseur fournisseur) {
        FournisseurDto dto = new FournisseurDto();
        dto.setId(fournisseur.getId());
        dto.setNom(fournisseur.getNom());
        dto.setAdresse(fournisseur.getAdresse());
        dto.setTelephone(fournisseur.getTelephone());
        dto.setEmail(fournisseur.getEmail());
        return dto;
    }

    private Fournisseur toEntity(FournisseurDto dto) {
        Fournisseur fournisseur = new Fournisseur();
        fournisseur.setId(dto.getId());
        fournisseur.setNom(dto.getNom());
        fournisseur.setAdresse(dto.getAdresse());
        fournisseur.setTelephone(dto.getTelephone());
        fournisseur.setEmail(dto.getEmail());
        return fournisseur;
    }

    public FournisseurDto ajouterFournisseur(FournisseurDto fournisseurDto) {
        Fournisseur fournisseur = toEntity(fournisseurDto);
        fournisseur = fournisseurRepository.save(fournisseur);
        return toDto(fournisseur);
    }

    public FournisseurDto modifierFournisseur(Long id, FournisseurDto fournisseurDto) {
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));

        fournisseur.setNom(fournisseurDto.getNom());
        fournisseur.setAdresse(fournisseurDto.getAdresse());
        fournisseur.setTelephone(fournisseurDto.getTelephone());
        fournisseur.setEmail(fournisseurDto.getEmail());

        fournisseur = fournisseurRepository.save(fournisseur);
        return toDto(fournisseur);
    }

    public void supprimerFournisseur(Long id) {
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
        fournisseurRepository.delete(fournisseur);
    }

    public List<FournisseurDto> listerFournisseurs() {
        List<Fournisseur> fournisseurs = fournisseurRepository.findAll();
        return fournisseurs.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<FournisseurDto> filtrerFournisseurs(String nom, String email) {
        List<Fournisseur> fournisseurs = fournisseurRepository.findAll();

        if (nom != null) {
            fournisseurs = fournisseurs.stream()
                    .filter(f -> f.getNom().toLowerCase().contains(nom.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (email != null) {
            fournisseurs = fournisseurs.stream()
                    .filter(f -> f.getEmail().toLowerCase().contains(email.toLowerCase()))
                    .collect(Collectors.toList());
        }

        return fournisseurs.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<Article> listerArticlesFournisseur(Long id) {
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
        return fournisseur.getArticles();
    }
    public FournisseurDto getFournisseurById(Long id) {
        return fournisseurRepository.findById(id)
                .map(fournisseur -> {
                    FournisseurDto dto = new FournisseurDto();
                    dto.setId(fournisseur.getId());
                    dto.setNom(fournisseur.getNom());
                    dto.setAdresse(fournisseur.getAdresse());
                    dto.setTelephone(fournisseur.getTelephone());
                    dto.setEmail(fournisseur.getEmail());
                    return dto;
                })
                .orElse(null);
    }
}