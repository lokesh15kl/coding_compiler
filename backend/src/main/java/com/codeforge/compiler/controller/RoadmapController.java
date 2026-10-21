package com.codeforge.compiler.controller;

import com.codeforge.compiler.dto.roadmap.RoadmapProgressResponse;
import com.codeforge.compiler.dto.roadmap.RoadmapProgressSummaryResponse;
import com.codeforge.compiler.dto.roadmap.RoadmapPushRequest;
import com.codeforge.compiler.dto.roadmap.RoadmapTopicResponse;
import com.codeforge.compiler.service.RoadmapProgressService;
import com.codeforge.compiler.service.RoadmapService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RoadmapController {

  private final RoadmapService roadmapService;
  private final RoadmapProgressService progressService;

  public RoadmapController(RoadmapService roadmapService, RoadmapProgressService progressService) {
    this.roadmapService = roadmapService;
    this.progressService = progressService;
  }

  @GetMapping("/roadmap")
  public List<RoadmapTopicResponse> getRoadmap() {
    return roadmapService.getRoadmap();
  }

  @PostMapping("/admin/roadmap")
  public List<RoadmapTopicResponse> pushRoadmap(@Valid @RequestBody RoadmapPushRequest request) {
    roadmapService.saveRoadmap(request);
    return roadmapService.getRoadmap();
  }

  @GetMapping("/roadmap/progress")
  public List<RoadmapProgressResponse> getMyProgress() {
    return progressService.getMyProgress();
  }

  @PostMapping("/roadmap/questions/{questionId}/attempt")
  public RoadmapProgressResponse markAttempt(@PathVariable String questionId, @RequestParam boolean completed) {
    return progressService.markAttempt(questionId, completed);
  }

  @GetMapping("/admin/roadmap/progress")
  public List<RoadmapProgressSummaryResponse> getRoadmapProgressSummary() {
    return progressService.getProgressSummary();
  }

  @PostMapping("/admin/roadmap/reset")
  public List<RoadmapTopicResponse> resetRoadmapToDefault() {
    roadmapService.resetToDefault();
    return roadmapService.getRoadmap();
  }
}
