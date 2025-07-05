package com.concertmania.ticketing.user.service;

import com.concertmania.ticketing.user.entity.User;
import com.concertmania.ticketing.user.enums.UserRole;
import com.concertmania.ticketing.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminInitConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 관리자 기본 계정 생성
    @Bean
    public ApplicationRunner initializeAdmin() {
        return args -> {
            createDefaultAdminIfNotExists();
        };
    }

    @Transactional
    public void createDefaultAdminIfNotExists() {
        long adminCount = userRepository.countByRole(UserRole.ADMIN);
        
        if (adminCount == 0) {
            User defaultAdmin = User.builder()
                    .username("관리자")
                    .email("admin@admin")
                    .password(passwordEncoder.encode("1234"))
                    .role(UserRole.ADMIN)
                    .build();
            
            userRepository.save(defaultAdmin);
        }
    }
}
