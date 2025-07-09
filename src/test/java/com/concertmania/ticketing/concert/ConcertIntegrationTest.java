package com.concertmania.ticketing.concert;

import com.concertmania.ticketing.concert.dto.ConcertCreateRequest;
import com.concertmania.ticketing.concert.dto.ConcertResponse;
import com.concertmania.ticketing.concert.entity.Concert;
import com.concertmania.ticketing.concert.enums.ConcertStatus;
import com.concertmania.ticketing.concert.repository.ConcertRepository;
import com.concertmania.ticketing.config.security.JwtTokenProvider;
import com.concertmania.ticketing.user.entity.User;
import com.concertmania.ticketing.user.enums.UserRole;
import com.concertmania.ticketing.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
@ActiveProfiles("test")
public class ConcertIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("ticketing_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User normalUser;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        // 관리자 사용자 생성
        adminUser = User.builder()
                .username("admin")
                .email("admin@test.com")
                .password(passwordEncoder.encode("password"))
                .role(UserRole.ADMIN)
                .build();
        adminUser = userRepository.save(adminUser);

        // 일반 사용자 생성
        normalUser = User.builder()
                .username("user")
                .email("user@test.com")
                .password(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .build();
        normalUser = userRepository.save(normalUser);

        // JWT 토큰 생성
        adminToken = jwtTokenProvider.createToken(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        adminUser, null, adminUser.getAuthorities()
                )
        );
        userToken = jwtTokenProvider.createToken(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        normalUser, null, normalUser.getAuthorities()
                )
        );
    }

    @Test
    @DisplayName("콘서트 생성")
    void createConcert_Success() throws Exception {
        // given
        ConcertCreateRequest request = new ConcertCreateRequest(
                "아이유 콘서트 2024",
                "올림픽공원 체조경기장",
                LocalDateTime.of(2024, 12, 25, 19, 0),
                LocalDateTime.of(2024, 12, 1, 10, 0),
                LocalDateTime.of(2024, 12, 24, 23, 59)
        );

        // when & then
        mockMvc.perform(post("/api/concert")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("아이유 콘서트 2024"))
                .andExpect(jsonPath("$.venue").value("올림픽공원 체조경기장"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("콘서트 ID 콘서트를 조회")
    void getConcert_Success() throws Exception {
        // given
        Concert concert = Concert.builder()
                .title("아이유 콘서트 2024")
                .venue("올림픽공원 체조경기장")
                .concertDate(LocalDateTime.of(2024, 12, 25, 19, 0))
                .openTime(LocalDateTime.of(2024, 12, 1, 10, 0))
                .closeTime(LocalDateTime.of(2024, 12, 24, 23, 59))
                .status(ConcertStatus.SCHEDULED)
                .build();
        Concert savedConcert = concertRepository.save(concert);

        // when & then
        mockMvc.perform(get("/api/concert/" + savedConcert.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("아이유 콘서트 2024"))
                .andExpect(jsonPath("$.venue").value("올림픽공원 체조경기장"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }
}