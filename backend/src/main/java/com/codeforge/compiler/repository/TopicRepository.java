package com.codeforge.compiler.repository;

import com.codeforge.compiler.entity.Topic;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {
  List<Topic> findAllByOrderByCreatedAtDesc();
}
