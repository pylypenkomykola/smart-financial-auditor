package com.mykolapylypenko.smart_financial_auditor.chat.service;

import com.mykolapylypenko.smart_financial_auditor.chat.dto.ChatRequest;
import com.mykolapylypenko.smart_financial_auditor.chat.dto.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpringAiChatServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
    }

    @Test
    void chat_returnsChatResponseWithCorrectFields() {
        String question = "What is KYC in banking?";
        String aiReply = "KYC (Know Your Customer) is a mandatory process...";

        ChatClient.ChatClientRequest promptRequest = mock(ChatClient.ChatClientRequest.class);
        ChatClient.ChatClientRequest.CallResponseSpec callSpec = mock(ChatClient.ChatClientRequest.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(promptRequest);
        when(promptRequest.user(question)).thenReturn(promptRequest);
        when(promptRequest.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn(aiReply);

        SpringAiChatService service = new SpringAiChatService(chatClientBuilder);
        ChatResponse response = service.chat(new ChatRequest(question));

        assertThat(response.message()).isEqualTo(question);
        assertThat(response.response()).isEqualTo(aiReply);
        assertThat(response.respondedAt()).isNotNull();
    }
}
