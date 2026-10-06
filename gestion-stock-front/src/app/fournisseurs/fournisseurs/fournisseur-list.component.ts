import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Fournisseur } from '../../interfaces/fournisseur.interface';
import { FournisseurService } from '../../services/fournisseur.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-fournisseur-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './fournisseur-list.component.html',
  styleUrls: ['./fournisseur-list.component.css']
})
export class FournisseurListComponent implements OnInit {
  public fournisseurs: Fournisseur[] = [];
  userRole: string | null = null;
  readOnly: boolean = false;

  constructor(
    private fournisseurService: FournisseurService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userRole = localStorage.getItem('userRole');
    this.readOnly = this.userRole !== 'ADMIN';

    this.fournisseurService.listerFournisseurs().subscribe(data => {
      this.fournisseurs = data;
    });
  }


  modifier(id: number) {
    this.router.navigate(['/fournisseurs/edit', id]);
  }

  supprimer(id: number) {
    if (confirm('Supprimer ce fournisseur ?')) {
      this.fournisseurService.supprimerFournisseur(id).subscribe(() => {
        this.fournisseurs = this.fournisseurs.filter(f => f.id !== id);
      });
    }
  }

  ajouter() {
    this.router.navigate(['/fournisseurs/add']);
  }
}
