package com.codeforge.compiler.service;

import com.codeforge.compiler.domain.ExecutionStatus;
import com.codeforge.compiler.dto.compiler.RunRequest;
import com.codeforge.compiler.dto.compiler.RunResponse;
import com.codeforge.compiler.entity.ExecutionHistory;
import com.codeforge.compiler.entity.User;
import com.codeforge.compiler.repository.ExecutionHistoryRepository;
import com.codeforge.compiler.repository.UserRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DockerCodeExecutionService implements CodeExecutionService {

  @Value("${app.execution.timeout-seconds:8}")
  private long timeoutSeconds = 8L;

  @Value("${app.execution.max-memory-mb:256}")
  private long maxMemoryMb = 256L;

  @Value("${app.execution.cpu-limit:0.5}")
  private String cpuLimit = "0.5";

  private final ExecutionHistoryRepository executionHistoryRepository;
  private final UserRepository userRepository;

  public DockerCodeExecutionService(ExecutionHistoryRepository executionHistoryRepository, UserRepository userRepository) {
    this.executionHistoryRepository = executionHistoryRepository;
    this.userRepository = userRepository;
  }

  private static final Map<String, LanguageRuntime> RUNTIMES = Map.of(
      "java", new LanguageRuntime("openjdk:21-jdk-slim", "Main.java", "javac Main.java && java Main", "Main", "javac Main.java && java Main"),
      "python", new LanguageRuntime("python:3.12-slim", "main.py", "python main.py", "main", "python main.py"),
      "c", new LanguageRuntime("gcc:14", "main.c", "gcc main.c -O2 -o main && ./main", "main", "gcc main.c -O2 -o main && main"),
      "cpp", new LanguageRuntime("gcc:14", "main.cpp", "g++ main.cpp -O2 -std=c++20 -o main && ./main", "main", "g++ main.cpp -O2 -std=c++20 -o main && main"),
      "javascript", new LanguageRuntime("node:22-slim", "main.js", "node main.js", "main", "node main.js"));

  @Override
  public RunResponse execute(RunRequest request) {
    LanguageRuntime runtime = RUNTIMES.get(normalize(request.language()));
    if (runtime == null) {
      return new RunResponse(ExecutionStatus.FAILED, "", "Unsupported language", 0L, 0L);
    }

    Path tempDirectory = null;
    try {
      tempDirectory = Files.createTempDirectory("compiler-" + UUID.randomUUID());
      Files.writeString(tempDirectory.resolve(runtime.fileName()), request.code(), StandardCharsets.UTF_8);
      Files.writeString(tempDirectory.resolve("input.txt"), request.input() == null ? "" : request.input(), StandardCharsets.UTF_8);

      Instant start = Instant.now();
      ProcessResult result = isDockerAvailable() ? runDocker(runtime, tempDirectory) : runLocally(runtime, tempDirectory);
      long elapsed = Duration.between(start, Instant.now()).toMillis();
      ExecutionStatus status = classify(result.exitCode(), result.stderr());
      persistHistory(request, result.stdout(), result.stderr(), status, elapsed);
      return new RunResponse(status, result.stdout(), result.stderr(), elapsed, 0L);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      return new RunResponse(ExecutionStatus.FAILED, "", exception.getMessage(), 0L, 0L);
    } catch (IOException exception) {
      return new RunResponse(ExecutionStatus.FAILED, "", exception.getMessage(), 0L, 0L);
    } finally {
      if (tempDirectory != null) {
        deleteRecursively(tempDirectory);
      }
    }
  }

  private ProcessResult runDocker(LanguageRuntime runtime, Path tempDirectory) throws IOException, InterruptedException {
    String safeCpuLimit = cpuLimit == null || cpuLimit.isBlank() ? "0.5" : cpuLimit;
    String safeMemory = maxMemoryMb > 0 ? maxMemoryMb + "m" : "256m";

    List<String> command = new ArrayList<>();
    command.add("docker");
    command.add("run");
    command.add("--rm");
    command.add("--network");
    command.add("none");
    command.add("--cpus");
    command.add(safeCpuLimit);
    command.add("--memory");
    command.add(safeMemory);
    command.add("--pids-limit");
    command.add("64");
    command.add("-v");
    command.add(tempDirectory.toAbsolutePath() + ":/workspace");
    command.add("-w");
    command.add("/workspace");
    command.add(runtime.image());
    command.add("sh");
    command.add("-lc");
    command.add(runtime.dockerCommand() + " < input.txt");

    Process process = new ProcessBuilder(command)
        .redirectErrorStream(false)
        .start();

    boolean finished = process.waitFor(timeoutSeconds + 2, TimeUnit.SECONDS);
    if (!finished) {
      process.destroyForcibly();
      return new ProcessResult("", "Execution timed out", -1);
    }

    String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
    return new ProcessResult(stdout, stderr, process.exitValue());
  }

  private ProcessResult runLocally(LanguageRuntime runtime, Path tempDirectory) throws IOException, InterruptedException {
    String commandText = runtime.localCommand() + " < input.txt";
    List<String> command = isWindows() ? List.of("cmd", "/c", commandText) : List.of("sh", "-lc", commandText);

    Process process = new ProcessBuilder(command)
        .directory(tempDirectory.toFile())
        .redirectErrorStream(false)
        .start();

    boolean finished = process.waitFor(timeoutSeconds + 2, TimeUnit.SECONDS);
    if (!finished) {
      process.destroyForcibly();
      return new ProcessResult("", "Execution timed out", -1);
    }

    String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
    return new ProcessResult(stdout, stderr, process.exitValue());
  }

  boolean isDockerAvailable() {
    try {
      Process process = new ProcessBuilder("docker", "info").start();
      boolean finished = process.waitFor(5, TimeUnit.SECONDS);
      if (!finished) {
        process.destroyForcibly();
        return false;
      }
      return process.exitValue() == 0;
    } catch (IOException | InterruptedException exception) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  private ExecutionStatus classify(int exitCode, String stderr) {
    if (exitCode == 0) {
      return ExecutionStatus.SUCCESS;
    }
    if (stderr != null && stderr.toLowerCase().contains("timed out")) {
      return ExecutionStatus.TIMEOUT;
    }
    if (stderr != null && stderr.toLowerCase().contains("error")) {
      return ExecutionStatus.COMPILE_ERROR;
    }
    return ExecutionStatus.RUNTIME_ERROR;
  }

  private String normalize(String language) {
    return language == null ? "" : language.trim().toLowerCase().replace("c++", "cpp");
  }

  private boolean isWindows() {
    return System.getProperty("os.name", "").toLowerCase().contains("win");
  }

  private void deleteRecursively(Path root) {
    try (var stream = Files.walk(root)) {
      stream.sorted((left, right) -> right.compareTo(left)).forEach(path -> {
        try {
          Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
      });
    } catch (IOException ignored) {
    }
  }

  private void persistHistory(RunRequest request, String output, String stderr, ExecutionStatus status, long executionTime) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    executionHistoryRepository.save(ExecutionHistory.builder()
        .program(null)
        .user(user)
        .input(request.input())
        .output(output != null && !output.isBlank() ? output : stderr)
        .status(status)
        .executionTime(executionTime)
        .memoryUsage(0L)
        .createdAt(java.time.LocalDateTime.now())
        .build());
  }

  private record LanguageRuntime(String image, String fileName, String dockerCommand, String executableName, String localCommand) {}

  private record ProcessResult(String stdout, String stderr, int exitCode) {}
}