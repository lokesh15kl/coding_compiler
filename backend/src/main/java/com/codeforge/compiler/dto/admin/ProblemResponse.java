package com.codeforge.compiler.dto.admin;

import java.time.LocalDateTime;

public record ProblemResponse(
    Long id,
    String title,
    String difficulty,
    String language,
    String statement,
    String inputFormat,
    String outputFormat,
    String sampleInput,
    String sampleOutput,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
