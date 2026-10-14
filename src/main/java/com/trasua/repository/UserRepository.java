package com.trasua.repository;

import com.trasua.entity.Role;
import com.trasua.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findFirstByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);

    List<User> findAllByOrderByRoleAscUsernameAsc();

    long countByRole(Role role);
}
