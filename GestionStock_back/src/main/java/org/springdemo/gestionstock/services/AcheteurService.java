package org.springdemo.gestionstock.services;

import org.springdemo.gestionstock.entity.Acheteur;
import org.springdemo.gestionstock.repository.AcheteurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AcheteurService {
    @Autowired
    private AcheteurRepository acheteurRepository;

    public Acheteur save(Acheteur acheteur) {
        return acheteurRepository.save(acheteur);
    }

    public List<Acheteur> findAll() {
        return acheteurRepository.findAll();
    }

    public boolean existsByEmail(String email) {
        return acheteurRepository.existsByEmail(email);
    }

    public Acheteur findByEmail(String email) {
        return acheteurRepository.findByEmail(email);
    }
    public Acheteur findById(Long id) {
        return acheteurRepository.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        acheteurRepository.deleteById(id);
    }


}
