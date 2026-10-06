package org.springdemo.gestionstock.repository;

import org.springdemo.gestionstock.entity.Besoin;
import org.springdemo.gestionstock.entity.TypeBesoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BesoinRepository extends JpaRepository<Besoin, Long> {
    List<Besoin> findByTypeBesoin(TypeBesoin typeBesoin);
    List<Besoin> findAllByOrderByDateBesoinAsc();
}
