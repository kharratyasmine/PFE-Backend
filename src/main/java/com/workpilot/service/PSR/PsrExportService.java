package com.workpilot.service.PSR;

import com.workpilot.dto.PsrDTO.PsrDTO;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;

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

            // Team Organization (1 seule fois)
            Sheet teamSheet = workbook.createSheet("Team Organization");
            teamSheet.setColumnWidth(0, 25 * 256);
            Row teamHeader = teamSheet.createRow(0);
            String[] teamHeaders = {"Name", "Position", "Location", "Email", "Phone"};
            for (int i = 0; i < teamHeaders.length; i++) {
                Cell cell = teamHeader.createCell(i);
                cell.setCellValue(teamHeaders[i]);
                cell.setCellStyle(sectionStyle);
            }

            // Planning sheet
            Sheet planningSheet = workbook.createSheet("Planning");
            planningSheet.setColumnWidth(0, 25 * 256);
            Row rowP = planningSheet.createRow(0);
            Cell cellP = rowP.createCell(0);
            cellP.setCellValue("Planning Overview");

            // Export to stream
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
