package com.workpilot.controller.DevisController;

import com.workpilot.service.DevisExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/devisExport")
public class DevisExportController {

    @Autowired
    private DevisExportService devisExportService;

    @GetMapping("/word/{devisId}")
    public ResponseEntity<byte[]> exportWord(@PathVariable Long devisId) {
        ByteArrayOutputStream wordFile = devisExportService.exportDevisToWord(devisId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=devis_" + devisId + ".docx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(wordFile.toByteArray());
    }
}
