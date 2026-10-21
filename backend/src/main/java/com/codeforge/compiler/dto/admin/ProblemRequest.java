package com.codeforge.compiler.dto.admin;

public record ProblemRequest(
    Long topicId,
    String title,
    String difficulty,
    String language,
    String statement,
    String inputFormat,
    String outputFormat,
    String sampleInput,
    String sampleOutput) {}
