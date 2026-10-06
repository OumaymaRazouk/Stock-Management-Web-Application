import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { of } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import { ArticleService } from '../services/article.service';
import { BesoinService } from '../services/besoin.service';
import { CommandeService } from '../services/commande.service';
import { FournisseurService } from '../services/fournisseur.service';
import { BandeCommandeService } from '../services/bande-commande.service';
import { AccountService } from '../services/account.service';

describe('Dashboard permissions', () => {
  let accounts: jasmine.Spy;
  let purchases: jasmine.Spy;
  let preparation: jasmine.Spy;
  let bandes: jasmine.Spy;
  beforeEach(async () => {
    localStorage.clear();
    accounts = jasmine.createSpy('accounts').and.returnValue(of([]));
    purchases = jasmine.createSpy('purchases').and.returnValue(of([]));
    preparation = jasmine.createSpy('preparation').and.returnValue(of([]));
    bandes = jasmine.createSpy('bandes').and.returnValue(of([]));
    await TestBed.configureTestingModule({
      imports: [DashboardComponent], providers: [
        provideRouter([]), provideHttpClient(),
        { provide: ArticleService, useValue: { getArticles: () => of([]) } },
        { provide: BesoinService, useValue: { getAllBesoins: () => of([]), getPreparationCommande: preparation } },
        { provide: CommandeService, useValue: { getAll: purchases } },
        { provide: FournisseurService, useValue: { getFournisseurs: () => of([]) } },
        { provide: BandeCommandeService, useValue: { getAllBandes: bandes } },
        { provide: AccountService, useValue: { list: accounts } }
      ]
    }).compileComponents();
  });
  afterEach(() => localStorage.clear());
  for (const role of ['ADMIN', 'ACHETEUR', 'COMMERCIAL']) {
    it(`only requests APIs permitted for ${role}`, () => {
      localStorage.setItem('userRole', role);
      const fixture = TestBed.createComponent(DashboardComponent);
      fixture.detectChanges();
      const routes = fixture.componentInstance.cards.map(c => c.route);
      expect(accounts.calls.count()).toBe(role === 'ADMIN' ? 1 : 0);
      expect(routes.includes('/comptes')).toBe(role === 'ADMIN');
      const canPurchase = role !== 'COMMERCIAL';
      expect(purchases.calls.count()).toBe(canPurchase ? 1 : 0);
      expect(preparation.calls.count()).toBe(canPurchase ? 1 : 0);
      expect(bandes.calls.count()).toBe(canPurchase ? 1 : 0);
      expect(routes.includes('/commandes')).toBe(canPurchase);
      expect(fixture.componentInstance.loading).toBeFalse();
    });
  }
});
