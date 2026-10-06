import { environment } from '../../../environments/environment';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import {CommonModule, DatePipe, DecimalPipe} from '@angular/common';




@Component({
  selector: 'app-bande-commande-detail',
  templateUrl: './bande-commande-detail.component.html',
  imports: [
    DecimalPipe,
    DatePipe ,
    CommonModule
  ],
  styleUrls: ['./bande-commande-detail.component.css']
})
export class BandeCommandeDetailComponent implements OnInit {
  bande: any;
  loading = true;

  constructor(private route: ActivatedRoute, private http: HttpClient) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.http.get(`${environment.apiUrl}/api/commandes/bandes/${id}`).subscribe({
      next: (data) => { this.bande = data; this.loading = false; },
      error: (err) => { alert('Erreur chargement bande'); this.loading = false; }
    });
  }


  async telechargerPDF() {
    const [{ default: jsPDF }, { default: autoTable }] = await Promise.all([import('jspdf'), import('jspdf-autotable')]);
    const doc = new jsPDF();

    // Logo (exemple, à adapter avec ton vrai logo)
    // doc.addImage('assets/logo-power-integration.png', 'PNG', 10, 10, 30, 15);

    // Titre et société
    doc.setFontSize(18);
    doc.setTextColor(44, 62, 80);
    doc.text('Power Integration', 105, 20, { align: 'center' });

    doc.setFontSize(14);
    doc.setTextColor(0, 0, 0);
    doc.text(`Bon de Commande N° BC-${this.bande.commandeId}`, 105, 30, { align: 'center' });
    doc.setFontSize(11);
    doc.text(`Date de commande : ${this.bande.dateCommande}`, 14, 40);
    doc.text(`Date d'arrivage : ${this.bande.dateArrivage}`, 14, 47);

    // Fournisseur
    doc.setFontSize(12);
    doc.text(`Fournisseur : ${this.bande.fournisseurNom}`, 14, 60);
    doc.text(`Téléphone : ${this.bande.fournisseurTelephone || '-'}`, 14, 67);
    doc.text(`Email : ${this.bande.fournisseurEmail || '-'}`, 14, 74);

    // Tableau
    autoTable(doc, {
      startY: 80,
      head: [['N°', 'Référence', 'Désignation', 'Qté', 'Cond.', 'PU', 'Total HT']],
      body: [[
        1,
        this.bande.articleReference,
        this.bande.designationArticle,
        this.bande.quantite,
        this.bande.quantiteConditionnement,
        this.bande.prixUnitaire,
        this.bande.montantBande
      ]],
      headStyles: { fillColor: [44, 62, 80], textColor: 255 },
      styles: { fontSize: 11 }
    });

    // Totaux
    const totalHT = this.bande.montantBande;
    const tva = totalHT * 0.2;
    const ttc = totalHT * 1.2;
    const finalY = (doc as any).lastAutoTable.finalY || 100;

    doc.setFontSize(12);
    doc.setTextColor(44, 62, 80);
    doc.text(`Total HT : ${totalHT.toFixed(2)} DH`, 150, finalY + 10);
    doc.text(`TVA (20%) : ${tva.toFixed(2)} DH`, 150, finalY + 17);
    doc.setFontSize(13);
    doc.setTextColor(0, 102, 204);
    doc.text(`Total TTC : ${ttc.toFixed(2)} DH`, 150, finalY + 25);

    doc.save(`BonCommande_BC-${this.bande.commandeId}.pdf`);
  }
}
