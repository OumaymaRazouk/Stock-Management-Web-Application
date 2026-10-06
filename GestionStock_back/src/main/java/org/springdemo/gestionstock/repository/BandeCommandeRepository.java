package org.springdemo.gestionstock.repository;

import org.springdemo.gestionstock.entity.BandeCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BandeCommandeRepository extends JpaRepository<BandeCommande, Long> {
}
