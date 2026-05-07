package com.mykolapylypenko.smart_financial_auditor.audit.service;

import com.mykolapylypenko.smart_financial_auditor.audit.dto.AuditRequest;
import com.mykolapylypenko.smart_financial_auditor.audit.dto.AuditResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * RAG-based audit service backed by Spring AI 2.0.
 *
 * Uses Spring AI's {@link QuestionAnswerAdvisor} which performs a similarity search
 * in the Spring AI PgVector store (vector_store table) and augments the prompt with
 * the retrieved chunks before calling the LLM — same RAG pattern as LangChain4j,
 * demonstrating both frameworks side-by-side.
 */
@Service
public class SpringAiAuditService {

    private static final Logger log = LoggerFactory.getLogger(SpringAiAuditService.class);

    private final ChatClient ragChatClient;

    public SpringAiAuditService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.ragChatClient = chatClientBuilder
                .defaultSystem("""
                        You are a Professional Banking Auditor specializing in regulatory compliance.
                        Your role is to analyze Polish financial regulations, KNF guidelines, and banking policies.

                        STRICT RULES:
                        1. Answer ONLY based on the regulatory context provided.
                        2. If the answer is not in the context, respond:
                           "I cannot find relevant information in the provided regulatory documents."
                        3. Cite the specific document or section when answering.
                        4. Be precise and professional.
                        """)
                .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .topK(3)
                                        .similarityThreshold(0.7)
                                        .build())
                                .build()
                )
                .build();
    }

    public AuditResponse performAudit(AuditRequest request) {
        log.info("Spring AI RAG audit query: {}", request.question());
        String answer = ragChatClient.prompt()
                .user(request.question())
                .call()
                .content();
        log.debug("Spring AI audit answer length: {} chars", answer.length());
        return new AuditResponse(request.question(), answer, "Spring AI RAG", LocalDateTime.now());
    }
}
