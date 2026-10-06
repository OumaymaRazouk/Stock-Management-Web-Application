// gestion-stock-front/src/app/auth/login/login.component.ts

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {Router, RouterModule} from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import {AuthService} from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterModule
  ],
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  showPassword = false;
  isAcheteur = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      motDePasse: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  get formControls() {
    return this.loginForm.controls;
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      const loginData = {
        email: this.loginForm.value.email,
        motDePasse: this.loginForm.value.motDePasse
      };

      this.authService.login(loginData).subscribe({
        next: (response) => {
          this.isLoading = false;

          if (response.success) {
            // Le service AuthService stocke déjà le token et 'connected'
            localStorage.setItem('userEmail', loginData.email);

            //role
            localStorage.setItem('userRole', response.role ?? '');
            this.isAcheteur = response.role === 'ACHETEUR';

            // Redirection vers le dashboard
            this.router.navigate(['/dashboard']);
          } else {
            this.errorMessage = response.message || 'Erreur de connexion';
          }
        },
        error: (error) => {
          this.isLoading = false;
          console.error('Erreur de connexion:', error);

          if (error.error && error.error.message) {
            this.errorMessage = error.error.message;
          } else if (error.status === 401) {
            this.errorMessage = 'Email ou mot de passe incorrect';
          } else if (error.status === 0) {
            this.errorMessage = 'Impossible de se connecter au serveur';
          } else {
            this.errorMessage = 'Une erreur est survenue lors de la connexion';
          }
        }
      });
    } else {
      // Marquer tous les champs comme touchés pour afficher les erreurs
      Object.keys(this.loginForm.controls).forEach(key => {
        const control = this.loginForm.get(key);
        if (control?.invalid) {
          control.markAsTouched();
        }
      });
    }
  }

  // Méthode pour effacer le message d'erreur
  clearError(): void {
    this.errorMessage = '';
  }

  // Méthode pour vérifier si un champ est invalide
  isFieldInvalid(fieldName: string): boolean {
    const field = this.loginForm.get(fieldName);
    return field ? field.invalid : false;
  }

  // Méthode pour obtenir le message d'erreur d'un champ
  getFieldErrorMessage(fieldName: string): string {
    const field = this.loginForm.get(fieldName);
    if (!field || !field.errors) return '';

    if (field.errors['required']) {
      return `${this.getFieldLabel(fieldName)} est requis`;
    }

    if (field.errors['email']) {
      return 'Format email invalide';
    }

    if (field.errors['minlength']) {
      return `${this.getFieldLabel(fieldName)} doit contenir au moins ${field.errors['minlength'].requiredLength} caractères`;
    }

    return '';
  }

  private getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      email: 'L\'email',
      motDePasse: 'Le mot de passe'
    };
    return labels[fieldName] || fieldName;
  }
}
