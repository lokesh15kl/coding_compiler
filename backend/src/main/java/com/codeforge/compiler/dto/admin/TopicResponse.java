package com.codeforge.compiler.dto.admin;

import java.time.LocalDateTime;

public record TopicResponse(Long id, String name, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {}
