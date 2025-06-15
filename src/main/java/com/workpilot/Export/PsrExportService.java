package com.workpilot.Export;

import com.workpilot.dto.PsrDTO.*;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.PresetColor;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;


import java.io.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PsrExportService {

    public ByteArrayInputStream exportPsrToExcel(PsrDTO psr) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {

            // === 1. COVER PAGE ===
            Sheet sheet = workbook.createSheet("Cover Page");
            sheet.setDisplayGridlines(false);
            sheet.setPrintGridlines(false);
            CreationHelper helper = workbook.getCreationHelper();
            sheet.createFreezePane(1, 2);

            // Style for blue bold text with borders for labels
            CellStyle blueBoldStyle = workbook.createCellStyle();
            Font blueFont = workbook.createFont();
            blueFont.setBold(true);
            blueFont.setColor(IndexedColors.BLUE.getIndex()); // Darker blue text
            blueBoldStyle.setFont(blueFont);

// Nouveau style : Texte bleu + gras + bordures
            CellStyle borderedBlueBoldStyle = workbook.createCellStyle();
            borderedBlueBoldStyle.cloneStyleFrom(blueBoldStyle); // copier le style bleu existant
            borderedBlueBoldStyle.setBorderTop(BorderStyle.THIN);
            borderedBlueBoldStyle.setBorderBottom(BorderStyle.THIN);
            borderedBlueBoldStyle.setBorderLeft(BorderStyle.THIN);
            borderedBlueBoldStyle.setBorderRight(BorderStyle.THIN);


            // Basic border style
            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);

            // Section header style (exact #99ccff background with white text)
            XSSFCellStyle sectionHeaderStyle = (XSSFCellStyle) workbook.createCellStyle();
            Font sectionFont = workbook.createFont();
            sectionFont.setBold(true);
            sectionFont.setColor(IndexedColors.BLACK.getIndex());
            sectionHeaderStyle.setFont(sectionFont);
            byte[] rgb = { (byte) 153, (byte) 204, (byte) 255 }; // Custom blue color
            XSSFColor customColor = new XSSFColor(rgb, null);
            sectionHeaderStyle.setFillForegroundColor(customColor);
            sectionHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            sectionHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
            sectionHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            sectionHeaderStyle.setBorderTop(BorderStyle.THIN);
            sectionHeaderStyle.setBorderBottom(BorderStyle.THIN);
            sectionHeaderStyle.setBorderLeft(BorderStyle.THIN);
            sectionHeaderStyle.setBorderRight(BorderStyle.THIN);


            CellStyle boldBorderStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldBorderStyle.setFont(boldFont);

// Ajouter les bordures
            boldBorderStyle.setBorderTop(BorderStyle.THIN);
            boldBorderStyle.setBorderBottom(BorderStyle.THIN);
            boldBorderStyle.setBorderLeft(BorderStyle.THIN);
            boldBorderStyle.setBorderRight(BorderStyle.THIN);



            // Style for Diffusion List headers (blue background with black bold text)
            XSSFCellStyle diffusionHeadersBgStyle = (XSSFCellStyle) workbook.createCellStyle();
            Font diffusionHeadersFont = workbook.createFont();
            diffusionHeadersFont.setBold(true);
            diffusionHeadersFont.setColor(IndexedColors.BLACK.getIndex());
            diffusionHeadersBgStyle.setFont(diffusionHeadersFont);
            diffusionHeadersBgStyle.setFillForegroundColor(customColor); // Blue background
            diffusionHeadersBgStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            diffusionHeadersBgStyle.setAlignment(HorizontalAlignment.CENTER);
            diffusionHeadersBgStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            diffusionHeadersBgStyle.setBorderTop(BorderStyle.THIN);
            diffusionHeadersBgStyle.setBorderBottom(BorderStyle.THIN);
            diffusionHeadersBgStyle.setBorderLeft(BorderStyle.THIN);
            diffusionHeadersBgStyle.setBorderRight(BorderStyle.THIN);

            // Création des styles
            CellStyle titleStyle = workbook.createCellStyle(); // Unused, can be removed
            Font titleFont = workbook.createFont(); // Unused, can be removed
            titleFont.setBold(true); // Unused, can be removed
            titleFont.setFontHeightInPoints((short) 16); // Unused, can be removed
            titleStyle.setFont(titleFont); // Unused, can be removed
            titleStyle.setAlignment(HorizontalAlignment.CENTER); // Unused, can be removed
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Unused, can be removed

            CellStyle rightBlockStyle = workbook.createCellStyle(); // Unused, can be removed
            Font rightFont = workbook.createFont(); // Unused, can be removed
            rightFont.setBold(true); // Unused, can be removed
            rightBlockStyle.setFont(rightFont); // Unused, can be removed
            rightBlockStyle.setVerticalAlignment(VerticalAlignment.TOP); // Unused, can be removed
            rightBlockStyle.setWrapText(true); // Unused, can be removed
            rightBlockStyle.setBorderTop(BorderStyle.THIN); // Unused, can be removed
            rightBlockStyle.setBorderBottom(BorderStyle.THIN); // Unused, can be removed
            rightBlockStyle.setBorderLeft(BorderStyle.THIN); // Unused, can be removed
            rightBlockStyle.setBorderRight(BorderStyle.THIN); // Unused, can be removed


            // === LIGNE DE TITRE (ligne 2 visuelle, donc index 1) ===
            Row titleRow = sheet.createRow(1); // Ligne 2 (index 1)
            //Row titleRow2 = sheet.createRow(2); // Ligne 3 (index 2)
            titleRow.setHeightInPoints(60); // Ajuster la hauteur de la ligne 2
            //titleRow2.setHeightInPoints(20); // Ajuster la hauteur de la ligne 3 (pour le logo)

            // === LOGO en B2:B3 ===
                        try (InputStream logoStream = getClass().getClassLoader().getResourceAsStream("static/img/images.jpg")) {
                if (logoStream != null) {
                    byte[] logoBytes = IOUtils.toByteArray(logoStream);
                    int pictureIdx = workbook.addPicture(logoBytes, Workbook.PICTURE_TYPE_JPEG);
                    Drawing<?> drawing = sheet.createDrawingPatriarch();
                    ClientAnchor anchor = helper.createClientAnchor();
                    anchor.setCol1(1); // Colonne B
                    anchor.setRow1(2); // Ligne 2
                    anchor.setCol2(1); // Colonne C
                    anchor.setRow2(2);
                    Picture pict = drawing.createPicture(anchor, pictureIdx);
                    pict.resize(1); // Ajustement plus fin pour que le logo reste dans B2:B3
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Cell logoCell = titleRow.createCell(1); // B2
            logoCell.setCellValue(""); // Facultatif, peut rester vide

            CellStyle logoStyle = workbook.createCellStyle();
            logoStyle.setBorderTop(BorderStyle.MEDIUM);
            logoStyle.setBorderBottom(BorderStyle.MEDIUM);
            logoStyle.setBorderLeft(BorderStyle.MEDIUM);
            logoStyle.setBorderRight(BorderStyle.MEDIUM);

            logoCell.setCellStyle(logoStyle);

            // === TITRE en C2:D3 ===
          /*  sheet.addMergedRegion(new CellRangeAddress(1, 2, 2, 3)); // C2:D3
            Cell titleCell = titleRow.createCell(2);*/
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 2, 3));
            Cell titleCell = titleRow.createCell(2);
            Cell titleCellD = titleRow.createCell(3);
            titleCell.setCellValue("Project Status Report");

            // Style pour le titre (fond gris + texte bleu + centré)
            CellStyle titleStyleLocal = workbook.createCellStyle();
            Font titleFontCov = workbook.createFont();
            titleFontCov.setBold(true);
            titleFontCov.setColor(IndexedColors.BLUE.getIndex());
            titleFontCov.setFontHeightInPoints((short) 14);
            titleStyleLocal.setFont(titleFontCov);
            titleStyleLocal.setAlignment(HorizontalAlignment.CENTER);
            titleStyleLocal.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyleLocal.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            titleStyleLocal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyleLocal.setBorderTop(BorderStyle.MEDIUM);
            titleStyleLocal.setBorderBottom(BorderStyle.MEDIUM);
            titleStyleLocal.setBorderLeft(BorderStyle.MEDIUM);
            titleStyleLocal.setBorderRight(BorderStyle.MEDIUM);

            titleCell.setCellStyle(titleStyleLocal);
            titleCellD.setCellStyle(titleStyleLocal);
            // === BLOC REF en E2:F3 ===
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 4, 5)); // E2:F3
            Cell rightBlockCell = titleRow.createCell(4);
            Cell rightBlockCellF = titleRow.createCell(5);
            String rightBlockText = "Ref : " + (psr.getReference() != null ? psr.getReference() : "") + "\n" +
                    "Edition : " + (psr.getEdition() != null ? psr.getEdition() : "") + "\n" +
                    "Date : " + (psr.getReportDate() != null ? psr.getReportDate().toString() : "") + "\n" +
                    "Author : " + (psr.getAuthorName() != null ? psr.getAuthorName() : "");
            rightBlockCell.setCellValue(rightBlockText);

            // Style pour bloc ref : bordures épaisses + texte aligné en haut gauche + wrap
            CellStyle rightBlockStyleLocal = workbook.createCellStyle();
            Font rightFontCOVE = workbook.createFont();
            rightFontCOVE.setBold(true);
            rightBlockStyleLocal.setFont(rightFontCOVE);
            rightBlockStyleLocal.setWrapText(true);
            rightBlockStyleLocal.setVerticalAlignment(VerticalAlignment.TOP);
            rightBlockStyleLocal.setAlignment(HorizontalAlignment.LEFT);
            rightBlockStyleLocal.setBorderTop(BorderStyle.MEDIUM);
            rightBlockStyleLocal.setBorderBottom(BorderStyle.MEDIUM);
            rightBlockStyleLocal.setBorderLeft(BorderStyle.MEDIUM);
            rightBlockStyleLocal.setBorderRight(BorderStyle.MEDIUM);

            rightBlockCell.setCellStyle(rightBlockStyleLocal);
            rightBlockCellF.setCellStyle(rightBlockStyleLocal);
            // === REF TEMPLATE (ligne 4, index 3)
            Row refRow = sheet.createRow(2); // Ligne 4
            Cell refCell = refRow.createCell(4);
            refCell.setCellValue("Ref Template MON_DSS_TM_002_EN 07");

            CellStyle refStyle = workbook.createCellStyle();
            Font refFont = workbook.createFont();
            refFont.setItalic(true);
            refFont.setFontHeightInPoints((short) 9);
            refStyle.setFont(refFont);
            refCell.setCellStyle(refStyle);

            // === LARGEURS DE COLONNES ===
            sheet.setColumnWidth(0, 5 * 256);  // A (bordure gauche)
            sheet.setColumnWidth(1, 20 * 256); // B (logo et labels)
            sheet.setColumnWidth(2, 25 * 256); // C
            sheet.setColumnWidth(3, 20 * 256); // D (labels Date)
            sheet.setColumnWidth(4, 20 * 256); // E (valeurs Date / bloc droit)
            sheet.setColumnWidth(5, 25 * 256); // F (bloc droit)

            // === DOCUMENT CONTENT ===

            // Spacing before Prepared By section
            sheet.createRow(4); // Empty row after Ref Template (line 5 in Excel)

            // Prepared By/Verified By/etc. section
            String[][] approvalData = {
                    {"Prepared By:", psr.getPreparedBy(), "Date:", (psr.getPreparedByDate() != null ? psr.getPreparedByDate().toString() : "")},
                    {"Verified By:", psr.getApprovedBy(), "Date:", (psr.getApprovedByDate() != null ? psr.getApprovedByDate().toString() : "")},
                    {"Validated By:", psr.getValidatedBy(), "Date:", (psr.getValidatedByDate() != null ? psr.getValidatedByDate().toString() : "")},
                    {"Week:", (psr.getWeek() != null ? psr.getWeek() : ""), "Date:", (psr.getReportDate() != null ? psr.getReportDate().toString() : "")}
            };

            int currentRow = 5; // Start from line 6 in Excel
            for (String[] rowData : approvalData) {
                Row row = sheet.createRow(currentRow++);

                // B: label gauche
                Cell cellB = row.createCell(1);
                cellB.setCellValue(rowData[0]);
                cellB.setCellStyle(borderedBlueBoldStyle);

                // C: valeur gauche
                Cell cellC = row.createCell(2);
                cellC.setCellValue(rowData[1]);
                cellC.setCellStyle(borderStyle);

                // D: label "Date:"
                Cell cellD = row.createCell(3);
                cellD.setCellValue(rowData[2]);
                cellD.setCellStyle(borderedBlueBoldStyle);

                // E: valeur date
                Cell cellE = row.createCell(4);
                cellE.setCellValue(rowData[3]);
                cellE.setCellStyle(borderStyle);

                // F: cellule fusionnée (doit aussi avoir style !)
                Cell cellF = row.createCell(5);
                cellF.setCellStyle(borderStyle);

                // ✅ Fusionner E:F (col 4 à 5)
                sheet.addMergedRegion(new CellRangeAddress(
                        row.getRowNum(), row.getRowNum(), 4, 5
                ));
            }



            // Document Status row
            Row docStatusRow = sheet.createRow(currentRow++);
            Cell labelCell = docStatusRow.createCell(1);
            labelCell.setCellValue("Document Status:");
            labelCell.setCellStyle(borderedBlueBoldStyle);

            Cell statusCell = docStatusRow.createCell(2);
            statusCell.setCellValue(psr.getOverallStatus());
            statusCell.setCellStyle(borderStyle);

// Fusionner C to E (col 2 to 4)
            sheet.addMergedRegion(new CellRangeAddress(docStatusRow.getRowNum(), docStatusRow.getRowNum(), 2, 5));

// ⚠️ Appliquer la bordure aux cellules fusionnées (C3, D3, E3)
            for (int col = 3; col <= 5; col++) {
                Cell merged = docStatusRow.createCell(col);
                merged.setCellStyle(borderStyle);
            }

            // Spacing before Document Type
            sheet.createRow(currentRow++); // Empty row

// === Document Type ===
            Row docTypeRow = sheet.createRow(currentRow++);
            Cell docTypeLabel = docTypeRow.createCell(1);
            docTypeLabel.setCellValue("Document Type");
            docTypeLabel.setCellStyle(borderedBlueBoldStyle);

            Cell docTypeValue = docTypeRow.createCell(2);
            docTypeValue.setCellValue("Project Status Report");
            docTypeValue.setCellStyle(boldBorderStyle);

// Fusionner C to F
            sheet.addMergedRegion(new CellRangeAddress(docTypeRow.getRowNum(), docTypeRow.getRowNum(), 2, 5));
            for (int col = 3; col <= 5; col++) {
                Cell c = docTypeRow.createCell(col);
                c.setCellStyle(borderStyle);
            }

// === Project ===
            Row projectRow = sheet.createRow(currentRow++);
            Cell projectLabel = projectRow.createCell(1);
            projectLabel.setCellValue("Project");
            projectLabel.setCellStyle(borderedBlueBoldStyle);

            Cell projectValue = projectRow.createCell(2);
            projectValue.setCellValue(psr.getProjectName());
            projectValue.setCellStyle(borderStyle);

// Fusionner C to F
            sheet.addMergedRegion(new CellRangeAddress(projectRow.getRowNum(), projectRow.getRowNum(), 2, 5));
            for (int col = 3; col <= 5; col++) {
                Cell c = projectRow.createCell(col);
                c.setCellStyle(borderStyle);
            }


// Summary title
            Row summaryHeaderRow = sheet.createRow(currentRow++);
            Cell summaryTitle = summaryHeaderRow.createCell(1);
            summaryTitle.setCellValue("Summary");
            summaryTitle.setCellStyle(sectionHeaderStyle);

// Fusionner B to F
            sheet.addMergedRegion(new CellRangeAddress(summaryHeaderRow.getRowNum(), summaryHeaderRow.getRowNum(), 1, 5));
            for (int col = 2; col <= 5; col++) {
                Cell c = summaryHeaderRow.createCell(col);
                c.setCellStyle(sectionHeaderStyle);
            }

// Summary content
            Row summaryContentRow = sheet.createRow(currentRow++);
            Cell summaryText = summaryContentRow.createCell(1);
            summaryText.setCellValue("This document presents a weekly Project Status Report for TELNET team activities on '" +
                    psr.getProjectName() + "' Project");
            summaryText.setCellStyle(boldBorderStyle);

// Fusionner B to F
            sheet.addMergedRegion(new CellRangeAddress(summaryContentRow.getRowNum(), summaryContentRow.getRowNum(), 1, 5));
            for (int col = 2; col <= 5; col++) {
                Cell c = summaryContentRow.createCell(col);
                c.setCellStyle(sectionHeaderStyle);
            }

            // Diffusion List
            sheet.createRow(currentRow++); // Empty row for spacing
            Row diffusionHeaderRow = sheet.createRow(currentRow++);
            diffusionHeaderRow.createCell(1).setCellValue("Diffusion List");
            diffusionHeaderRow.getCell(1).setCellStyle(sectionHeaderStyle);
            sheet.addMergedRegion(new CellRangeAddress(diffusionHeaderRow.getRowNum(), diffusionHeaderRow.getRowNum(), 1, 5)); // B to F
            for (int col = 2; col <= 5; col++) {
                Cell extraCell = diffusionHeaderRow.createCell(col);
                extraCell.setCellStyle(sectionHeaderStyle);
            }

            // Diffusion List headers
            Row diffusionHeadersRow = sheet.createRow(currentRow++);

// Fusion B + C
            Cell nameHeader = diffusionHeadersRow.createCell(1); // B
            nameHeader.setCellValue("Name");
            nameHeader.setCellStyle(boldBorderStyle);

            sheet.addMergedRegion(new CellRangeAddress(
                    diffusionHeadersRow.getRowNum(), diffusionHeadersRow.getRowNum(), 1, 2
            ));
            diffusionHeadersRow.createCell(2).setCellStyle(diffusionHeadersBgStyle); // C

// D = Location
            Cell locationHeader = diffusionHeadersRow.createCell(3);
            locationHeader.setCellValue("Location");
            locationHeader.setCellStyle(boldBorderStyle);

// E = Principal List
            Cell principalHeader = diffusionHeadersRow.createCell(4);
            principalHeader.setCellValue("Principal List (To)");
            principalHeader.setCellStyle(boldBorderStyle);

// F = Secondary List
            Cell secondaryHeader = diffusionHeadersRow.createCell(5);
            secondaryHeader.setCellValue("Secondary List (Cc)");
            secondaryHeader.setCellStyle(boldBorderStyle);

            // Note: The image shows empty cells for location and principal list for some rows.
            String[][] diffusionData = {
                    {"Offshore Responsible", "", "", ""},
                    {"", "", "", ""},
                    {"", "", "", ""},
                    {"", "", "", ""},
                    {"Line Of Business Director", psr.getProjectName(), "", ""}, // Location is empty in the second image provided
                    {"Quality Product Coordinator",psr.getProjectName(), "", ""},
                    {"", psr.getProjectName(), "", ""},
                    {"", psr.getProjectName(), "", ""}
            };

            for (String[] rowData : diffusionData) {
                Row dataRow = sheet.createRow(currentRow++);

                // Fusion B + C pour "Name"
                Cell nameCell = dataRow.createCell(1); // B
                nameCell.setCellValue(rowData[0]);
                nameCell.setCellValue(rowData[0]);

// Vérifie si le nom doit être en bleu
                if (
                        rowData[0].equals("Offshore Responsible") ||
                                rowData[0].equals("Line Of Business Director") ||
                                rowData[0].equals("Quality Product Coordinator")
                ) {
                    nameCell.setCellStyle(borderedBlueBoldStyle);
                    Cell nameCellC = dataRow.createCell(2);
                    nameCellC.setCellStyle(borderedBlueBoldStyle);
                } else {
                    nameCell.setCellStyle(borderStyle);
                    Cell nameCellC = dataRow.createCell(2);
                    nameCellC.setCellStyle(borderStyle);
                }


                Cell nameCellC = dataRow.createCell(2); // C
                nameCellC.setCellStyle(borderStyle);

                sheet.addMergedRegion(new CellRangeAddress(
                        dataRow.getRowNum(), dataRow.getRowNum(), 1, 2
                ));

                // D = Location
                Cell locationCell = dataRow.createCell(3);
                locationCell.setCellValue(rowData[1]);
                locationCell.setCellStyle(borderStyle);

                // E = Principal List
                Cell principalCell = dataRow.createCell(4);
                principalCell.setCellValue(rowData[2]);
                principalCell.setCellStyle(borderStyle);

                // F = Secondary List
                Cell secondaryCell = dataRow.createCell(5);
                secondaryCell.setCellValue(rowData[3]);
                secondaryCell.setCellStyle(borderStyle);
            }


//-------------------------------------------------------------------------------
            // === 2. TEAM ORGANIZATION ===
// === Créer la feuille ===
            Sheet teamSheet = workbook.createSheet("Team Organization");
            teamSheet.setDisplayGridlines(false);
            teamSheet.setPrintGridlines(false);

// === Titre ===
            Row titleRowTeam = teamSheet.createRow(0);
            titleRowTeam.setHeightInPoints(35); // HAUTEUR DE LA LIGNE DU TITRE
            Cell titleCellTeam = titleRowTeam.createCell(0);
            titleCellTeam.setCellValue("Team Organization");

            CellStyle titleStyleTeam = workbook.createCellStyle();
            Font titleFontTeam = workbook.createFont();
            titleFontTeam.setFontHeightInPoints((short) 26); // taille police titre
            titleFontTeam.setBold(true);
            titleFontTeam.setColor(IndexedColors.DARK_BLUE.getIndex()); // texte bleu foncé

// Fond gris clair pour le titre
            titleStyleTeam.setFont(titleFontTeam);
            titleStyleTeam.setAlignment(HorizontalAlignment.CENTER);
            titleStyleTeam.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyleTeam.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            titleStyleTeam.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            teamSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));
            titleCellTeam.setCellStyle(titleStyleTeam);

// === En-têtes ===
            Row headerRowTeam = teamSheet.createRow(2); // ligne 3
            headerRowTeam.setHeightInPoints(25); // hauteur de l'en-tête

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLACK.getIndex());
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            // === Couleur personnalisée : #99ccff ===
            byte[] rgbA = new byte[] { (byte) 153, (byte) 204, (byte) 255 };
            XSSFColor customBlue = new XSSFColor(rgbA, null);

            headerStyle.setFillForegroundColor(customBlue);
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            String[] headersTeam = {
                    "#", "Initial", "Member", "Role", "Project",
                    "Planned Start Date", "Planned End Date", "Allocation (%)",
                    "Coming From Team", "Going To Team", "Holiday"
            };

            for (int i = 0; i < headersTeam.length; i++) {
                Cell cell = headerRowTeam.createCell(i + 1);
                cell.setCellValue(headersTeam[i]);
                cell.setCellStyle(headerStyle);
                teamSheet.setColumnWidth(i + 1, 20 * 256); // largeur colonne
                // Colonne B ("#") plus étroite
                teamSheet.setColumnWidth(1, 6 * 256); // 6 caractères de large

// Colonne L ("Holiday") plus large
                teamSheet.setColumnWidth(11, 35 * 256); // 35 caractères de large

            }

// === Contenu ===
            CellStyle contentStyle = workbook.createCellStyle();
            Font contentFont = workbook.createFont();
            contentFont.setFontHeightInPoints((short) 11); // taille police contenu
            contentStyle.setFont(contentFont);
            contentStyle.setBorderTop(BorderStyle.THIN);
            contentStyle.setBorderBottom(BorderStyle.THIN);
            contentStyle.setBorderLeft(BorderStyle.THIN);
            contentStyle.setBorderRight(BorderStyle.THIN);
            contentStyle.setVerticalAlignment(VerticalAlignment.TOP);
            contentStyle.setAlignment(HorizontalAlignment.LEFT);
            contentStyle.setWrapText(true); // important pour "Holiday"

            List<TeamOrganizationDTO> members = psr.getTeamOrganizations();
            if (members != null) {
                for (int i = 0; i < members.size(); i++) {
                    TeamOrganizationDTO m = members.get(i);
                    Row row = teamSheet.createRow(i + 3); // ligne 4
                    row.setHeightInPoints(40); // hauteur des lignes de contenu

                    row.createCell(1).setCellValue(i + 1);
                    row.createCell(2).setCellValue(m.getInitial());
                    row.createCell(3).setCellValue(m.getFullName());
                    row.createCell(4).setCellValue(m.getRole());
                    row.createCell(5).setCellValue(psr.getProjectName());
                    row.createCell(6).setCellValue(m.getPlannedStartDate() != null ? m.getPlannedStartDate().toString() : "");
                    row.createCell(7).setCellValue(m.getPlannedEndDate() != null ? m.getPlannedEndDate().toString() : "");
                    row.createCell(8).setCellValue(m.getAllocation());
                    row.createCell(9).setCellValue(m.getComingFromTeam());
                    row.createCell(10).setCellValue(m.getGoingToTeam());

                    // Pour "Holiday", on ajoute \n pour que ce soit bien multi-ligne
                    String holidays = m.getHoliday() != null ? String.join("\n", m.getHoliday().split(",")) : "";
                    row.createCell(11).setCellValue(holidays);

                    // Appliquer le style à toutes les colonnes
                    for (int j = 1; j <= 11; j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null) {
                            cell.setCellStyle(contentStyle);
                        }
                    }
                }
            }
//--------------------------------------------------------------------------------------------------
            Sheet weeklySheet = workbook.createSheet("Weekly Report");
            weeklySheet.setDisplayGridlines(false);
            weeklySheet.setPrintGridlines(false);
            XSSFWorkbook xssfWorkbookWeekly = (XSSFWorkbook) workbook;

            // === Récupérer les données ===
            List<WeeklyReportDTO> weeklyReports = psr.getWeeklyReports(); // Ces rapports sont les individuels (tache2, tacheee)

            // --- Styles communs --- (Gardez vos styles ici)
            XSSFCellStyle borderedCellStyle = xssfWorkbookWeekly.createCellStyle();
            borderedCellStyle.setBorderTop(BorderStyle.THIN);
            borderedCellStyle.setBorderBottom(BorderStyle.THIN);
            borderedCellStyle.setBorderLeft(BorderStyle.THIN);
            borderedCellStyle.setBorderRight(BorderStyle.THIN);
            borderedCellStyle.setAlignment(HorizontalAlignment.CENTER);
            borderedCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            XSSFCellStyle sectionTitleStyle = xssfWorkbookWeekly.createCellStyle();
            XSSFFont sectionTitleFont = xssfWorkbookWeekly.createFont();
            sectionTitleFont.setBold(true);
            sectionTitleFont.setFontHeightInPoints((short) 10);
            sectionTitleStyle.setFont(sectionTitleFont);
            sectionTitleStyle.setAlignment(HorizontalAlignment.CENTER);
            sectionTitleStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            sectionTitleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            sectionTitleStyle.setBorderTop(BorderStyle.THIN);
            sectionTitleStyle.setBorderBottom(BorderStyle.THIN);
            sectionTitleStyle.setBorderLeft(BorderStyle.THIN);
            sectionTitleStyle.setBorderRight(BorderStyle.THIN);

            XSSFCellStyle headerStyleWeekly = xssfWorkbookWeekly.createCellStyle();
            XSSFFont headerFontWeekly = xssfWorkbookWeekly.createFont();
            headerFontWeekly.setBold(true);
            headerFontWeekly.setFontHeightInPoints((short) 10);
            headerFontWeekly.setColor(IndexedColors.BLACK.getIndex());
            headerStyleWeekly.setFont(headerFontWeekly);
            headerStyleWeekly.setAlignment(HorizontalAlignment.CENTER);
            headerStyleWeekly.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyleWeekly.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 153, (byte) 204, (byte) 255}, null)); // Light blue
            headerStyleWeekly.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyleWeekly.setBorderTop(BorderStyle.THIN);
            headerStyleWeekly.setBorderBottom(BorderStyle.THIN);
            headerStyleWeekly.setBorderLeft(BorderStyle.THIN);
            headerStyleWeekly.setBorderRight(BorderStyle.THIN);

            XSSFCellStyle dataCellStyle = xssfWorkbookWeekly.createCellStyle();
            dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
            dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataCellStyle.setBorderTop(BorderStyle.THIN);
            dataCellStyle.setBorderBottom(BorderStyle.THIN);
            dataCellStyle.setBorderLeft(BorderStyle.THIN);
            dataCellStyle.setBorderRight(BorderStyle.THIN);
            dataCellStyle.setWrapText(true);

            XSSFCellStyle nonZeroVarianceStyle = xssfWorkbookWeekly.createCellStyle();
            nonZeroVarianceStyle.cloneStyleFrom(dataCellStyle);
            nonZeroVarianceStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            nonZeroVarianceStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            nonZeroVarianceStyle.setAlignment(HorizontalAlignment.CENTER);

            XSSFCellStyle zeroVarianceStyle = xssfWorkbookWeekly.createCellStyle();
            zeroVarianceStyle.cloneStyleFrom(dataCellStyle);
            zeroVarianceStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex()); // If needed, image shows red for non-zero
            zeroVarianceStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            zeroVarianceStyle.setAlignment(HorizontalAlignment.CENTER);

            // --- Agrégation des données (similaire à processReports() en Angular) ---
            LocalDate psrReportDate = psr.getReportDate(); // Assurez-vous que psr.getReportDate() retourne LocalDate
            int psrMonthValue = psrReportDate.getMonthValue();
            int psrYear = psrReportDate.getYear();
            String psrProjectName = psr.getProjectName(); // Assurez-vous que psr.getProjectName() existe

            java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.ISO; // Pour la compatibilité des numéros de semaine (W23)

            // Déterminer toutes les semaines du mois du PSR
            LocalDate firstDayOfMonth = LocalDate.of(psrYear, psrMonthValue, 1);
            LocalDate lastDayOfMonth = firstDayOfMonth.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());

            List<Integer> weeksInPsrMonth = new ArrayList<>();
            int firstWeekOfMonth = firstDayOfMonth.get(weekFields.weekOfWeekBasedYear());
            int lastWeekOfMonth = lastDayOfMonth.get(weekFields.weekOfWeekBasedYear());

            // Gérer le cas où le mois chevauche la fin/début de l'année pour les numéros de semaine
            if (firstWeekOfMonth > lastWeekOfMonth) { // Ex: Décembre, où les semaines peuvent aller 50, 51, 52, 1, 2
                for (int w = firstWeekOfMonth; w <= LocalDate.of(psrYear, 12, 31).get(weekFields.weekOfWeekBasedYear()); w++) {
                    weeksInPsrMonth.add(w);
                }
                for (int w = 1; w <= lastWeekOfMonth; w++) {
                    weeksInPsrMonth.add(w);
                }
            } else {
                for (int w = firstWeekOfMonth; w <= lastWeekOfMonth; w++) {
                    weeksInPsrMonth.add(w);
                }
            }

            // Grouper les rapports individuels par leur numéro de semaine
            Map<Integer, List<WeeklyReportDTO>> groupedReportsByWeek = weeklyReports.stream()
                    .collect(Collectors.groupingBy(WeeklyReportDTO::getWeekNumber));

            // Calculer les données agrégées pour le tableau "Monthly Workload By Project/Module" et le graphique associé
            List<ConsolidatedWeeklyWorkload> monthlyWorkloadDataAggregated = new ArrayList<>();
            // Pour le graphique "Weekly Effort Variance"
            List<Double> weeklyEffortVarianceDataForChart = new ArrayList<>();
            List<String> weeklyEffortVarianceLabelsForChart = new ArrayList<>();

            // Pour le calcul de la "Monthly Consolidated Effort Variance"
            double totalWorkedDaysMonth = 0;
            double totalEstimatedDaysMonth = 0;

            for (int weekNum : weeksInPsrMonth) {
                double totalWorkingDaysForWeek = 0;
                double totalEstimatedDaysForWeek = 0;

                List<WeeklyReportDTO> reportsForThisWeek = groupedReportsByWeek.getOrDefault(weekNum, Collections.emptyList());
                for (WeeklyReportDTO report : reportsForThisWeek) {
                    totalWorkingDaysForWeek += (report.getWorkingDays() != null ? report.getWorkingDays() : 0);
                    totalEstimatedDaysForWeek += (report.getEstimatedDays() != null ? report.getEstimatedDays() : 0);
                }

                // Cumuler pour le total mensuel consolidé
                totalWorkedDaysMonth += totalWorkingDaysForWeek;
                totalEstimatedDaysMonth += totalEstimatedDaysForWeek;

                // Ajouter une ligne au tableau Monthly Workload si des données existent pour la semaine
                if (totalWorkingDaysForWeek > 0 || totalEstimatedDaysForWeek > 0) {
                    double effortVarianceForWeek = totalEstimatedDaysForWeek == 0 ? 0 : ((totalWorkingDaysForWeek - totalEstimatedDaysForWeek) / totalEstimatedDaysForWeek) * 100;
                    monthlyWorkloadDataAggregated.add(new ConsolidatedWeeklyWorkload(
                            psrReportDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase(),
                            psrProjectName + " (W" + weekNum + ")", // Nom consolidé pour le tableau
                            totalWorkingDaysForWeek,
                            totalEstimatedDaysForWeek,
                            effortVarianceForWeek
                    ));
                }

                // Toujours ajouter au tableau de données pour le graphique Weekly Effort Variance, même si zéro
                double effortVarianceForWeeklyChart = totalEstimatedDaysForWeek == 0 ? 0 : ((totalWorkingDaysForWeek - totalEstimatedDaysForWeek) / totalEstimatedDaysForWeek) * 100;
                weeklyEffortVarianceDataForChart.add(effortVarianceForWeeklyChart);
                weeklyEffortVarianceLabelsForChart.add("W" + weekNum);
            }

            // Calculer la "Monthly Consolidated Effort Variance" (une seule valeur pour le mois)
            double monthlyConsolidatedEffortVariance = totalEstimatedDaysMonth == 0 ? 0 : ((totalWorkedDaysMonth - totalEstimatedDaysMonth) / totalEstimatedDaysMonth) * 100;

            // === Header with date and page info (Row 0) ===
            Row headerInfoRow = weeklySheet.createRow(0);
            headerInfoRow.setHeightInPoints(20);

            for (int i = 0; i <= 15; i++) {
                Cell cell = headerInfoRow.createCell(i);
                cell.setCellStyle(borderedCellStyle);
            }

            Cell titleCellWeekly = headerInfoRow.getCell(2);
            if (titleCellWeekly == null) titleCellWeekly = headerInfoRow.createCell(2);
            titleCellWeekly.setCellValue("Weekly Report");
            XSSFCellStyle titleStyleWeekly = xssfWorkbookWeekly.createCellStyle();
            XSSFFont titleFontWeekly = xssfWorkbookWeekly.createFont();
            titleFontWeekly.setBold(true);
            titleFontWeekly.setFontHeightInPoints((short) 12);
            titleFontWeekly.setColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyleWeekly.setFont(titleFontWeekly);
            titleStyleWeekly.setAlignment(HorizontalAlignment.CENTER);
            titleStyleWeekly.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyleWeekly.setBorderTop(BorderStyle.THIN);
            titleStyleWeekly.setBorderBottom(BorderStyle.THIN);
            titleStyleWeekly.setBorderLeft(BorderStyle.THIN);
            titleStyleWeekly.setBorderRight(BorderStyle.THIN);
            //weeklySheet.addMergedRegion(new CellRangeAddress(0, 0, 2, 6));
            safeMergeRegion(weeklySheet, 0, 0, 2, 6);
            titleCellWeekly.setCellStyle(titleStyleWeekly);
            for (int col = 2; col <= 6; col++) {
                Cell cell = headerInfoRow.getCell(col);
                if (cell == null) cell = headerInfoRow.createCell(col);
                cell.setCellStyle(titleStyleWeekly);
            }
            weeklySheet.setColumnWidth(2, 20 * 256);
            weeklySheet.setColumnWidth(3, 20 * 256);
            weeklySheet.setColumnWidth(4, 20 * 256);
            weeklySheet.setColumnWidth(5, 20 * 256);
            weeklySheet.setColumnWidth(6, 20 * 256);
            weeklySheet.setColumnWidth(7, 20 * 256);

            XSSFCellStyle greyBackgroundWhiteText = xssfWorkbookWeekly.createCellStyle();
            greyBackgroundWhiteText.cloneStyleFrom(borderedCellStyle);
            greyBackgroundWhiteText.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
            greyBackgroundWhiteText.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            XSSFFont whiteFont = xssfWorkbookWeekly.createFont();
            whiteFont.setColor(IndexedColors.WHITE.getIndex());
            whiteFont.setBold(true);
            greyBackgroundWhiteText.setFont(whiteFont);

            Cell monthLabelCell = headerInfoRow.getCell(12);
            if (monthLabelCell == null) monthLabelCell = headerInfoRow.createCell(12);
            monthLabelCell.setCellValue("Month");
            monthLabelCell.setCellStyle(greyBackgroundWhiteText);

            Cell monthValueCell = headerInfoRow.getCell(13);
            if (monthValueCell == null) monthValueCell = headerInfoRow.createCell(13);
            monthValueCell.setCellStyle(greyBackgroundWhiteText);

            Cell weekLabelCell = headerInfoRow.getCell(14);
            if (weekLabelCell == null) weekLabelCell = headerInfoRow.createCell(14);
            weekLabelCell.setCellValue("Week");
            weekLabelCell.setCellStyle(greyBackgroundWhiteText);

            Cell weekValueCell = headerInfoRow.getCell(15);
            if (weekValueCell == null) weekValueCell = headerInfoRow.createCell(15);
            weekValueCell.setCellStyle(greyBackgroundWhiteText);

            weeklySheet.setColumnWidth(12, 15 * 256);
            weeklySheet.setColumnWidth(13, 12 * 256);
            weeklySheet.setColumnWidth(14, 12 * 256);
            weeklySheet.setColumnWidth(15, 12 * 256);
            // Dynamically set Month and Week values using PSR's report date
            if (psrReportDate != null) {
                monthValueCell.setCellValue(psrReportDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase());
                weekValueCell.setCellValue("W" + psrReportDate.get(weekFields.weekOfWeekBasedYear()));
            } else {
                monthValueCell.setCellValue("");
                weekValueCell.setCellValue("");
            }

            // Empty row (Row 2 in image, index 1)
            weeklySheet.createRow(1);

            // === Monthly Workload By Project/Module Title (Row 3 in image, index 2) ===
            Row monthlyWorkloadTitleRow = weeklySheet.createRow(6);
            Cell monthlyWorkloadTitleCell = monthlyWorkloadTitleRow.createCell(0);
            monthlyWorkloadTitleCell.setCellValue("Monthly Workload By Project/Module");
            //weeklySheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 15));
            safeMergeRegion(weeklySheet, 6, 6, 0, 12);
            monthlyWorkloadTitleCell.setCellStyle(sectionTitleStyle);
            for (int col = 0; col <= 15; col++) {
                Cell cell = monthlyWorkloadTitleRow.getCell(col);
                if (cell == null) cell = monthlyWorkloadTitleRow.createCell(col);
                cell.setCellStyle(sectionTitleStyle);
            }

            // === Workload Table Headers (Row 4 in image, index 3) ===
            Row headerRow = weeklySheet.createRow(7);
            String[] headersWeekly = {"Month", "Project Name", "Working Days (MD)", "Estimated Days (MD)", "Effort Variance"};

            for (int i = 0; i < headersWeekly.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headersWeekly[i]);
                cell.setCellStyle(headerStyleWeekly);
            }
            weeklySheet.setColumnWidth(0, 30 * 256);
            weeklySheet.setColumnWidth(1, 30 * 256);
            weeklySheet.setColumnWidth(2, 20 * 256);
            weeklySheet.setColumnWidth(3, 20 * 256);
            weeklySheet.setColumnWidth(4, 20 * 256);

            // === Workload Data (Starts Row 5 in image, index 4) ===
            int workloadDataStartRow = 8;
            if (!monthlyWorkloadDataAggregated.isEmpty()) {
                String currentMonthInTable = null;
                int monthStartMergeRow = workloadDataStartRow;

                for (int i = 0; i < monthlyWorkloadDataAggregated.size(); i++) {
                    ConsolidatedWeeklyWorkload w = monthlyWorkloadDataAggregated.get(i);
                    Row dataRow = weeklySheet.createRow(workloadDataStartRow + i);

                    // Month (A) - fusionner si le même mois
                    Cell monthCell = dataRow.createCell(0);
                    if (currentMonthInTable == null || !currentMonthInTable.equals(w.getMonth())) {
                        if (currentMonthInTable != null) {
                            weeklySheet.addMergedRegion(new CellRangeAddress(monthStartMergeRow, workloadDataStartRow + i - 1, 0, 0));
                            for (int rowIdx = monthStartMergeRow; rowIdx <= workloadDataStartRow + i - 1; rowIdx++) {
                                Cell cell = weeklySheet.getRow(rowIdx).getCell(0);
                                if (cell == null) cell = weeklySheet.getRow(rowIdx).createCell(0);
                                cell.setCellStyle(dataCellStyle);
                            }
                        }
                        currentMonthInTable = w.getMonth();
                        monthStartMergeRow = workloadDataStartRow + i;
                        monthCell.setCellValue(currentMonthInTable);
                    } else {
                        monthCell.setCellValue(""); // Laisser vide pour la fusion
                    }
                    monthCell.setCellStyle(dataCellStyle);

                    // Project Name (B)
                    Cell projectCell = dataRow.createCell(1);
                    projectCell.setCellValue(w.getProjectNameWithWeek());
                    projectCell.setCellStyle(dataCellStyle);

                    // Working Days (C)
                    Cell workingDaysCell = dataRow.createCell(2);
                    workingDaysCell.setCellValue(w.getWorkingDays());
                    workingDaysCell.setCellStyle(dataCellStyle);

                    Cell estimatedDaysCell = dataRow.createCell(3);
                    estimatedDaysCell.setCellValue(w.getEstimatedDays());
                    estimatedDaysCell.setCellStyle(dataCellStyle);

                    Cell varianceCell = dataRow.createCell(4);
                    double varianceValue = w.getEffortVariance();
                    varianceCell.setCellValue(varianceValue);
                    if (varianceValue != 0) {
                        varianceCell.setCellStyle(nonZeroVarianceStyle);
                    } else {
                        varianceCell.setCellStyle(dataCellStyle);
                    }
                }
                // Fusionner les cellules du dernier mois après la boucle
                if (currentMonthInTable != null) {
                   // weeklySheet.addMergedRegion(new CellRangeAddress(monthStartMergeRow, workloadDataStartRow + monthlyWorkloadDataAggregated.size() - 1, 0, 0));
                    safeMergeRegion(weeklySheet, monthStartMergeRow, workloadDataStartRow + monthlyWorkloadDataAggregated.size() - 1, 0, 0);
                    for (int rowIdx = monthStartMergeRow; rowIdx <= workloadDataStartRow + monthlyWorkloadDataAggregated.size() - 1; rowIdx++) {
                        Cell cell = weeklySheet.getRow(rowIdx).getCell(0);
                        if (cell == null) cell = weeklySheet.getRow(rowIdx).createCell(0);
                        cell.setCellStyle(dataCellStyle);
                    }
                }
            }

            // === Totals Row ===
            int totalsRowIndex = workloadDataStartRow + monthlyWorkloadDataAggregated.size(); // Positionnement juste après les données
            Row totalsRow = weeklySheet.createRow(totalsRowIndex);

            Cell totalsLabelCell = totalsRow.createCell(0);
            totalsLabelCell.setCellValue("Totals");
           // weeklySheet.addMergedRegion(new CellRangeAddress(totalsRowIndex, totalsRowIndex, 0, 1));
            safeMergeRegion(weeklySheet, totalsRowIndex, totalsRowIndex, 0, 1);
            XSSFCellStyle totalsLabelStyle = xssfWorkbookWeekly.createCellStyle();
            totalsLabelStyle.cloneStyleFrom(dataCellStyle);
            totalsLabelStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            totalsLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalsLabelStyle.setAlignment(HorizontalAlignment.CENTER);
            totalsLabelCell.setCellStyle(totalsLabelStyle);
            Cell bTot = totalsRow.createCell(1);
            bTot.setCellStyle(totalsLabelStyle);

            Cell totalWorkingDaysCell = totalsRow.createCell(2);
            totalWorkingDaysCell.setCellValue(totalWorkedDaysMonth); // Total agrégé
            totalWorkingDaysCell.setCellStyle(dataCellStyle);

            Cell totalEstimatedDaysCell = totalsRow.createCell(3);
            totalEstimatedDaysCell.setCellValue(totalEstimatedDaysMonth); // Total agrégé
            totalEstimatedDaysCell.setCellStyle(dataCellStyle);

            Cell totalVarianceCell = totalsRow.createCell(4);
            totalVarianceCell.setCellValue(monthlyConsolidatedEffortVariance); // Variance mensuelle consolidée
            if (monthlyConsolidatedEffortVariance != 0) {
                totalVarianceCell.setCellStyle(nonZeroVarianceStyle);
            } else {
                totalVarianceCell.setCellStyle(dataCellStyle);
            }

            // === Weekly Workload Chart ===
            XSSFDrawing drawingWorkload = (XSSFDrawing) weeklySheet.createDrawingPatriarch();
// Place le graphique à droite du tableau, adapte les indices si besoin
            XSSFClientAnchor anchorWorkload = drawingWorkload.createAnchor(0, 0, 0, 0, 6, workloadDataStartRow, 13, workloadDataStartRow + 12);

            XSSFChart workloadChart = drawingWorkload.createChart(anchorWorkload);
            workloadChart.setTitleText("Weekly Workload");
            workloadChart.setTitleOverlay(false);
            int chartDataEndRow = workloadDataStartRow + monthlyWorkloadDataAggregated.size() - 1;
            XDDFCategoryAxis bottomAxisWorkload = workloadChart.createCategoryAxis(AxisPosition.BOTTOM);
            XDDFValueAxis leftAxisWorkload = workloadChart.createValueAxis(AxisPosition.LEFT);
            leftAxisWorkload.setCrosses(AxisCrosses.AUTO_ZERO);
            XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(
                    (XSSFSheet) weeklySheet, new CellRangeAddress(workloadDataStartRow, chartDataEndRow, 1, 1)
            );
            XDDFNumericalDataSource<Double> workingDays = XDDFDataSourcesFactory.fromNumericCellRange(
                    (XSSFSheet) weeklySheet, new CellRangeAddress(workloadDataStartRow, chartDataEndRow, 2, 2)
            );
            XDDFNumericalDataSource<Double> estimatedDays = XDDFDataSourcesFactory.fromNumericCellRange(
                    (XSSFSheet) weeklySheet, new CellRangeAddress(workloadDataStartRow, chartDataEndRow, 3, 3)
            );

            XDDFBarChartData barChartDataWorkload = (XDDFBarChartData) workloadChart.createData(ChartTypes.BAR, bottomAxisWorkload, leftAxisWorkload);

            XDDFBarChartData.Series seriesWorkingDays = (XDDFBarChartData.Series) barChartDataWorkload.addSeries(categories, workingDays);
            seriesWorkingDays.setTitle("Working Days (MD)", null);

            XDDFBarChartData.Series seriesEstimatedDays = (XDDFBarChartData.Series) barChartDataWorkload.addSeries(categories, estimatedDays);
            seriesEstimatedDays.setTitle("Estimated Days (MD)", null);

// Optionnel : couleurs personnalisées
            XDDFSolidFillProperties fillRed = new XDDFSolidFillProperties(XDDFColor.from(PresetColor.RED));
            XDDFShapeProperties propertiesRed = new XDDFShapeProperties();
            propertiesRed.setFillProperties(fillRed);
            seriesWorkingDays.setShapeProperties(propertiesRed);

            XDDFSolidFillProperties fillGreen = new XDDFSolidFillProperties(XDDFColor.from(PresetColor.GREEN));
            XDDFShapeProperties propertiesGreen = new XDDFShapeProperties();
            propertiesGreen.setFillProperties(fillGreen);
            seriesEstimatedDays.setShapeProperties(propertiesGreen);

// Groupement et direction
            barChartDataWorkload.setBarGrouping(BarGrouping.CLUSTERED);
            barChartDataWorkload.setBarDirection(BarDirection.COL);
            workloadChart.plot(barChartDataWorkload);

// Légende
            XDDFChartLegend legendWorkload = workloadChart.getOrAddLegend();
            legendWorkload.setPosition(LegendPosition.BOTTOM);

// Optionnel : ajuster l'espacement entre les groupes de barres
            org.openxmlformats.schemas.drawingml.x2006.chart.CTBarChart ctBarChart = workloadChart.getCTChart().getPlotArea().getBarChartArray(0);
            if (ctBarChart.getGapWidth() == null) {
                ctBarChart.addNewGapWidth();
            }
            ctBarChart.getGapWidth().setVal(150); // 150 = espacement standard, ajuste si besoin


            // --- Ajuster l'index de ligne pour la prochaine section ---
            // L'index de la prochaine section (Effort Variance) doit être après le tableau des totaux.
            int currentFreeRow = totalsRowIndex + 1; // 1ère ligne vide après les totaux

            // === Effort Variance Section Title (Row 19 in image) ===
            //currentFreeRow++; // Ligne du titre de la section
          int EffortVarianceRow = 18;
            Row effortVarianceSectionTitleRow = weeklySheet.createRow(EffortVarianceRow);
            Cell effortVarianceSectionTitleCell = effortVarianceSectionTitleRow.createCell(0);
            effortVarianceSectionTitleCell.setCellValue("Effort Variance");
            //weeklySheet.addMergedRegion(new CellRangeAddress(currentFreeRow, currentFreeRow, 0, 15));
            safeMergeRegion(weeklySheet, EffortVarianceRow, EffortVarianceRow, 0, 12);
            effortVarianceSectionTitleCell.setCellStyle(sectionTitleStyle);
            for (int col = 0; col <= 15; col++) {
                Cell cell = effortVarianceSectionTitleRow.getCell(col);
                if (cell == null) cell = effortVarianceSectionTitleRow.createCell(col);
                cell.setCellStyle(sectionTitleStyle);
            }
            EffortVarianceRow++; // Ligne des headers des semaines

            // === Dynamic Weekly Effort Variance Headers (W1, W2...) ===
            Row weekHeaderRow = weeklySheet.createRow(EffortVarianceRow);
            EffortVarianceRow++;
            Row effortDataRow = weeklySheet.createRow(EffortVarianceRow);
            EffortVarianceRow++;

            Cell labelCellWEE = weekHeaderRow.createCell(0);
            labelCellWEE.setCellValue("Week");
            labelCellWEE.setCellStyle(headerStyleWeekly);

            Cell dataLabelCell = effortDataRow.createCell(0);
            dataLabelCell.setCellValue("Weekly Effort Variance");
            dataLabelCell.setCellStyle(dataCellStyle);

            int colIndexForWeeklyVariance = 1;
            for (int i = 0; i < weeklyEffortVarianceLabelsForChart.size(); i++) {
                String weekLabel = weeklyEffortVarianceLabelsForChart.get(i);
                Double varianceValue = weeklyEffortVarianceDataForChart.get(i);

                Cell weekCell = weekHeaderRow.createCell(colIndexForWeeklyVariance);
                weekCell.setCellValue(weekLabel);
                weekCell.setCellStyle(headerStyleWeekly);
                weeklySheet.setColumnWidth(colIndexForWeeklyVariance, 12 * 256);

                Cell valueCell = effortDataRow.createCell(colIndexForWeeklyVariance);
                valueCell.setCellValue(varianceValue);
                valueCell.setCellStyle(varianceValue != 0 ? nonZeroVarianceStyle : dataCellStyle);

                colIndexForWeeklyVariance++;
            }
            // Remplir les colonnes restantes si moins de semaines que le maximum prévu (ex: W20)
            for (int i = colIndexForWeeklyVariance; i <= 15; i++) { // Jusqu'à W20 pour aligner au template
                Cell weekCell = weekHeaderRow.createCell(i);
                weekCell.setCellStyle(headerStyleWeekly);
                weeklySheet.setColumnWidth(i, 12 * 256);

                Cell valueCell = effortDataRow.createCell(i);
                valueCell.setCellStyle(dataCellStyle);
            }


            // === Weekly Effort Variance Line Chart ===
            XSSFDrawing drawingVarianceLine = (XSSFDrawing) weeklySheet.createDrawingPatriarch();
            // L'ancre du graphique commence après le tableau "Weekly Effort Variance"
            int weeklyVarianceChartStartRow = currentFreeRow;
            XSSFClientAnchor anchorVarianceLine = drawingVarianceLine.createAnchor(0, 0, 0, 0, 0, 21, 7, 35); // Colonnes A à L, 28 lignes de haut

            XSSFChart varianceLineChart = drawingVarianceLine.createChart(anchorVarianceLine);
            varianceLineChart.setTitleText("Weekly Effort Variance");
            varianceLineChart.setTitleOverlay(false);

            XDDFCategoryAxis bottomAxisVarianceLine = varianceLineChart.createCategoryAxis(AxisPosition.BOTTOM);
            XDDFValueAxis leftAxisVarianceLine = varianceLineChart.createValueAxis(AxisPosition.LEFT);
            leftAxisVarianceLine.setCrosses(AxisCrosses.AUTO_ZERO);
            leftAxisVarianceLine.setMinimum(-0.20); // -20%
            leftAxisVarianceLine.setMaximum(0.10);  // 10%

            XDDFLineChartData lineChartDataVariance = (XDDFLineChartData) varianceLineChart.createData(ChartTypes.LINE, bottomAxisVarianceLine, leftAxisVarianceLine);

            XDDFDataSource<String> chartWeekLabels = XDDFDataSourcesFactory.fromStringCellRange(
                    (XSSFSheet) weeklySheet,
                    new CellRangeAddress(weekHeaderRow.getRowNum(), weekHeaderRow.getRowNum(), 1, colIndexForWeeklyVariance - 1)
            );
            XDDFNumericalDataSource<Double> chartWeeklyEffortVarianceValues = XDDFDataSourcesFactory.fromNumericCellRange(
                    (XSSFSheet) weeklySheet,
                    new CellRangeAddress(effortDataRow.getRowNum(), effortDataRow.getRowNum(), 1, colIndexForWeeklyVariance - 1)
            );

            XDDFLineChartData.Series seriesVarianceLine = (XDDFLineChartData.Series) lineChartDataVariance.addSeries(chartWeekLabels, chartWeeklyEffortVarianceValues);
            seriesVarianceLine.setTitle("Weekly Effort Variance", null);
            varianceLineChart.plot(lineChartDataVariance);

            XDDFChartLegend legendVarianceLine = varianceLineChart.getOrAddLegend();
            legendVarianceLine.setPosition(LegendPosition.BOTTOM);

            currentFreeRow = weeklyVarianceChartStartRow + 28; // Mettre à jour l'index de ligne après le graphique

            // === Month Headers (Row 25 in image) ===
            // Note: Ce tableau peut nécessiter des données consolidées pour TOUS les mois de l'année
            // pour correspondre parfaitement à la capture 1, ce qui n'est pas fourni par weeklyReports (limité au mois du PSR).
            // Nous allons le remplir avec la valeur du mois actuel et zéro pour les autres.
            EffortVarianceRow++; // Ligne vide
            int monthHeaderRowIndex = EffortVarianceRow;
            Row monthHeaderRow = weeklySheet.createRow(monthHeaderRowIndex);
            String[] monthHeaders = {"Month", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

            for (int i = 0; i < monthHeaders.length; i++) {
                Cell cell = monthHeaderRow.createCell(i);
                cell.setCellValue(monthHeaders[i]);
                cell.setCellStyle(headerStyleWeekly);
                weeklySheet.setColumnWidth(i, 10 * 256);
            }
            weeklySheet.setColumnWidth(0, 15 * 256);
            EffortVarianceRow++; // Ligne des données mensuelles consolidées

            // === Monthly Consolidated Effort Variance Data (Row 26 in image) ===
            Row monthlyEffortVarianceDataRow = weeklySheet.createRow(EffortVarianceRow);
            EffortVarianceRow++;
            Cell monthlyConsolidatedLabelCell = monthlyEffortVarianceDataRow.createCell(0);
            monthlyConsolidatedLabelCell.setCellValue("Consolidated Effort Variance");
            monthlyConsolidatedLabelCell.setCellStyle(dataCellStyle);

            // Populer la variance consolidée pour le mois actuel du PSR
            // psrMonthValue (1-12) correspond à l'index de colonne B (1) à M (12)
            int currentMonthColForChart = psrMonthValue;
            Cell currentMonthConsolidatedCell = monthlyEffortVarianceDataRow.createCell(currentMonthColForChart);
            currentMonthConsolidatedCell.setCellValue(monthlyConsolidatedEffortVariance);
            currentMonthConsolidatedCell.setCellStyle(monthlyConsolidatedEffortVariance != 0 ? nonZeroVarianceStyle : dataCellStyle);

            // Remplir les autres mois avec 0 si aucune donnée n'est disponible (pour le graphique)
            for (int i = 1; i <= 12; i++) { // De janvier (col 1) à décembre (col 12)
                if (i != currentMonthColForChart) {
                    Cell cell = monthlyEffortVarianceDataRow.createCell(i);
                    cell.setCellValue(0.0); // Ou Cell.setBlank() si vous préférez des cellules vides
                    cell.setCellStyle(dataCellStyle);
                }
            }

            // === Monthly Consolidated Effort Variance Line Chart ===
            XSSFDrawing drawingMonthlyVarianceLine = (XSSFDrawing) weeklySheet.createDrawingPatriarch();
            // L'ancre du graphique est à côté du graphique Weekly Effort Variance
            XSSFClientAnchor anchorMonthlyVarianceLine = drawingMonthlyVarianceLine.createAnchor(0, 0, 0, 0, 8, 21, 15, 35);

            XSSFChart monthlyVarianceLineChart = drawingMonthlyVarianceLine.createChart(anchorMonthlyVarianceLine);
            monthlyVarianceLineChart.setTitleText("Monthly Consolidated Effort Variance");
            monthlyVarianceLineChart.setTitleOverlay(false);

            XDDFCategoryAxis bottomAxisMonthlyVarianceLine = monthlyVarianceLineChart.createCategoryAxis(AxisPosition.BOTTOM);
            XDDFValueAxis leftAxisMonthlyVarianceLine = monthlyVarianceLineChart.createValueAxis(AxisPosition.LEFT);
            leftAxisMonthlyVarianceLine.setCrosses(AxisCrosses.AUTO_ZERO);
            leftAxisMonthlyVarianceLine.setMinimum(-0.10); // -10%
            leftAxisMonthlyVarianceLine.setMaximum(0.10);  // 10%

            XDDFLineChartData lineChartDataMonthlyVariance = (XDDFLineChartData) monthlyVarianceLineChart.createData(ChartTypes.LINE, bottomAxisMonthlyVarianceLine, leftAxisMonthlyVarianceLine);

            XDDFDataSource<String> chartMonthLabelsMonthly = XDDFDataSourcesFactory.fromStringCellRange(
                    (XSSFSheet) weeklySheet,
                    new CellRangeAddress(monthHeaderRow.getRowNum(), monthHeaderRow.getRowNum(), 1, 12) // Étiquettes de mois
            );
            XDDFNumericalDataSource<Double> chartMonthlyEffortVarianceValuesMonthly = XDDFDataSourcesFactory.fromNumericCellRange(
                    (XSSFSheet) weeklySheet,
                    new CellRangeAddress(monthlyEffortVarianceDataRow.getRowNum(), monthlyEffortVarianceDataRow.getRowNum(), 1, 12) // Valeurs
            );

            XDDFLineChartData.Series seriesMonthlyVarianceLine = (XDDFLineChartData.Series) lineChartDataMonthlyVariance.addSeries(chartMonthLabelsMonthly, chartMonthlyEffortVarianceValuesMonthly);
            seriesMonthlyVarianceLine.setTitle("Monthly Consolidated Effort Variance", null);
            monthlyVarianceLineChart.plot(lineChartDataMonthlyVariance);

            XDDFChartLegend legendMonthlyVarianceLine = monthlyVarianceLineChart.getOrAddLegend();
            legendMonthlyVarianceLine.setPosition(LegendPosition.BOTTOM);

            // --- Ajuster l'index de ligne pour la section Actions ---
            // S'assurer que currentFreeRow est après tous les éléments précédents
            currentFreeRow = Math.max(currentFreeRow, weeklyVarianceChartStartRow + 28); // Après le premier graphique
            currentFreeRow = Math.max(currentFreeRow, monthlyEffortVarianceDataRow.getRowNum() + 1); // Après le tableau des données mensuelles

            // Lignes vides avant la section Actions
            weeklySheet.createRow(currentFreeRow);
            currentFreeRow++;
            weeklySheet.createRow(currentFreeRow);
            currentFreeRow++;

            // === Actions Section ===
            Row actionsRow = weeklySheet.createRow(currentFreeRow);
            Cell actionsTitleCell = actionsRow.createCell(0);
            actionsTitleCell.setCellValue("Actions");
            //weeklySheet.addMergedRegion(new CellRangeAddress(currentFreeRow, currentFreeRow, 0, 15));
            safeMergeRegion(weeklySheet, currentFreeRow, currentFreeRow, 0, 12);
            actionsTitleCell.setCellStyle(sectionTitleStyle);
            for (int col = 0; col <= 15; col++) {
                Cell cell = actionsRow.getCell(col);
                if (cell == null) cell = actionsRow.createCell(col);
                cell.setCellStyle(sectionTitleStyle);
            }
            currentFreeRow++;

            // === Actions Table Headers ===
            Row actionsHeaderRow = weeklySheet.createRow(currentFreeRow);
            String[] actionsHeaders = {"Action ID", "Priority", "Origin", "Who", "Subject", "Open Date", "Close Date", "Status", "Effectiveness Criteria", "Effectiveness", "Comments"};

            for (int i = 0; i < actionsHeaders.length; i++) {
                Cell cell = actionsHeaderRow.createCell(i);
                cell.setCellValue(actionsHeaders[i]);
                cell.setCellStyle(headerStyleWeekly);
                if (i == 0) weeklySheet.setColumnWidth(i, 15 * 256);
                else if (i == 1) weeklySheet.setColumnWidth(i, 15 * 256);
                else if (i == 2) weeklySheet.setColumnWidth(i, 20 * 256);
                else if (i == 3) weeklySheet.setColumnWidth(i, 20 * 256);
                else if (i == 4) weeklySheet.setColumnWidth(i, 30 * 256);
                else if (i == 5) weeklySheet.setColumnWidth(i, 20 * 256);
                else if (i == 6) weeklySheet.setColumnWidth(i, 20 * 256);
                else if (i == 7) weeklySheet.setColumnWidth(i, 15 * 256);
                else if (i == 8) weeklySheet.setColumnWidth(i, 25 * 256);
                else if (i == 9) weeklySheet.setColumnWidth(i, 20 * 256);
                else if (i == 10) weeklySheet.setColumnWidth(i, 35 * 256);
            }
            currentFreeRow++;

            // === Empty rows for actions data ===
            for (int i = 0; i < 5; i++) {
                Row actionDataRow = weeklySheet.createRow(currentFreeRow + i);
                for (int j = 0; j < actionsHeaders.length; j++) {
                    Cell cell = actionDataRow.createCell(j);
                    cell.setCellStyle(dataCellStyle);
                }
            }


//***************************************************************************************************************************************************
// === TASKS TRACKER SHEET ===
            Sheet taskSheet = workbook.createSheet("Tasks Tracker");
            taskSheet.setDisplayGridlines(false);
            taskSheet.setPrintGridlines(false);

// === Freeze les 3 premières colonnes ===
            taskSheet.createFreezePane(3, 3);
// → 3 colonnes figées (A, B, C) et 3 lignes (titre + header)




// === Titre "Tasks Tracker" ===
            Row taskTitleRow = taskSheet.createRow(0);
            taskTitleRow.setHeightInPoints(35);
            Cell taskTitleCell = taskTitleRow.createCell(0);
            taskTitleCell.setCellValue("Tasks Tracker");

            CellStyle taskTitleStyle = workbook.createCellStyle();
            Font taskTitleFont = workbook.createFont();
            taskTitleFont.setFontHeightInPoints((short) 20);
            taskTitleFont.setBold(true);
            taskTitleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            taskTitleStyle.setFont(taskTitleFont);
            taskTitleStyle.setAlignment(HorizontalAlignment.CENTER);
            taskTitleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            taskTitleStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            taskTitleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            taskSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 15));
            taskTitleCell.setCellStyle(taskTitleStyle);

// === Headers ===
            Row taskHeader = taskSheet.createRow(2); // Ligne 4
            taskHeader.setHeightInPoints(25);

            CellStyle taskHeaderStyle = workbook.createCellStyle();
            Font taskHeaderFont = workbook.createFont();
            taskHeaderFont.setBold(true);
            taskHeaderFont.setColor(IndexedColors.BLACK.getIndex());
            taskHeaderFont.setFontHeightInPoints((short) 10);
            taskHeaderStyle.setFont(taskHeaderFont);
            taskHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
            taskHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

// Couleur personnalisée #99ccff
            byte[] rgbTask = new byte[] { (byte) 153, (byte) 204, (byte) 255 };
            XSSFColor customTaskBlue = new XSSFColor(rgbTask, null);
            ((XSSFCellStyle) taskHeaderStyle).setFillForegroundColor(customTaskBlue);
            taskHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            taskHeaderStyle.setBorderTop(BorderStyle.THIN);
            taskHeaderStyle.setBorderBottom(BorderStyle.THIN);
            taskHeaderStyle.setBorderLeft(BorderStyle.THIN);
            taskHeaderStyle.setBorderRight(BorderStyle.THIN);

            String[] taskHeaders = {
                    "Task ID", "Project ID", "Description", "Week", "Who", "Start Date", "Estimated End Date",
                    "Effective End Date", "Worked (MD)", "Estimated (MD)", "Remaining (MD)", "Progress (%)",
                    "Current Status", "Effort Variance", "Reason of deviation", "Note"
            };

// Appliquer le header + setColumnWidth
            for (int i = 0; i < taskHeaders.length; i++) {
                Cell cell = taskHeader.createCell(i);
                cell.setCellValue(taskHeaders[i]);
                cell.setCellStyle(taskHeaderStyle);

                if (i == 0) { // Task ID
                    taskSheet.setColumnWidth(i, 15 * 256);
                } else if (i == 1) { // Project ID
                    taskSheet.setColumnWidth(i, 15 * 256);
                } else if (i == 2) { // Description
                    taskSheet.setColumnWidth(i, 40 * 256);
                } else {
                    // Colonnes dynamiques → initial width
                    taskSheet.setColumnWidth(i, 18 * 256);
                }
            }
// === STYLES COULEURS ===
            XSSFCellStyle positiveStyle = (XSSFCellStyle) workbook.createCellStyle();
            positiveStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            positiveStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            positiveStyle.setBorderTop(BorderStyle.THIN);
            positiveStyle.setBorderBottom(BorderStyle.THIN);
            positiveStyle.setBorderLeft(BorderStyle.THIN);
            positiveStyle.setBorderRight(BorderStyle.THIN);

            XSSFCellStyle negativeStyle = (XSSFCellStyle) workbook.createCellStyle();
            negativeStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
            negativeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            negativeStyle.setBorderTop(BorderStyle.THIN);
            negativeStyle.setBorderBottom(BorderStyle.THIN);
            negativeStyle.setBorderLeft(BorderStyle.THIN);
            negativeStyle.setBorderRight(BorderStyle.THIN);


// === Style des données ===
            CellStyle taskDataStyle = workbook.createCellStyle();
            Font taskDataFont = workbook.createFont();
            taskDataFont.setFontHeightInPoints((short) 11);
            taskDataStyle.setFont(taskDataFont);
            taskDataStyle.setBorderTop(BorderStyle.THIN);
            taskDataStyle.setBorderBottom(BorderStyle.THIN);
            taskDataStyle.setBorderLeft(BorderStyle.THIN);
            taskDataStyle.setBorderRight(BorderStyle.THIN);
            taskDataStyle.setVerticalAlignment(VerticalAlignment.TOP);
            taskDataStyle.setAlignment(HorizontalAlignment.LEFT);
            taskDataStyle.setWrapText(true);

// === Remplissage des données ===
            List<TaskTrackerDTO> tasks = psr.getTaskTrackers();
            if (tasks != null && !tasks.isEmpty()) {
                for (int i = 0; i < tasks.size(); i++) {
                    Row row = taskSheet.createRow(i + 3); // Ligne 5
                    row.setHeightInPoints(30);

                    TaskTrackerDTO t = tasks.get(i);

                    row.createCell(0).setCellValue(t.getId());
                    row.createCell(1).setCellValue(t.getProjectId());
                    row.createCell(2).setCellValue(t.getDescription());
                    row.createCell(3).setCellValue(t.getWeek());
                    row.createCell(4).setCellValue(t.getWho());
                    row.createCell(5).setCellValue(t.getStartDate() != null ? t.getStartDate().toString() : "");
                    row.createCell(6).setCellValue(t.getEstimatedEndDate() != null ? t.getEstimatedEndDate().toString() : "");
                    row.createCell(7).setCellValue(t.getEffectiveEndDate() != null ? t.getEffectiveEndDate().toString() : "");
                    row.createCell(8).setCellValue(t.getWorkedMD() != null ? t.getWorkedMD() : 0);
                    row.createCell(9).setCellValue(t.getEstimatedMD() != null ? t.getEstimatedMD() : 0);
                    row.createCell(10).setCellValue(t.getRemainingMD() != null ? t.getRemainingMD() : 0);
                    row.createCell(11).setCellValue((t.getProgress() != null ? t.getProgress() : 0) + "%");
                    row.createCell(12).setCellValue(t.getCurrentStatus());
                    double effort = t.getEffortVariance() != null ? t.getEffortVariance() : 0;

                    Cell effortCell = row.createCell(13);
                    effortCell.setCellValue(effort);

// Appliquer le style selon le signe
                    if (effort < 0) {
                        effortCell.setCellStyle(negativeStyle); // ✅ Vert
                    } else {
                        effortCell.setCellStyle(positiveStyle); // ✅ Rouge
                    }

                    row.createCell(14).setCellValue(t.getDeviationReason());
                    row.createCell(15).setCellValue(t.getNote());

                    // Appliquer style à chaque cellule
                    for (int j = 0; j < taskHeaders.length; j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null) {
                            cell.setCellStyle(taskDataStyle);
                        }
                    }
                }
            }

// Auto-size → uniquement pour colonnes dynamiques (3 → fin)
            for (int j = 3; j < taskHeaders.length; j++) {
                taskSheet.autoSizeColumn(j);
            }

// === Appliquer un filtre automatique sur toute la ligne d’en-tête ===
            taskSheet.setAutoFilter(new CellRangeAddress(2, 2, 0, taskHeaders.length - 1));
//*******************************************************************************************************************************************************************************
            // === Ligne 1 : Titre "Risks" centré fond gris ===
            Sheet risksSheet = workbook.createSheet("Risks");
            risksSheet.setDisplayGridlines(false);
            risksSheet.setPrintGridlines(false);
            XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
            // Motif décoratif pour cellule A4 et A5
            XSSFCellStyle patternStyle = xssfWorkbook.createCellStyle();
            patternStyle.setFillPattern(FillPatternType.FINE_DOTS);
            patternStyle.setFillForegroundColor(IndexedColors.BLACK.getIndex());
            patternStyle.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
            patternStyle.setBorderTop(BorderStyle.THIN);
            patternStyle.setBorderBottom(BorderStyle.THIN);
            patternStyle.setBorderLeft(BorderStyle.THIN);
            patternStyle.setBorderRight(BorderStyle.THIN);

// === Style: Avertissement rouge (B4:F5)
            XSSFCellStyle warningStyle = xssfWorkbook.createCellStyle();
            XSSFFont warningFont = xssfWorkbook.createFont();
            warningFont.setBold(true);
            warningFont.setFontHeightInPoints((short) 10);
            warningFont.setColor(IndexedColors.RED.getIndex());
            warningStyle.setFont(warningFont);
            warningStyle.setAlignment(HorizontalAlignment.LEFT);
            warningStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            warningStyle.setWrapText(true);
            warningStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            warningStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            warningStyle.setBorderTop(BorderStyle.THIN);
            warningStyle.setBorderBottom(BorderStyle.THIN);
            warningStyle.setBorderLeft(BorderStyle.THIN);
            warningStyle.setBorderRight(BorderStyle.THIN);

            Row titleRowRisk = risksSheet.createRow(0);
            titleRowRisk.setHeightInPoints(30);
            Cell titleCellRisk = titleRowRisk.createCell(1); // COLONNE B
            titleCellRisk.setCellValue("Risks");

            XSSFCellStyle titleStyleRisk = xssfWorkbook.createCellStyle();
            XSSFFont titleFontRisk = xssfWorkbook.createFont();
            titleFontRisk.setBold(true);
            titleFontRisk.setFontHeightInPoints((short) 30);
            titleFontRisk.setColor(IndexedColors.DARK_BLUE.getIndex());
            titleFontRisk.setColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyleRisk.setFont(titleFontRisk);
            titleStyleRisk.setAlignment(HorizontalAlignment.CENTER);
            titleStyleRisk.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyleRisk.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            titleStyleRisk.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            risksSheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 18)); // Fusion B → U (index 20)
            titleCellRisk.setCellStyle(titleStyleRisk);
            XSSFColor riskBlue = new XSSFColor(new byte[]{(byte) 217, (byte) 225, (byte) 242}, null);      // Risk Identification (bleu gris)
            XSSFColor yellow = new XSSFColor(new byte[]{(byte) 255, (byte) 192, (byte) 0}, null);           // Risk Analysis + Assessment (jaune)
            XSSFColor turquoise = new XSSFColor(new byte[]{(byte) 184, (byte) 204, (byte) 228}, null);      // Risk Treatment (bleu clair)

// === Lignes 2-5 : espaces ===
            risksSheet.createRow(1);
            risksSheet.createRow(2);
            // === Ligne 4 : Message rouge ===
            // === Lignes 4 et 5 (Avertissement)
            Row row4 = risksSheet.createRow(3);
            Row row5 = risksSheet.createRow(4);
            row4.setHeightInPoints(15);
            row5.setHeightInPoints(15);

// Cellules A4, A5 → motif
            Cell a4 = row4.createCell(0);
            a4.setCellStyle(patternStyle);
            Cell a5 = row5.createCell(0);
            a5.setCellStyle(patternStyle);

            risksSheet.addMergedRegion(new CellRangeAddress(3, 4, 0, 0)); // ✅ A4:A5

// Cellule B4 (texte principal)
            Cell b4 = row4.createCell(1);
            b4.setCellValue("Please do not modify cells with this pattern, values or formulas - Please do not insert rows / columns");
            b4.setCellStyle(warningStyle);

// Fusionner B4 → F5 (index colonne 1 à 5)
            risksSheet.addMergedRegion(new CellRangeAddress(3, 4, 1, 5));

// Appliquer warningStyle aux cellules fusionnées
            for (int rowIdx = 3; rowIdx <= 4; rowIdx++) {
                Row row = risksSheet.getRow(rowIdx);
                for (int col = 1; col <= 5; col++) {
                    Cell cell = row.getCell(col);
                    if (cell == null) cell = row.createCell(col);
                    cell.setCellStyle(warningStyle);
                }
            }

// === Ligne 6 : Sections ===
            risksSheet.createRow(5);

            Row sectionRow = risksSheet.createRow(6); // Ligne 7 visuelle
            sectionRow.setHeightInPoints(20);

            XSSFCellStyle sectionHeaderStyleRisk = xssfWorkbook.createCellStyle();
            XSSFFont sectionFontRisk = xssfWorkbook.createFont();
            sectionFontRisk.setBold(true);
            sectionHeaderStyleRisk.setFont(sectionFontRisk);
            sectionHeaderStyleRisk.setAlignment(HorizontalAlignment.CENTER);
            sectionHeaderStyleRisk.setVerticalAlignment(VerticalAlignment.CENTER);
            sectionHeaderStyleRisk.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            sectionHeaderStyleRisk.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            sectionHeaderStyleRisk.setBorderTop(BorderStyle.THIN);
            sectionHeaderStyleRisk.setBorderBottom(BorderStyle.THIN);
            sectionHeaderStyleRisk.setBorderLeft(BorderStyle.THIN);
            sectionHeaderStyleRisk.setBorderRight(BorderStyle.THIN);
            XSSFCellStyle sectionRiskIdentification = xssfWorkbook.createCellStyle();
            sectionRiskIdentification.cloneStyleFrom(sectionHeaderStyleRisk);
            sectionRiskIdentification.setFillForegroundColor(riskBlue);
            sectionRiskIdentification.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle sectionRiskAnalysis = xssfWorkbook.createCellStyle();
            sectionRiskAnalysis.cloneStyleFrom(sectionHeaderStyleRisk);
            sectionRiskAnalysis.setFillForegroundColor(yellow);
            sectionRiskAnalysis.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle sectionRiskAssessment = xssfWorkbook.createCellStyle();
            sectionRiskAssessment.cloneStyleFrom(sectionHeaderStyleRisk);
            sectionRiskAssessment.setFillForegroundColor(yellow);
            sectionRiskAssessment.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle sectionRiskTreatment = xssfWorkbook.createCellStyle();
            sectionRiskTreatment.cloneStyleFrom(sectionHeaderStyleRisk);
            sectionRiskTreatment.setFillForegroundColor(turquoise);
            sectionRiskTreatment.setFillPattern(FillPatternType.SOLID_FOREGROUND);

// Risk Identification (B→J)
            CellRangeAddress idRange = new CellRangeAddress(6, 6, 1, 9);
            risksSheet.addMergedRegion(idRange);
            Cell idCell = sectionRow.createCell(1);
            idCell.setCellValue("Risk Identification");
            applyStyleToMergedRegion(risksSheet, idRange, sectionRiskIdentification);

// Risk Analysis (K→L)
            CellRangeAddress analysisRange = new CellRangeAddress(6, 6, 10, 11);
            risksSheet.addMergedRegion(analysisRange);
            Cell analysisCell = sectionRow.createCell(10);
            analysisCell.setCellValue("Risk analysis");
            applyStyleToMergedRegion(risksSheet, analysisRange, sectionRiskAnalysis);

// Risk Assessment (M→N)
            CellRangeAddress assessmentRange = new CellRangeAddress(6, 6, 12, 13);
            risksSheet.addMergedRegion(assessmentRange);
            Cell assessmentCell = sectionRow.createCell(12);
            assessmentCell.setCellValue("Risk assessment");
            applyStyleToMergedRegion(risksSheet, assessmentRange, sectionRiskAssessment);

// Risk Treatment (O→U)
            CellRangeAddress treatmentRange = new CellRangeAddress(6, 6, 14, 18); // Fusion O → U (index 20)
            risksSheet.addMergedRegion(treatmentRange);
            Cell treatmentCell = sectionRow.createCell(14);
            treatmentCell.setCellValue("Risk treatment");
            applyStyleToMergedRegion(risksSheet, treatmentRange, sectionRiskTreatment);

// === Ligne 7 : HEADERS ===
            String[] riskHeaders = {
                    "Id", "Risk", "Origin", "Category", "Open Date", "Due Date", "Causes", "Consequences", "Applied measures",
                    "Probability", "Gravity", "Criticality", "Measure",
                    "Risk Treatment Decision", "Justification", "Id Action", "Risk state", "Close Date"
            };

            Row headerRowRisk = risksSheet.createRow(7); // Ligne 8 visuelle (index 7)

            XSSFCellStyle headerStyleRisk = xssfWorkbook.createCellStyle();
            XSSFFont boldFontRis = xssfWorkbook.createFont();
            boldFontRis.setBold(true);
            boldFontRis.setFontHeightInPoints((short) 10); // Adjusted font size to 10
            headerStyleRisk.setFont(boldFontRis);
            headerStyleRisk.setAlignment(HorizontalAlignment.CENTER);
            headerStyleRisk.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyleRisk.setBorderBottom(BorderStyle.THIN);
            headerStyleRisk.setBorderTop(BorderStyle.THIN);
            headerStyleRisk.setBorderLeft(BorderStyle.THIN);
            headerStyleRisk.setBorderRight(BorderStyle.THIN);

            // Re-using defined XSSFColors for section headers to match visual
            XSSFCellStyle headerRiskIdentification = xssfWorkbook.createCellStyle();
            headerRiskIdentification.cloneStyleFrom(headerStyleRisk);
            headerRiskIdentification.setFillForegroundColor(riskBlue); // Uses riskBlue
            headerRiskIdentification.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle headerRiskAnalysisAssessment = xssfWorkbook.createCellStyle();
            headerRiskAnalysisAssessment.cloneStyleFrom(headerStyleRisk);
            headerRiskAnalysisAssessment.setFillForegroundColor(yellow); // Uses yellow
            headerRiskAnalysisAssessment.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle headerRiskTreatment = xssfWorkbook.createCellStyle();
            headerRiskTreatment.cloneStyleFrom(headerStyleRisk);
            headerRiskTreatment.setFillForegroundColor(turquoise); // Uses turquoise
            headerRiskTreatment.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < riskHeaders.length; i++) {
                Cell cell = headerRowRisk.createCell(i + 1); // Commencer en COLONNE B !
                cell.setCellValue(riskHeaders[i]);

                // Apply styles based on section
                if (i >= 0 && i <= 8) { // Id to Applied measures (Risk Identification)
                    cell.setCellStyle(headerRiskIdentification);
                } else if (i >= 9 && i <= 12) { // Probability to Measure (Risk Analysis/Assessment)
                    cell.setCellStyle(headerRiskAnalysisAssessment);
                } else if (i >= 13 && i <= 17) { // Risk Treatment Decision to Close Date (Risk Treatment)
                    cell.setCellStyle(headerRiskTreatment);
                }
            }
            risksSheet.setAutoFilter(new CellRangeAddress(
                    headerRowRisk.getRowNum(),
                    headerRowRisk.getRowNum(),
                    1,                           // Colonne de début (B)
                    18                          // Colonne de fin (U - index 20)
            ));
// ... existing code ...
            // Set precise column widths for Risks sheet to match image
            risksSheet.setColumnWidth(0, 5 * 256);   // Column A (index 0)
            risksSheet.setColumnWidth(1, 10 * 256);  // Id (B)
            risksSheet.setColumnWidth(2, 40 * 256);  // Risk (C)
            risksSheet.setColumnWidth(3, 20 * 256);  // Origin (D)
            risksSheet.setColumnWidth(4, 20 * 256);  // Category (E)
            risksSheet.setColumnWidth(5, 20 * 256);  // Open Date (F)
            risksSheet.setColumnWidth(6, 20 * 256);  // Due Date (G)
            risksSheet.setColumnWidth(7, 45 * 256);  // Causes (H)
            risksSheet.setColumnWidth(8, 45 * 256);  // Consequences (I)
            risksSheet.setColumnWidth(9, 45 * 256);  // Applied measures (J)
            risksSheet.setColumnWidth(10, 20 * 256); // Probability (K)
            risksSheet.setColumnWidth(11, 20 * 256); // Gravity (L)
            risksSheet.setColumnWidth(12, 20 * 256); // Criticality (M)
            risksSheet.setColumnWidth(13, 20 * 256); // Measure (N)
            risksSheet.setColumnWidth(14, 30 * 256); // Risk Treatment Decision (O)
            risksSheet.setColumnWidth(15, 30 * 256); // Justification (P)
            risksSheet.setColumnWidth(16, 30 * 256); // Id Action (Q)
            risksSheet.setColumnWidth(17, 30 * 256); // Risk state (R)
            risksSheet.setColumnWidth(18, 30 * 256); // Close Date (S)
            risksSheet.setColumnWidth(19, 10 * 256); // Placeholder for T (not explicitly in headers)
            risksSheet.setColumnWidth(20, 10 * 256); // Placeholder for U (not explicitly in headers)

            // Style pour les données
            XSSFCellStyle dataStyle = xssfWorkbook.createCellStyle();
            dataStyle.setWrapText(true);
            dataStyle.setVerticalAlignment(VerticalAlignment.TOP);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
// === Récupérer la liste des risques ===
           List<RisksDTO> risks = psr.getRisks();
          // Boucle d'insertion
            if (risks != null) {
                for (int i = 0; i < risks.size(); i++) {
                    RisksDTO r = risks.get(i);
                    System.out.println("Risk " + i + ": " + r.getId() + ", " + r.getDescription());
                    Row row = risksSheet.createRow(i + 8); // Commencer ligne 9
                    String[] values = {
                            r.getId() != null ? String.valueOf(r.getId()) : "",
                            r.getDescription(), r.getOrigin(), r.getCategory(),
                            r.getOpenDate(), r.getDueDate(), r.getCauses(), r.getConsequences(), r.getAppliedMeasures(),
                            r.getProbability(), r.getGravity(), r.getCriticality(), r.getMeasure(),
                            r.getRiskTreatmentDecision(), r.getJustification(), r.getIdAction(),
                            r.getRiskStat(), r.getCloseDate()
                    };

                    for (int j = 0; j < values.length; j++) {
                        Cell cell = row.createCell(j + 1);
                        cell.setCellValue(values[j] != null ? values[j] : "");
                        cell.setCellStyle(dataStyle);
                    }
                }
            }

            //-------------------------------------------------------------------------------------------------------------------------
            // === Feuille DELIVERIES ===
            Sheet deliveriesSheet = workbook.createSheet("Deliveries");
            deliveriesSheet.setDisplayGridlines(false);
            deliveriesSheet.setPrintGridlines(false);
            XSSFWorkbook xssfWorkbookDel = (XSSFWorkbook) workbook;

// === Ligne 1 : Titre "Project Deliveries" ===
            Row titleRowDeliveries = deliveriesSheet.createRow(0);
            titleRowDeliveries.setHeightInPoints(30);
            Cell titleCellDeliveries = titleRowDeliveries.createCell(0); // colonne B
            titleCellDeliveries.setCellValue("Project Deliveries");

            XSSFCellStyle titleStyleDeliveries = xssfWorkbookDel.createCellStyle();
            XSSFFont titleFontDeliveries = xssfWorkbookDel.createFont();
            titleFontDeliveries.setBold(true);
            titleFontDeliveries.setFontHeightInPoints((short) 22);
            titleFontDeliveries.setColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyleDeliveries.setFont(titleFontDeliveries);
            titleStyleDeliveries.setAlignment(HorizontalAlignment.CENTER);
            titleStyleDeliveries.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyleDeliveries.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            titleStyleDeliveries.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            deliveriesSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8)); // Fusion B → J
            titleCellDeliveries.setCellStyle(titleStyleDeliveries);

// === Ligne 2 : vide (espace)
            deliveriesSheet.createRow(1);

// === Ligne 3 : Headers ===
            Row deliveryHeader = deliveriesSheet.createRow(2); // ligne 3 (index 2)
            deliveryHeader.setHeightInPoints(20);
            String[] deliveryHeaders = {"ID", "Deliverable", "Description", "Version", "Planned date", "Effective date", "Status", "Delivery Support", "Customer Feedback"};

            XSSFCellStyle headerStyleDeli = xssfWorkbookDel.createCellStyle();
            XSSFFont headerFontDeli = xssfWorkbookDel.createFont();
            headerFontDeli.setBold(true);
            headerFontDeli.setFontHeightInPoints((short) 12);
            headerFontDeli.setColor(IndexedColors.BLACK.getIndex());
            headerStyleDeli.setFont(headerFontDeli);
            headerStyleDeli.setAlignment(HorizontalAlignment.CENTER);
            headerStyleDeli.setVerticalAlignment(VerticalAlignment.CENTER);
            // === Couleur personnalisée : #99ccff ===
            byte[] rgbDeli = new byte[] { (byte) 153, (byte) 204, (byte) 255 };
            XSSFColor customDeliBlue = new XSSFColor(rgbDeli, null);

            headerStyleDeli.setFillForegroundColor(customDeliBlue);
            headerStyleDeli.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            headerStyleDeli.setBorderTop(BorderStyle.THIN);
            headerStyleDeli.setBorderBottom(BorderStyle.THIN);
            headerStyleDeli.setBorderLeft(BorderStyle.THIN);
            headerStyleDeli.setBorderRight(BorderStyle.THIN);

// === Style de base ===
            XSSFCellStyle baseStyle = xssfWorkbookDel.createCellStyle();
            baseStyle.setBorderTop(BorderStyle.THIN);
            baseStyle.setBorderBottom(BorderStyle.THIN);
            baseStyle.setBorderLeft(BorderStyle.THIN);
            baseStyle.setBorderRight(BorderStyle.THIN);
            baseStyle.setVerticalAlignment(VerticalAlignment.TOP);
            baseStyle.setWrapText(true);
            XSSFFont baseFont = xssfWorkbookDel.createFont();
            baseFont.setColor(IndexedColors.BLACK.getIndex());
            baseStyle.setFont(baseFont);

// === Style vert ===
            XSSFCellStyle greenStyle = xssfWorkbookDel.createCellStyle();
            greenStyle.cloneStyleFrom(baseStyle);
            greenStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            greenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

// === Style rouge ===
            XSSFCellStyle redStyle = xssfWorkbookDel.createCellStyle();
            redStyle.cloneStyleFrom(baseStyle);
            redStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            redStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

// === Appliquer les en-têtes ===
            for (int i = 0; i < deliveryHeaders.length; i++) {
                Cell cell = deliveryHeader.createCell(i);
                cell.setCellValue(deliveryHeaders[i]);
                cell.setCellStyle(headerStyleDeli);

                // Largeur colonnes :
                if (i == 1 || i == 2) { // Deliverable, Description
                    deliveriesSheet.setColumnWidth(i + 1, 35 * 256);
                } else {
                    deliveriesSheet.setColumnWidth(i + 1, 20 * 256);
                }
            }

// === Remplissage des données ===
            List<DeliveriesDTO> deliveries = psr.getDeliveries();
            if (deliveries != null) {
                for (int i = 0; i < deliveries.size(); i++) {
                    DeliveriesDTO d = deliveries.get(i);
                    Row row = deliveriesSheet.createRow(i + 3); // données commencent ligne 4 (index 3)

                    String status = d.getStatus();
                    String feedback = d.getCustomerFeedback();

                    XSSFCellStyle rowStyle = baseStyle;
                    if ("Delivered".equalsIgnoreCase(status)) {
                        if ("Accepted".equalsIgnoreCase(feedback)) {
                            rowStyle = greenStyle;
                        } else if ("Refused".equalsIgnoreCase(feedback)) {
                            rowStyle = redStyle;
                        }
                    }

                    String[] values = {
                            d.getId() != null ? String.valueOf(d.getId()) : "",
                            d.getDeliveriesName(),
                            d.getDescription(),
                            d.getVersion(),
                            d.getPlannedDate() != null ? d.getPlannedDate().toString() : "",
                            d.getEffectiveDate() != null ? d.getEffectiveDate().toString() : "",
                            status,
                            d.getDeliverySupport(),
                            feedback
                    };

                    for (int j = 0; j < values.length; j++) {
                        Cell cell = row.createCell(j);
                        cell.setCellValue(values[j]);
                        cell.setCellStyle(rowStyle);
                    }
                }
            }
// === Auto-filter sur les en-têtes Deliveries ===
            deliveriesSheet.setAutoFilter(new CellRangeAddress(2, 2, 0, deliveryHeaders.length - 1));


            // === 7. PLANNING === (structure simplifiée à adapter)
            Sheet planningSheet = workbook.createSheet("Planning");
            planningSheet.setDisplayGridlines(false);
            planningSheet.setPrintGridlines(false);
            Row planningRow = planningSheet.createRow(0);
            titleRowRisk.setHeightInPoints(25);
            Cell titleCellPlanning = planningRow.createCell(1); // COLONNE B
            titleCellPlanning.setCellValue("Project Plans");

            XSSFCellStyle titleStylePlanning = xssfWorkbook.createCellStyle();
            XSSFFont titleFontPlannin = xssfWorkbook.createFont();
            titleFontPlannin.setBold(true);
            titleFontPlannin.setFontHeightInPoints((short) 20);
            titleFontPlannin.setColor(IndexedColors.DARK_BLUE.getIndex());
            titleStylePlanning.setFont(titleFontPlannin);
            titleStylePlanning.setAlignment(HorizontalAlignment.CENTER);
            titleStylePlanning.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStylePlanning.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            titleStylePlanning.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            planningSheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 21)); // Fusion B → V (ajuster)
            titleCellPlanning.setCellStyle(titleStylePlanning);
            // Export final
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private void applyStyleToMergedRegion(Sheet sheet, CellRangeAddress range, CellStyle style) {
        for (int row = range.getFirstRow(); row <= range.getLastRow(); row++) {
            Row sheetRow = sheet.getRow(row);
            if (sheetRow == null) sheetRow = sheet.createRow(row);
            for (int col = range.getFirstColumn(); col <= range.getLastColumn(); col++) {
                Cell cell = sheetRow.getCell(col);
                if (cell == null) cell = sheetRow.createCell(col);
                cell.setCellStyle(style);
            }
        }
    }
    private void safeMergeRegion(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        // Vérifie que la région à fusionner est valide (au moins 2 cellules)
        if ((lastRow > firstRow) || (lastCol > firstCol)) {
            // Vérifie que toutes les cellules existent
            for (int rowNum = firstRow; rowNum <= lastRow; rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) row = sheet.createRow(rowNum);
                for (int colNum = firstCol; colNum <= lastCol; colNum++) {
                    Cell cell = row.getCell(colNum);
                    if (cell == null) row.createCell(colNum);
                }
            }
            // Vérifie si la région n'est pas déjà fusionnée
            boolean alreadyMerged = false;
            for (CellRangeAddress range : sheet.getMergedRegions()) {
                if (range.getFirstRow() == firstRow && range.getLastRow() == lastRow &&
                        range.getFirstColumn() == firstCol && range.getLastColumn() == lastCol) {
                    alreadyMerged = true;
                    break;
                }
            }
            if (!alreadyMerged) {
                sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
            }
        }
    }

}