package com.codeforge.compiler.repository;

import com.codeforge.compiler.entity.RoadmapProgress;
import com.codeforge.compiler.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapProgressRepository extends JpaRepository<RoadmapProgress, Long> {
  Optional<RoadmapProgress> findByUserAndQuestionId(User user, String questionId);

  List<RoadmapProgress> findByUserOrderByUpdatedAtDesc(User user);

  List<RoadmapProgress> findAllByOrderByUpdatedAtDesc();
}
