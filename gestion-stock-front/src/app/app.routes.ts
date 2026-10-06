import { Routes } from '@angular/router';

import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },

  { path: 'auth/login', loadComponent: () => import('./auth/login/login.component').then(m => m.LoginComponent) },
  { path: 'auth/register', redirectTo: 'auth/login', pathMatch: 'full' },

  {
    path: '',
    loadComponent: () => import('./layout/layout.component').then(m => m.LayoutComponent),
    canActivate: [AuthGuard],
    canActivateChild: [AuthGuard],
    children: [
      { path: 'comptes', data: { roles: ['ADMIN'] }, loadComponent: () => import('./admin/accounts.component').then(m => m.AccountsComponent) },
      { path: 'dashboard', loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'alertes', loadComponent: () => import('./dashboard/alertes/alertes.component').then(m => m.AlertesComponent) },
      { path: 'articles', loadComponent: () => import('./dashboard/article-list/article-list.component').then(m => m.ArticleListComponent) },
      { path: 'articles/new', data: { roles: ['ADMIN'] }, loadComponent: () => import('./dashboard/article-form/article-form.component').then(m => m.ArticleFormComponent) },
      { path: 'articles/:id/edit', data: { roles: ['ADMIN'] }, loadComponent: () => import('./dashboard/article-form/article-form.component').then(m => m.ArticleFormComponent) },

      {
        path: 'fournisseurs',
        children: [
          { path: '', loadComponent: () => import('./fournisseurs/fournisseurs/fournisseur-list.component').then(m => m.FournisseurListComponent) },
          { path: 'add', data: { roles: ['ADMIN'] }, loadComponent: () => import('./fournisseurs/form-fournisseur/fournisseur-form.component').then(m => m.FormFournisseurComponent) },
          { path: 'edit/:id', data: { roles: ['ADMIN'] }, loadComponent: () => import('./fournisseurs/form-fournisseur/fournisseur-form.component').then(m => m.FormFournisseurComponent) }
        ]
      },

      { path: 'besoins', loadComponent: () => import('./dashboard/besoins/besoin-article-list/besoin-article-list.component').then(m => m.BesoinArticleListComponent) },
      { path: 'besoins/new', data: { roles: ['ADMIN', 'COMMERCIAL'] }, loadComponent: () => import('./dashboard/besoins/besoin-form/besoin-form.component').then(m => m.BesoinFormComponent) },
      { path: 'article-besoins/:id', loadComponent: () => import('./dashboard/besoins/article-besoin/article-besoins.component').then(m => m.ArticleBesoinsComponent) },
      {
        path: 'dashboard/besoins/edit/:id', data: { roles: ['ADMIN'] },
        loadComponent: () => import('./dashboard/besoins/besoin-form/besoin-form.component').then(m => m.BesoinFormComponent)
      },

      {
        path: 'acheteurs', data: { roles: ['ADMIN'] },
        children: [
          { path: '', loadComponent: () => import('./dashboard/acheteur-list/acheteur-list.component').then(m => m.AcheteurListComponent) },
          { path: 'add', loadComponent: () => import('./dashboard/acheteur-form/acheteur-form.component').then(m => m.AcheteurFormComponent) },
          { path: 'edit/:id', loadComponent: () => import('./dashboard/acheteur-form/acheteur-form.component').then(m => m.AcheteurFormComponent) }
        ]
      },

      { path: 'preparation-commande', data: { roles: ['ADMIN', 'ACHETEUR'] }, loadComponent: () => import('./dashboard/preparation-commande/preparation-commande.component').then(m => m.PreparationCommandeComponent) },

      // ✅ Nouvelle route
      { path: 'commandes', data: { roles: ['ADMIN', 'ACHETEUR'] }, loadComponent: () => import('./dashboard/commande-list/commande-list.component').then(m => m.CommandeListComponent) },
      { path: 'bandes-commande', data: { roles: ['ADMIN', 'ACHETEUR'] }, loadComponent: () => import('./dashboard/bande-commande/bande-commande.component').then(m => m.BandeCommandeComponent) },
      { path: 'bandes-commande/:id', data: { roles: ['ADMIN', 'ACHETEUR'] }, loadComponent: () => import('./dashboard/bande-commande-detail/bande-commande-detail.component').then(m => m.BandeCommandeDetailComponent) }

    ]
  },

];
