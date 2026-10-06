import { environment } from '../../../../environments/environment';
import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BesoinArticle, BesoinDto } from '../../../interfaces/besoin';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-besoin-article-list',
  templateUrl: './besoin-article-list.component.html',
  imports: [
    DatePipe,
    CommonModule,
    FormsModule
  ],
  styleUrls: ['./besoin-article-list.component.css']
})
export class BesoinArticleListComponent implements OnInit {
  allBesoins: BesoinArticle[] = [];
  filteredBesoins: BesoinArticle[] = [];
  typeFiltre: string = 'ALL';
  referenceFiltre: string = '';
  fournisseurFiltre: string = '';
  dateFiltre: string = '';
  selectedBesoinIds: number[] = [];
  userRole: string | null = null;
  canCreate = false;
  canModify = false;

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit(): void {
    this.userRole = localStorage.getItem('userRole');
    this.canCreate = ['ADMIN', 'COMMERCIAL'].includes(this.userRole ?? '');
    this.canModify = this.userRole === 'ADMIN';
    this.loadBesoins();
  }

  loadBesoins(): void {
    this.http.get<BesoinDto[]>(`${environment.apiUrl}/api/besoins`).subscribe({
      next: (data) => {
        // Ajout de la propriété quantiteEnStock récupérée du backend
        this.allBesoins = data.flatMap(b => b.besoinArticles || []).map(ba => ({
          ...ba,
          quantiteEnStock: ba.quantiteEnStock ?? 0 // valeur par défaut si non défini
        }));
        this.filtrer();
      },
      error: (err) => console.error('Erreur de chargement des besoins :', err)
    });
  }

  filtrer(): void {
    this.filteredBesoins = this.allBesoins
      .filter(besoin => {
        const matchType = this.typeFiltre === 'ALL' || besoin.typeBesoin === this.typeFiltre;
        const matchReference = !this.referenceFiltre || besoin.articleReference?.toLowerCase().includes(this.referenceFiltre.toLowerCase());
        const matchFournisseur = !this.fournisseurFiltre || besoin.fournisseur?.toLowerCase().includes(this.fournisseurFiltre.toLowerCase());
        return matchType && matchReference && matchFournisseur;
      })
      .sort((a, b) => new Date(a.dateBesoin!).getTime() - new Date(b.dateBesoin!).getTime()); // tri par date
  }

  filtrerParDate(): void {
    if (!this.dateFiltre) {
      this.loadBesoins(); // reset
      return;
    }

    this.http.get<BesoinDto[]>(`${environment.apiUrl}/api/besoins/par-date?date=${this.dateFiltre}`).subscribe({
      next: (data) => {
        this.allBesoins = data.flatMap(b => b.besoinArticles || []);
        this.filtrer(); // réutilise le filtrage secondaire + tri
      },
      error: err => console.error('Erreur lors du filtrage par date :', err)
    });
  }

  onTypeChange(event: any): void {
    this.typeFiltre = event.target.value;
    this.filtrer();
  }

  goToForm(): void {
    this.router.navigate(['/besoins/new']);
  }

  deleteBesoin(besoin: BesoinArticle): void {
    if (confirm(`Supprimer le besoin de l'article ${besoin.articleReference} ?`)) {
      this.http.delete(`${environment.apiUrl}/api/besoins/${besoin.id}`).subscribe({
        next: () => {
          this.allBesoins = this.allBesoins.filter(b => b.id !== besoin.id);
          this.filtrer();
        },
        error: err => console.error('Erreur de suppression du besoin :', err)
      });
    }
  }

  getTotalMontant(): number {
    return this.filteredBesoins.reduce((sum, besoin) => {
      return sum + (besoin.montantTotal || 0);
    }, 0);
  }

  onCheckboxChange(besoin: BesoinArticle, event: any): void {
    if (event.target.checked) {
      if (!this.selectedBesoinIds.includes(besoin.id!)) {
        this.selectedBesoinIds.push(besoin.id!);
      }
    } else {
      this.selectedBesoinIds = this.selectedBesoinIds.filter(id => id !== besoin.id);
    }
  }

  creerCommandes(): void {
    if (this.selectedBesoinIds.length === 0) {
      alert('Veuillez sélectionner au moins un besoin.');
      return;
    }

    const payload = this.selectedBesoinIds.map(id => {
      const besoin = this.allBesoins.find(b => b.id === id)!;
      return {
        dateCommande: new Date().toISOString().split('T')[0],
        statusCommande: 'EN_COURS',
        fournisseurId: besoin.fournisseurId,
        bandesCommande: [
          {
            articleId: besoin.articleId,
            quantiteConditionnement: besoin.quantite,
            dateArrivage: besoin.dateBesoin
          }
        ]
      };
    });

    payload.forEach(commandePayload => {
      this.http.post(`${environment.apiUrl}/api/commandes`, commandePayload).subscribe({
        next: () => {
          alert(`Commande créée pour l'article ${commandePayload.bandesCommande[0].articleId}`);
        },
        error: err => {
          console.error('Erreur création commande', err);
          alert('Erreur lors de la création d\'une commande.');
        }
      });
    });

    this.selectedBesoinIds = [];
  }

  toggleSelectAll(event: any): void {
    if (event.target.checked) {
      this.selectedBesoinIds = this.filteredBesoins
        .filter(b => !b.commandeCreee && b.id != null)
        .map(b => b.id!);
    } else {
      this.selectedBesoinIds = [];
    }
  }

  editBesoin(id: number | undefined): void {
    this.router.navigate(['/dashboard/besoins/edit', id]);
  }
}
