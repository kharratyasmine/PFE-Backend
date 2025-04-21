package com.workpilot.service;

import com.workpilot.entity.devis.*;
import com.workpilot.exception.ResourceNotFoundException;
import com.workpilot.repository.devis.DevisRepository;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class DevisExportService {

    @Autowired
    private DevisRepository devisRepository;

    public ByteArrayOutputStream exportDevisToWord(Long devisId) {
        Devis devis = devisRepository.findById(devisId)
                .orElseThrow(() -> new ResourceNotFoundException("Devis non trouvé"));

        XWPFDocument document = new XWPFDocument();

        // 📌 Page de couverture
        try (InputStream is = new FileInputStream("src/main/resources/static/img/images.jpg")) {
            XWPFParagraph imagePara = document.createParagraph();
            imagePara.setAlignment(ParagraphAlignment.CENTER);
            imagePara.setSpacingAfter(2500); // 🟦 Espace entre image et titre
            XWPFRun imageRun = imagePara.createRun();
            imageRun.addPicture(is, Document.PICTURE_TYPE_PNG, "logo-telnet", Units.toEMU(200), Units.toEMU(80));
        } catch (Exception e) {
            throw new RuntimeException("Erreur chargement logo", e);
        }

// 📌 Titre principal
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        title.setSpacingBefore(500); // (remplace 200 + doublon inutile)
        title.setSpacingAfter(500);  // espace après le titre
        XWPFRun titleCoverRun = title.createRun();
        titleCoverRun.setText("Technical and Financial Proposal\nProject:");
        titleCoverRun.setColor("00B0F0");
        titleCoverRun.setFontSize(18);
        titleCoverRun.setBold(true);
        titleCoverRun.setItalic(true);

// 📌 Référence
        XWPFParagraph refPara = document.createParagraph();
        refPara.setAlignment(ParagraphAlignment.CENTER);
        refPara.setSpacingAfter(5000); // 🟦 espace entre référence et abstract
        XWPFRun refRun = refPara.createRun();
        refRun.setText("DOCUMENT REFERENCE :\n" + devis.getReference());
        refRun.addBreak();
        refRun.setText("Edition :\n" + devis.getEdition());
        refRun.setItalic(true);

// 📌 Abstract
        addParagraph(document, "\nABSTRACT", "00B0F0", true);
        addParagraph(document,
                "This document is a financial proposal for the project " + devis.getProject().getName()
                        + " during the period " + devis.getCreationDate() + ".\nIt covers workload, financial, and invoicing estimations.",
                "000000", false);

// Saut de page après la page de garde
        document.createParagraph().setPageBreak(true);



        // PAGE DE COUVERTURE AVEC LOGO
        XWPFTable headerTable = document.createTable(1, 3);
        setTableWidth(headerTable, 9000);

        // Colonne 1 : Logo
        XWPFTableCell logoCell = headerTable.getRow(0).getCell(0);
        logoCell.removeParagraph(0);
        XWPFParagraph logoPara = logoCell.addParagraph();
        logoPara.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun logoRun = logoPara.createRun();
        try (InputStream logoStream = new FileInputStream("src/main/resources/static/img/images.jpg")) {
            logoRun.addPicture(logoStream, Document.PICTURE_TYPE_PNG, "logo_telnet", Units.toEMU(100), Units.toEMU(35));
        } catch (Exception e) {
            throw new RuntimeException("Erreur image logo", e);
        }

        // Colonne 2 : Titre centré
        XWPFTableCell titleCell = headerTable.getRow(0).getCell(1);
        titleCell.removeParagraph(0);
        XWPFParagraph centerTitle = titleCell.addParagraph();
        centerTitle.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleTableRun = centerTitle.createRun();
        titleTableRun.setText("Technical and Financial Proposal");
        titleTableRun.setBold(true);
        titleTableRun.setFontSize(14);
        titleTableRun.setColor("5A5A5A");

        // Colonne 3 : Informations
        XWPFTableCell infoCell = headerTable.getRow(0).getCell(2);
        infoCell.removeParagraph(0);
        XWPFParagraph infoPara = infoCell.addParagraph();
        infoPara.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun infoRun = infoPara.createRun();
        infoRun.setFontSize(10);
        infoRun.setColor("00B0F0");
        infoRun.setText("Ref: " + devis.getReference());
        infoRun.addBreak();
        infoRun.setText("Edition : " + devis.getEdition());
        infoRun.addBreak();
        infoRun.setText("Date : " + devis.getCreationDate());
        infoRun.addBreak();
        infoRun.setText("Author : " + devis.getAuthor());

        // SEPARATEUR
        XWPFParagraph separator = document.createParagraph();
        separator.setBorderBottom(Borders.SINGLE);

// 🔷 Titre
        // 🔷 Titre "Distribution"
        XWPFParagraph distTitle = document.createParagraph();
        distTitle.setSpacingBefore(400);
        XWPFRun distRun = distTitle.createRun();
        distRun.setText("Distribution (P : PARTIAL ; C : COMPLETE)");
        distRun.setBold(true);
        distRun.setItalic(true);
        distRun.setFontSize(12);
        distRun.setColor("2F75B5");

// 🔷 Tableau Distribution
        XWPFTable distTable = document.createTable();
        setTableWidth(distTable, 9000);

// En-tête
        XWPFTableRow header = distTable.getRow(0);
        header.getCell(0).setText("Name");
        header.addNewTableCell().setText("Function");
        header.addNewTableCell().setText("P");
        header.addNewTableCell().setText("C");
        colorTableHeader(header);

// 🔄 Groupement par type (Customer, Telnet, etc.)
        String currentGroup = "";
        for (Distribution dist : devis.getDistributions()) {

            // Si nouveau groupe
            if (!dist.getType().equals(currentGroup)) {
                currentGroup = dist.getType();


                // 🔹 Ligne du groupe
                XWPFTableRow groupRow = distTable.createRow();

                // Supprimer cellules inutiles (il reste 3 cellules vides après createRow())
                while (groupRow.getTableCells().size() > 1) {
                    groupRow.removeCell(1);
                }

                // Fusionner les 4 colonnes
                CTTcPr tcPr = groupRow.getCell(0).getCTTc().addNewTcPr();
                tcPr.addNewGridSpan().setVal(BigInteger.valueOf(4));

                // Style du texte
                XWPFParagraph groupPara = groupRow.getCell(0).getParagraphs().get(0);
                groupPara.setAlignment(ParagraphAlignment.LEFT);
                XWPFRun groupRun = groupPara.createRun();
                groupRun.setText(currentGroup);
                groupRun.setBold(true);
                groupRun.setItalic(true);
            }

            // 🔹 Données ligne
            XWPFTableRow row = distTable.createRow();
            while (row.getTableCells().size() < 4) {
                row.addNewTableCell();
            }

            row.getCell(0).setText(dist.getName());
            row.getCell(1).setText(dist.getFunction() != null ? dist.getFunction() : "-");
            row.getCell(2).setText(dist.isPartial() ? "x" : "");
            row.getCell(3).setText(dist.isComplete() ? "x" : "");
        }

        // 🖋️ Visas Table
        XWPFParagraph visaTitle = document.createParagraph();
        visaTitle.setSpacingBefore(400);
        XWPFRun visaRun = visaTitle.createRun();
        visaRun.setText("Visas");
        visaRun.setBold(true);
        visaRun.setItalic(true);
        visaRun.setFontSize(13);
        visaRun.setColor("2F75B5");

// Créer le tableau avec 4 colonnes
        XWPFTable visaTable = document.createTable();
        setTableWidth(visaTable, 9000);

// En-tête
        XWPFTableRow headerRow = visaTable.getRow(0);
        headerRow.getCell(0).setText("Action");
        headerRow.addNewTableCell().setText("Name");
        headerRow.addNewTableCell().setText("Date");
        headerRow.addNewTableCell().setText("Visa");
        colorTableHeader(headerRow);

// Affichage ligne par ligne
        for (Visa visa : devis.getVisas()) {
            XWPFTableRow row = visaTable.createRow();
            while (row.getTableCells().size() < 4) {
                row.addNewTableCell();
            }

            row.getCell(0).setText(visa.getAction());
            row.getCell(1).setText(visa.getName());
            row.getCell(2).setText(visa.getDate() != null ? visa.getDate().toString() : "-");
            row.getCell(3).setText(visa.getVisa() != null ? visa.getVisa() : "-");
        }



        // 💰 Financial Proposal
        addParagraph(document, "\n--- Financial Proposal ---");
        XWPFTable financeTable = document.createTable();
        setTableWidth(financeTable, 9000);
        XWPFTableRow fHeader = financeTable.getRow(0);
        fHeader.getCell(0).setText("Position");
        fHeader.addNewTableCell().setText("Workload");
        fHeader.addNewTableCell().setText("Daily Cost");
        fHeader.addNewTableCell().setText("Total Cost");
        colorTableHeader(fHeader);

        for (FinancialDetail fd : devis.getFinancialDetails()) {
            XWPFTableRow row = financeTable.createRow();
            row.getCell(0).setText(fd.getPosition());
            row.getCell(1).setText(String.valueOf(fd.getWorkload()));
            row.getCell(2).setText(fd.getDailyCost().toPlainString());
            row.getCell(3).setText(fd.getTotalCost().toPlainString());
        }

        // 🧾 Invoicing Proposal
        addParagraph(document, "\n--- Invoicing Proposal ---");
        XWPFTable invoiceTable = document.createTable();
        setTableWidth(invoiceTable, 9000);
        XWPFTableRow iHeader = invoiceTable.getRow(0);
        iHeader.getCell(0).setText("Description");
        iHeader.addNewTableCell().setText("Date");
        iHeader.addNewTableCell().setText("Amount");
        colorTableHeader(iHeader);

        for (InvoicingDetail inv : devis.getInvoicingDetails()) {
            XWPFTableRow row = invoiceTable.createRow();
            row.getCell(0).setText(inv.getDescription());
            row.getCell(1).setText(inv.getInvoicingDate().toString());
            row.getCell(2).setText(inv.getAmount().toPlainString());
        }

        // 📊 Workload Proposal
        addParagraph(document, "\n--- Workload Proposal ---");
        XWPFTable workloadTable = document.createTable();
        setTableWidth(workloadTable, 9000);
        XWPFTableRow wHeader = workloadTable.getRow(0);
        wHeader.getCell(0).setText("Period");
        wHeader.addNewTableCell().setText("Estimated Workload");
        wHeader.addNewTableCell().setText("Public Holidays");
        colorTableHeader(wHeader);

        for (WorkloadDetail wl : devis.getWorkloadDetails()) {
            XWPFTableRow row = workloadTable.createRow();
            row.getCell(0).setText(wl.getPeriod());
            row.getCell(1).setText(String.valueOf(wl.getEstimatedWorkload()));
            row.getCell(2).setText(String.valueOf(wl.getPublicHolidays()));
        }

        // 📌 Footer
        XWPFHeaderFooterPolicy footerPolicy = document.createHeaderFooterPolicy();
        XWPFFooter footer = footerPolicy.createFooter(XWPFHeaderFooterPolicy.DEFAULT);
        XWPFParagraph para = footer.createParagraph();
        para.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun footerRun = para.createRun();
        footerRun.setFontSize(8);
        footerRun.setText("Siège : TELNET Group Immeuble ENNOUR - 1082 Tunis - TUNISIE   |   Site Sfax : Rue El-Arghani, N°25 - 3000 Sfax - TUNISIE");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.write(out);
            document.close();
            return out;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la génération du Word", e);
        }
    }

    private void addParagraph(XWPFDocument doc, String text) {
        addParagraph(doc, text, "000000", false);
    }

    private void addParagraph(XWPFDocument doc, String text, String colorHex, boolean bold) {
        XWPFParagraph paragraph = doc.createParagraph();
        paragraph.setSpacingBefore(200);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(12);
        run.setColor(colorHex);
        run.setBold(bold);
    }

    private void colorTableHeader(XWPFTableRow header) {
        for (XWPFTableCell cell : header.getTableCells()) {
            CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
            CTShd ctshd = tcPr.isSetShd() ? tcPr.getShd() : tcPr.addNewShd();
            ctshd.setFill("D9E1F2");
        }
    }

    private void setTableWidth(XWPFTable table, int widthInTwips) {
        CTTblPr tblPr = table.getCTTbl().getTblPr() == null ? table.getCTTbl().addNewTblPr() : table.getCTTbl().getTblPr();
        CTTblWidth tblWidth = tblPr.isSetTblW() ? tblPr.getTblW() : tblPr.addNewTblW();
        tblWidth.setW(BigDecimal.valueOf(widthInTwips).toBigInteger());
        tblWidth.setType(STTblWidth.DXA);
    }
}
