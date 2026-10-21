package com.codeforge.compiler.dto.admin;

public record UserPerformanceResponse(
    Long userId,
    String name,
    String email,
    long totalRuns,
    long successfulRuns,
    long failedRuns,
    double successRate,
    long bestExecutionTimeMs) {}
