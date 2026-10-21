package com.codeforge.compiler.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codeforge.compiler.entity.Problem;
import com.codeforge.compiler.entity.Topic;
import com.codeforge.compiler.repository.ProblemRepository;
import com.codeforge.compiler.repository.TopicRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProblemServiceTest {

  @Test
  void shouldGroupProblemsByTopicForPracticeView() {
    TopicRepository topicRepository = mock(TopicRepository.class);
    ProblemRepository problemRepository = mock(ProblemRepository.class);
    ProblemService service = new ProblemService(problemRepository, topicRepository);

    Topic topic = Topic.builder()
        .id(1L)
        .name("Arrays")
        .description("Array problems")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    Problem problem = Problem.builder()
        .id(10L)
        .title("Two Sum")
        .difficulty("Easy")
        .language("Java")
        .statement("Find sum")
        .topic(topic)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    when(topicRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(topic));
    when(problemRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(problem));

    var result = service.getTopicsWithProblems();

    assertEquals(1, result.size());
    assertEquals("Arrays", result.get(0).name());
    assertEquals(1, result.get(0).problems().size());
    assertEquals("Two Sum", result.get(0).problems().get(0).title());
  }
}
