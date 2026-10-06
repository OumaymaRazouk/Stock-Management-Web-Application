// src/app/dashboard/alertes/alertes.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ArticleService } from '../../services/article.service';
import { Article } from '../../interfaces/article';

@Component({
  selector: 'app-alertes',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './alertes.component.html',
  styleUrls: ['./alertes.component.css']
})
export class AlertesComponent implements OnInit {
  articlesCritiques: Article[] = [];
  loading = false;
  error: string | null = null;

  constructor(private articleService: ArticleService) {}

  ngOnInit(): void {
    this.loadArticlesCritiques();
  }

  loadArticlesCritiques(): void {
    this.loading = true;
    this.articleService.getArticles().subscribe({
      next: (articles) => {
        this.articlesCritiques = articles.filter(
          a => a.statutStock === 'FAIBLE' || a.statutStock === 'RUPTURE'
        );
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des alertes';
        this.loading = false;
        console.error(err);
      }
    });
  }

  getRuptureCount(): number {
    return this.articlesCritiques.filter(a => a.statutStock === 'RUPTURE').length;
  }

  getFaibleCount(): number {
    return this.articlesCritiques.filter(a => a.statutStock === 'FAIBLE').length;
  }
  getArticlesRupture(): Article[] {
    return this.articlesCritiques.filter(a => a.statutStock === 'RUPTURE');
  }

  getArticlesFaible(): Article[] {
    return this.articlesCritiques.filter(a => a.statutStock === 'FAIBLE');
  }
}
