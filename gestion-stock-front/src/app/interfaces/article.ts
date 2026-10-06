export interface Article {
  id?: number;
  reference: string;
  description: string;
  stockSecurite: number;
  quantiteConditionnement: number;
  quantite: number;
  prix: number;
  fournisseurId?: number;
  statutStock: 'OK' | 'FAIBLE' | 'RUPTURE'; // <-- AJOUTE CETTE LIGNE
}
