package com.concertmania.ticketing.seat;

import com.concertmania.ticketing.concert.entity.Concert;
import com.concertmania.ticketing.concert.enums.ConcertStatus;
import com.concertmania.ticketing.concert.repository.ConcertRepository;
import com.concertmania.ticketing.config.security.JwtTokenProvider;
import com.concertmania.ticketing.seat.dto.SeatCreateRequest;
import com.concertmania.ticketing.seat.dto.SeatResponse;
import com.concertmania.ticketing.seat.dto.SeatSelectResponse;
import com.concertmania.ticketing.seat.entity.Seat;
import com.concertmania.ticketing.seat.repository.SeatRepository;
import com.concertmania.ticketing.user.entity.User;
import com.concertmania.ticketing.user.enums.UserRole;
import com.concertmania.ticketing.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
@ActiveProfiles("test")
public class SeatIntegrationTest {

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
    private SeatRepository seatRepository;

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
    private Concert testConcert;

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

        // 테스트용 콘서트 생성
        testConcert = Concert.builder()
                .title("아이유 콘서트 2024")
                .venue("올림픽공원 체조경기장")
                .concertDate(LocalDateTime.of(2024, 12, 25, 19, 0))
                .openTime(LocalDateTime.of(2024, 12, 1, 10, 0))
                .closeTime(LocalDateTime.of(2024, 12, 24, 23, 59))
                .status(ConcertStatus.SCHEDULED)
                .build();
        testConcert = concertRepository.save(testConcert);
    }

    @Test
    @DisplayName("좌석 일괄 생성")
    void createSeats_Success() throws Exception {
        // given
        List<SeatCreateRequest> requests = List.of(
                new SeatCreateRequest(testConcert.getId(), "A", "1", "1", "VIP", new BigDecimal("150000")),
                new SeatCreateRequest(testConcert.getId(), "A", "1", "2", "VIP", new BigDecimal("150000")),
                new SeatCreateRequest(testConcert.getId(), "B", "1", "1", "R", new BigDecimal("100000"))
        );

        // when & then
        mockMvc.perform(post("/api/seat/batch")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].section").value("A"))
                .andExpect(jsonPath("$[0].row").value("1"))
                .andExpect(jsonPath("$[0].number").value("1"))
                .andExpect(jsonPath("$[0].grade").value("VIP"))
                .andExpect(jsonPath("$[0].price").value(150000))
                .andExpect(jsonPath("$[1].section").value("A"))
                .andExpect(jsonPath("$[1].row").value("1"))
                .andExpect(jsonPath("$[1].number").value("2"))
                .andExpect(jsonPath("$[2].section").value("B"))
                .andExpect(jsonPath("$[2].grade").value("R"))
                .andExpect(jsonPath("$[2].price").value(100000));
    }

    @Test
    @DisplayName("콘서트별 좌석 맵 조회")
    void getSeatSeatMapByConcert_Success() throws Exception {
        // given
        Seat seat1 = Seat.builder()
                .concert(testConcert)
                .section("A")
                .row("1")
                .number("1")
                .grade("VIP")
                .price(new BigDecimal("150000"))
                .build();

        Seat seat2 = Seat.builder()
                .concert(testConcert)
                .section("A")
                .row("1")
                .number("2")
                .grade("VIP")
                .price(new BigDecimal("150000"))
                .build();

        seatRepository.saveAll(List.of(seat1, seat2));

        // when & then
        mockMvc.perform(get("/api/seat/concert/" + testConcert.getId() + "/seat-map")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].section").value("A"))
                .andExpect(jsonPath("$[0].row").value("1"))
                .andExpect(jsonPath("$[0].number").value("1"))
                .andExpect(jsonPath("$[0].grade").value("VIP"))
                .andExpect(jsonPath("$[0].price").value(150000))
                .andExpect(jsonPath("$[0].concertTitle").value("아이유 콘서트 2024"))
                .andExpect(jsonPath("$[1].section").value("A"))
                .andExpect(jsonPath("$[1].row").value("1"))
                .andExpect(jsonPath("$[1].number").value("2"))
                .andExpect(jsonPath("$[1].grade").value("VIP"))
                .andExpect(jsonPath("$[1].price").value(150000))
                .andExpect(jsonPath("$[1].concertTitle").value("아이유 콘서트 2024"));
    }

    @Test
    @DisplayName("좌석 선택")
    void selectSeat_Success() throws Exception {
        // given
        Seat seat = Seat.builder()
                .concert(testConcert)
                .section("A")
                .row("1")
                .number("1")
                .grade("VIP")
                .price(new BigDecimal("150000"))
                .build();
        seat = seatRepository.save(seat);

        // when & then
        mockMvc.perform(post("/api/seat/" + seat.getId() + "/select")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(seat.getId()))
                .andExpect(jsonPath("$.section").value("A"))
                .andExpect(jsonPath("$.row").value("1"))
                .andExpect(jsonPath("$.number").value("1"))
                .andExpect(jsonPath("$.grade").value("VIP"))
                .andExpect(jsonPath("$.price").value(150000))
                .andExpect(jsonPath("$.expiresAt").exists());
    }
}