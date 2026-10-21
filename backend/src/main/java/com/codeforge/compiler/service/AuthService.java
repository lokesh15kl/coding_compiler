package com.codeforge.compiler.service;

import com.codeforge.compiler.domain.Role;
import com.codeforge.compiler.dto.auth.AuthResponse;
import com.codeforge.compiler.dto.auth.ChangePasswordRequest;
import com.codeforge.compiler.dto.auth.LoginRequest;
import com.codeforge.compiler.dto.auth.RegisterRequest;
import com.codeforge.compiler.entity.User;
import com.codeforge.compiler.repository.UserRepository;
import com.codeforge.compiler.security.JwtService;
import jakarta.transaction.Transactional;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager, JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  public void register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("Email already exists");
    }
    User user = User.builder()
        .name(request.name())
        .email(request.email())
        .password(passwordEncoder.encode(request.password()))
        .role(Role.USER)
        .build();
    userRepository.save(user);
  }

  public AuthResponse login(LoginRequest request) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password()));
    User user = userRepository.findByEmail(authentication.getName())
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    String token = jwtService.generateToken(user.getEmail(), Map.of("role", user.getRole().name()));
    return new AuthResponse(token, user.getName(), user.getRole().name());
  }

  @Transactional
  public void changePassword(ChangePasswordRequest request) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
      throw new IllegalArgumentException("Current password is incorrect");
    }
    user.setPassword(passwordEncoder.encode(request.newPassword()));
    userRepository.save(user);
  }

  @Transactional
  public void deleteCurrentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    userRepository.findByEmail(email).ifPresent(userRepository::delete);
  }
}