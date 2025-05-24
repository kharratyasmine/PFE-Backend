package com.workpilot.service.PSR;

import com.workpilot.dto.PsrDTO.*;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
public class PsrExportService {

    public ByteArrayInputStream exportPsrToExcel(PsrDTO psr) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Cover Page");
            CreationHelper helper = workbook.getCreationHelper();
            sheet.createFreezePane(0, 2);

            for (int i = 1; i <= 4; i++) {
                sheet.setColumnWidth(i, 25 * 256);
            }

            // === STYLES ===
            Font blueFont = workbook.createFont();
            blueFont.setBold(true);
            blueFont.setColor(IndexedColors.BLUE.getIndex());

            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);

            CellStyle blueBoldStyle = workbook.createCellStyle();
            blueBoldStyle.cloneStyleFrom(borderStyle);
            blueBoldStyle.setFont(blueFont);

            CellStyle sectionStyle = workbook.createCellStyle();
            Font sectionFont = workbook.createFont();
            sectionFont.setBold(true);
            sectionStyle.setFont(sectionFont);
            sectionStyle.setAlignment(HorizontalAlignment.CENTER);
            sectionStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            sectionStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            sectionStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            sectionStyle.cloneStyleFrom(borderStyle);

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.cloneStyleFrom(borderStyle);

            for (int i = 1; i <= 4; i++) {
                Row row = sheet.getRow(i);
                if (row == null) row = sheet.createRow(i);
                Cell cell = row.createCell(1);
                cell.setCellStyle(borderStyle);
            }

            // Logo
            try (InputStream logoStream = new FileInputStream("src/main/resources/static/img/images.jpg")) {
                byte[] logoBytes = IOUtils.toByteArray(logoStream);
                int pictureIdx = workbook.addPicture(logoBytes, Workbook.PICTURE_TYPE_JPEG);
                Drawing<?> drawing = sheet.createDrawingPatriarch();
                ClientAnchor anchor = helper.createClientAnchor();
                anchor.setCol1(1);
                anchor.setRow1(1);
                anchor.setCol2(2);
                anchor.setRow2(5);
                anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);
                Picture pict = drawing.createPicture(anchor, pictureIdx);
                pict.resize();
            }

            for (int i = 1; i <= 3; i++) {
                if (sheet.getRow(i) == null) sheet.createRow(i);
            }

            Row row2 = sheet.getRow(1);
            Cell cellC2 = row2.createCell(2);
            cellC2.setCellValue("Project Status Report");
            sheet.addMergedRegion(new CellRangeAddress(1, 4, 2, 3));
            cellC2.setCellStyle(titleStyle);

            // Bloc Ref / Edition / Date / Author
            Cell cellE2 = row2.createCell(4);
            String blocDroit =
                    "Ref : " + (psr.getReference() != null ? psr.getReference() : "") + "\n" +
                            "Edition : " + (psr.getEdition() != null ? psr.getEdition() : "") + "\n" +
                            "Date : " + (psr.getReportDate() != null ? psr.getReportDate().toString() : "") + "\n" +
                            "Author : " + (psr.getAuthorName() != null ? psr.getAuthorName() : "");

            cellE2.setCellValue(blocDroit);
            sheet.addMergedRegion(new CellRangeAddress(1, 4, 4, 4));

            CellStyle rightBlockStyle = workbook.createCellStyle();
            rightBlockStyle.setWrapText(true);
            rightBlockStyle.setVerticalAlignment(VerticalAlignment.TOP);
            rightBlockStyle.setBorderTop(BorderStyle.THIN);
            rightBlockStyle.setBorderBottom(BorderStyle.THIN);
            rightBlockStyle.setBorderLeft(BorderStyle.THIN);
            rightBlockStyle.setBorderRight(BorderStyle.THIN);
            Font rightFont = workbook.createFont();
            rightFont.setBold(true);
            rightBlockStyle.setFont(rightFont);
            cellE2.setCellStyle(rightBlockStyle);

            // Bas de page
            Row row6 = sheet.createRow(5);
            Cell refTemplateCell = row6.createCell(4);
            refTemplateCell.setCellValue("Ref Template MON_DSS_TM_002_EN 07");

            CellStyle refStyle = workbook.createCellStyle();
            Font refFont = workbook.createFont();
            refFont.setItalic(true);
            refFont.setFontHeightInPoints((short) 9);
            refStyle.setFont(refFont);
            refTemplateCell.setCellStyle(refStyle);

            // Bloc basique infos
            String[][] blockData = {
                    {"Prepared By:", psr.getPreparedBy(), "Date:", String.valueOf(psr.getPreparedByDate())},
                    {"Approved By :", psr.getApprovedBy(), "Date:", String.valueOf(psr.getApprovedByDate())},
                    {"Validated By:", psr.getValidatedBy(), "Date:", String.valueOf(psr.getValidatedByDate())},
                    {"Week:", psr.getWeek(), "Date:", ""},
                    {"Document Status:", psr.getOverallStatus(), "", ""}
            };

            int startRow = 5;
            for (int i = 0; i < blockData.length; i++) {
                Row row = sheet.createRow(startRow + i);
                for (int j = 0; j < 4; j++) {
                    Cell cell = row.createCell(j + 1);
                    cell.setCellValue(blockData[i][j]);
                    if ((j == 0 || j == 2) && !blockData[i][j].isEmpty()) {
                        cell.setCellStyle(blueBoldStyle);
                    } else {
                        cell.setCellStyle(borderStyle);
                    }
                }
            }

            // Project info
            Row row10 = sheet.createRow(11);
            Cell docTypeCell = row10.createCell(1);
            docTypeCell.setCellValue("Document Type:");
            docTypeCell.setCellStyle(blueBoldStyle);
            Cell docTypeValue = row10.createCell(2);
            docTypeValue.setCellValue("Project Status Report");
            docTypeValue.setCellStyle(borderStyle);

            Row row11 = sheet.createRow(12);
            Cell projectLabel = row11.createCell(1);
            projectLabel.setCellValue("Project:");
            projectLabel.setCellStyle(blueBoldStyle);
            Cell projectValue = row11.createCell(2);
            projectValue.setCellValue(psr.getProjectName());
            projectValue.setCellStyle(borderStyle);

            // Summary
            Row row13 = sheet.createRow(13);
            Cell summaryTitle = row13.createCell(1);
            summaryTitle.setCellValue("Summary");
            sheet.addMergedRegion(new CellRangeAddress(13, 13, 1, 4));
            summaryTitle.setCellStyle(sectionStyle);

            Row row14 = sheet.createRow(14);
            Cell summaryContent = row14.createCell(1);
            summaryContent.setCellValue("This document presents a weekly Project Status Report for TELNET team activities on '" +
                    psr.getProjectName() + "' Project");
            sheet.addMergedRegion(new CellRangeAddress(14, 14, 1, 4));
            summaryContent.setCellStyle(borderStyle);

            // Diffusion List
            Row row17 = sheet.createRow(17);
            Cell diffusionTitle = row17.createCell(1);
            diffusionTitle.setCellValue("Diffusion List");
            sheet.addMergedRegion(new CellRangeAddress(17, 17, 1, 4));
            diffusionTitle.setCellStyle(sectionStyle);

            Row headerRow = sheet.createRow(18);
            String[] headers = {"Name", "Location", "Principal List (To)", "Secondary List (Cc)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i + 1);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(sectionStyle);
            }

            String[][] diffusionData = {
                    {"Offshore Responsible", "INGENICO", "", "X"},
                    {"", "INGENICO", "", ""},
                    {"", "INGENICO", "", ""},
                    {"", "INGENICO", "", ""},
                    {"Line Of Business Director", "TELNET", "X", ""}
            };

            int startDataRow = 19;
            for (int i = 0; i < diffusionData.length; i++) {
                Row dataRow = sheet.createRow(startDataRow + i);
                for (int j = 0; j < diffusionData[i].length; j++) {
                    Cell cell = dataRow.createCell(j + 1);
                    cell.setCellValue(diffusionData[i][j]);
                    CellStyle dataStyle = workbook.createCellStyle();
                    dataStyle.cloneStyleFrom(borderStyle);
                    Font font = workbook.createFont();
                    font.setColor(IndexedColors.BLUE.getIndex());
                    dataStyle.setFont(font);
                    cell.setCellStyle(dataStyle);
                }
            }

            // === 2. TEAM ORGANIZATION ===
            // === FEUILLE Team Organization ===
            Sheet teamSheet = workbook.createSheet("Team Organization");

// === Fusion titre ===
            Row titleRow = teamSheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Team Organization");

            CellStyle titleStyleTeam = workbook.createCellStyle();
            Font titleFontTeam = workbook.createFont();
            titleFontTeam.setFontHeightInPoints((short) 16);
            titleFontTeam.setBold(true);
            titleFontTeam.setColor(IndexedColors.VIOLET.getIndex());
            titleStyleTeam.setFont(titleFontTeam);
            titleStyleTeam.setAlignment(HorizontalAlignment.CENTER);
            titleStyleTeam.setVerticalAlignment(VerticalAlignment.CENTER);
            teamSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));
            titleCell.setCellStyle(titleStyleTeam);

// === En-têtes ===
            String[] headersTeam = {
                    "#", "Initial", "Member", "Role", "Project",
                    "Planned Start Date", "Planned End Date", "Allocation (%)",
                    "Coming From Team", "Going To Team", "Holiday"
            };

            Row headerRowTeam = teamSheet.createRow(1);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            for (int i = 0; i < headersTeam.length; i++) {
                Cell cell = headerRowTeam.createCell(i);
                cell.setCellValue(headersTeam[i]);
                cell.setCellStyle(headerStyle);
                teamSheet.setColumnWidth(i, 20 * 256);
            }

// === Contenu ===
            CellStyle contentStyle = workbook.createCellStyle();
            contentStyle.setBorderTop(BorderStyle.THIN);
            contentStyle.setBorderBottom(BorderStyle.THIN);
            contentStyle.setBorderLeft(BorderStyle.THIN);
            contentStyle.setBorderRight(BorderStyle.THIN);
            contentStyle.setVerticalAlignment(VerticalAlignment.TOP);
            contentStyle.setWrapText(true); // pour afficher plusieurs lignes (ex: Holiday)

            List<TeamOrganizationDTO> members = psr.getTeamOrganizations();
            if (members != null) {
                for (int i = 0; i < members.size(); i++) {
                    TeamOrganizationDTO m = members.get(i);
                    Row row = teamSheet.createRow(i + 2); // ligne 2 = index 1 + 1 (header)

                    row.createCell(0).setCellValue(i + 1);
                    row.createCell(1).setCellValue(m.getInitial());
                    row.createCell(2).setCellValue(m.getFullName());
                    row.createCell(3).setCellValue(m.getRole());
                    row.createCell(4).setCellValue(psr.getProjectName());
                    row.createCell(5).setCellValue(m.getPlannedStartDate() != null ? m.getPlannedStartDate().toString() : "");
                    row.createCell(6).setCellValue(m.getPlannedEndDate() != null ? m.getPlannedEndDate().toString() : "");
                    row.createCell(7).setCellValue(m.getAllocation());
                    row.createCell(8).setCellValue(m.getComingFromTeam());
                    row.createCell(9).setCellValue(m.getGoingToTeam());
                    String holidays = String.join("\n", m.getHoliday().split(","));
                    row.createCell(10).setCellValue(holidays);


                    for (int j = 0; j <= 10; j++) {
                        row.getCell(j).setCellStyle(contentStyle);
                    }
                }
            }

            // === 3. WEEKLY REPORT ===
            Sheet weeklySheet = workbook.createSheet("Weekly Report");
            weeklySheet.createRow(0).createCell(0).setCellValue("Monthly Workload By Project / Module");

            // === 4. TASKS TRACKER ===
            Sheet taskSheet = workbook.createSheet("Tasks Tracker");
            Row taskHeader = taskSheet.createRow(0);
            String[] taskHeaders = {"Task ID", "Project ID", "Description", "Week", "Who", "Start Date", "Estimated End Date", "Effective End Date", "Worked (MD)", "Estimated (MD)", "Remaining (MD)", "Progress (%)", "Current Status","Effort Variance" ,"Reason of deviation","Note"};
            for (int i = 0; i < taskHeaders.length; i++) {
                taskHeader.createCell(i).setCellValue(taskHeaders[i]);
            }
            List<TaskTrackerDTO> tasks = psr.getTaskTrackers();
            if (tasks != null) {
                for (int i = 0; i < tasks.size(); i++) {
                    Row row = taskSheet.createRow(i + 1);
                    TaskTrackerDTO t = tasks.get(i);
                    row.createCell(0).setCellValue(t.getId());
                    row.createCell(1).setCellValue(t.getProjectId());
                    row.createCell(2).setCellValue(t.getDescription());
                    row.createCell(3).setCellValue(t.getWeek());
                    row.createCell(4).setCellValue(t.getWho());
                    row.createCell(5).setCellValue(t.getStartDate().toString());
                    row.createCell(6).setCellValue(t.getEstimatedEndDate().toString());
                    row.createCell(7).setCellValue(t.getEffectiveEndDate().toString());
                    row.createCell(8).setCellValue(t.getWorkedMD());
                    row.createCell(9).setCellValue(t.getEstimatedMD());
                    row.createCell(10).setCellValue(t.getRemainingMD());
                    row.createCell(11).setCellValue(t.getProgress());
                    row.createCell(12).setCellValue(t.getCurrentStatus());
                    row.createCell(13).setCellValue(t.getEffortVariance());
                    row.createCell(14).setCellValue(t.getDeviationReason());
                    row.createCell(15).setCellValue(t.getNote());
                }
            }


            // === Feuille RISKS ===

            Sheet risksSheet = workbook.createSheet("Risks");
            XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
// === Titre centré violet ===
            Row titleRowRisk = risksSheet.createRow(1);
            Cell titleCellRisk = titleRowRisk.createCell(0);
            titleCellRisk.setCellValue("Risks");

            XSSFCellStyle titleStyleRisk = (XSSFCellStyle) workbook.createCellStyle();
            XSSFFont titleFontRisk = ((XSSFWorkbook) workbook).createFont();
            titleFontRisk.setBold(true);
            titleFontRisk.setFontHeightInPoints((short) 16);
            titleFontRisk.setColor(IndexedColors.VIOLET.getIndex());
            titleStyleRisk.setFont(titleFontRisk);
            titleStyleRisk.setAlignment(HorizontalAlignment.CENTER);

            titleCellRisk.setCellStyle(titleStyleRisk);
            risksSheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 17)); // fusionner sur 18 colonnes

// === Styles d'entêtes ===
            XSSFFont boldFont = ((XSSFWorkbook) workbook).createFont();
            boldFont.setBold(true);

            XSSFCellStyle headerStyleRisk = ((XSSFWorkbook) workbook).createCellStyle(); // ✅ XSSFCellStyle spécifique
            headerStyleRisk.setFont(boldFont);
            headerStyleRisk.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyleRisk.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyleRisk.setAlignment(HorizontalAlignment.CENTER);
            headerStyleRisk.setBorderBottom(BorderStyle.THIN);

            XSSFCellStyle yellowHeader = (XSSFCellStyle) workbook.createCellStyle();
            yellowHeader.cloneStyleFrom(headerStyleRisk);
            yellowHeader.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());

            XSSFCellStyle orangeHeader = (XSSFCellStyle) workbook.createCellStyle();
            orangeHeader.cloneStyleFrom(headerStyleRisk);
            orangeHeader.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());

            XSSFCellStyle turquoiseHeader = (XSSFCellStyle) workbook.createCellStyle();
            turquoiseHeader.cloneStyleFrom(headerStyleRisk);
            turquoiseHeader.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());

            XSSFCellStyle dataStyle = (XSSFCellStyle) workbook.createCellStyle();
            dataStyle.setWrapText(true);
            dataStyle.setVerticalAlignment(VerticalAlignment.TOP);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

// === Entêtes ===
            String[] riskHeaders = {
                    "ID", "Risk", "Origin", "Category", "Open Date", "Due Date",
                    "Causes", "Consequences", "Applied measures",
                    "Probability", "Gravity", "Criticality", "Measure",
                    "Risk Treatment Decision", "Justification", "Id Action",
                    "Risk status", "Close Date"
            };
// Ligne 2 (au-dessus des entêtes)
            Row sectionRow = risksSheet.createRow(2);

// Style pour les titres de section fusionnés
            XSSFCellStyle sectionHeaderStyle = xssfWorkbook.createCellStyle();
            XSSFFont sectionFontRisk = xssfWorkbook.createFont();
            sectionFontRisk.setBold(true);
            sectionHeaderStyle.setFont(sectionFontRisk);
            sectionHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
            sectionHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            sectionHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            sectionHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            sectionHeaderStyle.setBorderTop(BorderStyle.THIN);
            sectionHeaderStyle.setBorderBottom(BorderStyle.THIN);
            sectionHeaderStyle.setBorderLeft(BorderStyle.THIN);
            sectionHeaderStyle.setBorderRight(BorderStyle.THIN);

// Titres fusionnés sur les plages concernées
            Cell riskIdCell = sectionRow.createCell(0);
            riskIdCell.setCellValue("Risk Identification");
            riskIdCell.setCellStyle(sectionHeaderStyle);
            risksSheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 8));

            Cell analysisCell = sectionRow.createCell(9);
            analysisCell.setCellValue("Risk analysis");
            analysisCell.setCellStyle(sectionHeaderStyle);
            risksSheet.addMergedRegion(new CellRangeAddress(2, 2, 9, 10));

            Cell assessmentCell = sectionRow.createCell(11);
            assessmentCell.setCellValue("Risk assessment");
            assessmentCell.setCellStyle(sectionHeaderStyle);
            risksSheet.addMergedRegion(new CellRangeAddress(2, 2, 11, 12));

            Cell treatmentCell = sectionRow.createCell(13);
            treatmentCell.setCellValue("Risk treatment");
            treatmentCell.setCellStyle(sectionHeaderStyle);
            risksSheet.addMergedRegion(new CellRangeAddress(2, 2, 13, 17));

            Row headerRowRisk = risksSheet.createRow(3);
            for (int i = 0; i < riskHeaders.length; i++) {
                Cell cell = headerRowRisk.createCell(i);
                cell.setCellValue(riskHeaders[i]);

                if (i >= 9 && i <= 11) {
                    cell.setCellStyle(yellowHeader); // Risk Analysis
                } else if (i >= 12 && i <= 13) {
                    cell.setCellStyle(orangeHeader); // Risk Assessment
                } else if (i >= 14) {
                    cell.setCellStyle(turquoiseHeader); // Risk Treatment
                } else {
                    cell.setCellStyle(headerStyleRisk); // Standard
                }

                risksSheet.setColumnWidth(i, 20 * 256); // largeur auto
            }

// === Données dynamiques ===
            List<RisksDTO> risks = psr.getRisks();
            if (risks != null) {
                for (int i = 0; i < risks.size(); i++) {
                    RisksDTO r = risks.get(i);
                    Row row = risksSheet.createRow(i + 4); // ligne suivante

                    String[] values = {
                            String.valueOf(r.getId()), r.getDescription(), r.getOrigin(), r.getCategory(),
                            r.getOpenDate(), r.getDueDate(), r.getCauses(), r.getConsequences(), r.getAppliedMeasures(),
                            r.getProbability(), r.getGravity(), r.getCriticality(), r.getMeasure(),
                            r.getRiskTreatmentDecision(), r.getJustification(), r.getIdAction(),
                            r.getRiskStat(), r.getCloseDate()
                    };

                    for (int j = 0; j < values.length; j++) {
                        Cell cell = row.createCell(j);
                        cell.setCellValue(values[j] != null ? values[j] : "");
                        cell.setCellStyle(dataStyle);
                    }
                }
            }




            // === 6. DELIVERIES ===
            Sheet deliveriesSheet = workbook.createSheet("Deliveries");

// === Style de base avec bordures ===
            CellStyle baseStyle = workbook.createCellStyle();
            baseStyle.setBorderTop(BorderStyle.THIN);
            baseStyle.setBorderBottom(BorderStyle.THIN);
            baseStyle.setBorderLeft(BorderStyle.THIN);
            baseStyle.setBorderRight(BorderStyle.THIN);
            baseStyle.setVerticalAlignment(VerticalAlignment.TOP);
            baseStyle.setWrapText(true);
            Font baseFont = workbook.createFont();
            baseFont.setColor(IndexedColors.BLACK.getIndex());
            baseStyle.setFont(baseFont);

// === Style header (bleu avec écriture blanche) ===
            Row deliveryHeader = deliveriesSheet.createRow(0);
            String[] deliveryHeaders = {"ID", "Deliverable", "Description", "Version", "Planned Date", "Effective Date", "Status", "Delivery Support", "Customer Feedback"};

            CellStyle headerStyleDeli = workbook.createCellStyle();
            Font headerFontDeli = workbook.createFont();
            headerFontDeli.setBold(true);
            headerFontDeli.setColor(IndexedColors.WHITE.getIndex());
            headerStyleDeli.setFont(headerFontDeli);
            headerStyleDeli.setAlignment(HorizontalAlignment.CENTER);
            headerStyleDeli.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyleDeli.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyleDeli.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyleDeli.setBorderTop(BorderStyle.THIN);
            headerStyleDeli.setBorderBottom(BorderStyle.THIN);
            headerStyleDeli.setBorderLeft(BorderStyle.THIN);
            headerStyleDeli.setBorderRight(BorderStyle.THIN);

// Appliquer les en-têtes
            for (int i = 0; i < deliveryHeaders.length; i++) {
                Cell cell = deliveryHeader.createCell(i);
                cell.setCellValue(deliveryHeaders[i]);
                cell.setCellStyle(headerStyleDeli);
                deliveriesSheet.setColumnWidth(i, 20 * 256);
            }

// === Style vert : Delivered + Accepted ===
            CellStyle greenStyle = workbook.createCellStyle();
            greenStyle.cloneStyleFrom(baseStyle);
            greenStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            greenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

// === Style rouge : Delivered + Refused ===
            CellStyle redStyle = workbook.createCellStyle();
            redStyle.cloneStyleFrom(baseStyle);
            redStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            redStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

// === Remplissage des données ===
            List<DeliveriesDTO> deliveries = psr.getDeliveries();
            if (deliveries != null) {
                for (int i = 0; i < deliveries.size(); i++) {
                    DeliveriesDTO d = deliveries.get(i);
                    Row row = deliveriesSheet.createRow(i + 1);

                    String status = d.getStatus();
                    String feedback = d.getCustomerFeedback();

                    CellStyle rowStyle = baseStyle;
                    if ("Delivered".equalsIgnoreCase(status)) {
                        if ("Accepted".equalsIgnoreCase(feedback)) {
                            rowStyle = greenStyle;
                        } else if ("Refused".equalsIgnoreCase(feedback)) {
                            rowStyle = redStyle;
                        }
                    }

                    String[] values = {
                            String.valueOf(d.getId()),
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



            // === 7. PLANNING === (structure simplifiée à adapter)
            Sheet planningSheet = workbook.createSheet("Planning");
            Row planningRow = planningSheet.createRow(0);
            planningRow.createCell(0).setCellValue("Planning Overview");

            // Export final
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
