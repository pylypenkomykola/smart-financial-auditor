package com.mykolapylypenko.smart_financial_auditor.chat.controller;

import com.mykolapylypenko.smart_financial_auditor.chat.dto.ChatRequest;
import com.mykolapylypenko.smart_financial_auditor.chat.dto.ChatResponse;
import com.mykolapylypenko.smart_financial_auditor.chat.service.SpringAiChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final SpringAiChatService chatService;

    public ChatController(SpringAiChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody @Valid ChatRequest request) {
        return ResponseEntity.ok(chatService.chat(request));
    }
}
