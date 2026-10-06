import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { ActivatedRoute } from '@angular/router';
import {BesoinService} from '../../../services/besoin.service';
import {Besoin} from '../../../interfaces/besoin';

@Component({
  selector: 'app-article-besoins',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './article-besoins.component.html',
  styleUrls: ['./article-besoins.component.css']
})
export class ArticleBesoinsComponent implements OnInit {
  articleId!: number;
  besoins: Besoin[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private besoinService: BesoinService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.articleId = +params['id'];
      this.loadBesoins();
    });
  }

  loadBesoins(): void {
    this.loading = true;
    this.error = null;
    this.besoinService.getBesoinsByArticle(this.articleId)
      .subscribe({
        next: (besoins) => {
          this.besoins = besoins;
        },
        error: (err) => {
          this.error = 'Erreur lors du chargement des besoins';
          console.error(err);
        },
        complete: () => {
          this.loading = false;
        }
      });
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  deleteBesoin(besoinId: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce besoin ?')) {
      this.besoinService.deleteBesoin(besoinId)
        .subscribe({
          next: () => {
            this.loadBesoins(); // Recharger la liste après la suppression
          },
          error: (err) => {
            alert('Erreur lors de la suppression du besoin');
            console.error(err);
          }
        });
    }
  }
}
