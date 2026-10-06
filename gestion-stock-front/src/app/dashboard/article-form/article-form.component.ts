import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ArticleService } from '../../services/article.service';
import { Article } from '../../interfaces/article';
import {AuthService} from '../../services/auth.service';

@Component({
  selector: 'app-article-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './article-form.component.html',
  styleUrls: ['./article-form.component.css']
})
export class ArticleFormComponent implements OnInit {
  articleForm: FormGroup;
  submitted = false;
  fournisseurs: any[] = [];
  article: Article | null = null;
  isModification = false;

  constructor(
    private formBuilder: FormBuilder,
    private articleService: ArticleService,
    private router: Router,
    private route: ActivatedRoute ,
    private authService: AuthService
  ) {
    this.articleForm = this.formBuilder.group({
      reference: ['', Validators.required],
      description: ['', Validators.required],
      stockSecurite: ['', [Validators.required, Validators.min(0)]],
      quantiteConditionnement: ['', [Validators.required, Validators.min(1)]],
      quantite: [null, [Validators.required, Validators.min(0)]],
      prix: ['', [Validators.required, Validators.min(0)]],
      fournisseurId: ['', Validators.required]
    });

    // Vérifier si nous sommes en mode modification
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.isModification = true;
      this.articleService.getArticle(Number(id)).subscribe({
        next: (data) => {
          this.article = data;
          this.articleForm.patchValue({
            reference: data.reference,
            description: data.description,
            stockSecurite: data.stockSecurite,
            quantiteConditionnement: data.quantiteConditionnement,
            quantite: data.quantite,
            prix: data.prix,
            fournisseurId: data.fournisseurId
          });
        },
        error: (err) => {
          console.error('Erreur chargement article :', err);
          this.router.navigate(['/dashboard/articles']);
        }
      });
    }
  }

  ngOnInit(): void {
    this.articleService.getFournisseurs().subscribe({
      next: (data) => {
        this.fournisseurs = data;
      },
      error: (err) => {
        console.error('Erreur chargement fournisseurs :', err);
      }
    });
  }

  get f(): { [key: string]: any } {
    return this.articleForm.controls;
  }

  onSubmit(): void {
    this.submitted = true;

    if (this.articleForm.valid) {
      const article: Article = {
        ...this.articleForm.value
      };

      if (this.isModification && this.article?.id) {
        // Mode modification
        this.articleService.updateArticle(this.article.id, article).subscribe({
          next: () => {
            alert('Article modifié avec succès !');
            this.router.navigate(['/articles']);
          },
          error: (err) => {
            console.error('Erreur modification article :', err);
            alert('Erreur lors de la modification de l\'article : ' + err.message);
          }
        });
      } else {
        // Mode création
        this.articleService.createArticle(article).subscribe({
          next: () => {
            alert('Article ajouté avec succès !');
            this.articleForm.reset();
            this.submitted = false;
            this.router.navigate(['/articles']);
          },
          error: (err) => {
            console.error('Erreur création article :', err);
            alert('Erreur lors de l\'ajout de l\'article : ' + err.message);
          }
        });
      }
    }
  }

  onCancel(): void {
    this.router.navigate(['/articles']);

  }
}
