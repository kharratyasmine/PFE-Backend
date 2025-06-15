package com.workpilot.Export;

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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DevisExportService {

    @Autowired
    private DevisRepository devisRepository;

    public ByteArrayOutputStream exportDevisToWord(Long devisId) {
        Devis devis = devisRepository.findById(devisId)
                .orElseThrow(() -> new ResourceNotFoundException("Devis non trouvé"));

        XWPFDocument document = new XWPFDocument();

        // ✅ 1. Activer la première page différente
        CTSectPr sectPr = document.getDocument().getBody().isSetSectPr()
                ? document.getDocument().getBody().getSectPr()
                : document.getDocument().getBody().addNewSectPr();
        sectPr.addNewTitlePg(); // Première page différente

        XWPFHeaderFooterPolicy headerPolicy = new XWPFHeaderFooterPolicy(document, sectPr);

        // ✅ 2. EN-TÊTE DE LA PREMIÈRE PAGE (logo uniquement, centré)
        XWPFHeader firstPageHeader = headerPolicy.createHeader(XWPFHeaderFooterPolicy.FIRST);
        XWPFParagraph headerLogoPara = firstPageHeader.createParagraph();
        headerLogoPara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun logoRun = headerLogoPara.createRun();
        try (InputStream is = new FileInputStream("src/main/resources/static/img/logoTELNET.png")) {
            logoRun.addPicture(is, Document.PICTURE_TYPE_PNG, "logo-telnet", Units.toEMU(300), Units.toEMU(100));
        } catch (Exception e) {
            throw new RuntimeException("Erreur chargement logo", e);
        }

        // ✅ 3. PIED DE PAGE DE LA PREMIÈRE PAGE
        XWPFFooter firstPageFooter = headerPolicy.createFooter(XWPFHeaderFooterPolicy.FIRST);

// === Barre de séparation (ligne horizontale) ===
        XWPFParagraph separator = firstPageFooter.createParagraph();
        separator.setSpacingAfter(100); // petit espace après la ligne

// === Adresse TELNET (alignée à gauche) ===
        XWPFParagraph contactPara = firstPageFooter.createParagraph();
        contactPara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun contactRun = contactPara.createRun();
        contactRun.setFontSize(8);
        contactRun.setFontFamily("Verdana");
        contactRun.setColor("7F7F7F");
        contactRun.setText("Siège : TELNET Group Immeuble ENNOUR - 1082 Tunis - TUNISIE");
        contactRun.addBreak();
        contactRun.setText("Site Sfax : Z.I Poudrière 1, Rue Jameleddine El-Afghani, N°25-3000 Sfax - TUNISIE");

// === Bloc de confidentialité (centré) ===
        XWPFParagraph confidentialityPara = firstPageFooter.createParagraph();
        confidentialityPara.setAlignment(ParagraphAlignment.CENTER);
        confidentialityPara.setSpacingBefore(100);

        XWPFRun runConf = confidentialityPara.createRun();
        runConf.setFontSize(8);
        runConf.setFontFamily("Verdana");
        runConf.setColor("7F7F7F");
        runConf.setText("All Information in this document are the property of TELNET and disseminated in confidence for a specific purpose.");
        runConf.addBreak();
        runConf.setText("The recipient shall have custody and supervision of this document and agrees that it will not be copied or reproduced ");
        runConf.setText("in whole or part and its contents will be revealed in any manner to any person, except to fulfill the purpose for which it was transmitted.");
        runConf.addBreak();
        runConf.setText("This recommendation applies to all pages of this document.");
        runConf.addBreak();
        runConf.setText("© TELNET property - Reproduction and disclosure prohibited.");


        // ✅ 4. HEADER + FOOTER PAR DÉFAUT POUR LES AUTRES PAGES
        XWPFHeaderFooterPolicy defaultFooterPolicy = document.createHeaderFooterPolicy();

        // ➕ Footer (pages suivantes)
        XWPFFooter defaultFooter = defaultFooterPolicy.createFooter(XWPFHeaderFooterPolicy.DEFAULT);
        XWPFParagraph defFooterPara = defaultFooter.createParagraph();
        defFooterPara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun defaultFooterRun = defFooterPara.createRun();
        defaultFooterRun.setFontSize(8);
        defaultFooterRun.setFontFamily("Verdana");
        defaultFooterRun.setText("© propriété de TELNET - Reproduction et divulgation interdites                          MON_RCT_TM_001_EN 07  ");
        XWPFParagraph pageNumberPara = defaultFooter.createParagraph();
        pageNumberPara.setAlignment(ParagraphAlignment.CENTER);

        XWPFParagraph pageNumberParaFO = defaultFooter.createParagraph();
        pageNumberParaFO.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun run = pageNumberParaFO.createRun();
        run.setFontSize(8);
        run.setFontFamily("Verdana");
        run.setText("Page ");

        CTSimpleField pageField = pageNumberParaFO.getCTP().addNewFldSimple();
        pageField.setInstr("PAGE");

// Facultatif : afficher " of N"
        XWPFRun ofRun = pageNumberParaFO.createRun();
        ofRun.setText(" of ");

        CTSimpleField totalPagesField = pageNumberParaFO.getCTP().addNewFldSimple();
        totalPagesField.setInstr("NUMPAGES");


        // ➕ Header (pages suivantes)
        XWPFHeader defaultHeader = defaultFooterPolicy.createHeader(XWPFHeaderFooterPolicy.DEFAULT);

// Créer le tableau directement dans le header
        XWPFTable headerTable = defaultHeader.createTable(1, 3);
        setTableWidth(headerTable, 9000);
        addBordersToTable(headerTable);

// Colonne 1 : Logo
        XWPFTableCell logoCell = headerTable.getRow(0).getCell(0);
        logoCell.removeParagraph(0);
        XWPFParagraph logoPara = logoCell.addParagraph();
        logoPara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun logoRun1 = logoPara.createRun();
        try (InputStream logoStream = new FileInputStream("src/main/resources/static/img/logoTELNET.png")) {
            logoRun1.addPicture(logoStream, Document.PICTURE_TYPE_PNG, "logo_telnet", Units.toEMU(100), Units.toEMU(35));
        } catch (Exception e) {
            throw new RuntimeException("Erreur image logo", e);
        }

// Colonne 2 : Titre
        XWPFTableCell titleCell = headerTable.getRow(0).getCell(1);
        titleCell.removeParagraph(0);
        XWPFParagraph centerTitle = titleCell.addParagraph();
        centerTitle.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleTableRun = centerTitle.createRun();
        titleTableRun.setText("Technical and Financial Proposal");
        titleTableRun.setBold(true);
        titleTableRun.setFontSize(13);
        titleTableRun.setColor("808080");

// Colonne 3 : Infos
        XWPFTableCell infoCell = headerTable.getRow(0).getCell(2);
        infoCell.removeParagraph(0);
        XWPFParagraph infoPara = infoCell.addParagraph();
        infoPara.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun infoRun = infoPara.createRun();
        infoRun.setFontSize(10);
        infoRun.setFontFamily("Calibri");
        infoRun.setColor("00B0F0");
        infoRun.setText("Ref: " + devis.getReference());
        infoRun.addBreak();
        infoRun.setText("Edition : " + devis.getEdition());
        infoRun.addBreak();
        infoRun.setText("Date : " + devis.getCreationDate());
        infoRun.addBreak();
        infoRun.setText("Author : " + devis.getAuthor());


    // 📌 Titre principal
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        title.setSpacingBefore(500); // (remplace 200 + doublon inutile)
        title.setSpacingBefore(2500);// espace après le titre
        XWPFRun titleCoverRun = title.createRun();
        titleCoverRun.setText("Technical and Financial Proposal\nProject :");
        titleCoverRun.setColor("00B0F0");
        titleCoverRun.setFontSize(26);
        titleCoverRun.setBold(true);       // Gras
        titleCoverRun.setItalic(true);     // Italique
        titleCoverRun.setColor("00B0F0");
        titleCoverRun.setFontFamily("Arial");


// 📌 Référence
        XWPFParagraph refPara = document.createParagraph();
        refPara.setAlignment(ParagraphAlignment.CENTER);
        refPara.setSpacingAfter(2500); // 🟦 espace entre référence et abstract
        XWPFRun refRun = refPara.createRun();
        refRun.setText("DOCUMENT REFERENCE :\n" + devis.getReference());
        refRun.addBreak();
        refRun.setText("Edition :\n" + devis.getEdition());
        refRun.setItalic(true);
        refRun.setFontSize(11);
        refRun.setFontFamily("Arial Rounded MT Bold");

        XWPFParagraph abstractTitle = document.createParagraph();
        abstractTitle.setSpacingBefore(200);
        XWPFRun abstractRun = abstractTitle.createRun();
        abstractRun.setText("ABSTRACT");
        abstractRun.setBold(true);
        abstractRun.setItalic(true);
        abstractRun.setColor("00B0F0");
        abstractRun.setFontSize(10);
        abstractRun.setFontFamily("Verdana");
// Table pour encadrer le texte
        XWPFTable abstractTable = document.createTable(1, 1);
        setTableWidth(abstractTable, 9000);
        addBordersToTable(abstractTable);
        XWPFTableCell abstractCell = abstractTable.getRow(0).getCell(0);
        XWPFParagraph abstractTextPara = abstractCell.getParagraphs().get(0);
        XWPFRun abstractTextRun = abstractTextPara.createRun();
        abstractTextPara.setAlignment(ParagraphAlignment.BOTH);
        abstractTextRun.setFontSize(10);
        abstractTextRun.setText("This document is a financial proposal for the project \"" +
                devis.getProject().getName() + "\" during the period .... It covers .... \".");
        abstractTextRun.setFontFamily("Verdana");
// Bordure correcte
        CTTcPr tcPr = abstractCell.getCTTc().isSetTcPr() ? abstractCell.getCTTc().getTcPr() : abstractCell.getCTTc().addNewTcPr();
        CTTcBorders borders = tcPr.isSetTcBorders() ? tcPr.getTcBorders() : tcPr.addNewTcBorders();
        borders.addNewTop().setVal(STBorder.SINGLE);
        borders.addNewBottom().setVal(STBorder.SINGLE);
        borders.addNewLeft().setVal(STBorder.SINGLE);
        borders.addNewRight().setVal(STBorder.SINGLE);



/// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        document.createParagraph().setPageBreak(true);

// 🔷 Tableau Distribution
        // ✅ Création du tableau principal
        XWPFTable distTable = document.createTable();
        setTableWidth(distTable, 9000);
        addBordersToTable(distTable);

// ✅ Ligne de titre dans le tableau (fusionnée sur 4 colonnes)
        XWPFTableRow titleRow = distTable.getRow(0); // première ligne créée automatiquement
        XWPFTableCell titleCellD = titleRow.getCell(0);

// Fusion sur 4 colonnes
        CTTcPr titleTcPr = titleCellD.getCTTc().addNewTcPr();
        titleTcPr.addNewGridSpan().setVal(BigInteger.valueOf(4));

// Style texte
        titleCellD.removeParagraph(0);
        XWPFParagraph titlePara = titleCellD.addParagraph();
        titlePara.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun titleRunDist = titlePara.createRun();
        titleRunDist.setText("Distribution (P : PARTIAL ; C : COMPLETE)");
        titleRunDist.setItalic(true);
        titleRunDist.setColor("FFFFFF");
        titleRunDist.setFontSize(14);
        titleRunDist.setFontFamily("Arial Rounded MT Bold");

// Fond bleu
        CTShd shd = titleTcPr.isSetShd() ? titleTcPr.getShd() : titleTcPr.addNewShd();
        shd.setFill("2F75B5"); // bleu foncé

// ✅ Ligne d'en-tête
        XWPFTableRow header = distTable.createRow();
        header.getCell(0).setText("Name");
        header.addNewTableCell().setText("Function");
        header.addNewTableCell().setText("P");
        header.addNewTableCell().setText("C");


// Appliquer le style gras à chaque cellule d'en-tête
        for (XWPFTableCell cell : header.getTableCells()) {
            XWPFParagraph p = cell.getParagraphs().get(0);
            XWPFRun r = p.createRun();
            r.setItalic(true);
            r.setFontSize(10);
            r.setFontFamily("Arial Rounded MT Bold");
            r.setColor("2F75B5");
            r.setText(p.getText()); // copy previous text
            p.removeRun(0); // remove old run


            // ✅ Appliquer fond gris clair
            CTTcPr tcPrd = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
            CTShd cellShd = tcPrd.isSetShd() ? tcPrd.getShd() : tcPrd.addNewShd();
            cellShd.setFill("D9E1F2"); // gris clair
        }


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
                }// Fusionner les 4 colonnes
                CTTcPr tcPP = groupRow.getCell(0).getCTTc().addNewTcPr();
                tcPP.addNewGridSpan().setVal(BigInteger.valueOf(4));
                // Style du texte
                XWPFParagraph groupPara = groupRow.getCell(0).getParagraphs().get(0);
                groupPara.setAlignment(ParagraphAlignment.LEFT);
                XWPFRun groupRun = groupPara.createRun();
                groupRun.setText(currentGroup);
                groupRun.setBold(true);
                groupRun.setItalic(true);
            }// 🔹 Données ligne
            XWPFTableRow row = distTable.createRow();
            while (row.getTableCells().size() < 4) {
                row.addNewTableCell();
            }
            row.getCell(0).setText(dist.getName());
            row.getCell(1).setText(dist.getFunction() != null ? dist.getFunction() : "-");
            row.getCell(2).setText(dist.isPartial() ? "x" : "");
            row.getCell(3).setText(dist.isComplete() ? "x" : "");
        }


// 🔹 Espace entre les deux tableaux
        XWPFParagraph spaceBetweenTables = document.createParagraph();
        spaceBetweenTables.setSpacingBefore(500); // ou setSpacingAfter(200) si tu préfères

        // 🔷 Table Visas
        // 📌 Créer tableau Visas avec 4 colonnes
        XWPFTable visaTable = document.createTable(2, 4);
        setTableWidth(visaTable, 9000);
        addBordersToTable(visaTable);

// 🔷 Ligne 0 : Titre "Visas" sur 4 colonnes (fond bleu)
        XWPFTableRow visaTitleRow = visaTable.getRow(0);
        mergeCellsHorizontally(visaTable, 0, 0, 3);
        XWPFTableCell titleCellVIS = visaTitleRow.getCell(0);
        titleCellVIS.removeParagraph(0);
        XWPFParagraph titleParaVISA = titleCellVIS.addParagraph();
        titleParaVISA.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun titleRunVIS = titleParaVISA.createRun();
        titleRunVIS.setText("Visas");
        titleRunVIS.setItalic(true);
        titleRunVIS.setFontSize(14);
        titleRunVIS.setFontFamily("Arial Rounded MT Bold");
        titleRunVIS.setColor("FFFFFF");

// 💠 Couleur de fond bleu pour le titre
        CTTcPr tcPrTitle = titleCellVIS.getCTTc().isSetTcPr() ? titleCellVIS.getCTTc().getTcPr() : titleCellVIS.getCTTc().addNewTcPr();
        CTShd shdTitle = tcPrTitle.isSetShd() ? tcPrTitle.getShd() : tcPrTitle.addNewShd();
        shdTitle.setFill("2F75B5"); // bleu foncé

// 🔷 Ligne 1 : En-têtes des colonnes Action / Name / Date / Visa (gris clair)
        XWPFTableRow visaHeaderRow = visaTable.getRow(1);
        String[] visaHeaders = {"Action", "Name", "Date", "Visa"};

        for (int i = 0; i < 4; i++) {
            XWPFTableCell cell = visaHeaderRow.getCell(i) != null ? visaHeaderRow.getCell(i) : visaHeaderRow.addNewTableCell();
            cell.removeParagraph(0);
            XWPFParagraph para = cell.addParagraph();
            para.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun runVISA = para.createRun();
            runVISA.setText(visaHeaders[i]);
            runVISA.setItalic(true);
            runVISA.setFontSize(11);
            runVISA.setColor("2F75B5");

            CTTcPr tcPrVIs = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
            CTShd shdVIS = tcPrVIs.isSetShd() ? tcPrVIs.getShd() : tcPrVIs.addNewShd();
            shdVIS.setFill("D9E1F2"); // gris clair
        }

// 🔽 Ajout des données (écriture ligne par ligne)
        for (Visa visa : devis.getVisas()) {
            XWPFTableRow row = visaTable.createRow();
            while (row.getTableCells().size() < 4) {
                row.addNewTableCell();
            }

            XWPFTableCell actionCell = row.getCell(0);
            actionCell.removeParagraph(0);
            XWPFParagraph actionPara = actionCell.addParagraph();
            XWPFRun actionRun = actionPara.createRun();
            actionRun.setText(visa.getAction());
            actionRun.setItalic(true);
            actionRun.setFontSize(11);
            actionRun.setBold(true);
            actionRun.setFontFamily("Verdana");

            row.getCell(1).setText(visa.getName() != null ? visa.getName() : "");
            row.getCell(2).setText(visa.getDate() != null ? visa.getDate().toString() : "");
            row.getCell(3).setText(visa.getVisa() != null ? visa.getVisa() : "");
        }


/// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        document.createParagraph().setPageBreak(true);

// 📌 Proposal Summary
        XWPFTable summaryTable = document.createTable();
        setTableWidth(summaryTable, 9000);
        addBordersToTable(summaryTable);

// 🔷 Ligne 0 : Titre "Proposal Summary" sur 2 colonnes avec fond bleu et texte blanc
        XWPFTableRow titleRowSUM = summaryTable.getRow(0);
        while (titleRowSUM.getTableCells().size() < 2) {
            titleRowSUM.addNewTableCell();
        }
        mergeCellsHorizontally(summaryTable, 0, 0, 1);
        XWPFTableCell titleCellSUM = titleRowSUM.getCell(0);
        titleCellSUM.removeParagraph(0);
        XWPFParagraph titleParaSU = titleCellSUM.addParagraph();
        titleParaSU.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun titleRunSU = titleParaSU.createRun();
        titleRunSU.setText("Proposal Summary");
        titleRunSU.setItalic(true);
        titleRunSU.setFontSize(14);
        titleRunSU.setFontFamily("Arial Rounded MT Bold");
        titleRunSU.setColor("FFFFFF");

// 🟦 Fond bleu foncé
        CTTcPr titleTcPrSUM = titleCellSUM.getCTTc().isSetTcPr() ? titleCellSUM.getCTTc().getTcPr() : titleCellSUM.getCTTc().addNewTcPr();
        CTShd titleShd = titleTcPrSUM.isSetShd() ? titleTcPrSUM.getShd() : titleTcPrSUM.addNewShd();
        titleShd.setFill("2F75B5");

// 🔷 Ligne 1 : En-tête "Summary"
        XWPFTableRow summaryHeader = summaryTable.createRow();
        while (summaryHeader.getTableCells().size() < 2) {
            summaryHeader.addNewTableCell();
        }
        summaryHeader.getCell(0).removeParagraph(0);
        XWPFParagraph summaryPara = summaryHeader.getCell(0).addParagraph();
        XWPFRun summaryRun = summaryPara.createRun();
        summaryRun.setText("Summary");
        summaryRun.setFontFamily("Arial Rounded MT Bold");
        summaryRun.setFontSize(11);
        summaryRun.setItalic(true);
        summaryRun.setColor("2F75B5");

        colorTableHeader(summaryHeader);
        mergeCellsHorizontally(summaryTable, 1, 0, 1);

// 🔷 Données principales
        addSummaryRow(summaryTable, "Customer", devis.getProject().getClient().getCompany());
        addSummaryRow(summaryTable, "Project", devis.getProject().getName());
        addSummaryRow(summaryTable, "Project Type", devis.getProject().getProjectType());
        addSummaryRow(summaryTable, "Proposal validity", devis.getProposalValidity());
        addSummaryRow(summaryTable, "Estimated workload",
                devis.getWorkloadDetails().stream()
                        .mapToInt(w -> w.getTotalEstimatedWorkload() != null ? w.getTotalEstimatedWorkload() : 0)
                        .sum() + " Man/day");
        addSummaryRow(summaryTable, "Possible start date",
                devis.getProject().getStartDate() != null ? devis.getProject().getStartDate().toString() : "-");
        addSummaryRow(summaryTable, "Estimated end date",
                devis.getProject().getEndDate() != null ? devis.getProject().getEndDate().toString() : "-");

// 🔷 Ligne "TELNET Contact"
        XWPFTableRow telnetRow = summaryTable.createRow();
        while (telnetRow.getTableCells().size() < 2) {
            telnetRow.addNewTableCell();
        }
        telnetRow.getCell(0).removeParagraph(0);
        XWPFParagraph telnetPara = telnetRow.getCell(0).addParagraph();
        XWPFRun telnetRunSU = telnetPara.createRun();
        telnetRunSU.setText("TELNET Contact");
        telnetRunSU.setFontFamily("Arial Rounded MT Bold");
        telnetRunSU.setFontSize(11);
        telnetRunSU.setItalic(true);
        telnetRunSU.setColor("2F75B5");
        colorTableHeader(telnetRow);
        mergeCellsHorizontally(summaryTable, summaryTable.getNumberOfRows() - 1, 0, 1);

// 🔷 Détails TELNET
        addSummaryRow(summaryTable, "Technical & Project Management Aspects", "");
        addSummaryRow(summaryTable, "Organisational Aspect", "");
        addSummaryRow(summaryTable, "Commercial Aspect", "");
        addSummaryRow(summaryTable, "Quality Aspect", "");

/// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        document.createParagraph().setPageBreak(true);

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        XWPFParagraph tocPara = document.createParagraph();
        tocPara.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun runSUMM = tocPara.createRun();
        runSUMM.setText("Summary");
        runSUMM.setBold(true);
        runSUMM.setFontSize(16);
        runSUMM.addBreak();
        runSUMM.setColor("2F75B5");

        XWPFRun fieldRun = tocPara.createRun();
        fieldRun.getCTR().addNewFldChar().setFldCharType(STFldCharType.BEGIN);
        fieldRun = tocPara.createRun();
        fieldRun.getCTR().addNewInstrText().setStringValue("TOC \\o \"1-3\" \\h \\z \\u");
        fieldRun = tocPara.createRun();
        fieldRun.getCTR().addNewFldChar().setFldCharType(STFldCharType.SEPARATE);
        fieldRun = tocPara.createRun();
        fieldRun.getCTR().addNewFldChar().setFldCharType(STFldCharType.END);

        document.createParagraph().setPageBreak(true); // saut après le sommaire
/// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// 🔷 Titre
        XWPFParagraph historyTitle = document.createParagraph();
        historyTitle.setStyle("Heading 1");
        XWPFRun titleRun = historyTitle.createRun();
        titleRun.setText("1 History");
        titleRun.setBold(true);
        titleRun.setItalic(true);
        titleRun.setColor("2F75B5");
        titleRun.setFontSize(16);
        titleRun.setFontFamily("Arial Rounded MT Bold");

// 📄 Récupération et regroupement
        List<DevisHistory> historyList = devis.getHistory();
        if (historyList != null && !historyList.isEmpty()) {
            // Groupement par version + description
            Map<String, List<DevisHistory>> grouped = historyList.stream()
                    .collect(Collectors.groupingBy(h -> h.getVersion() + "|" + h.getModificationDescription()));

            for (Map.Entry<String, List<DevisHistory>> entry : grouped.entrySet()) {
                String[] parts = entry.getKey().split("\\|", 2);
                String version = parts.length > 0 ? parts[0] : "-";
                String description = parts.length > 1 ? parts[1] : "-";

                List<DevisHistory> rows = entry.getValue();

                // ➕ Création du tableau (header + n lignes)
                XWPFTable table = document.createTable(rows.size() + 1, 5); // Version + Desc + Action + Date + Name
                setTableWidth(table, 9000);
                addBordersToTable(table);

                // 🔹 En-tête
                String[] headers = {"Version", "Modification description", "Action", "Date", "Name"};
                XWPFTableRow headerHis = table.getRow(0);
                for (int i = 0; i < headers.length; i++) {
                    headerHis.getCell(i).setText(headers[i]);
                    applyHeaderStyle(headerHis.getCell(i)); // Gras + fond bleu

                }

                // 🔹 Remplissage des lignes
                for (int i = 0; i < rows.size(); i++) {
                    XWPFTableRow row = table.getRow(i + 1);
                    while (row.getTableCells().size() < 5) row.addNewTableCell();

                    row.getCell(2).setText(rows.get(i).getAction());
                    row.getCell(3).setText(rows.get(i).getDate() != null ? rows.get(i).getDate().toString() : "-");
                    row.getCell(4).setText(rows.get(i).getName() != null ? rows.get(i).getName() : "-");
                }

                // 🔹 Fusion verticale des colonnes "Version" et "Modification"
                if (rows.size() > 1) {
                    mergeCellsVertically(table, 0, 1, rows.size());
                    mergeCellsVertically(table, 1, 1, rows.size());
                }

                // ➕ Écriture dans la première ligne
                table.getRow(1).getCell(0).setText(version);
                table.getRow(1).getCell(1).setText(description);

                // ✅ ➕ Séparation entre tableaux (visuel clair entre versions)
                XWPFParagraph separatorHi = document.createParagraph();
                separatorHi.setSpacingBefore(200);
                separatorHi.setSpacingAfter(150);
            }
        }




/// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        document.createParagraph().setPageBreak(true); // saut après le sommaire


        // 📌 SECTION 2 - Reference and Applicable Documents
        XWPFParagraph sectionTitle = document.createParagraph();
        sectionTitle.setStyle("Heading 1");
        XWPFRun sectionRun = sectionTitle.createRun();
        sectionRun.setText(" 2 Reference and applicable Documents");
        sectionRun.setBold(true);
        sectionRun.setItalic(true);
        sectionRun.setColor("2F75B5");
        sectionRun.setFontSize(16);
        sectionRun.setFontFamily("Arial Rounded MT Bold");

// 🔹 2.1 Applicable documents
        XWPFParagraph applicableTitle = document.createParagraph();
        applicableTitle.setStyle("Heading2");
        XWPFRun applicableRun = applicableTitle.createRun();
        applicableRun.setText("\t2.1 Applicable documents");
        applicableRun.setItalic(true);
        applicableRun.setColor("2F75B5");
        applicableRun.setFontSize(14);

// 🔹 Table Applicable Documents
        XWPFTable applicableTable = document.createTable();
        setTableWidth(applicableTable, 9000);
        addBordersToTable(applicableTable);
        // En-tête
        XWPFTableRow appHeader = applicableTable.getRow(0);

// Colonne 1 : Title
        XWPFTableCell cell0 = appHeader.getCell(0);
        cell0.removeParagraph(0);
        XWPFParagraph p0Tit = cell0.addParagraph();
        p0Tit.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r0T = p0Tit.createRun();
        r0T.setText("Title");
        r0T.setBold(true);
        r0T.setFontSize(11);
        r0T.setFontFamily("Calibri");
        r0T.setColor("FFFFFF");
        cell0.setColor("2F75B5");

// Colonne 2 : Reference
        XWPFTableCell cell1 = appHeader.addNewTableCell();
        XWPFParagraph p1 = cell1.addParagraph();
        p1.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r1 = p1.createRun();
        r1.setText("Reference");
        r1.setBold(true);
        r1.setFontSize(11);
        r1.setFontFamily("Calibri");
        r1.setColor("FFFFFF");
        cell1.setColor("2F75B5");

// Colonne 3 : Version
        XWPFTableCell cell2 = appHeader.addNewTableCell();
        XWPFParagraph p2 = cell2.addParagraph();
        p2.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r2 = p2.createRun();
        r2.setText("Version");
        r2.setBold(true);
        r2.setFontSize(11);
        r2.setFontFamily("Calibri");
        r2.setColor("FFFFFF");
        cell2.setColor("2F75B5");


// Exemples dynamiques (tu peux remplacer par devis.getApplicableDocuments())
        String[][] applicableDocs = {
                {"A01", "", ""},
                {"A02", "", ""}
        };
        for (String[] rowData : applicableDocs) {
            XWPFTableRow row = applicableTable.createRow();
            for (int i = 0; i < rowData.length; i++) {
                row.getCell(i).setText(rowData[i]);
            }
        }

// 🔹 2.2 Reference documents
        XWPFParagraph referenceTitle = document.createParagraph();
        referenceTitle.setSpacingBefore(200);
        referenceTitle.setStyle("Heading2");
        XWPFRun referenceRun = referenceTitle.createRun();
        referenceRun.setText("\t2.2 Reference documents");
        referenceRun.setItalic(true);
        referenceRun.setColor("2F75B5");
        referenceRun.setFontSize(14);

// 🔹 Table Reference Documents
        XWPFTable referenceTable = document.createTable();
        setTableWidth(referenceTable, 9000);
        addBordersToTable(referenceTable);
// En-tête
        XWPFTableRow refHeader = referenceTable.getRow(0);

// Title
        XWPFTableCell c0 = refHeader.getCell(0);
        c0.removeParagraph(0);
        XWPFParagraph pp0 = c0.addParagraph();
        pp0.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun rr0 = pp0.createRun();
        rr0.setText("Title");
        rr0.setBold(true);
        rr0.setFontSize(11);
        rr0.setFontFamily("Calibri");
        rr0.setColor("FFFFFF");
        c0.setColor("2F75B5");

// Reference
        XWPFTableCell c1 = refHeader.addNewTableCell();
        XWPFParagraph pp1 = c1.addParagraph();
        pp1.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun rr1 = pp1.createRun();
        rr1.setText("Reference");
        rr1.setBold(true);
        rr1.setFontSize(11);
        rr1.setFontFamily("Calibri");
        rr1.setColor("FFFFFF");
        c1.setColor("2F75B5");

// Version
        XWPFTableCell c2 = refHeader.addNewTableCell();
        XWPFParagraph pp2 = c2.addParagraph();
        pp2.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun rr2 = pp2.createRun();
        rr2.setText("Version");
        rr2.setBold(true);
        rr2.setFontSize(11);
        rr2.setFontFamily("Calibri");
        rr2.setColor("FFFFFF");
        c2.setColor("2F75B5");



// Exemple dynamique (à remplacer par devis.getReferenceDocuments())
        referenceTable.createRow(); // ligne vide
        referenceTable.createRow(); // ligne vide


/// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        document.createParagraph().setPageBreak(true); // saut après le sommaire

        // 📌 SECTION 3 - Introduction
        XWPFParagraph introTitle = document.createParagraph();
        introTitle.setStyle("Heading1");
        XWPFRun introRun = introTitle.createRun();
        introRun.setText("3 INTRODUCTION");
        introRun.setBold(true);
        introRun.setItalic(true);
        introRun.setColor("2F75B5");
        introRun.setFontSize(16);
        introRun.setFontFamily("Arial Rounded MT Bold");

// 🔹 3.1 TELNET Presentation
        XWPFParagraph telnetTitle = document.createParagraph();
        telnetTitle.setStyle("Heading2");
        XWPFRun telnetRun = telnetTitle.createRun();
        telnetRun.setText("\t 3.1 TELNET Presentation");
        telnetRun.setItalic(true);
        telnetRun.setColor("2F75B5");
        telnetRun.setFontSize(14);
        telnetRun.setFontFamily("Arial Rounded MT Bold");

        XWPFParagraph GoalTitle = document.createParagraph();
        GoalTitle.setSpacingBefore(200);
        GoalTitle.setStyle("Heading2");
        XWPFRun applicableRunGoal = GoalTitle.createRun();
        applicableRunGoal.setText("\t 3.2 Document Goal");
        applicableRunGoal.setItalic(true);
        applicableRunGoal.setColor("2F75B5");
        applicableRunGoal.setFontSize(14);

        XWPFParagraph scopeTextGoal = document.createParagraph();
        scopeTextGoal.setAlignment(ParagraphAlignment.BOTH);
        XWPFRun scopeTextRunGoal = scopeTextGoal.createRun();
        scopeTextRunGoal.setText("This document describes all the technical and financial arrangements proposed by TELNET for the project \"" +
                devis.getProject().getName() + "\".");
        scopeTextRunGoal.setFontSize(11);

// 🔹 3.2 Project Scope
        XWPFParagraph scopeTitle = document.createParagraph();
        scopeTitle.setStyle("Heading2");
        XWPFRun scopeRun = scopeTitle.createRun();
        scopeRun.setText("\t 3.3 Project Scope");
        scopeRun.setItalic(true);
        scopeRun.setColor("2F75B5");
        scopeRun.setFontSize(14);
        scopeRun.setFontFamily("Arial Rounded MT Bold");
// 🔹 Texte d’introduction
        XWPFParagraph scopeText = document.createParagraph();
        scopeText.setAlignment(ParagraphAlignment.BOTH);
        XWPFRun scopeTextRun = scopeText.createRun();
        scopeTextRun.setText("In the scope of this project, Telnet will provide, during all the defined project period:");
        scopeTextRun.setFontSize(11);

// 🔹 Liste à puce
        XWPFParagraph bullet = document.createParagraph();
        bullet.setStyle("ListBullet"); // Pour Word interprétation liste
        XWPFRun bulletRun = bullet.createRun();
        bulletRun.setText("-----");
        bulletRun.setFontSize(10);
        bulletRun.setFontFamily("Verdana");

        XWPFParagraph scopeCutomer = document.createParagraph();
        scopeCutomer.setStyle("Heading2");
        XWPFRun scopeRunCustomer = scopeCutomer.createRun();
        scopeRunCustomer.setText("\t 3.4 Customer Requirements");
        scopeRunCustomer.setItalic(true);
        scopeRunCustomer.setColor("2F75B5");
        scopeRunCustomer.setFontSize(14);
        scopeRunCustomer.setFontFamily("Arial Rounded MT Bold");


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        document.createParagraph().setPageBreak(true); // saut après le sommaire
        // 📊 Workload Proposal

// 📌 SECTION 3 - Workload
        XWPFParagraph WorkloadTitle = document.createParagraph();
        WorkloadTitle.setStyle("Heading1");
        XWPFRun workloadRun = WorkloadTitle.createRun();
        workloadRun.setText("4 TECHNICAL PROPOSAL");
        workloadRun.setBold(true);
        workloadRun.setItalic(true);
        workloadRun.setColor("2F75B5");
        workloadRun.setFontSize(16);
        workloadRun.setFontFamily("Arial Rounded MT Bold");

        XWPFParagraph WorkloadEstimationTitle = document.createParagraph();
        WorkloadEstimationTitle.setStyle("Heading2");
        XWPFRun WorkloadEstimationRun = WorkloadEstimationTitle.createRun();
        WorkloadEstimationRun.setText("\t 4.1 Workload Estimation and Planning");
        WorkloadEstimationRun.setItalic(true);
        WorkloadEstimationRun.setColor("2F75B5");
        WorkloadEstimationRun.setFontSize(14);

        XWPFParagraph WorkloadDetailsTitle = document.createParagraph();
        WorkloadDetailsTitle.setStyle("Heading3");
        XWPFRun WorkloadDetailsRun = WorkloadDetailsTitle.createRun();
        WorkloadDetailsRun.setText("4.1.1 Workload");
        WorkloadDetailsRun.setItalic(true);
        WorkloadDetailsRun.setColor("2F75B5");
        WorkloadDetailsRun.setFontSize(12);

        XWPFParagraph detailsParaWorWO = document.createParagraph();
        detailsParaWorWO.setAlignment(ParagraphAlignment.BOTH);
        detailsParaWorWO.setSpacingBefore(200);
        XWPFRun detailsDescWorKe = detailsParaWorWO.createRun();
        detailsDescWorKe.setFontSize(10);
        detailsDescWorKe.setText("The table below describes the estimated workload per month:");
        detailsDescWorKe.setFontFamily("Verdana");
        detailsDescWorKe.setItalic(true);

// 🧾 Création du tableau avec 7 colonnes (N° + 6 colonnes de données)
        XWPFTable workloadTable = document.createTable(1, 7);
        setTableWidth(workloadTable, 9000);
        addBordersToTable(workloadTable);

// ✅ Ligne d'en-tête
        XWPFTableRow wHeader = workloadTable.getRow(0);
        String[] titlesWORK = {
                "N°", "Period", "Estimated workload per Resource (Man/day)", "Public Holidays",
                "Number of Resources", "Total Estimated Workload (Man/day)", "Note"
        };

        for (int i = 0; i < titlesWORK.length; i++) {
            XWPFTableCell cell = wHeader.getCell(i);
            cell.removeParagraph(0); // Nettoyage
            cell.setColor("4472C4"); // fond bleu foncé
            cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
            XWPFParagraph para = cell.addParagraph();
            para.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun runWORKL = para.createRun();
            runWORKL.setText(titlesWORK[i]);
            runWORKL.setColor("FFFFFF"); // texte blanc
            runWORKL.setBold(true);
            runWORKL.setFontSize(10);
            runWORKL.setFontFamily("Calibri");
        }


        int index = 1;
        for (WorkloadDetail wl : devis.getWorkloadDetails()) {
            XWPFTableRow row = workloadTable.createRow();

            while (row.getTableCells().size() < 7) row.addNewTableCell();

            row.getCell(0).setText(String.format("%02d", index++));
            row.getCell(0).getParagraphs().get(0).setAlignment(ParagraphAlignment.CENTER);
            row.getCell(1).setText(wl.getPeriod() != null ? wl.getPeriod() : "-");
            row.getCell(1).getParagraphs().get(0).setAlignment(ParagraphAlignment.LEFT);
            row.getCell(2).setText(wl.getEstimatedWorkload() != null ? wl.getEstimatedWorkload().toString() : "0");
            row.getCell(2).getParagraphs().get(0).setAlignment(ParagraphAlignment.CENTER);
            row.getCell(3).setText(wl.getPublicHolidays() != null ? wl.getPublicHolidays().toString() : "0");
            row.getCell(3).getParagraphs().get(0).setAlignment(ParagraphAlignment.CENTER);
            row.getCell(4).setText(wl.getNumberOfResources() != null ? wl.getNumberOfResources().toString() : "0");
            row.getCell(4).getParagraphs().get(0).setAlignment(ParagraphAlignment.CENTER);
            row.getCell(5).setText(wl.getTotalEstimatedWorkload() != null ? wl.getTotalEstimatedWorkload().toString() : "0");
            row.getCell(5).getParagraphs().get(0).setAlignment(ParagraphAlignment.CENTER);
            row.getCell(6).setText(wl.getNote() != null ? wl.getNote() : "-");
            row.getCell(6).getParagraphs().get(0).setAlignment(ParagraphAlignment.LEFT);
        }


        int totalWorkload = devis.getWorkloadDetails().stream()
                .mapToInt(w -> w.getTotalEstimatedWorkload() != null ? w.getTotalEstimatedWorkload() : 0)
                .sum();

        XWPFTableRow totalRow = workloadTable.createRow();
        while (totalRow.getTableCells().size() < 7) totalRow.addNewTableCell();
        mergeCellsHorizontally(workloadTable, workloadTable.getNumberOfRows() - 1, 0, 6);

        XWPFTableCell totalCell = totalRow.getCell(0);
        totalCell.removeParagraph(0);
        XWPFParagraph para = totalCell.addParagraph();
        para.setAlignment(ParagraphAlignment.CENTER); // Center align the total row text
        XWPFRun totalRun = para.createRun();
        totalRun.setText(" Total 3 FTE : " + totalWorkload + " Man/day ");
        totalRun.setBold(true);
        totalRun.setFontSize(12);
        colorTableRow(totalRow, "BDD7EE"); // fond bleu clair


        XWPFParagraph tableLegendWork = document.createParagraph();
        tableLegendWork.setAlignment(ParagraphAlignment.CENTER);

        XWPFParagraph tableTitleWorkl = document.createParagraph();
        tableTitleWorkl.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRunWORKL = tableTitleWorkl.createRun();
        titleRunWORKL.setBold(true);
        titleRunWORKL.setFontSize(10);
        titleRunWORKL.setFontFamily("Arial");
        titleRunWORKL.setText("Table 1 : ");
        XWPFRun titleTextWORKL = tableTitleWorkl.createRun();
        titleTextWORKL.setText("Workload Detail");
        titleTextWORKL.setFontSize(10);
        titleTextWORKL.setFontFamily("Verdana");

        /// ////////////////////////////////////////////////////////////////////////////////////
        // 📌 SECTION 3 - Introduction
        XWPFParagraph introDELI = document.createParagraph();
        introDELI.setStyle("Heading3");
        XWPFRun introRunWORK = introDELI.createRun();
        introRunWORK.setText("4.1.2 Deliveries and Milestones");
        introRunWORK.setItalic(true);
        introRunWORK.setColor("2F75B5");
        introRunWORK.setFontSize(12);
        introRunWORK.setFontFamily("Arial Rounded MT Bold");


        // 📦 Créer la table
        XWPFTable table = document.createTable(4, 3); // 1 ligne d'en-tête + 3 lignes vides
        setTableWidth(table, 9000);
        addBordersToTable(table);

// 🟦 Entêtes
        String[] headers = {"Type", "Description", "Milestone"};
        XWPFTableRow headerRow = table.getRow(0);
        for (int i = 0; i < 3; i++) {
            XWPFTableCell cell = headerRow.getCell(i);
            XWPFParagraph p = cell.getParagraphs().get(0);
            p.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun r = p.createRun();
            r.setBold(true);
            r.setFontSize(10);
            r.setFontFamily("Arial");
            r.setColor("FFFFFF");
            r.setText(headers[i]);

            // ✅ Fond bleu clair
            CTTcPr tcPrDELI = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
            CTShd shdDELI = tcPrDELI.isSetShd() ? tcPrDELI.getShd() : tcPrDELI.addNewShd();
            shdDELI.setFill("2F75B5");
        }

// 🖋️ Texte en bas "Table 2 : Deliveries Detail"
        XWPFParagraph tableTitle = document.createParagraph();
        tableTitle.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRunDELI = tableTitle.createRun();
        titleRunDELI.setBold(true);
        titleRunDELI.setFontSize(10);
        titleRunDELI.setFontFamily("Arial");
        titleRunDELI.setText("Table 2 : ");
        XWPFRun titleText = tableTitle.createRun();
        titleText.setText("Deliveries Detail");
        titleText.setFontSize(10);
        titleText.setFontFamily("Arial");

        XWPFParagraph PlanTitle = document.createParagraph();
        PlanTitle.setStyle("Heading3");
        XWPFRun PlanRun = PlanTitle.createRun();
        PlanRun.setText("4.1.3 Planning");
        PlanRun.setItalic(true);
        PlanRun.setColor("2F75B5");
        PlanRun.setFontSize(12);
        PlanRun.setFontFamily("Arial Rounded MT Bold");

        XWPFParagraph RepoTitle = document.createParagraph();
        RepoTitle.setStyle("Heading3");
        XWPFRun RepoRun = RepoTitle.createRun();
        RepoRun.setText("4.1.4 Reporting");
        RepoRun.setItalic(true);
        RepoRun.setColor("2F75B5");
        RepoRun.setFontSize(12);
        RepoRun.setFontFamily("Arial Rounded MT Bold");
        document.createParagraph().setPageBreak(true);
// 4.1.4.1 Weekly Project Status Report (PSR)
        XWPFParagraph psrTitle = document.createParagraph();
        psrTitle.setStyle("Heading4");
        XWPFRun psrRun = psrTitle.createRun();
        psrRun.setText("4.1.4.1 Weekly Project Status Report (PSR)");
        psrRun.setItalic(true);
        psrRun.setColor("2F75B5");
        psrRun.setFontSize(10);
        psrRun.setFontFamily("Arial Rounded MT Bold");
        XWPFParagraph psrText = document.createParagraph();
        psrText.setAlignment(ParagraphAlignment.BOTH);
        XWPFRun psrContent = psrText.createRun();
        psrContent.setFontSize(11);
        psrContent.setText("A weekly project status report will be sent to Customer name for project tracking and monitoring. "
                + "The PSR will include an overall status of the project (Progress, action plan, risks …) on a weekly basis, "
                + "as well as the workload of the performed tasks during the specific week.");
        psrContent.setFontFamily("Verdana");
// 4.1.4.2 Monthly Dashboard
        XWPFParagraph dashTitle = document.createParagraph();
        dashTitle.setStyle("Heading4");
        XWPFRun dashRun = dashTitle.createRun();
        dashRun.setText("4.1.4.2 Monthly Dashboard");
        dashRun.setItalic(true);
        dashRun.setColor("2F75B5");
        dashRun.setFontSize(10);
        dashRun.setFontFamily("Arial Rounded MT Bold");
        XWPFParagraph dashText = document.createParagraph();
        dashText.setAlignment(ParagraphAlignment.BOTH);
        XWPFRun dashContent = dashText.createRun();
        dashContent.setFontSize(11);
        dashContent.setText("A monthly Dashboard will be sent to Customer name for tracking the effective working days per resource. "
                + "The Monthly dashboard is used to monitor the effective working days versus the initial planned budget.");
        dashContent.setFontFamily("Verdana");

// 📌 SECTION 4 - Financial Proposal
        document.createParagraph().setPageBreak(true); // saut après le sommaire
        XWPFParagraph finanTitle = document.createParagraph();
        finanTitle.setStyle("Heading1");
        XWPFRun finanRun = finanTitle.createRun();
        finanRun.setText("5\nFINANCIAL PROPOSAL");
        finanRun.setBold(true);
        finanRun.setItalic(true);
        finanRun.setColor("2F75B5");
        finanRun.setFontSize(20);

        XWPFParagraph DetailsTitle = document.createParagraph();
        DetailsTitle.setStyle("Heading2");
        XWPFRun DetailsRun = DetailsTitle.createRun();
        DetailsRun.setText("\t 5.1\tDetails");
        DetailsRun.setItalic(true);
        DetailsRun.setColor("2F75B5");
        DetailsRun.setFontSize(15);

        XWPFParagraph detailsPara = document.createParagraph();
        detailsPara.setAlignment(ParagraphAlignment.BOTH);
        detailsPara.setSpacingBefore(200);
        XWPFRun detailsDesc = detailsPara.createRun();
        detailsDesc.setFontSize(11);
        detailsDesc.setText("The table below describes the project development and management workload costs detailed by resource types and project phases.");


        // 📌 1. Extraire les positions (ex: Project manager, Developer)
        List<FinancialDetail> details = devis.getFinancialDetails();
        List<String> positions = details.stream()
                .map(FinancialDetail::getPosition)
                .distinct()
                .collect(Collectors.toList());

        XWPFTable financeTable = document.createTable(1, positions.size() + 1);
        setTableWidth(financeTable, 9000);
        addBordersToTable(financeTable);

// 📌 2. En-tête du tableau : Position | PM | Dev | ...
        XWPFTableRow headerRowFina = financeTable.getRow(0);

// 🔷 Cellule 0 : "Position" statique
        XWPFTableCell firstCell = headerRowFina.getCell(0);
        firstCell.removeParagraph(0);
        XWPFParagraph p0 = firstCell.addParagraph();
        p0.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r0 = p0.createRun();
        r0.setText("Position");
        r0.setBold(true);
        r0.setFontSize(11);
        r0.setFontFamily("Calibri");
        r0.setColor("FFFFFF");

// Appliquer fond bleu
        CTTcPr pr0 = firstCell.getCTTc().isSetTcPr() ? firstCell.getCTTc().getTcPr() : firstCell.getCTTc().addNewTcPr();
        CTShd shd0 = pr0.isSetShd() ? pr0.getShd() : pr0.addNewShd();
        shd0.setFill("2F75B5");

// 🔄 Colonnes dynamiques (Project Manager, Developer...)
        for (int i = 0; i < positions.size(); i++) {
            XWPFTableCell cell = headerRowFina.getCell(i + 1);
            cell.removeParagraph(0);
            XWPFParagraph paraFinanc = cell.addParagraph();
            paraFinanc.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun runFIn = paraFinanc.createRun();
            runFIn.setText(positions.get(i)); // nom du rôle dynamique
            runFIn.setBold(true);
            runFIn.setFontSize(11);
            runFIn.setFontFamily("Calibri");
            runFIn.setColor("FFFFFF");

            CTTcPr tcPrFi = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
            CTShd shdFIN = tcPrFi.isSetShd() ? tcPrFi.getShd() : tcPrFi.addNewShd();
            shdFIN.setFill("2F75B5");
        }
// 📌 3. Lignes : workload, daily cost, total cost
        String[] labels = {
                "Total Workload (Man/Day) / Type",
                "Daily cost (Euro) / Type",
                "Total cost (Euro) / Type"
        };

        // ... existing code ...
        for (String label : labels) {
            XWPFTableRow row = financeTable.createRow();
            row.getCell(0).setText(label);

            for (int i = 0; i < positions.size(); i++) {
                String position = positions.get(i);
                FinancialDetail fd = details.stream()
                        .filter(d -> d.getPosition().equals(position))
                        .findFirst().orElse(null);

                String value = "-";
                if (fd != null) {
                    switch (label) {
                        case "Total Workload (Man/Day) / Type":
                            value = String.valueOf(fd.getWorkload());
                            if (!value.equals("-")) value += " Man/Day";
                            break;
                        case "Daily cost (Euro) / Type":
                            BigDecimal dailyCost = fd.getDailyCost();
                            if (dailyCost != null) {
                                if (dailyCost.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                                    value = dailyCost.toBigInteger().toString();
                                } else {
                                    value = dailyCost.toPlainString();
                                }
                            } else {
                                value = "-";
                            }
                            if (!value.equals("-")) value += " €"; // Unit added here previously
                            break;
                        case "Total cost (Euro) / Type":
                            BigDecimal totalCost = fd.getTotalCost();
                            if (totalCost != null) {
                                if (totalCost.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                                    value = totalCost.toBigInteger().toString();
                                } else {
                                    value = totalCost.toPlainString();
                                }
                            } else {
                                value = "-";
                            }
                            if (!value.equals("-")) value += " €"; // Unit added here previously
                            break;
                    }
                }

                row.getCell(i + 1).setText(value);
            }
        }

// 📌 4. Dernière ligne : total
        BigDecimal total = details.stream()
                .map(FinancialDetail::getTotalCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        XWPFTableRow totalRowFI = financeTable.createRow();
        while (totalRowFI.getTableCells().size() < positions.size() + 1) totalRowFI.addNewTableCell();
        mergeCellsHorizontally(financeTable, financeTable.getNumberOfRows() - 1, 0, positions.size());

        XWPFTableCell totalCellFI = totalRowFI.getCell(0);
        totalCellFI.removeParagraph(0);
        XWPFParagraph paraWorklo = totalCellFI.addParagraph();
        paraWorklo.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r = paraWorklo.createRun();

        String totalText;
        if (total.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            totalText = total.toBigInteger().toString();
        } else {
            totalText = total.toPlainString();
        }

        r.setText("Total (Euros) :  " + totalText + " €");
        r.setBold(true);
        r.setFontSize(12);
        colorTableRow(totalRowFI, "BDD7EE");

        XWPFParagraph tableTitleFINAN = document.createParagraph();
        tableTitleFINAN.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRunFINANC = tableTitleFINAN.createRun();
        titleRunFINANC.setBold(true);
        titleRunFINANC.setFontSize(10);
        titleRunFINANC.setFontFamily("Arial");
        titleRunFINANC.setText("Table 3 : ");
        XWPFRun titleTextFINAN = tableTitleFINAN.createRun();
        titleTextFINAN.setText("Financial Estimation");
        titleTextFINAN.setFontSize(10);
        titleTextFINAN.setFontFamily("Verdana");


        XWPFParagraph SpecificTitle = document.createParagraph();
        SpecificTitle.setStyle("Heading2");
        XWPFRun SpecificRun = SpecificTitle.createRun();
        SpecificRun.setText("\t 5.2 \tSpecific Arrangements");
        SpecificRun.setItalic(true);
        SpecificRun.setColor("2F75B5");
        SpecificRun.setFontSize(15);


        XWPFParagraph introDIS = document.createParagraph();
        introDIS.setStyle("Heading3");
        XWPFRun introRunDISC = introDIS.createRun();
        introRunDISC.setText("5.2.1 Discount – Credit Note");
        introRunDISC.setItalic(true);
        introRunDISC.setColor("2F75B5");
        introRunDISC.setFontSize(12);
        introRunDISC.setFontFamily("Arial Rounded MT Bold");


        XWPFParagraph introTRA = document.createParagraph();
        introTRA.setStyle("Heading3");
        XWPFRun introRunTRAV = introTRA.createRun();
        introRunTRAV.setText("5.2.2 Travel Expenses");
        introRunTRAV.setItalic(true);
        introRunTRAV.setColor("2F75B5");
        introRunTRAV.setFontSize(12);
        introRunTRAV.setFontFamily("Arial Rounded MT Bold");

        XWPFParagraph introEVOL = document.createParagraph();
        introEVOL.setStyle("Heading3");
        XWPFRun introRunEVOL = introEVOL.createRun();
        introRunEVOL.setText("5.2.3 Evolutions");
        introRunEVOL.setItalic(true);
        introRunEVOL.setColor("2F75B5");
        introRunEVOL.setFontSize(12);
        introRunEVOL.setFontFamily("Arial Rounded MT Bold");

        XWPFParagraph introOTH = document.createParagraph();
        introOTH.setStyle("Heading3");
        XWPFRun introRunOTHE = introOTH.createRun();
        introRunOTHE.setText("5.2.4 Other Expenses");
        introRunOTHE.setItalic(true);
        introRunOTHE.setColor("2F75B5");
        introRunOTHE.setFontSize(12);
        introRunOTHE.setFontFamily("Arial Rounded MT Bold");


        XWPFParagraph introProposalV = document.createParagraph();
        introProposalV.setStyle("Heading3");
        XWPFRun introRunProposalV = introProposalV.createRun();
        introRunProposalV.setText("5.2.5 Proposal Validity");
        introRunProposalV.setItalic(true);
        introRunProposalV.setColor("2F75B5");
        introRunProposalV.setFontSize(12);
        introRunProposalV.setFontFamily("Arial Rounded MT Bold");


        XWPFParagraph paragraphPROP = document.createParagraph();
        paragraphPROP.setSpacingBefore(200); // espace avant
        paragraphPROP.setSpacingAfter(200);  // espace après
        paragraphPROP.setAlignment(ParagraphAlignment.LEFT); // ou CENTER
        paragraphPROP.setAlignment(ParagraphAlignment.BOTH);
        XWPFRun runPROP = paragraphPROP.createRun();
        runPROP.setText("This proposal is valid for the duration of one month.\n");
        runPROP.setFontSize(12);
        runPROP.setFontFamily("Calibri");
        document.createParagraph().setPageBreak(true);
// 📌 SECTION 5.3 - Invoicing
        XWPFParagraph InvoicingTitle = document.createParagraph();
        InvoicingTitle.setStyle("Heading2");
        XWPFRun InvoicingRun = InvoicingTitle.createRun();
        InvoicingRun.setText("\t 5.3 Invoicing");
        InvoicingRun.setItalic(true);
        InvoicingRun.setColor("2F75B5");
        InvoicingRun.setFontSize(15);

        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingBefore(200); // espace avant
        paragraph.setSpacingAfter(200);  // espace après
        paragraphPROP.setAlignment(ParagraphAlignment.BOTH);
        XWPFRun runINV = paragraph.createRun();
        runINV.setText("Dates and details of project invoicing are as follows:");
        runINV.setFontSize(10);
        runINV.setFontFamily("Verdana");


        XWPFTable invoiceTable = document.createTable();
        setTableWidth(invoiceTable, 9000);

// Créer l'en-tête manuellement avec des cellules vides
        XWPFTableRow headerInvoi = invoiceTable.getRow(0);
        while (headerInvoi.getTableCells().size() < 4) headerInvoi.addNewTableCell();

// Liste des titres
        String[] titles = {"N°", "Description", "Invoicing Date", "Amount"};

// Écriture stylisée en blanc dans chaque cellule
        for (int i = 0; i < 4; i++) {
            XWPFTableCell cell = headerInvoi.getCell(i);

            // 🧹 Supprimer les anciens paragraphes
            while (cell.getParagraphs().size() > 0) cell.removeParagraph(0);

            // 🎨 Ajout d’un paragraphe stylisé
            cell.setColor("4472C4"); // fond bleu foncé
            XWPFParagraph paraInvo = cell.addParagraph();
            paraInvo.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run1 = paraInvo.createRun();
            run1.setText(titles[i]);
            run1.setBold(true);
            run1.setColor("FFFFFF");
            run1.setFontSize(11);
            run1.setFontFamily("Calibri");
        }


        int indexINV = 1;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (InvoicingDetail inv : devis.getInvoicingDetails()) {
            XWPFTableRow row = invoiceTable.createRow();
            row.getCell(0).setText(String.format("%02d", indexINV++));
            row.getCell(1).setText("Service fees for " + inv.getDescription());
            row.getCell(2).setText(inv.getInvoicingDate().toString());
            // Format Amount
            BigDecimal amount = inv.getAmount();
            String amountText;
            if (amount != null) {
                if (amount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                    amountText = amount.toBigInteger().toString();
                } else {
                    amountText = amount.toPlainString();
                }
            } else {
                amountText = "-";
            }
            row.getCell(3).setText(amountText + " €"); // Added € here

            if (amount != null) {
                totalAmount = totalAmount.add(amount);
            }
        }

        XWPFTableRow totalRowInvoi = invoiceTable.createRow();
        while (totalRowInvoi.getTableCells().size() < 4) totalRowInvoi.addNewTableCell();
        mergeCellsHorizontally(invoiceTable, invoiceTable.getNumberOfRows() - 1, 0, 2);

        XWPFTableCell totalInvCell = totalRowInvoi.getCell(0);
        XWPFRun invRun = totalInvCell.getParagraphs().get(0).createRun();
        invRun.setBold(true);
        invRun.setFontFamily("Calibri");
        invRun.setFontSize(12);
        // Format Total Amount
        String totalAmountText;
        if (totalAmount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            totalAmountText = totalAmount.toBigInteger().toString();
        } else {
            totalAmountText = totalAmount.toPlainString();
        }
        invRun.setText("Total Cost : " + totalAmountText + " €"); // Added € here
        colorTableRow(totalRowInvoi, "BDD7EE");
        colorTableRow(totalRowInvoi, "BDD7EE");

        XWPFParagraph tableTitleInvoi = document.createParagraph();
        tableTitleInvoi.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRunInvoi = tableTitleInvoi.createRun();
        titleRunInvoi.setBold(true);
        titleRunInvoi.setFontSize(10);
        titleRunInvoi.setFontFamily("Arial");
        titleRunInvoi.setText("Table 4 : ");
        XWPFRun titleTextInvoi = tableTitleInvoi.createRun();
        titleTextInvoi.setText("Invoicing Details");
        titleTextInvoi.setFontSize(10);
        titleTextInvoi.setFontFamily("Verdana");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.write(out);
            document.close();
            return out;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la génération du Word", e);
        }
    }

    private void colorTableHeaderOK(XWPFTableRow row) {
        for (XWPFTableCell cell : row.getTableCells()) {
            cell.setColor("4472C4"); // bleu foncé
            XWPFParagraph para = cell.getParagraphs().get(0);
            para.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run = para.createRun();
            run.setBold(true);
            run.setColor("FFFFFF");
            run.setFontSize(11);
        }
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
    private void addSummaryRow(XWPFTable table, String label, String value) {
        XWPFTableRow row = table.createRow();
        while (row.getTableCells().size() < 2) {
            row.addNewTableCell();
        }
        row.getCell(0).setText(label != null ? label : "-");
        row.getCell(1).setText(value != null ? value : "-");
    }
    private void mergeCellsHorizontally(XWPFTable table, int row, int fromCol, int toCol) {
        for (int colIndex = fromCol; colIndex <= toCol; colIndex++) {
            XWPFTableCell cell = table.getRow(row).getCell(colIndex);
            CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
            CTHMerge hMerge = tcPr.isSetHMerge() ? tcPr.getHMerge() : tcPr.addNewHMerge();
            hMerge.setVal(colIndex == fromCol ? STMerge.RESTART : STMerge.CONTINUE);
        }
    }
    private void mergeCellsVertically(XWPFTable table, int col, int fromRow, int toRow) {
        for (int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            XWPFTableCell cell = table.getRow(rowIndex).getCell(col);
            CTVMerge vmerge = CTVMerge.Factory.newInstance();
            vmerge.setVal(rowIndex == fromRow ? STMerge.RESTART : STMerge.CONTINUE);
            CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
            tcPr.setVMerge(vmerge);
        }
    }
    public static void applyHeaderStyle(XWPFTableCell cell) {
        XWPFParagraph p = cell.getParagraphs().get(0);
        p.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun r = p.createRun();
        r.setColor("FFFFFF");
        r.setFontSize(10);
        r.setFontFamily("Arial Rounded MT Bold");
        r.setText(p.getText());
        r.setItalic(true);
        p.removeRun(0); // supprime l'ancien run

        CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        CTShd shd = tcPr.isSetShd() ? tcPr.getShd() : tcPr.addNewShd();
        shd.setFill("2F75B5"); // bleu
    }
    private void addBordersToTable(XWPFTable table) {
        CTTblPr tblPr = table.getCTTbl().getTblPr() == null ? table.getCTTbl().addNewTblPr() : table.getCTTbl().getTblPr();
        CTTblBorders borders = tblPr.isSetTblBorders() ? tblPr.getTblBorders() : tblPr.addNewTblBorders();

        CTBorder border = borders.addNewTop();
        border.setVal(STBorder.SINGLE);
        border.setSz(BigInteger.valueOf(4));
        border.setColor("000000");

        border = borders.addNewBottom();
        border.setVal(STBorder.SINGLE);
        border.setSz(BigInteger.valueOf(4));
        border.setColor("000000");

        border = borders.addNewLeft();
        border.setVal(STBorder.SINGLE);
        border.setSz(BigInteger.valueOf(4));
        border.setColor("000000");

        border = borders.addNewRight();
        border.setVal(STBorder.SINGLE);
        border.setSz(BigInteger.valueOf(4));
        border.setColor("000000");

        border = borders.addNewInsideH();
        border.setVal(STBorder.SINGLE);
        border.setSz(BigInteger.valueOf(4));
        border.setColor("000000");

        border = borders.addNewInsideV();
        border.setVal(STBorder.SINGLE);
        border.setSz(BigInteger.valueOf(4));
        border.setColor("000000");
    }
    private void colorTableRow(XWPFTableRow row, String colorHex) {
        for (XWPFTableCell cell : row.getTableCells()) {
            CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
            CTShd ctshd = tcPr.isSetShd() ? tcPr.getShd() : tcPr.addNewShd();
            ctshd.setFill(colorHex); // ex: "BDD7EE"
        }
    }

}
