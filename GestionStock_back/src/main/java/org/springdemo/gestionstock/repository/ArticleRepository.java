package org.springdemo.gestionstock.repository;


import org.springdemo.gestionstock.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    boolean existsByReference(String reference);

}