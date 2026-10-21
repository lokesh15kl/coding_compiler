package com.codeforge.compiler.dto.program;

import java.time.LocalDateTime;

public record ProgramResponse(Long id, String title, String language, LocalDateTime updatedAt, String shareId) {}
