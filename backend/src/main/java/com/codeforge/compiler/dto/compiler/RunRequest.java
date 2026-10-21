package com.codeforge.compiler.dto.compiler;

import jakarta.validation.constraints.NotBlank;

public record RunRequest(@NotBlank String language, @NotBlank String code, String input) {}
