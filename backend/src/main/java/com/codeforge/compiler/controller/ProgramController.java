package com.codeforge.compiler.controller;

import com.codeforge.compiler.dto.program.ProgramResponse;
import com.codeforge.compiler.dto.program.SaveProgramRequest;
import com.codeforge.compiler.dto.program.SharedProgramResponse;
import com.codeforge.compiler.service.ProgramService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/program")
public class ProgramController {

  private final ProgramService programService;

  public ProgramController(ProgramService programService) {
    this.programService = programService;
  }

  @PostMapping("/save")
  public ProgramResponse save(@Valid @RequestBody SaveProgramRequest request) {
    return programService.saveProgram(request);
  }

  @GetMapping("/my")
  public List<ProgramResponse> myPrograms() {
    return programService.myPrograms();
  }

  @GetMapping("/share/{shareId}")
  public SharedProgramResponse share(@PathVariable String shareId) {
    return programService.findSharedProgram(shareId);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    programService.deleteProgram(id);
    return ResponseEntity.noContent().build();
  }
}