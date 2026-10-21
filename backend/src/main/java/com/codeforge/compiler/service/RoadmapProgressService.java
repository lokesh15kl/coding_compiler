package com.codeforge.compiler.service;

import com.codeforge.compiler.dto.roadmap.RoadmapProgressResponse;
import com.codeforge.compiler.dto.roadmap.RoadmapProgressSummaryResponse;
import com.codeforge.compiler.entity.RoadmapProgress;
import com.codeforge.compiler.entity.User;
import com.codeforge.compiler.repository.RoadmapProgressRepository;
import com.codeforge.compiler.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoadmapProgressService {

  private final RoadmapProgressRepository roadmapProgressRepository;
  private final UserRepository userRepository;
  private final RoadmapService roadmapService;

  public RoadmapProgressService(RoadmapProgressRepository roadmapProgressRepository,
      UserRepository userRepository, RoadmapService roadmapService) {
    this.roadmapProgressRepository = roadmapProgressRepository;
    this.userRepository = userRepository;
    this.roadmapService = roadmapService;
  }

  @Transactional
  public RoadmapProgressResponse markAttempt(String questionId, boolean completed) {
    User user = getCurrentUser();
    Optional<RoadmapProgress> existing = roadmapProgressRepository.findByUserAndQuestionId(user, questionId);
    RoadmapProgress progress = existing.orElseGet(() -> {
      var question = roadmapService.flattenQuestions().stream()
          .filter(item -> item.id().equals(questionId))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
      return RoadmapProgress.builder()
          .user(user)
          .questionId(questionId)
          .topic(question.topic())
          .title(question.title())
          .attempts(0)
          .completed(false)
          .updatedAt(LocalDateTime.now())
          .build();
    });

    progress.setAttempts(progress.getAttempts() + 1);
    progress.setCompleted(progress.isCompleted() || completed);
    progress.setUpdatedAt(LocalDateTime.now());
    progress.setTopic(progress.getTopic());
    progress.setTitle(progress.getTitle());
    roadmapProgressRepository.save(progress);

    return new RoadmapProgressResponse(
        progress.getQuestionId(),
        progress.getTopic(),
        progress.getTitle(),
        progress.getAttempts(),
        progress.isCompleted(),
        progress.getUpdatedAt());
  }

  public List<RoadmapProgressResponse> getMyProgress() {
    User user = getCurrentUser();
    return roadmapProgressRepository.findByUserOrderByUpdatedAtDesc(user).stream()
        .map(item -> new RoadmapProgressResponse(
            item.getQuestionId(),
            item.getTopic(),
            item.getTitle(),
            item.getAttempts(),
            item.isCompleted(),
            item.getUpdatedAt()))
        .toList();
  }

  public List<RoadmapProgressSummaryResponse> getProgressSummary() {
    List<User> users = userRepository.findAll();
    List<RoadmapProgressSummaryResponse> summary = new ArrayList<>();
    var allQuestions = roadmapService.flattenQuestions();
    for (User user : users) {
      var progress = roadmapProgressRepository.findByUserOrderByUpdatedAtDesc(user);
      long totalQuestions = allQuestions.size();
      long completedQuestions = progress.stream().filter(RoadmapProgress::isCompleted).count();
      long totalAttempts = progress.stream().mapToLong(RoadmapProgress::getAttempts).sum();
      double completionRate = totalQuestions == 0 ? 0 : (completedQuestions * 100.0) / totalQuestions;
      summary.add(new RoadmapProgressSummaryResponse(
          user.getId(),
          user.getName(),
          user.getEmail(),
          totalQuestions,
          completedQuestions,
          totalAttempts,
          completionRate));
    }
    return summary;
  }

  private User getCurrentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }
}
