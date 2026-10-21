package com.codeforge.compiler.dto.roadmap;

import java.time.LocalDateTime;

public record RoadmapProgressResponse(
    String questionId,
    String topic,
    String title,
    int attempts,
    boolean completed,
    LocalDateTime updatedAt) {}
