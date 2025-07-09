package com.concertmania.ticketing.queue;

import com.concertmania.ticketing.concert.entity.Concert;
import com.concertmania.ticketing.concert.enums.ConcertStatus;
import com.concertmania.ticketing.concert.repository.ConcertRepository;
import com.concertmania.ticketing.config.security.JwtTokenProvider;
import com.concertmania.ticketing.queue.dto.QueueRequest;
import com.concertmania.ticketing.user.entity.User;
import com.concertmania.ticketing.user.enums.UserRole;
import com.concertmania.ticketing.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
@ActiveProfiles("test")
public class QueueIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("ticketing_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

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

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private User normalUser;
    private String userToken;
    private Concert testConcert;

    @BeforeEach
    void setUp() {
        // Redis 초기화
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // 일반 사용자 생성
        normalUser = User.builder()
                .username("user")
                .email("user@test.com")
                .password(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .build();
        normalUser = userRepository.save(normalUser);

        // JWT 토큰 생성
        userToken = jwtTokenProvider.createToken(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        normalUser, null, normalUser.getAuthorities()
                )
        );

        // 테스트용 콘서트 생성
        testConcert = Concert.builder()
                .title("아이유 콘서트 2024")
                .venue("올림픽공원 체조경기장")
                .concertDate(LocalDateTime.now().plusDays(30))
                .openTime(LocalDateTime.now().plusDays(1))
                .closeTime(LocalDateTime.now().plusDays(29))
                .status(ConcertStatus.SCHEDULED)
                .build();
        testConcert = concertRepository.save(testConcert);
    }

    @Test
    @DisplayName("대기열 등록 - 성공")
    void registerToQueue_Success() throws Exception {
        // given
        QueueRequest request = new QueueRequest(testConcert.getId());

        // when & then
        mockMvc.perform(post("/api/queue")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concertId").value(testConcert.getId()))
                .andExpect(jsonPath("$.userId").value(normalUser.getId()))
                .andExpect(jsonPath("$.position").exists())
                .andExpect(jsonPath("$.register").value(true))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("대기열 등록 - 중복 등록")
    void registerToQueue_Duplicate() throws Exception {
        // given
        QueueRequest request = new QueueRequest(testConcert.getId());

        // 첫 번째 등록
        mockMvc.perform(post("/api/queue")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // when & then - 두 번째 등록 시도
        mockMvc.perform(post("/api/queue")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.register").value(false))
                .andExpect(jsonPath("$.message").value("이미 대기열에 등록되어 있습니다. 현재 순번: 1"));
    }

    @Test
    @DisplayName("대기열 등록 - 존재하지 않는 콘서트")
    void registerToQueue_ConcertNotFound() throws Exception {
        // given
        QueueRequest request = new QueueRequest(99999L);

        // when & then
        mockMvc.perform(post("/api/queue")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("대기열 SSE 스트리밍 - 연결 테스트")
    void streamQueueStatus_Success() throws Exception {
        // given - 먼저 대기열에 등록
        QueueRequest request = new QueueRequest(testConcert.getId());
        mockMvc.perform(post("/api/queue")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // when & then - SSE 스트림 연결 테스트 (연결만 확인)
        mockMvc.perform(get("/api/queue/" + testConcert.getId() + "/stream")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/event-stream;charset=UTF-8"));
    }

    @Test
    @DisplayName("대기열 등록 - 유효성 검증 실패")
    void registerToQueue_ValidationFail() throws Exception {
        // given - concertId가 null인 요청
        QueueRequest request = new QueueRequest(null);

        // when & then
        mockMvc.perform(post("/api/queue")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}