package com.codeforge.compiler.dto.roadmap;

public record RoadmapProgressSummaryResponse(
    Long userId,
    String name,
    String email,
    long totalQuestions,
    long completedQuestions,
    long totalAttempts,
    double completionRate) {}
