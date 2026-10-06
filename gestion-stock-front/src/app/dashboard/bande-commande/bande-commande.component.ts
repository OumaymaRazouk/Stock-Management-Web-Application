import { Component, OnInit } from '@angular/core';
import { BandeCommandeService, BandeCommande } from '../../services/bande-commande.service';
import {CommonModule, CurrencyPipe, DatePipe} from '@angular/common';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-bande-commande',
  templateUrl: './bande-commande.component.html',
  imports: [
    DatePipe,
    CurrencyPipe,
    CommonModule,
    RouterLink
  ],
  styleUrls: ['./bande-commande.component.css']
})
export class BandeCommandeComponent implements OnInit {
  bandes: BandeCommande[] = [];

  constructor(private bandeCommandeService: BandeCommandeService) {}

  ngOnInit(): void {
    this.bandeCommandeService.getAllBandes().subscribe({
      next: (data) => this.bandes = data,
      error: (err) => console.error('Erreur chargement bandes:', err)
    });
  }

  getNbPaquets(quantite: number, quantiteConditionnement: number): number {
    if (!quantite || !quantiteConditionnement) return 0;
    return quantite / quantiteConditionnement;
  }
}
