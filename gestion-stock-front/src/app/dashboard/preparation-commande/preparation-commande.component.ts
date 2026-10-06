import { environment } from '../../../environments/environment';
// ... existing code ...
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CommandeService } from '../../services/commande.service'; // Ajout de l'import

@Component({
  selector: 'app-preparation-commande',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './preparation-commande.component.html',
  styleUrls: ['./preparation-commande.component.css']
})
export class PreparationCommandeComponent implements OnInit {
  tableauCommande: any[] = [];
  tableauCommandeFiltre: any[] = [];
  toutSelectionne = false;
  filtreFournisseur: string = '';

  constructor(
    private commandeService: CommandeService, // Injection du service
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.http.get<any[]>(`${environment.apiUrl}/api/besoins/commande/preparation`)
      .subscribe(data => {
        this.tableauCommande = data.map(d => ({ ...d, selectionne: false }));
        this.appliquerFiltre();
      });
  }
  totalMontant: number = 0;
  appliquerFiltre(): void {
    const filtre = this.filtreFournisseur.trim().toLowerCase();
    if (!filtre) {
      this.tableauCommandeFiltre = this.tableauCommande;
    } else {
      this.tableauCommandeFiltre = this.tableauCommande.filter(item =>
        item.fournisseurNom?.toLowerCase().includes(filtre)
      );
    }
    // Calcule la somme des montants totaux pour les lignes filtrées
    this.totalMontant = this.tableauCommandeFiltre
      .reduce((sum, item) => sum + (item.montantTotal || 0), 0);
  }

  toggleToutSelection() {
    this.tableauCommandeFiltre.forEach(item => item.selectionne = this.toutSelectionne);
  }

  creerCommandes(): void {
    const commandesSelectionnees = this.tableauCommandeFiltre.filter(item => item.selectionne);

    if (commandesSelectionnees.length === 0) {
      alert('Veuillez sélectionner au moins une commande.');
      return;
    }

    const groupedByFournisseur: { [fournisseurId: number]: any[] } = {};

    commandesSelectionnees.forEach(item => {
      if (!groupedByFournisseur[item.fournisseurId]) {
        groupedByFournisseur[item.fournisseurId] = [];
      }
      groupedByFournisseur[item.fournisseurId].push({
        articleId: item.articleId,
        quantiteACommander: item.quantiteACommander, // <-- le bon champ
        dateArrivage: item.dateDernierBesoin,
        besoinArticleId: item.besoinArticleId
      });
    });

    Object.entries(groupedByFournisseur).forEach(([fournisseurId, bandesCommande]) => {
      console.log("Appel avec fournisseurId:", fournisseurId);
      console.log("Payload envoyé:", bandesCommande);

      this.http.post(
        `${environment.apiUrl}/api/commandes/passer?fournisseurId=${fournisseurId}`,
        bandesCommande,
        { headers: { 'Content-Type': 'application/json' } }
      ).subscribe({
        next: () => alert(`Commande créée pour le fournisseur ${fournisseurId}`),
        error: (err) => {
          console.error('Erreur création commande', err);
          alert(`Erreur lors de la création d'une commande pour fournisseur ${fournisseurId}.`);
        }
      });
    });

    this.toutSelectionne = false;
    this.tableauCommandeFiltre.forEach(item => item.selectionne = false);
  }

}
