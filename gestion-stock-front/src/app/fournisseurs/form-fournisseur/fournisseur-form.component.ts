import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Fournisseur } from '../../interfaces/fournisseur.interface';
import { FournisseurService } from '../../services/fournisseur.service';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-fournisseur-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './fournisseur-form.component.html',
  styleUrls: ['./fournisseur-form.component.css']
})
export class FormFournisseurComponent implements OnInit {
  public fournisseurForm!: FormGroup;
  public isLoading = false;
  private id?: number;
  userRole: string | null = null;
  readOnly: boolean = false;
  loading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private fournisseurService: FournisseurService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.fournisseurForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      adresse: ['', [Validators.required, Validators.minLength(5)]],
      telephone: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
      email: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnInit(): void {
    this.userRole = localStorage.getItem('userRole');
    this.readOnly = this.userRole !== 'ADMIN';
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.id = +idParam;
      this.fournisseurService.getFournisseurById(this.id!).subscribe({
        next: (fournisseur) => {
          this.fournisseurForm.patchValue(fournisseur);
        },
        error: (error) => {
          console.error('Erreur lors du chargement du fournisseur :', error);
          alert('Erreur lors du chargement du fournisseur.');
        }
      });
    }
  }

  onSubmitForm(): void {
    if (this.readOnly) {
      alert("Vous n'avez pas le droit de modifier ou ajouter un fournisseur.");
      return;
    }
    if (this.fournisseurForm.invalid) {
      this.fournisseurForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const fournisseurData = this.fournisseurForm.value;

    const obs = this.id
      ? this.fournisseurService.modifierFournisseur(this.id!, fournisseurData)
      : this.fournisseurService.ajouterFournisseur(fournisseurData);

    obs.subscribe({
      next: () => {
        alert('Fournisseur enregistré avec succès !');
        this.fournisseurForm.reset();
        this.isLoading = false;
        this.router.navigate(['/fournisseurs']);
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Erreur:', error);
        alert('Erreur lors de l\'enregistrement du fournisseur : ' + error.message);
      }
    });
  }

  annuler(): void {
    this.router.navigate(['/fournisseurs']);
  }



  public get isEditMode(): boolean {
    return this.id !== undefined;
  }

  public get formControls() {
    return this.fournisseurForm.controls;
  }
}
