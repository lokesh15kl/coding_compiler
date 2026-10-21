package com.codeforge.compiler.service;

import com.codeforge.compiler.dto.dashboard.DashboardResponse;
import com.codeforge.compiler.domain.ExecutionStatus;
import com.codeforge.compiler.entity.User;
import com.codeforge.compiler.repository.ExecutionHistoryRepository;
import com.codeforge.compiler.repository.ProgramRepository;
import com.codeforge.compiler.repository.UserRepository;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

  private final ProgramRepository programRepository;
  private final ExecutionHistoryRepository executionHistoryRepository;
  private final UserRepository userRepository;

  public DashboardService(ProgramRepository programRepository, ExecutionHistoryRepository executionHistoryRepository,
      UserRepository userRepository) {
    this.programRepository = programRepository;
    this.executionHistoryRepository = executionHistoryRepository;
    this.userRepository = userRepository;
  }

  public DashboardResponse stats() {
    User user = currentUser();
    long totalPrograms = programRepository.countByUser(user);
    long totalExecutions = executionHistoryRepository.countByUser(user);
    long successfulExecutions = executionHistoryRepository.countByUserAndStatus(user, ExecutionStatus.SUCCESS);
    String favoriteLanguage = programRepository.findByUserOrderByUpdatedAtDesc(user).stream()
        .collect(Collectors.groupingBy(program -> program.getLanguage(), Collectors.counting()))
        .entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse("Java");
    return new DashboardResponse(totalPrograms, totalExecutions, successfulExecutions, favoriteLanguage);
  }

  private User currentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
  }
}