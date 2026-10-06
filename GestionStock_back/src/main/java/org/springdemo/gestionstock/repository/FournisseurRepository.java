package org.springdemo.gestionstock.repository;

import org.springdemo.gestionstock.entity.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {
    boolean existsByEmail(String email);
    Fournisseur findByEmail(String email);
}