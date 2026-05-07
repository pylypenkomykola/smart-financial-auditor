package com.mykolapylypenko.smart_financial_auditor.ingestion.dto;

import java.time.LocalDateTime;

public record IngestionResponse(
        String filename,
        int totalSegments,
        String message,
        LocalDateTime processedAt
) {
}
