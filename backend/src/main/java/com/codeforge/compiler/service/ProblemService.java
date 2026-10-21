package com.codeforge.compiler.service;

import com.codeforge.compiler.dto.admin.ProblemResponse;
import com.codeforge.compiler.dto.admin.TopicWithProblemsResponse;
import com.codeforge.compiler.entity.Problem;
import com.codeforge.compiler.entity.Topic;
import com.codeforge.compiler.repository.ProblemRepository;
import com.codeforge.compiler.repository.TopicRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProblemService {

  private final ProblemRepository problemRepository;
  private final TopicRepository topicRepository;

  public ProblemService(ProblemRepository problemRepository, TopicRepository topicRepository) {
    this.problemRepository = problemRepository;
    this.topicRepository = topicRepository;
  }

  public List<TopicWithProblemsResponse> getTopicsWithProblems() {
    var topics = topicRepository.findAllByOrderByCreatedAtDesc();
    var problemsByTopic = problemRepository.findAllByOrderByCreatedAtDesc().stream()
        .filter(problem -> problem.getTopic() != null)
        .collect(java.util.stream.Collectors.groupingBy(problem -> problem.getTopic().getId()));

    return topics.stream()
        .map(topic -> new TopicWithProblemsResponse(
            topic.getId(),
            topic.getName(),
            topic.getDescription(),
            problemsByTopic.getOrDefault(topic.getId(), List.of()).stream()
                .map(this::toProblemResponse)
                .toList()))
        .toList();
  }

  private ProblemResponse toProblemResponse(Problem problem) {
    return new ProblemResponse(
        problem.getId(),
        problem.getTitle(),
        problem.getDifficulty(),
        problem.getLanguage(),
        problem.getStatement(),
        problem.getInputFormat(),
        problem.getOutputFormat(),
        problem.getSampleInput(),
        problem.getSampleOutput(),
        problem.getCreatedAt(),
        problem.getUpdatedAt());
  }
}
