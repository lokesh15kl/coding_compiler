package com.codeforge.compiler.service;

import com.codeforge.compiler.domain.ExecutionStatus;
import com.codeforge.compiler.dto.admin.AdminStatsResponse;
import com.codeforge.compiler.dto.admin.ProblemRequest;
import com.codeforge.compiler.dto.admin.ProblemResponse;
import com.codeforge.compiler.dto.admin.TopicRequest;
import com.codeforge.compiler.dto.admin.TopicResponse;
import com.codeforge.compiler.dto.admin.TopicWithProblemsResponse;
import com.codeforge.compiler.dto.admin.UserPerformanceResponse;
import com.codeforge.compiler.entity.ExecutionHistory;
import com.codeforge.compiler.entity.Problem;
import com.codeforge.compiler.entity.Program;
import com.codeforge.compiler.entity.Topic;
import com.codeforge.compiler.entity.User;
import com.codeforge.compiler.repository.ExecutionHistoryRepository;
import com.codeforge.compiler.repository.ProblemRepository;
import com.codeforge.compiler.repository.ProgramRepository;
import com.codeforge.compiler.repository.TopicRepository;
import com.codeforge.compiler.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

  private final UserRepository userRepository;
  private final ProgramRepository programRepository;
  private final ExecutionHistoryRepository executionHistoryRepository;
  private final ProblemRepository problemRepository;
  private final TopicRepository topicRepository;

  public AdminService(UserRepository userRepository, ProgramRepository programRepository,
      ExecutionHistoryRepository executionHistoryRepository, ProblemRepository problemRepository,
      TopicRepository topicRepository) {
    this.userRepository = userRepository;
    this.programRepository = programRepository;
    this.executionHistoryRepository = executionHistoryRepository;
    this.problemRepository = problemRepository;
    this.topicRepository = topicRepository;
  }

  public AdminStatsResponse stats() {
    long totalUsers = userRepository.count();
    long totalPrograms = programRepository.count();
    long totalExecutions = executionHistoryRepository.count();
    String mostUsedLanguage = programRepository.findAll().stream()
        .map(Program::getLanguage)
        .collect(java.util.stream.Collectors.groupingBy(language -> language, java.util.stream.Collectors.counting()))
        .entrySet().stream()
        .max(java.util.Map.Entry.comparingByValue())
        .map(java.util.Map.Entry::getKey)
        .orElse("Java");
    return new AdminStatsResponse(totalUsers, totalPrograms, totalExecutions, mostUsedLanguage);
  }

  public List<TopicResponse> getTopics() {
    return topicRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toTopicResponse).toList();
  }

  public TopicResponse createTopic(TopicRequest request) {
    LocalDateTime now = LocalDateTime.now();
    Topic topic = Topic.builder()
        .name(Objects.requireNonNullElse(request.name(), "General"))
        .description(Objects.requireNonNullElse(request.description(), ""))
        .createdAt(now)
        .updatedAt(now)
        .build();
    return toTopicResponse(topicRepository.save(topic));
  }

  public List<ProblemResponse> getProblems() {
    return problemRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toProblemResponse).toList();
  }

  public ProblemResponse createProblem(ProblemRequest request) {
    LocalDateTime now = LocalDateTime.now();
    Topic topic = request.topicId() == null ? null : topicRepository.findById(request.topicId()).orElse(null);
    Problem problem = Problem.builder()
        .title(Objects.requireNonNullElse(request.title(), "Untitled problem"))
        .difficulty(Objects.requireNonNullElse(request.difficulty(), "Easy"))
        .language(Objects.requireNonNullElse(request.language(), "Java"))
        .statement(Objects.requireNonNullElse(request.statement(), ""))
        .inputFormat(Objects.requireNonNullElse(request.inputFormat(), ""))
        .outputFormat(Objects.requireNonNullElse(request.outputFormat(), ""))
        .sampleInput(Objects.requireNonNullElse(request.sampleInput(), ""))
        .sampleOutput(Objects.requireNonNullElse(request.sampleOutput(), ""))
        .topic(topic)
        .createdAt(now)
        .updatedAt(now)
        .build();
    return toProblemResponse(problemRepository.save(problem));
  }

  public ProblemResponse updateProblem(Long problemId, ProblemRequest request) {
    Problem problem = problemRepository.findById(problemId).orElseThrow(() -> new IllegalArgumentException("Problem not found"));
    problem.setTitle(Objects.requireNonNullElse(request.title(), problem.getTitle()));
    problem.setDifficulty(Objects.requireNonNullElse(request.difficulty(), problem.getDifficulty()));
    problem.setLanguage(Objects.requireNonNullElse(request.language(), problem.getLanguage()));
    problem.setStatement(Objects.requireNonNullElse(request.statement(), problem.getStatement()));
    problem.setInputFormat(Objects.requireNonNullElse(request.inputFormat(), problem.getInputFormat()));
    problem.setOutputFormat(Objects.requireNonNullElse(request.outputFormat(), problem.getOutputFormat()));
    problem.setSampleInput(Objects.requireNonNullElse(request.sampleInput(), problem.getSampleInput()));
    problem.setSampleOutput(Objects.requireNonNullElse(request.sampleOutput(), problem.getSampleOutput()));
    problem.setUpdatedAt(LocalDateTime.now());
    return toProblemResponse(problemRepository.save(problem));
  }

  @Transactional
  public void deleteProblem(Long problemId) {
    problemRepository.deleteById(problemId);
  }

  public List<UserPerformanceResponse> getUserPerformance() {
    List<User> users = userRepository.findAll();
    return users.stream()
        .map(user -> {
          List<ExecutionHistory> history = executionHistoryRepository.findByUserOrderByCreatedAtDesc(user);
          long totalRuns = history.size();
          long successfulRuns = history.stream().filter(item -> item.getStatus() == ExecutionStatus.SUCCESS).count();
          long failedRuns = history.stream().filter(item -> item.getStatus() != ExecutionStatus.SUCCESS).count();
          double successRate = totalRuns == 0 ? 0 : (successfulRuns * 100.0) / totalRuns;
          long bestExecutionTimeMs = history.stream()
              .filter(item -> item.getExecutionTime() != null)
              .mapToLong(ExecutionHistory::getExecutionTime)
              .min()
              .orElse(0L);
          return new UserPerformanceResponse(
              user.getId(),
              user.getName(),
              user.getEmail(),
              totalRuns,
              successfulRuns,
              failedRuns,
              successRate,
              bestExecutionTimeMs);
        })
        .sorted(Comparator.comparingLong(UserPerformanceResponse::totalRuns).reversed())
        .toList();
  }

  @Transactional
  public void deleteUser(Long userId) {
    userRepository.deleteById(userId);
  }

  @Transactional
  public void deleteProgram(Long programId) {
    programRepository.deleteById(programId);
  }

  public List<TopicWithProblemsResponse> getTopicsWithProblems() {
    return topicRepository.findAllByOrderByCreatedAtDesc().stream()
        .map(topic -> new TopicWithProblemsResponse(
            topic.getId(),
            topic.getName(),
            topic.getDescription(),
            problemRepository.findByTopicIdOrderByCreatedAtDesc(topic.getId()).stream()
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

  private TopicResponse toTopicResponse(Topic topic) {
    return new TopicResponse(topic.getId(), topic.getName(), topic.getDescription(), topic.getCreatedAt(), topic.getUpdatedAt());
  }
}