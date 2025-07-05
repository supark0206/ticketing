package com.concertmania.ticketing.user.repository;

import com.concertmania.ticketing.user.entity.User;
import com.concertmania.ticketing.user.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    long countByRole(UserRole role);
}