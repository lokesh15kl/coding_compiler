package com.codeforge.compiler.repository;

import com.codeforge.compiler.entity.Problem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
  List<Problem> findAllByOrderByCreatedAtDesc();
  List<Problem> findByTopicIdOrderByCreatedAtDesc(Long topicId);
}
