export interface Article {
  id: number;
  reference: string;
  description: string;
}

export interface BesoinArticle {
  id?: number;
  articleId: number;
  articleReference: string;
  article?: Article;
  quantite: number;
  typeBesoin: 'PRODUCTION' | 'NEGOCE' | 'DEVIS';
  dateBesoin: string;
  fournisseur?: string;
  fournisseurId?: number;
  prix?: number;           // prix unitaire
  montantTotal?: number;
  commandeCreee?: boolean;
  quantiteEnStock?: number;
}

export interface Besoin {
  id: number;
  dateBesoin: string;
  typeBesoin: 'PRODUCTION' | 'NEGOCE' | 'DEVIS';
  besoinArticles: BesoinArticle[];
}

export interface BesoinDto {
  id: number;
  dateBesoin: string;
  typeBesoin: 'PRODUCTION' | 'NEGOCE' | 'DEVIS';
  besoinArticles: BesoinArticle[];
}

