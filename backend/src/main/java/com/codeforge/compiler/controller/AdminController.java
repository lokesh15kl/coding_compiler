package com.codeforge.compiler.controller;

import com.codeforge.compiler.dto.admin.AdminStatsResponse;
import com.codeforge.compiler.dto.admin.ProblemRequest;
import com.codeforge.compiler.dto.admin.ProblemResponse;
import com.codeforge.compiler.dto.admin.TopicRequest;
import com.codeforge.compiler.dto.admin.TopicResponse;
import com.codeforge.compiler.dto.admin.TopicWithProblemsResponse;
import com.codeforge.compiler.dto.admin.UserPerformanceResponse;
import com.codeforge.compiler.service.AdminService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

  private final AdminService adminService;

  public AdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping("/stats")
  public AdminStatsResponse stats() {
    return adminService.stats();
  }

  @GetMapping("/topics")
  public List<TopicResponse> getTopics() {
    return adminService.getTopics();
  }

  @PostMapping("/topics")
  public TopicResponse createTopic(@RequestBody TopicRequest request) {
    return adminService.createTopic(request);
  }

  @GetMapping("/problems")
  public List<ProblemResponse> getProblems() {
    return adminService.getProblems();
  }

  @PostMapping("/problems")
  public ProblemResponse createProblem(@RequestBody ProblemRequest request) {
    return adminService.createProblem(request);
  }

  @PutMapping("/problems/{id}")
  public ProblemResponse updateProblem(@PathVariable Long id, @RequestBody ProblemRequest request) {
    return adminService.updateProblem(id, request);
  }

  @DeleteMapping("/problems/{id}")
  public ResponseEntity<Void> deleteProblem(@PathVariable Long id) {
    adminService.deleteProblem(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/performance")
  public List<UserPerformanceResponse> getUserPerformance() {
    return adminService.getUserPerformance();
  }

  @GetMapping("/practice")
  public List<TopicWithProblemsResponse> getPracticeProblems() {
    return adminService.getTopicsWithProblems();
  }

  @DeleteMapping("/users/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    adminService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/programs/{id}")
  public ResponseEntity<Void> deleteProgram(@PathVariable Long id) {
    adminService.deleteProgram(id);
    return ResponseEntity.noContent().build();
  }
}