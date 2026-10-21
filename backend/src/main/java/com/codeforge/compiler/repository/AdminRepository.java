package com.codeforge.compiler.repository;

import com.codeforge.compiler.domain.Role;
import com.codeforge.compiler.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<User, Long> {
  long countByRole(Role role);
  List<User> findByRole(Role role);
}