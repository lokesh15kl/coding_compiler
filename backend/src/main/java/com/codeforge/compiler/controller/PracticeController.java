package com.codeforge.compiler.controller;

import com.codeforge.compiler.dto.admin.TopicWithProblemsResponse;
import com.codeforge.compiler.service.ProblemService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PracticeController {

  private final ProblemService problemService;

  public PracticeController(ProblemService problemService) {
    this.problemService = problemService;
  }

  @GetMapping("/practice")
  public List<TopicWithProblemsResponse> getPracticeProblems() {
    return problemService.getTopicsWithProblems();
  }
}
