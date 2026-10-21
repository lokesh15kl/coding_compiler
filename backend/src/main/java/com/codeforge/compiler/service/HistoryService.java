package com.codeforge.compiler.service;

import com.codeforge.compiler.dto.history.HistoryResponse;
import com.codeforge.compiler.entity.User;
import com.codeforge.compiler.repository.ExecutionHistoryRepository;
import com.codeforge.compiler.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class HistoryService {

  private final ExecutionHistoryRepository executionHistoryRepository;
  private final UserRepository userRepository;

  public HistoryService(ExecutionHistoryRepository executionHistoryRepository, UserRepository userRepository) {
    this.executionHistoryRepository = executionHistoryRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public List<HistoryResponse> myHistory() {
    User user = currentUser();
    return executionHistoryRepository.findTop20ByUserOrderByCreatedAtDesc(user).stream()
        .map(history -> new HistoryResponse(
            history.getId(),
            history.getProgram() != null ? history.getProgram().getTitle() : "Untitled",
            history.getProgram() != null ? history.getProgram().getLanguage() : "Unknown",
            history.getStatus(),
            history.getExecutionTime(),
            history.getMemoryUsage(),
            history.getCreatedAt()))
        .toList();
  }

  private User currentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
  }
}