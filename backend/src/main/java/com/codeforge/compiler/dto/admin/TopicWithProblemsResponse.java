package com.codeforge.compiler.dto.admin;

import java.util.List;

public record TopicWithProblemsResponse(Long id, String name, String description, List<ProblemResponse> problems) {}
