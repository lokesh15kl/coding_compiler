package com.codeforge.compiler.service;

import com.codeforge.compiler.dto.compiler.RunRequest;
import com.codeforge.compiler.dto.compiler.RunResponse;

public interface CodeExecutionService {
  RunResponse execute(RunRequest request);
}