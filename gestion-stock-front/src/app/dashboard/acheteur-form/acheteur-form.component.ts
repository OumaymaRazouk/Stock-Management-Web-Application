import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AcheteurService } from '../../services/acheteur.service';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-acheteur-form',
  templateUrl: './acheteur-form.component.html',
  imports: [
    ReactiveFormsModule, CommonModule, RouterModule
  ],
  styleUrls: ['./acheteur-form.component.css']
})
export class AcheteurFormComponent implements OnInit {
  acheteurForm!: FormGroup;
  isEdit = false;
  acheteurId?: number;
  loading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private acheteurService: AcheteurService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Récupère l'id depuis l'URL (converti en number)
    const idParam = this.route.snapshot.params['id'];
    this.acheteurId = idParam ? +idParam : undefined;
    this.isEdit = !!this.acheteurId;

    this.acheteurForm = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      motDePasse: ['', this.isEdit ? [] : [Validators.required, Validators.minLength(6)]]
    });

    if (this.isEdit && this.acheteurId !== undefined) {
      this.acheteurService.getAcheteurById(this.acheteurId).subscribe(acheteur => {
        this.acheteurForm.patchValue({
          nom: acheteur.nom,
          prenom: acheteur.prenom,
          email: acheteur.email
          // On ne pré-remplit pas le mot de passe
        });
      });
    }
  }

  onSubmit() {
    if (this.acheteurForm.valid) {
      if (this.isEdit && this.acheteurId !== undefined) {
        // Modification
        this.acheteurService.updateAcheteur(this.acheteurId, this.acheteurForm.value).subscribe({
          next: (res) => {
            alert('Modification effectuée avec succès');
            this.router.navigate(['/acheteurs']);
          },
          error: (err) => {
            alert('Erreur lors de la modification : ' + (err.error?.message || err.message));
          }
        });
      } else {
        // Ajout
        this.acheteurService.addAcheteur(this.acheteurForm.value).subscribe({
          next: (res) => {
            alert('Ajout effectué avec succès');
            this.router.navigate(['/acheteurs']);
          },
          error: (err) => {
            alert('Erreur lors de l\'ajout : ' + (err.error?.message || err.message));
          }
        });
      }
    }
  }

  onCancel() {
    this.router.navigate(['/acheteurs']);
  }
}
