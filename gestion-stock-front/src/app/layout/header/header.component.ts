import {Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';

import {ArticleService} from '../../services/article.service';
import { Article } from '../../interfaces/article'
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  articlesCritiques: Article[] = [];
  isDropdownOpen = false;

  constructor(
    private articleService: ArticleService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadArticlesCritiques();
  }

  loadArticlesCritiques(): void {
    this.articleService.getArticles().subscribe({
      next: (articles) => {
        this.articlesCritiques = articles.filter(
          a => a.statutStock === 'FAIBLE' || a.statutStock === 'RUPTURE'
        );
      }
    });
  }

  toggleDropdown(event: Event): void {
    event.stopPropagation();
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        console.log('Déconnexion réussie');
        this.router.navigate(['/auth/login']);
      },
      error: (err) => {
        console.error('Erreur lors de la déconnexion', err);
        // Même en cas d'erreur, on redirige vers la page de login
        // car le token a déjà été supprimé localement.
        this.router.navigate(['/auth/login']);
      }
    });
  }
}
