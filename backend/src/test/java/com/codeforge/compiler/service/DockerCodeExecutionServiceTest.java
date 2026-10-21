package com.codeforge.compiler.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codeforge.compiler.domain.ExecutionStatus;
import com.codeforge.compiler.dto.compiler.RunRequest;
import com.codeforge.compiler.dto.compiler.RunResponse;
import com.codeforge.compiler.entity.ExecutionHistory;
import com.codeforge.compiler.entity.User;
import com.codeforge.compiler.repository.ExecutionHistoryRepository;
import com.codeforge.compiler.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class DockerCodeExecutionServiceTest {

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void executePreservesExactJavaOutput() {
    ExecutionHistoryRepository executionHistoryRepository = mock(ExecutionHistoryRepository.class);
    UserRepository userRepository = mock(UserRepository.class);
    User user = new User();
    user.setEmail("user@test.com");
    when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
    when(executionHistoryRepository.save(any(ExecutionHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

    DockerCodeExecutionService service = new DockerCodeExecutionService(executionHistoryRepository, userRepository) {
      @Override
      boolean isDockerAvailable() {
        return false;
      }
    };
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user@test.com", null, List.of()));

    RunRequest request = new RunRequest(
        "java",
        "public class Main { public static void main(String[] args) { System.out.print(\"  Hello  \"); } }",
        "");

    RunResponse response = service.execute(request);

    assertEquals("  Hello  ", response.output());
    assertEquals(ExecutionStatus.SUCCESS, response.status());
  }
}
