package com.codeforge.compiler.controller;

import com.codeforge.compiler.dto.compiler.RunRequest;
import com.codeforge.compiler.dto.compiler.RunResponse;
import com.codeforge.compiler.service.CodeExecutionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compiler")
public class CompilerController {

  private final CodeExecutionService codeExecutionService;

  public CompilerController(CodeExecutionService codeExecutionService) {
    this.codeExecutionService = codeExecutionService;
  }

  @PostMapping("/run")
  public RunResponse run(@Valid @RequestBody RunRequest request) {
    return codeExecutionService.execute(request);
  }
}