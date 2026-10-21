package com.codeforge.compiler.service;

import com.codeforge.compiler.dto.roadmap.RoadmapQuestionResponse;
import com.codeforge.compiler.dto.roadmap.RoadmapPushRequest;
import com.codeforge.compiler.dto.roadmap.RoadmapTopicResponse;
import com.codeforge.compiler.entity.RoadmapConfig;
import com.codeforge.compiler.repository.RoadmapConfigRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;

@Service
public class RoadmapService {

  private final RoadmapConfigRepository roadmapConfigRepository;
  private final ObjectMapper objectMapper;

  public RoadmapService(RoadmapConfigRepository roadmapConfigRepository, ObjectMapper objectMapper) {
    this.roadmapConfigRepository = roadmapConfigRepository;
    this.objectMapper = objectMapper;
  }

  private static final List<String> TOPICS = List.of(
      "Arrays",
      "Strings",
      "Hashing",
      "Two Pointers",
      "Stack",
      "Queue",
      "Linked List",
      "Trees",
      "Graphs",
      "Dynamic Programming"
  );

  private static final List<String> TOPIC_DESCRIPTIONS = List.of(
      "Core array manipulation and indexing patterns.",
      "Character-based and substring challenge solving.",
      "Fast lookups with maps, sets, and frequency counts.",
      "Efficient traversal using left/right pointer logic.",
      "LIFO structures for parsing and sequencing problems.",
      "FIFO processing and scheduling patterns.",
      "Pointer-heavy linked list problem solving.",
      "Hierarchy and recursion-driven tree thinking.",
      "Traversal, connectivity, and shortest-path reasoning.",
      "State optimization and recurrence-based problem solving."
  );

  private static final Map<String, List<String>> QUESTION_BANK = Map.ofEntries(
      Map.entry("Arrays", List.of(
          "Two Sum", "Best Time to Buy and Sell Stock", "Product of Array Except Self",
          "Maximum Subarray", "Contains Duplicate", "Move Zeroes", "Majority Element",
          "Container With Most Water", "Find Minimum in Rotated Sorted Array", "Subarray Product Less Than K")),
      Map.entry("Strings", List.of(
          "Valid Anagram", "Longest Palindromic Substring", "Group Anagrams", "Longest Common Prefix",
          "Isomorphic Strings", "Word Pattern", "String to Integer", "Reverse Words in a String",
          "Longest Repeating Character Replacement", "Minimum Window Substring")),
      Map.entry("Hashing", List.of(
          "Top K Frequent Elements", "Subarray Sum Equals K", "Longest Consecutive Sequence",
          "Jewels and Stones", "Find Duplicate Number", "Two Sum II", "Valid Sudoku",
          "First Unique Character", "Longest Subarray With Sum K", "Group Shifted Strings")),
      Map.entry("Two Pointers", List.of(
          "3Sum", "Trapping Rain Water", "Remove Duplicates from Sorted Array", "Sort Colors",
          "Valid Palindrome", "Merge Sorted Array", "Intersection of Two Arrays", "Palindrome Linked List",
          "Squares of a Sorted Array", "Container With Most Water")),
      Map.entry("Stack", List.of(
          "Valid Parentheses", "Daily Temperatures", "Min Stack", "Largest Rectangle in Histogram",
          "Next Greater Element I", "Binary Tree Inorder Traversal", "Evaluate Reverse Polish Notation",
          "Simplify Path", "Remove Duplicate Letters", "Largest Odd Number in String")),
      Map.entry("Queue", List.of(
          "Number of Recent Calls", "Design Circular Queue", "Implement Queue Using Stacks",
          "Rotting Oranges", "Sliding Window Maximum", "Task Scheduler", "Find the Winner",
          "Open the Lock", "Design Hit Counter", "Maximum Frequency Stack")),
      Map.entry("Linked List", List.of(
          "Reverse Linked List", "Merge Two Sorted Lists", "Linked List Cycle", "Remove Nth Node From End",
          "Palindrome Linked List", "Add Two Numbers", "Intersection of Two Linked Lists",
          "Copy List with Random Pointer", "Middle of the Linked List", "Delete Node in a Linked List")),
      Map.entry("Trees", List.of(
          "Maximum Depth of Binary Tree", "Invert Binary Tree", "Same Tree", "Validate Binary Search Tree",
          "Level Order Traversal", "Binary Tree Maximum Path Sum", "Lowest Common Ancestor",
          "Serialize and Deserialize Binary Tree", "Diameter of Binary Tree", "Kth Smallest Element in a BST")),
      Map.entry("Graphs", List.of(
          "Number of Islands", "Clone Graph", "Course Schedule", "Flood Fill",
          "Pacific Atlantic Water Flow", "Topological Sort", "Word Ladder", "Shortest Path in Binary Matrix",
          "Graph Valid Tree", "All Paths From Source to Target")),
      Map.entry("Dynamic Programming", List.of(
          "Climbing Stairs", "House Robber", "Coin Change", "Longest Increasing Subsequence",
          "Word Break", "Unique Paths", "Partition Equal Subset Sum", "Best Time to Buy and Sell Stock III",
          "Maximum Product Subarray", "Edit Distance"))
  );

  private static final Map<String, String> QUESTION_DIFFICULTIES = Map.ofEntries(
      Map.entry("Two Sum", "Easy"),
      Map.entry("Best Time to Buy and Sell Stock", "Easy"),
      Map.entry("Product of Array Except Self", "Medium"),
      Map.entry("Maximum Subarray", "Medium"),
      Map.entry("Contains Duplicate", "Easy"),
      Map.entry("Move Zeroes", "Easy"),
      Map.entry("Majority Element", "Easy"),
      Map.entry("Container With Most Water", "Medium"),
      Map.entry("Find Minimum in Rotated Sorted Array", "Medium"),
      Map.entry("Subarray Product Less Than K", "Medium"),
      Map.entry("Valid Anagram", "Easy"),
      Map.entry("Longest Palindromic Substring", "Medium"),
      Map.entry("Group Anagrams", "Medium"),
      Map.entry("Longest Common Prefix", "Easy"),
      Map.entry("Isomorphic Strings", "Easy"),
      Map.entry("Word Pattern", "Easy"),
      Map.entry("String to Integer", "Medium"),
      Map.entry("Reverse Words in a String", "Medium"),
      Map.entry("Longest Repeating Character Replacement", "Medium"),
      Map.entry("Minimum Window Substring", "Hard"),
      Map.entry("Top K Frequent Elements", "Medium"),
      Map.entry("Subarray Sum Equals K", "Medium"),
      Map.entry("Longest Consecutive Sequence", "Medium"),
      Map.entry("Jewels and Stones", "Easy"),
      Map.entry("Find Duplicate Number", "Medium"),
      Map.entry("Two Sum II", "Medium"),
      Map.entry("Valid Sudoku", "Hard"),
      Map.entry("First Unique Character", "Easy"),
      Map.entry("Longest Subarray With Sum K", "Medium"),
      Map.entry("Group Shifted Strings", "Medium"),
      Map.entry("3Sum", "Medium"),
      Map.entry("Trapping Rain Water", "Hard"),
      Map.entry("Remove Duplicates from Sorted Array", "Easy"),
      Map.entry("Sort Colors", "Medium"),
      Map.entry("Valid Palindrome", "Easy"),
      Map.entry("Merge Sorted Array", "Easy"),
      Map.entry("Intersection of Two Arrays", "Easy"),
      Map.entry("Palindrome Linked List", "Medium"),
      Map.entry("Squares of a Sorted Array", "Easy"),
      Map.entry("Valid Parentheses", "Easy"),
      Map.entry("Daily Temperatures", "Medium"),
      Map.entry("Min Stack", "Medium"),
      Map.entry("Largest Rectangle in Histogram", "Hard"),
      Map.entry("Next Greater Element I", "Easy"),
      Map.entry("Binary Tree Inorder Traversal", "Medium"),
      Map.entry("Evaluate Reverse Polish Notation", "Medium"),
      Map.entry("Simplify Path", "Medium"),
      Map.entry("Remove Duplicate Letters", "Medium"),
      Map.entry("Largest Odd Number in String", "Easy"),
      Map.entry("Number of Recent Calls", "Easy"),
      Map.entry("Design Circular Queue", "Medium"),
      Map.entry("Implement Queue Using Stacks", "Medium"),
      Map.entry("Rotting Oranges", "Medium"),
      Map.entry("Sliding Window Maximum", "Hard"),
      Map.entry("Task Scheduler", "Medium"),
      Map.entry("Find the Winner", "Medium"),
      Map.entry("Open the Lock", "Medium"),
      Map.entry("Design Hit Counter", "Easy"),
      Map.entry("Maximum Frequency Stack", "Hard"),
      Map.entry("Reverse Linked List", "Easy"),
      Map.entry("Merge Two Sorted Lists", "Easy"),
      Map.entry("Linked List Cycle", "Easy"),
      Map.entry("Remove Nth Node From End", "Medium"),
      Map.entry("Add Two Numbers", "Medium"),
      Map.entry("Intersection of Two Linked Lists", "Easy"),
      Map.entry("Copy List with Random Pointer", "Hard"),
      Map.entry("Middle of the Linked List", "Easy"),
      Map.entry("Delete Node in a Linked List", "Easy"),
      Map.entry("Maximum Depth of Binary Tree", "Easy"),
      Map.entry("Invert Binary Tree", "Easy"),
      Map.entry("Same Tree", "Easy"),
      Map.entry("Validate Binary Search Tree", "Medium"),
      Map.entry("Level Order Traversal", "Medium"),
      Map.entry("Binary Tree Maximum Path Sum", "Hard"),
      Map.entry("Lowest Common Ancestor", "Medium"),
      Map.entry("Serialize and Deserialize Binary Tree", "Hard"),
      Map.entry("Diameter of Binary Tree", "Medium"),
      Map.entry("Kth Smallest Element in a BST", "Medium"),
      Map.entry("Number of Islands", "Medium"),
      Map.entry("Clone Graph", "Medium"),
      Map.entry("Course Schedule", "Medium"),
      Map.entry("Flood Fill", "Easy"),
      Map.entry("Pacific Atlantic Water Flow", "Hard"),
      Map.entry("Topological Sort", "Medium"),
      Map.entry("Word Ladder", "Hard"),
      Map.entry("Shortest Path in Binary Matrix", "Medium"),
      Map.entry("Graph Valid Tree", "Medium"),
      Map.entry("All Paths From Source to Target", "Hard"),
      Map.entry("Climbing Stairs", "Easy"),
      Map.entry("House Robber", "Medium"),
      Map.entry("Coin Change", "Medium"),
      Map.entry("Longest Increasing Subsequence", "Medium"),
      Map.entry("Word Break", "Medium"),
      Map.entry("Unique Paths", "Medium"),
      Map.entry("Partition Equal Subset Sum", "Medium"),
      Map.entry("Best Time to Buy and Sell Stock III", "Hard"),
      Map.entry("Maximum Product Subarray", "Medium"),
      Map.entry("Edit Distance", "Hard")
  );

  public List<RoadmapTopicResponse> getRoadmap() {
    return roadmapConfigRepository.findFirstByOrderByUpdatedAtDesc()
        .map(this::readStoredRoadmap)
        .orElseGet(this::defaultRoadmap);
  }

  public RoadmapConfig saveRoadmap(RoadmapPushRequest request) {
    LocalDateTime now = LocalDateTime.now();
    String json;
    try {
      json = objectMapper.writeValueAsString(request.topics());
    } catch (JsonProcessingException exception) {
      throw new IllegalArgumentException("Unable to serialize roadmap", exception);
    }
    RoadmapConfig entity = RoadmapConfig.builder()
        .name(Objects.requireNonNullElse(request.name(), "Roadmap"))
        .roadmapJson(json)
        .createdAt(now)
        .updatedAt(now)
        .build();
    return roadmapConfigRepository.save(entity);
  }

  public List<RoadmapQuestionResponse> flattenQuestions() {
    return getRoadmap().stream()
        .flatMap(topic -> topic.questions().stream())
        .toList();
  }

  public void resetToDefault() {
    roadmapConfigRepository.deleteAll();
  }

  private List<RoadmapTopicResponse> defaultRoadmap() {
    List<RoadmapTopicResponse> topics = new ArrayList<>();
    for (int i = 0; i < TOPICS.size(); i++) {
      String topicName = TOPICS.get(i);
      List<RoadmapQuestionResponse> questions = buildQuestionsForTopic(topicName, i);
      topics.add(new RoadmapTopicResponse(topicName, TOPIC_DESCRIPTIONS.get(i), questions));
    }
    return topics;
  }

  private List<RoadmapTopicResponse> readStoredRoadmap(RoadmapConfig config) {
    try {
      return objectMapper.readValue(config.getRoadmapJson(), new TypeReference<List<RoadmapTopicResponse>>() {});
    } catch (JsonProcessingException exception) {
      return defaultRoadmap();
    }
  }

  private List<RoadmapQuestionResponse> buildQuestionsForTopic(String topic, int topicIndex) {
    return IntStream.range(0, 10)
        .mapToObj(offset -> createQuestion(topic, topicIndex, offset))
        .toList();
  }

  private RoadmapQuestionResponse createQuestion(String topic, int topicIndex, int offset) {
    int number = (topicIndex * 10) + offset + 1;
    String title = QUESTION_BANK.get(topic).get(offset);
    String difficulty = QUESTION_DIFFICULTIES.getOrDefault(title, "Medium");
    return new RoadmapQuestionResponse(
        "Q-%03d".formatted(number),
        topic,
        title,
        difficulty,
        "Solve the %s problem in %s using an efficient approach and clear reasoning.".formatted(title, topic),
        List.of(topic.toLowerCase().replace(" & ", "-").replace(" ", "-"), "practice", "coding"));
  }
}
