package com.workpilot.service;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.workpilot.entity.Devis;
import com.workpilot.entity.FinancialDetail;
import com.workpilot.entity.InvoicingDetail;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    public byte[] generateDevisPdf(Devis devis) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.setMargins(20, 20, 20, 20); // Ajout de marges pour la mise en page

            // Ajout du titre du devis
            document.add(new Paragraph("DEVIS #" + devis.getReference())
                    .setBold()
                    .setFontSize(18));

            // Formatage des dates
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String creationDate = (devis.getCreationDate() != null) ? devis.getCreationDate().format(formatter) : "Non spécifiée";

            document.add(new Paragraph("Date de création: " + creationDate)
                    .setFontSize(12));

            document.add(new Paragraph("Projet: " + (devis.getProject() != null ? devis.getProject().getName() : "Non spécifié"))
                    .setFontSize(12));

            document.add(new Paragraph("Montant Total: " + devis.getTotalCost() + " €")
                    .setFontSize(12).setBold());

            document.add(new Paragraph("Statut: " + (devis.getStatus() != null ? devis.getStatus() : "Non spécifié"))
                    .setFontSize(12));

            // Ajout d'un tableau pour les détails financiers
            document.add(new Paragraph("\nDétails Financiers:").setBold().setFontSize(14));

            Table table = new Table(4);
            table.addCell(new Cell().add(new Paragraph("Position").setBold()));
            table.addCell(new Cell().add(new Paragraph("Charge de Travail (j)").setBold()));
            table.addCell(new Cell().add(new Paragraph("Coût Journalier (€)").setBold()));
            table.addCell(new Cell().add(new Paragraph("Coût Total (€)").setBold()));

            for (FinancialDetail detail : devis.getFinancialDetails()) {
                table.addCell(new Cell().add(new Paragraph(detail.getPosition() != null ? detail.getPosition() : "-")));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(detail.getWorkload()))));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(detail.getDailyCost()))));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(detail.getTotalCost()))));
            }

            document.add(table);

            // Ajout d'un tableau pour la facturation
            document.add(new Paragraph("\nDétails de Facturation:").setBold().setFontSize(14));

            Table invoiceTable = new Table(4);
            invoiceTable.addCell(new Cell().add(new Paragraph("Description").setBold()));
            invoiceTable.addCell(new Cell().add(new Paragraph("Date de Facturation").setBold()));
            invoiceTable.addCell(new Cell().add(new Paragraph("Montant (€)").setBold()));
            invoiceTable.addCell(new Cell().add(new Paragraph("Statut").setBold()));

            for (InvoicingDetail invoice : devis.getInvoicingDetails()) {
                String invoicingDate = (invoice.getInvoicingDate() != null) ? invoice.getInvoicingDate().format(formatter) : "Non spécifiée";
                invoiceTable.addCell(new Cell().add(new Paragraph(invoice.getDescription() != null ? invoice.getDescription() : "-")));
                invoiceTable.addCell(new Cell().add(new Paragraph(invoicingDate)));
                invoiceTable.addCell(new Cell().add(new Paragraph(String.valueOf(invoice.getAmount()))));
                invoiceTable.addCell(new Cell().add(new Paragraph(invoice.getStatus() != null ? invoice.getStatus() : "-")));
            }

            document.add(invoiceTable);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }

        return outputStream.toByteArray();
    }
}
