export interface BandeCommande {
  id?: number;
  articleReference?: string;
  quantite: number;
  dateArrivage?: string;
  prixUnitaire?: number;
  montantBande?: number;
}

export interface Commande {
  id: number;
  dateCommande: string;
  statusCommande: string;
  fournisseurNom?: string;
  montantTotal?: number;
  bandesCommande?: BandeCommande[];
}
