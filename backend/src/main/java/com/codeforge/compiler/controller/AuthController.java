package com.codeforge.compiler.controller;

import com.codeforge.compiler.dto.auth.AuthResponse;
import com.codeforge.compiler.dto.auth.ChangePasswordRequest;
import com.codeforge.compiler.dto.auth.LoginRequest;
import com.codeforge.compiler.dto.auth.RegisterRequest;
import com.codeforge.compiler.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
    authService.register(request);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest request) {
    return authService.login(request);
  }

  @PostMapping("/change-password")
  public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
    authService.changePassword(request);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> deleteAccount() {
    authService.deleteCurrentUser();
    return ResponseEntity.noContent().build();
  }
}