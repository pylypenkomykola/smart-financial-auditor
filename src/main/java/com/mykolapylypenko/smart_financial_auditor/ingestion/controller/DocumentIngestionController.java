package com.mykolapylypenko.smart_financial_auditor.ingestion.controller;

import com.mykolapylypenko.smart_financial_auditor.ingestion.dto.IngestionResponse;
import com.mykolapylypenko.smart_financial_auditor.ingestion.service.DocumentIngestionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentIngestionController {

    private final DocumentIngestionService ingestionService;

    public DocumentIngestionController(DocumentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IngestionResponse> ingestDocument(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ingestionService.ingestDocument(file));
    }
}
