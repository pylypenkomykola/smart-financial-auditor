package com.mykolapylypenko.smart_financial_auditor.audit.service;

import com.mykolapylypenko.smart_financial_auditor.audit.ai.BankingAuditorAi;
import com.mykolapylypenko.smart_financial_auditor.audit.dto.AuditRequest;
import com.mykolapylypenko.smart_financial_auditor.audit.dto.AuditResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private BankingAuditorAi bankingAuditorAi;

    @InjectMocks
    private AuditService auditService;

    @Test
    void performAudit_returnsPopulatedResponse() {
        String question = "What are the MFA requirements per KNF Recommendation D?";
        String aiAnswer = "According to KNF Recommendation D, section 4.2, multi-factor authentication is required for all online banking access.";
        when(bankingAuditorAi.audit(question)).thenReturn(aiAnswer);

        AuditResponse response = auditService.performAudit(new AuditRequest(question));

        assertThat(response.question()).isEqualTo(question);
        assertThat(response.answer()).isEqualTo(aiAnswer);
        assertThat(response.engine()).isEqualTo("LangChain4j RAG");
        assertThat(response.respondedAt()).isNotNull();
        verify(bankingAuditorAi).audit(question);
    }

    @Test
    void performAudit_whenTopicNotInDocuments_returnsNotFoundMessage() {
        String question = "What is the capital of France?";
        String notFoundAnswer = "I cannot find relevant information about this topic in the provided regulatory documents.";
        when(bankingAuditorAi.audit(question)).thenReturn(notFoundAnswer);

        AuditResponse response = auditService.performAudit(new AuditRequest(question));

        assertThat(response.answer()).isEqualTo(notFoundAnswer);
        assertThat(response.engine()).isEqualTo("LangChain4j RAG");
    }

    @Test
    void performAudit_propagatesQuestionToAi() {
        String question = "What are AML transaction monitoring requirements?";
        when(bankingAuditorAi.audit(question)).thenReturn("AML monitoring is required...");

        auditService.performAudit(new AuditRequest(question));

        verify(bankingAuditorAi).audit(question);
    }
}
