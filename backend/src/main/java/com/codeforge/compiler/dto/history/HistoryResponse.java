package com.codeforge.compiler.dto.history;

import com.codeforge.compiler.domain.ExecutionStatus;
import java.time.LocalDateTime;

public record HistoryResponse(
    Long id,
    String title,
    String language,
    ExecutionStatus status,
    Long executionTime,
    Long memoryUsage,
    LocalDateTime createdAt) {}
