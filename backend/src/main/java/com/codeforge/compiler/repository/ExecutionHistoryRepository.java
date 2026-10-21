package com.codeforge.compiler.repository;

import com.codeforge.compiler.entity.ExecutionHistory;
import com.codeforge.compiler.entity.Program;
import com.codeforge.compiler.entity.User;
import com.codeforge.compiler.domain.ExecutionStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionHistoryRepository extends JpaRepository<ExecutionHistory, Long> {
  List<ExecutionHistory> findByUserOrderByCreatedAtDesc(User user);
  List<ExecutionHistory> findTop20ByUserOrderByCreatedAtDesc(User user);
  long countByUser(User user);
  long countByUserAndStatus(User user, ExecutionStatus status);
  long countByStatus(ExecutionStatus status);
  List<ExecutionHistory> findByProgram(Program program);
}