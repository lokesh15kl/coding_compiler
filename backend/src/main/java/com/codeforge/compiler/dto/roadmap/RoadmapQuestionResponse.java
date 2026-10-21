package com.codeforge.compiler.dto.roadmap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RoadmapQuestionResponse(
    @NotBlank String id,
    @NotBlank String topic,
    @NotBlank String title,
    @NotBlank String difficulty,
    @NotBlank String prompt,
    @NotNull @Size(min = 1, message = "Each question must include at least one tag") List<String> tags) {}
