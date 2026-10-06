import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { Account, AccountService } from '../services/account.service';

@Component({
  selector: 'app-accounts',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './accounts.component.html'
})
export class AccountsComponent implements OnInit {
  private fb = inject(FormBuilder);
  private api = inject(AccountService);
  accounts: Account[] = [];
  selected: Account | null = null;
  loading = false;
  saving = false;
  error = '';
  message = '';
  roles = ['ADMIN', 'ACHETEUR', 'COMMERCIAL'];
  form = this.fb.nonNullable.group({
    nom: ['', Validators.required],
    prenom: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    role: ['ACHETEUR', Validators.required],
    motDePasse: ['', [Validators.required, Validators.minLength(6)]]
  });

  ngOnInit() { this.load(); }
  load() {
    this.loading = true;
    this.api.list().pipe(finalize(() => this.loading = false)).subscribe({
      next: accounts => this.accounts = accounts,
      error: error => this.showError(error)
    });
  }
  edit(account: Account) {
    this.selected = account;
    this.error = ''; this.message = '';
    this.form.controls.motDePasse.setValidators([Validators.minLength(6)]);
    this.form.reset({ nom: account.nom, prenom: account.prenom, email: account.email,
      role: this.roles.includes(account.role) ? account.role : '', motDePasse: '' });
  }
  reset() {
    this.selected = null;
    this.form.controls.motDePasse.setValidators([Validators.required, Validators.minLength(6)]);
    this.form.reset({ nom: '', prenom: '', email: '', role: 'ACHETEUR', motDePasse: '' });
  }
  save() {
    this.error = ''; this.message = '';
    if (this.saving || this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    const input = this.form.getRawValue();
    const request = this.selected ? this.api.update(this.selected, input) : this.api.create(input);
    request.pipe(finalize(() => this.saving = false)).subscribe({
      next: () => { this.reset(); this.message = 'Compte enregistré.'; this.load(); },
      error: error => this.showError(error)
    });
  }
  remove(account: Account) {
    if (this.saving || !confirm(`Supprimer définitivement le compte de ${account.prenom} ${account.nom} (${account.email}) ?`)) return;
    this.saving = true; this.error = ''; this.message = '';
    this.api.delete(account).pipe(finalize(() => this.saving = false)).subscribe({
      next: () => { this.reset(); this.message = 'Compte supprimé.'; this.load(); },
      error: error => this.showError(error)
    });
  }
  private showError(error: any) {
    this.error = error.error?.message || (error.status === 409
      ? 'Opération refusée : email déjà utilisé ou protection du compte administrateur.'
      : 'Impossible d’enregistrer cette opération. Vérifiez les données et vos droits.');
  }
}
