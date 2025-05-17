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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            imagePara.setSpacingAfter(3500); // 🟦 Espace entre image et titre
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
        titleCoverRun.setText("Technical and Financial Proposal\nProject :");
        titleCoverRun.setColor("00B0F0");
        titleCoverRun.setFontSize(18);
        titleCoverRun.setBold(true);
        titleCoverRun.setItalic(true);

// 📌 Référence
        XWPFParagraph refPara = document.createParagraph();
        refPara.setAlignment(ParagraphAlignment.CENTER);
        refPara.setSpacingAfter(3500); // 🟦 espace entre référence et abstract
        XWPFRun refRun = refPara.createRun();
        refRun.setText("DOCUMENT REFERENCE :\n" + devis.getReference());
        refRun.addBreak();
        refRun.setText("Edition :\n" + devis.getEdition());
        refRun.setItalic(true);

        XWPFParagraph abstractTitle = document.createParagraph();
        abstractTitle.setSpacingBefore(200);
        XWPFRun abstractRun = abstractTitle.createRun();
        abstractRun.setText("ABSTRACT");
        abstractRun.setBold(true);
        abstractRun.setItalic(true);
        abstractRun.setColor("00B0F0");
        abstractRun.setFontSize(12);

// Table pour encadrer le texte
        XWPFTable abstractTable = document.createTable(1, 1);
        setTableWidth(abstractTable, 9000);
        XWPFTableCell abstractCell = abstractTable.getRow(0).getCell(0);

        XWPFParagraph abstractTextPara = abstractCell.getParagraphs().get(0);
        XWPFRun abstractTextRun = abstractTextPara.createRun();
        abstractTextRun.setFontSize(11);
        abstractTextRun.setText("This document is a .... for the project \"" +
                devis.getProject().getName() + "\" during the period \"" +
                devis.getCreationDate() + "\". It covers ....\".");

// Bordure correcte
        CTTcPr tcPr = abstractCell.getCTTc().isSetTcPr() ? abstractCell.getCTTc().getTcPr() : abstractCell.getCTTc().addNewTcPr();
        CTTcBorders borders = tcPr.isSetTcBorders() ? tcPr.getTcBorders() : tcPr.addNewTcBorders();
        borders.addNewTop().setVal(STBorder.SINGLE);
        borders.addNewBottom().setVal(STBorder.SINGLE);
        borders.addNewLeft().setVal(STBorder.SINGLE);
        borders.addNewRight().setVal(STBorder.SINGLE);



/// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        document.createParagraph().setPageBreak(true);
        addHeaderTable(document, devis); // ⬅️ Ajout de ton header personnalisé

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

/// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        document.createParagraph().setPageBreak(true);
        addHeaderTable(document, devis); // ⬅️ Ajout de ton header personnalisé

// 📌 Proposal Summary
        XWPFParagraph proposalTitle = document.createParagraph();
        proposalTitle.setSpacingBefore(400);
        XWPFRun proposalRun = proposalTitle.createRun();
        proposalRun.setText("Proposal Summary");
        proposalRun.setBold(true);
        proposalRun.setItalic(true);
        proposalRun.setFontSize(13);
        proposalRun.setColor("2F75B5");

// 🔹 Créer le tableau principal
        XWPFTable summaryTable = document.createTable();
        setTableWidth(summaryTable, 9000);

// 🔹 Ligne d'entête "Summary"
        XWPFTableRow summaryHeader = summaryTable.getRow(0);
        summaryHeader.getCell(0).setText("Summary");
        while (summaryHeader.getTableCells().size() < 2) {
            summaryHeader.addNewTableCell();
            summaryHeader.getCell(0).getParagraphs().get(0).createRun().setItalic(true);
            colorTableHeader(summaryHeader);
        }
        mergeCellsHorizontally(summaryTable, 0, 0, 1); // fusionner les deux cellules

// 🔹 Données ligne par ligne
        addSummaryRow(summaryTable, "Customer", devis.getProject().getClient().getSalesManagers() != null
                ? String.join(", ", devis.getProject().getClient().getSalesManagers())
                : "-" );
        addSummaryRow(summaryTable, "Project", devis.getProject().getName());
        addSummaryRow(summaryTable, "Project Type", devis.getProject().getProjectType());
        addSummaryRow(summaryTable, "Proposal validity", devis.getProposalValidity());
        addSummaryRow(summaryTable, "Estimated workload", devis.getWorkloadDetails().stream()
                .mapToInt(w -> w.getTotalEstimatedWorkload() != null ? w.getTotalEstimatedWorkload() : 0).sum() + " Man/day");
        addSummaryRow(summaryTable, "Possible start date", devis.getProject().getStartDate() != null ? devis.getProject().getStartDate().toString() : "-");
        addSummaryRow(summaryTable, "Estimated end date", devis.getProject().getEndDate() != null ? devis.getProject().getEndDate().toString() : "-");

// 🔹 Titre "TELNET Contact"
        XWPFTableRow telnetHeader = summaryTable.createRow();
        while (telnetHeader.getTableCells().size() < 2) telnetHeader.addNewTableCell();
        telnetHeader.getCell(0).setText("TELNET Contact");
        mergeCellsHorizontally(summaryTable, summaryTable.getNumberOfRows() - 1, 0, 1);
        telnetHeader.getCell(0).getParagraphs().get(0).createRun().setItalic(true);
        colorTableHeader(telnetHeader);

// 🔹 Lignes contact TELNET
        addSummaryRow(summaryTable, "Technical & Project Management Aspects", "");
        addSummaryRow(summaryTable, "Organisational Aspect", "");
        addSummaryRow(summaryTable, "Commercial Aspect", "");
        addSummaryRow(summaryTable, "Quality Aspect", "");

/// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        document.createParagraph().setPageBreak(true);
        addHeaderTable(document, devis); // ⬅️ Ajout de ton header personnalisé

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        addTableOfContents(document);
        document.createParagraph().setPageBreak(true); // saut après le sommaire
/// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        addHeaderTable(document, devis); // ⬅️ Ajout de ton header personnalisé

        XWPFParagraph historyTitle = document.createParagraph();
        historyTitle.setStyle("Heading1");
        XWPFRun titleRun = historyTitle.createRun();
        titleRun.setText("1 History");
        titleRun.setBold(true);
        titleRun.setItalic(true);
        titleRun.setColor("2F75B5");
        titleRun.setFontSize(20);

        List<DevisHistory> historyList = devis.getHistory();
        if (historyList != null && !historyList.isEmpty()) {
            int totalRows = historyList.size();
            XWPFTable table = document.createTable(totalRows + 1, 5); // +1 for header
            setTableWidth(table, 9000);

            // En-tête
            String[] headers = {"Version", "Modification description", "Action", "Date", "Name"};
            XWPFTableRow head = table.getRow(0);
            for (int i = 0; i < headers.length; i++) {
                XWPFTableCell cell = head.getCell(i);
                cell.setText(headers[i]);
                applyHeaderStyle(cell);
            }

            // Regrouper par version/description (pour fusion)
            Map<String, List<DevisHistory>> grouped = historyList.stream()
                    .collect(Collectors.groupingBy(h -> h.getVersion() + "|" + h.getModificationDescription()));

            int rowIndex = 1;
            for (Map.Entry<String, List<DevisHistory>> entry : grouped.entrySet()) {
                String[] parts = entry.getKey().split("\\|");
                String version = parts[0];
                String description = parts[1];
                List<DevisHistory> groupRows = entry.getValue();
                int groupSize = groupRows.size();

                // Première ligne avec version + description
                XWPFTableRow firstRow = table.getRow(rowIndex);
                firstRow.getCell(0).setText(version);
                firstRow.getCell(1).setText(description);
                firstRow.getCell(2).setText(groupRows.get(0).getAction());
                firstRow.getCell(3).setText(groupRows.get(0).getDate() != null ? groupRows.get(0).getDate().toString() : "");
                firstRow.getCell(4).setText(groupRows.get(0).getName());

                for (int i = 1; i < groupSize; i++) {
                    rowIndex++;
                    XWPFTableRow row = table.getRow(rowIndex);
                    row.getCell(2).setText(groupRows.get(i).getAction());
                    row.getCell(3).setText(groupRows.get(i).getDate() != null ? groupRows.get(i).getDate().toString() : "");
                    row.getCell(4).setText(groupRows.get(i).getName());
                }

                // Fusion verticale version + description
                if (groupSize > 1) {
                    mergeCellsVertically(table, 0, rowIndex - groupSize + 1, rowIndex);
                    mergeCellsVertically(table, 1, rowIndex - groupSize + 1, rowIndex);
                }

                rowIndex++;
            }
        }

/// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        document.createParagraph().setPageBreak(true); // saut après le sommaire
        addHeaderTable(document, devis); // ⬅️ Ajout de ton header personnalisé

        // 📌 SECTION 2 - Reference and Applicable Documents
        XWPFParagraph sectionTitle = document.createParagraph();
        sectionTitle.setStyle("Heading1");
        XWPFRun sectionRun = sectionTitle.createRun();
        sectionRun.setText("2 Reference and applicable Documents");
        sectionRun.setBold(true);
        sectionRun.setItalic(true);
        sectionRun.setColor("2F75B5");
        sectionRun.setFontSize(20);

// 🔹 2.1 Applicable documents
        XWPFParagraph applicableTitle = document.createParagraph();
        applicableTitle.setStyle("Heading2");
        XWPFRun applicableRun = applicableTitle.createRun();
        applicableRun.setText("2.1 Applicable documents");
        applicableRun.setItalic(true);
        applicableRun.setColor("2F75B5");
        applicableRun.setFontSize(15);

// 🔹 Table Applicable Documents
        XWPFTable applicableTable = document.createTable();
        setTableWidth(applicableTable, 9000);

// En-tête
        XWPFTableRow appHeader = applicableTable.getRow(0);
        appHeader.getCell(0).setText("Title");
        appHeader.addNewTableCell().setText("Reference");
        appHeader.addNewTableCell().setText("Version");
        colorTableHeader(appHeader);

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
        referenceTitle.setStyle("Heading2");
        XWPFRun referenceRun = referenceTitle.createRun();
        referenceRun.setText("2.2  Reference documents");
        referenceRun.setItalic(true);
        referenceRun.setColor("2F75B5");
        referenceRun.setFontSize(15);

// 🔹 Table Reference Documents
        XWPFTable referenceTable = document.createTable();
        setTableWidth(referenceTable, 9000);

// En-tête
        XWPFTableRow refHeader = referenceTable.getRow(0);
        refHeader.getCell(0).setText("Title");
        refHeader.addNewTableCell().setText("Reference");
        refHeader.addNewTableCell().setText("Version");
        colorTableHeader(refHeader);

// Exemple dynamique (à remplacer par devis.getReferenceDocuments())
        referenceTable.createRow(); // ligne vide
        referenceTable.createRow(); // ligne vide


/// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        document.createParagraph().setPageBreak(true); // saut après le sommaire
        addHeaderTable(document, devis); // ⬅️ Ajout de ton header personnalisé


        // 📌 SECTION 3 - Introduction
        XWPFParagraph introTitle = document.createParagraph();
        introTitle.setStyle("Heading1");
        XWPFRun introRun = introTitle.createRun();
        introRun.setText("3 INTRODUCTION");
        introRun.setBold(true);
        introRun.setItalic(true);
        introRun.setColor("2F75B5");
        introRun.setFontSize(20);

// 🔹 3.1 TELNET Presentation
        XWPFParagraph telnetTitle = document.createParagraph();
        telnetTitle.setStyle("Heading2");
        XWPFRun telnetRun = telnetTitle.createRun();
        telnetRun.setText("3.1 TELNET Presentation");
        telnetRun.setItalic(true);
        telnetRun.setColor("2F75B5");
        telnetRun.setFontSize(15);

// 🔹 3.2 Project Scope
        XWPFParagraph scopeTitle = document.createParagraph();
        scopeTitle.setStyle("Heading2");
        XWPFRun scopeRun = scopeTitle.createRun();
        scopeRun.setText("3.2 Project Scope");
        scopeRun.setItalic(true);
        scopeRun.setColor("2F75B5");
        scopeRun.setFontSize(12);

// 🔹 Texte d’introduction
        XWPFParagraph scopeText = document.createParagraph();
        XWPFRun scopeTextRun = scopeText.createRun();
        scopeTextRun.setText("In the scope of this project, Telnet will provide, during all the defined project period:");
        scopeTextRun.setFontSize(11);

// 🔹 Liste à puce
        XWPFParagraph bullet = document.createParagraph();
        bullet.setStyle("ListBullet"); // Pour Word interprétation liste
        XWPFRun bulletRun = bullet.createRun();
        bulletRun.setText("-----");
        bulletRun.setFontSize(11);


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        document.createParagraph().setPageBreak(true); // saut après le sommaire
        addHeaderTable(document, devis); // ⬅️ Ajout de ton header personnalisé
        // 📊 Workload Proposal

        // 📌 SECTION 3 - Workload
        XWPFParagraph WorkloadTitle = document.createParagraph();
        WorkloadTitle.setStyle("Heading1");
        XWPFRun workloadRun = WorkloadTitle.createRun();
        workloadRun.setText("4\nTECHNICAL PROPOSAL");
        workloadRun.setBold(true);
        workloadRun.setItalic(true);
        workloadRun.setColor("2F75B5");
        workloadRun.setFontSize(20);

// 🔹 3.2 Project Scope
        XWPFParagraph WorkloadEstimationTitle = document.createParagraph();
        WorkloadEstimationTitle.setStyle("Heading2");
        XWPFRun WorkloadEstimationRun = WorkloadEstimationTitle.createRun();
        WorkloadEstimationRun.setText("4.1\tWorkload Estimation");
        WorkloadEstimationRun.setItalic(true);
        WorkloadEstimationRun.setColor("2F75B5");
        WorkloadEstimationRun.setFontSize(15);

        XWPFParagraph WorkloadDetailsTitle = document.createParagraph();
        WorkloadDetailsTitle.setStyle("Heading3");
        XWPFRun WorkloadDetailsRun = WorkloadDetailsTitle.createRun();
        WorkloadDetailsRun.setText("4.1.1\tWorkload");
        WorkloadDetailsRun.setItalic(true);
        WorkloadDetailsRun.setColor("2F75B5");
        WorkloadDetailsRun.setFontSize(15);

        XWPFTable workloadTable = document.createTable();
        setTableWidth(workloadTable, 9000);
        XWPFTableRow wHeader = workloadTable.getRow(0);
        wHeader.getCell(0).setText("Period");
        wHeader.addNewTableCell().setText("Estimated workload per Resource (Man/day)");
        wHeader.addNewTableCell().setText("Public Holidays");
        wHeader.addNewTableCell().setText("Number of Resources");
        wHeader.addNewTableCell().setText("Total Estimated Workload");
        colorTableHeader(wHeader);

        for (WorkloadDetail wl : devis.getWorkloadDetails()) {
            XWPFTableRow row = workloadTable.createRow();
            row.getCell(0).setText(wl.getPeriod());
            row.getCell(1).setText(String.valueOf(wl.getEstimatedWorkload()));
            row.getCell(2).setText(String.valueOf(wl.getPublicHolidays()));
            row.getCell(3).setText(String.valueOf(wl.getNumberOfResources()));
            row.getCell(4).setText(String.valueOf(wl.getTotalEstimatedWorkload()));

        }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        document.createParagraph().setPageBreak(true); // saut après le sommaire
        addHeaderTable(document, devis); // ⬅️ Ajout de ton header personnalisé

        XWPFParagraph finanTitle = document.createParagraph();
        finanTitle.setStyle("Heading1");
        XWPFRun finanRun = finanTitle.createRun();
        finanRun.setText("5\n FINANCIAL PROPOSAL");
        finanRun.setBold(true);
        finanRun.setItalic(true);
        finanRun.setColor("2F75B5");
        finanRun.setFontSize(20);

        // 🔹 3.2 Project Scope
        XWPFParagraph DetailsTitle = document.createParagraph();
        DetailsTitle.setStyle("Heading2");
        XWPFRun DetailsRun = DetailsTitle.createRun();
        DetailsRun.setText("5.1\tDetails");
        DetailsRun.setItalic(true);
        DetailsRun.setColor("2F75B5");
        DetailsRun.setFontSize(15);

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

// 🔹 3.2 Project Scope
        XWPFParagraph InvoicingTitle = document.createParagraph();
        InvoicingTitle.setStyle("Heading2");
        XWPFRun InvoicingRun = InvoicingTitle.createRun();
        InvoicingRun.setText("5.3\tInvoicing");
        InvoicingRun.setItalic(true);
        InvoicingRun.setColor("2F75B5");
        InvoicingRun.setFontSize(15);


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
    private void addHeaderTable(XWPFDocument document, Devis devis) {
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

        // Colonne 2 : Titre
        XWPFTableCell titleCell = headerTable.getRow(0).getCell(1);
        titleCell.removeParagraph(0);
        XWPFParagraph centerTitle = titleCell.addParagraph();
        centerTitle.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleTableRun = centerTitle.createRun();
        titleTableRun.setText("Technical and Financial Proposal");
        titleTableRun.setBold(true);
        titleTableRun.setFontSize(14);
        titleTableRun.setColor("5A5A5A");

        // Colonne 3 : Infos
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

        // Séparateur
        XWPFParagraph separator = document.createParagraph();
        separator.setBorderBottom(Borders.SINGLE);
    }

    private void addTableOfContents(XWPFDocument document) {
        XWPFParagraph tocTitle = document.createParagraph();
        tocTitle.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = tocTitle.createRun();
        run.setText("Summary");
        run.setBold(true);
        run.setItalic(true);
        run.setFontSize(14);
        run.setColor("2F75B5");

        XWPFParagraph paragraph = document.createParagraph();
        CTSimpleField toc = paragraph.getCTP().addNewFldSimple();
        toc.setInstr("TOC \\o \"1-3\" \\h \\z \\u");
        toc.setDirty("true");

        XWPFRun tocRun = paragraph.createRun();
        tocRun.setFontSize(12);
        tocRun.setText("Table of Contents");
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
    private void applyHeaderStyle(XWPFTableCell cell) {
        CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        CTShd shd = tcPr.isSetShd() ? tcPr.getShd() : tcPr.addNewShd();
        shd.setFill("BDD7EE"); // couleur bleu clair
        XWPFParagraph para = cell.getParagraphs().get(0);
        XWPFRun run = para.createRun();
        run.setBold(true);
    }

}
