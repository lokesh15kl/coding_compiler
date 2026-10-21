package com.codeforge.compiler.dto.roadmap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RoadmapTopicResponse(
    @NotBlank String topic,
    @NotBlank String description,
    @NotNull @Size(min = 1, message = "Each topic must contain at least one question") List<RoadmapQuestionResponse> questions) {}
