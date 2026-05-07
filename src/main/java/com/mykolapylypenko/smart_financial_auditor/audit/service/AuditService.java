package com.mykolapylypenko.smart_financial_auditor.audit.service;

import com.mykolapylypenko.smart_financial_auditor.audit.ai.BankingAuditorAi;
import com.mykolapylypenko.smart_financial_auditor.audit.dto.AuditRequest;
import com.mykolapylypenko.smart_financial_auditor.audit.dto.AuditResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * RAG-based audit service backed by LangChain4j.
 *
 * Flow (Step B → C from the RAG workflow):
 * 1. User question arrives
 * 2. ContentRetriever embeds the question and searches document_embeddings (pgvector)
 * 3. Top-3 most relevant regulatory chunks are injected into the prompt
 * 4. ChatLanguageModel (GPT-4o or Llama 3.2) generates a grounded answer
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final BankingAuditorAi bankingAuditorAi;

    public AuditService(BankingAuditorAi bankingAuditorAi) {
        this.bankingAuditorAi = bankingAuditorAi;
    }

    public AuditResponse performAudit(AuditRequest request) {
        log.info("LangChain4j RAG audit query: {}", request.question());
        String answer = bankingAuditorAi.audit(request.question());
        log.debug("LangChain4j audit answer length: {} chars", answer.length());
        return new AuditResponse(request.question(), answer, "LangChain4j RAG", LocalDateTime.now());
    }
}
