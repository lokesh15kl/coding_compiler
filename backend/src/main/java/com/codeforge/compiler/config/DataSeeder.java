package com.codeforge.compiler.config;

import com.codeforge.compiler.domain.Role;
import com.codeforge.compiler.entity.User;
import com.codeforge.compiler.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

  @Bean
  CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    return args -> {
      createUserIfMissing(userRepository, passwordEncoder, "Admin User", "admin@codeforge.dev", "Admin1234", Role.ADMIN);
      createUserIfMissing(userRepository, passwordEncoder, "Demo User", "user@codeforge.dev", "User1234", Role.USER);
    };
  }

  private void createUserIfMissing(UserRepository userRepository, PasswordEncoder passwordEncoder,
      String name, String email, String password, Role role) {
    if (userRepository.existsByEmail(email)) {
      return;
    }
    User user = User.builder()
        .name(name)
        .email(email)
        .password(passwordEncoder.encode(password))
        .role(role)
        .build();
    userRepository.save(user);
  }
}