package com.codeforge.compiler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "problems")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Problem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String difficulty;

  @Column(nullable = false)
  private String language;

  @Lob
  @Column(nullable = false, columnDefinition = "LONGTEXT")
  private String statement;

  @Lob
  @Column(columnDefinition = "LONGTEXT")
  private String inputFormat;

  @Lob
  @Column(columnDefinition = "LONGTEXT")
  private String outputFormat;

  @Lob
  @Column(columnDefinition = "LONGTEXT")
  private String sampleInput;

  @Lob
  @Column(columnDefinition = "LONGTEXT")
  private String sampleOutput;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "topic_id")
  private Topic topic;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;
}
