package com.workpilot.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class ExcelParserService {

    public Map<String, List<List<String>>> parseAllSheets(InputStream inputStream) {
        Map<String, List<List<String>>> excelData = new LinkedHashMap<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            for (Sheet sheet : workbook) {
                List<List<String>> rows = new ArrayList<>();
                for (Row row : sheet) {
                    List<String> rowData = new ArrayList<>();
                    for (Cell cell : row) {
                        cell.setCellType(CellType.STRING);
                        rowData.add(cell.getStringCellValue().trim());
                    }
                    rows.add(rowData);
                }
                excelData.put(sheet.getSheetName(), rows);
            }
        } catch (Exception e) {
            throw new RuntimeException("❌ Erreur lecture Excel : " + e.getMessage(), e);
        }

        return excelData;
    }
}
