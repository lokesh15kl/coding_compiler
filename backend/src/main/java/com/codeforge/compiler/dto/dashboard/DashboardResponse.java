package com.codeforge.compiler.dto.dashboard;

public record DashboardResponse(
    long totalPrograms,
    long totalExecutions,
    long successfulExecutions,
    String favoriteLanguage) {}
