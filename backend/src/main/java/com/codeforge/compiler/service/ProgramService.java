package com.codeforge.compiler.service;

import com.codeforge.compiler.dto.program.ProgramResponse;
import com.codeforge.compiler.dto.program.SaveProgramRequest;
import com.codeforge.compiler.dto.program.SharedProgramResponse;
import com.codeforge.compiler.entity.Program;
import com.codeforge.compiler.entity.User;
import com.codeforge.compiler.repository.ProgramRepository;
import com.codeforge.compiler.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ProgramService {

  private final ProgramRepository programRepository;
  private final UserRepository userRepository;

  public ProgramService(ProgramRepository programRepository, UserRepository userRepository) {
    this.programRepository = programRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public ProgramResponse saveProgram(SaveProgramRequest request) {
    User user = currentUser();
    Program program = Program.builder()
        .title(request.title())
        .language(request.language())
        .code(request.code())
        .shareId(UUID.randomUUID().toString().replace("-", "").substring(0, 12))
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .user(user)
        .build();
    Program saved = programRepository.save(program);
    return new ProgramResponse(saved.getId(), saved.getTitle(), saved.getLanguage(), saved.getUpdatedAt(), saved.getShareId());
  }

  public List<ProgramResponse> myPrograms() {
    return programRepository.findByUserOrderByUpdatedAtDesc(currentUser()).stream()
        .map(program -> new ProgramResponse(program.getId(), program.getTitle(), program.getLanguage(), program.getUpdatedAt(), program.getShareId()))
        .toList();
  }

  public SharedProgramResponse findSharedProgram(String shareId) {
    Program program = programRepository.findByShareId(shareId)
        .orElseThrow(() -> new IllegalArgumentException("Share link not found"));
    return new SharedProgramResponse(program.getTitle(), program.getLanguage(), program.getCode());
  }

  @Transactional
  public void deleteProgram(Long programId) {
    Program program = programRepository.findById(programId)
        .orElseThrow(() -> new IllegalArgumentException("Program not found"));
    if (!ownsProgram(program) && !isAdmin()) {
      throw new IllegalArgumentException("You are not allowed to delete this program");
    }
    programRepository.delete(program);
  }

  private User currentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  private boolean ownsProgram(Program program) {
    return program.getUser() != null && program.getUser().getEmail().equals(currentUser().getEmail());
  }

  private boolean isAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null && authentication.getAuthorities().stream()
        .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
  }
}