package com.codeforge.compiler.repository;

import com.codeforge.compiler.entity.Program;
import com.codeforge.compiler.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramRepository extends JpaRepository<Program, Long> {
  List<Program> findByUserOrderByUpdatedAtDesc(User user);
  Optional<Program> findByShareId(String shareId);
  long countByUser(User user);
}