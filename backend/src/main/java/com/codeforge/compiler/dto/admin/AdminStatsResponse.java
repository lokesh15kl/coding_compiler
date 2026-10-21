package com.codeforge.compiler.dto.admin;

public record AdminStatsResponse(
    long totalUsers,
    long totalPrograms,
    long totalExecutions,
    String mostUsedLanguage) {}
