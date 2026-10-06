package org.springdemo.gestionstock.repository;

import org.springdemo.gestionstock.entity.BesoinArticle;
import org.springdemo.gestionstock.entity.TypeBesoin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BesoinArticleRepository extends JpaRepository<BesoinArticle, Long> {
    List<BesoinArticle> findByArticleId(Long articleId);
    List<BesoinArticle> findByTypeBesoin(TypeBesoin typeBesoin);
    List<BesoinArticle> findByCommandeCreeeFalseOrderByDateBesoinAsc();
    Optional<BesoinArticle> findFirstByArticleIdAndCommandeCreeeFalseOrderByDateBesoinAsc(Long articleId);
}
