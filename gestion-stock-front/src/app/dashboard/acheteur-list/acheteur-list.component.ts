import { Component, OnInit } from '@angular/core';
import { AcheteurService } from '../../services/acheteur.service';
import { Acheteur } from '../../interfaces/acheteur';
import {Router, RouterModule} from '@angular/router';
import {ReactiveFormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-acheteur-list',
  templateUrl: './acheteur-list.component.html',
  imports: [
    ReactiveFormsModule , CommonModule , RouterModule
  ],
  styleUrls: ['./acheteur-list.component.css']
})
export class AcheteurListComponent implements OnInit {
  acheteurs: Acheteur[] = [];

  constructor(private acheteurService: AcheteurService, private router: Router) {}

  ngOnInit(): void {
    this.loadAcheteurs();
  }

  loadAcheteurs() {
    this.acheteurService.getAcheteurs().subscribe(data => this.acheteurs = data);
  }

  editAcheteur(id: number | undefined) {
    if (id !== undefined) {
      this.router.navigate(['/acheteurs/edit', id]);
    }
  }

  deleteAcheteur(id: number | undefined) {
    if (id !== undefined && confirm('Supprimer cet acheteur ?')) {
      this.acheteurService.deleteAcheteur(id).subscribe({
        next: () => {
          alert('Suppression effectuée avec succès');
          this.loadAcheteurs();
        },
        error: (err) => {
          alert('Erreur lors de la suppression : ' + (err.error?.message || err.message));
        }
      });
    }
  }

  addAcheteur() {
    this.router.navigate(['/acheteurs/add']);
  }
}
