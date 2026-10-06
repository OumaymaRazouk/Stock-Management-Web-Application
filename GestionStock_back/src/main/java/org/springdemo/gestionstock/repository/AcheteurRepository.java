package org.springdemo.gestionstock.repository;

import org.springdemo.gestionstock.entity.Acheteur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcheteurRepository extends JpaRepository<Acheteur, Long> {
    boolean existsByEmail(String email);
    Acheteur findByEmail(String email);
}
