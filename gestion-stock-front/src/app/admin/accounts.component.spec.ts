import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AccountsComponent } from './accounts.component';
import { Account, AccountService } from '../services/account.service';

describe('Admin accounts', () => {
  let api: jasmine.SpyObj<AccountService>;
  const legacy: Account = { id: 7, source: 'UTILISATEUR', nom: 'Ancien', prenom: 'Compte', email: 'legacy@example.invalid', role: 'USER' };
  beforeEach(async () => {
    api = jasmine.createSpyObj('AccountService', ['list', 'create', 'update', 'delete']);
    api.list.and.returnValue(of([legacy]));
    api.update.and.returnValue(of({ ...legacy, role: 'COMMERCIAL' }));
    await TestBed.configureTestingModule({ imports: [AccountsComponent],
      providers: [{ provide: AccountService, useValue: api }]
    }).compileComponents();
  });
  it('requires an explicit role for legacy users and never prefills a password', () => {
    const fixture = TestBed.createComponent(AccountsComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.edit(legacy);
    expect(component.form.controls.role.value).toBe('');
    expect(component.form.invalid).toBeTrue();
    expect(component.form.controls.motDePasse.value).toBe('');
    component.form.controls.role.setValue('COMMERCIAL');
    component.save();
    expect(api.update).toHaveBeenCalledWith(legacy, jasmine.objectContaining({ role: 'COMMERCIAL', motDePasse: '' }));
    expect(component.form.controls.motDePasse.value).toBe('');
  });
  it('requires a password for new accounts and offers only the three supported roles', () => {
    const component = TestBed.createComponent(AccountsComponent).componentInstance;
    expect(component.roles).toEqual(['ADMIN', 'ACHETEUR', 'COMMERCIAL']);
    component.form.patchValue({ nom: 'Test', prenom: 'Test', email: 'test@example.invalid' });
    component.save();
    expect(api.create).not.toHaveBeenCalled();
  });
});
