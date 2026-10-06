import { environment } from '../../environments/environment';
// src/app/services/article.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Article } from '../interfaces/article';

@Injectable({
  providedIn: 'root'
})
export class ArticleService {
  private apiUrl = `${environment.apiUrl}/api/articles`;

  constructor(private http: HttpClient) {}

  createArticle(article: Article): Observable<Article> {
    return this.http.post<Article>(this.apiUrl, article);
  }

  getArticles(): Observable<Article[]> {
    return this.http.get<Article[]>(this.apiUrl);
  }

  getArticle(id: number): Observable<Article> {
    return this.http.get<Article>(`${this.apiUrl}/${id}`);
  }

  updateArticle(id: number, article: Article): Observable<Article> {
    return this.http.put<Article>(`${this.apiUrl}/${id}`, article);
  }

  deleteArticle(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getFournisseurs(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/api/fournisseurs`);
  }

  getArticlesCritiques(articles: Article[]): Article[] {
    return articles.filter(
      a => a.statutStock === 'FAIBLE' || a.statutStock === 'RUPTURE'
    );
  }
}
