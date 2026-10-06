import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Observable, catchError, finalize, forkJoin, of, tap } from 'rxjs';
import { ArticleService } from '../services/article.service';
import { BesoinService } from '../services/besoin.service';
import { CommandeService } from '../services/commande.service';
import { FournisseurService } from '../services/fournisseur.service';
import { BandeCommandeService } from '../services/bande-commande.service';
import { AccountService } from '../services/account.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-dashboard', standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  cards = [
    { title: 'Articles', count: 0, color: 'primary', icon: 'box', route: '/articles' },
    { title: 'Besoins exprimés', count: 0, color: 'success', icon: 'clipboard-check', route: '/besoins' },
    { title: 'Bandes de commande', count: 0, color: 'warning', icon: 'calendar', route: '/bandes-commande' },
    { title: 'Alertes stock', count: 0, color: 'danger', icon: 'exclamation-triangle', route: '/alertes' },
    { title: 'Fournisseurs', count: 0, color: 'info', icon: 'truck', route: '/fournisseurs' },
    { title: 'Commandes d’achat', count: 0, color: 'secondary', icon: 'receipt', route: '/commandes' },
    { title: 'Préparation des commandes', count: 0, color: 'dark', icon: 'clipboard-data', route: '/preparation-commande' },
    { title: 'Gestion des comptes', count: 0, color: 'primary', icon: 'people', route: '/comptes' }
  ];
  loading = false;
  error = '';
  constructor(private articles: ArticleService, private besoins: BesoinService,
              private commandes: CommandeService, private fournisseurs: FournisseurService,
              private bandes: BandeCommandeService, private accounts: AccountService,
              public auth: AuthService) {}

  ngOnInit() {
    this.cards = this.cards.filter(card => {
      if (card.route === '/comptes') return this.auth.isAdmin();
      if (['/commandes', '/bandes-commande', '/preparation-commande'].includes(card.route)) return this.auth.canManagePurchases();
      return true;
    });
    this.loadDashboardData();
  }
  loadDashboardData() {
    this.loading = true; this.error = '';
    const requests: Observable<unknown>[] = [
      this.count(this.articles.getArticles().pipe(tap(data =>
        this.setCount('/alertes', data.filter(a => a.statutStock === 'FAIBLE' || a.statutStock === 'RUPTURE').length)
      )), '/articles'),
      this.count(this.fournisseurs.getFournisseurs(), '/fournisseurs'),
      this.count(this.besoins.getAllBesoins(), '/besoins')
    ];
    if (this.auth.canManagePurchases()) requests.push(
      this.count(this.commandes.getAll(), '/commandes'),
      this.count(this.bandes.getAllBandes(), '/bandes-commande'),
      this.count(this.besoins.getPreparationCommande(), '/preparation-commande')
    );
    if (this.auth.isAdmin()) requests.push(this.count(this.accounts.list(), '/comptes'));
    forkJoin(requests).pipe(finalize(() => this.loading = false)).subscribe();
  }
  private count<T>(request: Observable<T[]>, route: string): Observable<T[]> {
    return request.pipe(tap(data => this.setCount(route, data.length)), catchError(() => {
      this.error = 'Certaines données du tableau de bord ne sont pas disponibles.';
      return of([]);
    }));
  }
  private setCount(route: string, count: number) {
    const card = this.cards.find(card => card.route === route);
    if (card) card.count = count;
  }
}
