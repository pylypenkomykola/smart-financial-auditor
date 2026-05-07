package com.mykolapylypenko.smart_financial_auditor.audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuditRequest(
        @NotBlank(message = "Question must not be blank")
        @Size(max = 2000, message = "Question must not exceed 2000 characters")
        String question
) {
}
