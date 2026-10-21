package com.codeforge.compiler.repository;

import com.codeforge.compiler.entity.RoadmapConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapConfigRepository extends JpaRepository<RoadmapConfig, Long> {
  Optional<RoadmapConfig> findFirstByOrderByUpdatedAtDesc();
}
