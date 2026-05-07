package com.mykolapylypenko.smart_financial_auditor.audit.dto;

import java.time.LocalDateTime;

public record AuditResponse(
        String question,
        String answer,
        String engine,
        LocalDateTime respondedAt
) {
}
