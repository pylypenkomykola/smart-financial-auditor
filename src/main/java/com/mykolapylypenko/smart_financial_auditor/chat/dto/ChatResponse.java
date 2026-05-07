package com.mykolapylypenko.smart_financial_auditor.chat.dto;

import java.time.LocalDateTime;

public record ChatResponse(
        String message,
        String response,
        LocalDateTime respondedAt
) {
}
