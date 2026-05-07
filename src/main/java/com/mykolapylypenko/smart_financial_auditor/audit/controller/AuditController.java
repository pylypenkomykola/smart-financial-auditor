package com.mykolapylypenko.smart_financial_auditor.audit.controller;

import com.mykolapylypenko.smart_financial_auditor.audit.dto.AuditRequest;
import com.mykolapylypenko.smart_financial_auditor.audit.dto.AuditResponse;
import com.mykolapylypenko.smart_financial_auditor.audit.service.AuditService;
import com.mykolapylypenko.smart_financial_auditor.audit.service.SpringAiAuditService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;
    private final SpringAiAuditService springAiAuditService;

    public AuditController(AuditService auditService, SpringAiAuditService springAiAuditService) {
        this.auditService = auditService;
        this.springAiAuditService = springAiAuditService;
    }

    /**
     * LangChain4j RAG endpoint — searches document_embeddings table.
     */
    @PostMapping("/query")
    public ResponseEntity<AuditResponse> auditWithLangChain4j(
            @RequestBody @Valid AuditRequest request) {
        return ResponseEntity.ok(auditService.performAudit(request));
    }

    /**
     * Spring AI RAG endpoint — searches vector_store table.
     */
    @PostMapping("/spring-ai/query")
    public ResponseEntity<AuditResponse> auditWithSpringAi(
            @RequestBody @Valid AuditRequest request) {
        return ResponseEntity.ok(springAiAuditService.performAudit(request));
    }
}
