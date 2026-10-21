package com.codeforge.compiler.dto.compiler;

import com.codeforge.compiler.domain.ExecutionStatus;

public record RunResponse(
    ExecutionStatus status,
    String output,
    String stderr,
    Long executionTime,
    Long memoryUsage) {}
