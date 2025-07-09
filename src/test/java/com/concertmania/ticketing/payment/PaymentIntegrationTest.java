package com.concertmania.ticketing.payment;

import com.concertmania.ticketing.concert.entity.Concert;
import com.concertmania.ticketing.concert.enums.ConcertStatus;
import com.concertmania.ticketing.concert.repository.ConcertRepository;
import com.concertmania.ticketing.config.security.JwtTokenProvider;
import com.concertmania.ticketing.payment.dto.ConfirmPaymentRequest;
import com.concertmania.ticketing.payment.dto.PaymentRequest;
import com.concertmania.ticketing.payment.entity.Payment;
import com.concertmania.ticketing.payment.enums.PaymentMethod;
import com.concertmania.ticketing.payment.enums.PaymentStatus;
import com.concertmania.ticketing.payment.repository.PaymentRepository;
import com.concertmania.ticketing.seat.entity.Seat;
import com.concertmania.ticketing.seat.repository.SeatRepository;
import com.concertmania.ticketing.user.entity.User;
import com.concertmania.ticketing.user.enums.UserRole;
import com.concertmania.ticketing.user.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
@ActiveProfiles("test")
public class PaymentIntegrationTest {

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
    private PaymentRepository paymentRepository;

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

    private User normalUser;
    private String userToken;
    private Concert testConcert;
    private Seat testSeat;

    @BeforeEach
    void setUp() {
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
                .concertDate(LocalDateTime.of(2024, 12, 25, 19, 0))
                .openTime(LocalDateTime.of(2024, 12, 1, 10, 0))
                .closeTime(LocalDateTime.of(2024, 12, 24, 23, 59))
                .status(ConcertStatus.SCHEDULED)
                .build();
        testConcert = concertRepository.save(testConcert);

        // 테스트용 좌석 생성
        testSeat = Seat.builder()
                .concert(testConcert)
                .section("A")
                .row("1")
                .number("1")
                .grade("VIP")
                .price(new BigDecimal("150000"))
                .build();
        testSeat = seatRepository.save(testSeat);
    }

    @Test
    @DisplayName("결제 시작")
    void startPayment_Success() throws Exception {
        // given - 먼저 좌석 선택 API를 호출하여 Redis 락 생성
        mockMvc.perform(post("/api/seat/" + testSeat.getId() + "/select")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        PaymentRequest request = new PaymentRequest(
                testSeat.getId(),
                PaymentMethod.CREDIT_CARD,
                new BigDecimal("150000")
        );

        // when & then
        mockMvc.perform(post("/api/payment")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").exists())
                .andExpect(jsonPath("$.amount").value(150000))
                .andExpect(jsonPath("$.pgRequestSuccess").value(true))
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("결제 확인 - 성공")
    void confirmPayment_Success() throws Exception {
        // given - 먼저 좌석 선택 API를 호출하여 Redis 락 생성
        mockMvc.perform(post("/api/seat/" + testSeat.getId() + "/select")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // 결제 시작 API를 통해 실제 결제 데이터 생성
        PaymentRequest paymentRequest = new PaymentRequest(
                testSeat.getId(),
                PaymentMethod.CREDIT_CARD,
                new BigDecimal("150000")
        );

        // 결제 시작하여 거래 ID 획득
        String response = mockMvc.perform(post("/api/payment")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // JSON 응답에서 transactionId 추출
        String transactionId = objectMapper.readTree(response).get("transactionId").asText();

        // 결제 확인 요청 생성
        ConfirmPaymentRequest confirmRequest = new ConfirmPaymentRequest(transactionId);

        // when & then
        mockMvc.perform(post("/api/payment/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(jsonPath("$.success").isBoolean());
    }


    @Test
    @DisplayName("결제 시작 - 이미 예약된 좌석")
    void startPayment_AlreadyReservedSeat() throws Exception {
        // given - 먼저 좌석 선택 API를 호출하여 Redis 락 생성
        mockMvc.perform(post("/api/seat/" + testSeat.getId() + "/select")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // 첫 번째 결제 시작
        PaymentRequest firstRequest = new PaymentRequest(
                testSeat.getId(),
                PaymentMethod.CREDIT_CARD,
                new BigDecimal("150000")
        );

        String firstResponse = mockMvc.perform(post("/api/payment")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 첫 번째 결제 확인으로 예약 완료 처리
        String firstTransactionId = objectMapper.readTree(firstResponse).get("transactionId").asText();
        ConfirmPaymentRequest confirmRequest = new ConfirmPaymentRequest(firstTransactionId);

        // Mock을 성공으로 고정하기 위해 여러 번 시도 (랜덤이므로)
        mockMvc.perform(post("/api/payment/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmRequest)));

        // 두 번째 좌석 선택 시도 (같은 좌석에 대해)
        mockMvc.perform(post("/api/seat/" + testSeat.getId() + "/select")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isConflict()); // 이미 예약된 좌석이므로 선택 자체가 실패해야 함
    }
}