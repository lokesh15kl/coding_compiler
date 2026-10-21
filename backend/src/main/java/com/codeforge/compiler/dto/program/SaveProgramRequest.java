package com.codeforge.compiler.dto.program;

import jakarta.validation.constraints.NotBlank;

public record SaveProgramRequest(@NotBlank String title, @NotBlank String language, @NotBlank String code) {}
