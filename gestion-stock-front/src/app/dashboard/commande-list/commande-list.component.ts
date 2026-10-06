import { Component, OnInit } from '@angular/core';
import { CommandeService } from '../../services/commande.service';
import { Commande } from '../../interfaces/commande';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';

@Component({
  selector: 'app-commande-list',
  templateUrl: './commande-list.component.html',
  imports: [
    CommonModule,
    DatePipe,
    CurrencyPipe
  ],
  styleUrls: ['./commande-list.component.css']
})
export class CommandeListComponent implements OnInit {
  commandes: Commande[] = [];

  constructor(private commandeService: CommandeService) {}

  ngOnInit(): void {
    this.commandeService.getAll().subscribe({
      next: (data: Commande[]) => this.commandes = data,
      error: err => console.error('Erreur chargement commandes :', err)
    });
  }

  // Méthode pour obtenir la date d'arrivage la plus tardive
  getDateArrivageMax(commande: Commande): string | null {
    if (!commande.bandesCommande || commande.bandesCommande.length === 0) {
      return null;
    }

    const dates = commande.bandesCommande
      .map(bande => bande.dateArrivage)
      .filter(date => date != null)
      .map(date => new Date(date!));

    if (dates.length === 0) return null;

    const maxDate = new Date(Math.max(...dates.map(d => d.getTime())));
    return maxDate.toISOString().split('T')[0];
  }

  // Méthode pour obtenir la classe CSS du statut
  getStatusClass(status: string): string {
    switch (status) {
      case 'EN_ATTENTE': return 'badge bg-warning';
      case 'EN_COURS': return 'badge bg-info';
      case 'LIVREE': return 'badge bg-success';
      default: return 'badge bg-secondary';
    }
  }

  // Méthode pour obtenir le label du statut
  getStatusLabel(status: string): string {
    switch (status) {
      case 'EN_ATTENTE': return 'En attente';
      case 'EN_COURS': return 'En cours';
      case 'LIVREE': return 'Livrée';
      default: return status;
    }
  }
}
