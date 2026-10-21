package com.codeforge.compiler.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.codeforge.compiler.repository.RoadmapConfigRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class RoadmapServiceTest {

  @Test
  void shouldCreateStructuredRoadmapWithHundredQuestions() {
    RoadmapService service = new RoadmapService(mock(RoadmapConfigRepository.class), new ObjectMapper());

    var roadmap = service.getRoadmap();
    int totalQuestions = roadmap.stream().mapToInt(topic -> topic.questions().size()).sum();

    assertEquals(100, totalQuestions);
    assertTrue(roadmap.size() >= 5);
    assertTrue(roadmap.stream().anyMatch(topic -> "Arrays".equals(topic.topic())));
    assertTrue(roadmap.stream().allMatch(topic -> !topic.questions().isEmpty()));
  }
}
