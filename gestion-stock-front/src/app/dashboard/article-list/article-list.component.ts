import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';  // Ajout de RouterModule
import { ArticleService } from '../../services/article.service';
import { Article } from '../../interfaces/article';
import { Router } from '@angular/router';
import {ToastrModule, ToastrService} from 'ngx-toastr';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

@Component({
  selector: 'app-article-list',
  standalone: true,
  imports: [CommonModule, RouterModule], // Ajout de RouterModule dans imports
  templateUrl: './article-list.component.html',
  styleUrls: ['./article-list.component.css']
})
export class ArticleListComponent implements OnInit {
  articles: Article[] = [];
  loading = false;
  error: string | null = null;
  readOnly = false;

  constructor(
    private articleService: ArticleService,
    private router: Router ,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadArticles();
    this.readOnly = localStorage.getItem('userRole') !== 'ADMIN';
  }

  loadArticles(): void {
    this.loading = true;
    this.error = null;
    this.articleService.getArticles()
      .subscribe({
        next: (articles) => {
          this.articles = articles;
          console.log('Articles chargés:', articles); // Ajoute cette ligne
        },
        error: (err) => {
          this.error = 'Erreur lors du chargement des articles';
          console.error(err);
        },
        complete: () => {
          this.loading = false;
        }
      });
  }

  deleteArticle(article: Article): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet article ?')) {
      this.articleService.deleteArticle(article.id!)
        .subscribe({
          next: () => {
            this.loadArticles();
          },
          error: (err) => {
            alert('Erreur lors de la suppression de l\'article');
            console.error(err);
          }
        });
    }
  }

  editArticle(article: Article): void {
    this.router.navigate(['/articles', article.id, 'edit']);
  }
  onCancel(): void {
    this.router.navigate(['/dashboard/articles']);
  }



}
