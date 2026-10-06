export interface BesoinCommande {
  referenceArticle: string;
  quantiteACommander: number;
  dateDernierBesoin: string;  // ISO string (ex : "2025-07-14")
  fournisseurId?: number;
  articleId?: number;
  fournisseurNom?: string;
  besoinArticleId?: number; // Ajout du champ manquant
}
