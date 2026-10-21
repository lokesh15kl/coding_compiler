package com.codeforge.compiler.dto.roadmap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RoadmapPushRequest(
    @NotBlank String name,
    @NotNull @Size(min = 1, message = "At least one topic is required") List<RoadmapTopicResponse> topics) {}
