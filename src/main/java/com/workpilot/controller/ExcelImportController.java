package com.workpilot.controller;

import com.workpilot.service.ExcelParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/excel")
public class ExcelImportController {

    @Autowired
    private ExcelParserService excelParserService;

    @PostMapping("/import")
    public ResponseEntity<Map<String, List<List<String>>>> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, List<List<String>>> parsedData = excelParserService.parseAllSheets(file.getInputStream());
            return ResponseEntity.ok(parsedData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("Erreur", List.of(List.of(e.getMessage()))));
        }
    }
}
