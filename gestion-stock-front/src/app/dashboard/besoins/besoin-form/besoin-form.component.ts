// gestion-stock-front/src/app/dashboard/besoins/besoin-form/besoin-form.component.ts

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ArticleService } from '../../../services/article.service';
import { BesoinService } from '../../../services/besoin.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-besoin-form',
  templateUrl: './besoin-form.component.html',
  imports: [
    ReactiveFormsModule,
    CommonModule
  ],
  styleUrls: ['./besoin-form.component.css']
})
export class BesoinFormComponent implements OnInit {
  besoinForm!: FormGroup;
  articles: any[] = [];
  typeBesoins: string[] = ['PRODUCTION', 'NEGOCE', 'DEVIS'];
  submitted = false;
  isEditMode = false;
  besoinId: number | null = null;
  loading = false;
  userRole: string | null = null;
  readOnly: boolean = false;

  constructor(
    private fb: FormBuilder,
    private articleService: ArticleService,
    private besoinService: BesoinService,
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.userRole = localStorage.getItem('userRole');
    this.readOnly = !['ADMIN', 'COMMERCIAL'].includes(this.userRole ?? '');
    if (this.userRole === 'COMMERCIAL') this.typeBesoins = ['NEGOCE', 'DEVIS'];

    this.initForm();
    this.loadArticles();
    this.checkEditMode();

    if (this.readOnly) {
      this.besoinForm.disable();
    }
  }


  private initForm(): void {
    this.besoinForm = this.fb.group({
      articleId: ['', Validators.required],
      quantite: [null, [Validators.required, Validators.min(1)]],
      dateBesoin: ['', Validators.required],
      typeBesoin: ['', Validators.required]
    });
  }

  private loadArticles(): void {
    this.articleService.getArticles().subscribe({
      next: (data) => this.articles = data,
      error: (err) => console.error('Erreur de chargement des articles', err)
    });
  }

  private checkEditMode(): void {
    this.route.params.subscribe(params => {
      const id = params['id'];
      if (id) {
        this.isEditMode = true;
        if (this.userRole !== 'ADMIN') { this.readOnly = true; this.besoinForm.disable(); }
        this.besoinId = +id;
        this.loadBesoinForEdit(+id);
      }
    });
  }

  private loadBesoinForEdit(id: number): void {
    this.loading = true;
    this.besoinService.getBesoinById(id).subscribe({
      next: (besoin) => {
        if (besoin.besoinArticles && besoin.besoinArticles.length > 0) {
          const firstArticle = besoin.besoinArticles[0];
          this.besoinForm.patchValue({
            articleId: firstArticle.articleId?.toString() || '',
            quantite: firstArticle.quantite,
            dateBesoin: besoin.dateBesoin,
            typeBesoin: besoin.typeBesoin
          });
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement du besoin', err);
        alert('Erreur lors du chargement du besoin');
        this.loading = false;
      }
    });
  }

  get f() {
    return this.besoinForm.controls;
  }

  onSubmit(): void {
    if (this.readOnly) {
      alert("Vous n'avez pas l'autorisation de modifier ou créer un besoin.");
      return;
    }
    this.submitted = true;

    if (this.besoinForm.valid) {
      const formValue = this.besoinForm.value;
      const selectedId = formValue.articleId.toString();
      const selectedArticle = this.articles.find(article =>
        article.id.toString() === selectedId
      );

      if (!selectedArticle) {
        alert('Article non trouvé');
        return;
      }

      const payload = {
        dateBesoin: formValue.dateBesoin,
        typeBesoin: formValue.typeBesoin,
        besoinArticles: [
          {
            articleId: selectedArticle.id,
            quantite: formValue.quantite
          }
        ]
      };

      if (this.isEditMode && this.besoinId) {
        // Mode modification
        this.besoinService.updateBesoin(this.besoinId, payload).subscribe({
          next: () => {
            alert('Besoin modifié avec succès');
            this.router.navigate(['/besoins']);
          },
          error: (err) => {
            console.error('Erreur lors de la modification du besoin', err);
            alert('Erreur : ' + (err.error?.message || err.message));
          }
        });
      } else {
        // Mode création
        this.besoinService.createBesoin(payload).subscribe({
          next: () => {
            alert('Besoin ajouté avec succès');
            this.router.navigate(['/besoins']);
          },
          error: (err) => {
            console.error('Erreur lors de l\'ajout du besoin', err);
            alert('Erreur : ' + (err.error?.message || err.message));
          }
        });
      }
    }
  }

  goBack(): void {
    this.router.navigate(['/besoins']);
  }
}
