package com.mykolapylypenko.smart_financial_auditor.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank(message = "Message must not be blank")
        @Size(max = 4000, message = "Message must not exceed 4000 characters")
        String message
) {
}
