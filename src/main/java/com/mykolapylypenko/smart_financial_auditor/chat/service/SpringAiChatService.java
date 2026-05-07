package com.mykolapylypenko.smart_financial_auditor.chat.service;

import com.mykolapylypenko.smart_financial_auditor.chat.dto.ChatRequest;
import com.mykolapylypenko.smart_financial_auditor.chat.dto.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * General-purpose financial assistant powered by Spring AI 2.0 ChatClient.
 *
 * Uses Spring AI's fluent ChatClient API (analogous to WebClient/RestClient) to
 * communicate with the configured chat model. No RAG — answers from model knowledge.
 * For regulation-grounded answers use {@code SpringAiAuditService} instead.
 */
@Service
public class SpringAiChatService {

    private static final Logger log = LoggerFactory.getLogger(SpringAiChatService.class);

    private final ChatClient chatClient;

    public SpringAiChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        You are a knowledgeable Financial Assistant specializing in banking,
                        regulatory compliance, and financial auditing.
                        Help users understand financial regulations, banking processes,
                        and compliance requirements. Be precise and professional.
                        """)
                .build();
    }

    public ChatResponse chat(ChatRequest request) {
        log.info("Spring AI general chat: {}", request.message());
        String response = chatClient.prompt()
                .user(request.message())
                .call()
                .content();
        return new ChatResponse(request.message(), response, LocalDateTime.now());
    }
}
