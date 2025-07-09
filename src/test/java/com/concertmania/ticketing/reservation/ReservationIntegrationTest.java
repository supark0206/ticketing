package com.concertmania.ticketing.reservation;

import com.concertmania.ticketing.concert.entity.Concert;
import com.concertmania.ticketing.concert.enums.ConcertStatus;
import com.concertmania.ticketing.concert.repository.ConcertRepository;
import com.concertmania.ticketing.config.security.JwtTokenProvider;
import com.concertmania.ticketing.payment.entity.Payment;
import com.concertmania.ticketing.payment.enums.PaymentMethod;
import com.concertmania.ticketing.payment.enums.PaymentStatus;
import com.concertmania.ticketing.payment.repository.PaymentRepository;
import com.concertmania.ticketing.reservation.dto.CancelReservationRequest;
import com.concertmania.ticketing.reservation.entity.Reservation;
import com.concertmania.ticketing.reservation.entity.ReservationSeat;
import com.concertmania.ticketing.reservation.enums.ReservationStatus;
import com.concertmania.ticketing.reservation.repository.ReservationRepository;
import com.concertmania.ticketing.reservation.repository.ReservationSeatRepository;
import com.concertmania.ticketing.seat.entity.Seat;
import com.concertmania.ticketing.seat.repository.SeatRepository;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
@ActiveProfiles("test")
public class ReservationIntegrationTest {

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
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationSeatRepository reservationSeatRepository;

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
    private Reservation testReservation;

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

        // 테스트용 콘서트 생성 (미래 날짜로 설정)
        testConcert = Concert.builder()
                .title("아이유 콘서트 2024")
                .venue("올림픽공원 체조경기장")
                .concertDate(LocalDateTime.now().plusDays(30)) // 30일 후
                .openTime(LocalDateTime.now().plusDays(1)) // 1일 후
                .closeTime(LocalDateTime.now().plusDays(29)) // 29일 후
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

        // 테스트용 예약 생성
        testReservation = Reservation.builder()
                .user(normalUser)
                .concert(testConcert)
                .status(ReservationStatus.CONFIRMED)
                .build();
        testReservation = reservationRepository.save(testReservation);

        // 테스트용 예약 좌석 생성
        ReservationSeat reservationSeat = ReservationSeat.builder()
                .reservation(testReservation)
                .seat(testSeat)
                .build();
        reservationSeatRepository.save(reservationSeat);

        // 테스트용 결제 생성
        Payment payment = Payment.builder()
                .reservation(testReservation)
                .status(PaymentStatus.SUCCESS)
                .method(PaymentMethod.CREDIT_CARD)
                .amount(new BigDecimal("150000"))
                .transactionId("TXN_" + System.currentTimeMillis())
                .build();
        paymentRepository.save(payment);
    }

    @Test
    @DisplayName("내 예약 조회 - 성공")
    void getMyReservations_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/reservations/my")
                        .header("Authorization", "Bearer " + userToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].reservationId").value(testReservation.getId()))
                .andExpect(jsonPath("$.content[0].userEmail").value(normalUser.getEmail()))
                .andExpect(jsonPath("$.content[0].concertTitle").value(testConcert.getTitle()))
                .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$.content[0].seats").isArray())
                .andExpect(jsonPath("$.content[0].seats[0].section").value("A"))
                .andExpect(jsonPath("$.content[0].seats[0].row").value("1"))
                .andExpect(jsonPath("$.content[0].seats[0].number").value("1"))
                .andExpect(jsonPath("$.content[0].totalAmount").value(150000))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @DisplayName("예약 취소 - 성공")
    void cancelReservation_Success() throws Exception {
        // given
        CancelReservationRequest request = new CancelReservationRequest(
                testReservation.getId(),
                "개인 사정으로 인한 취소"
        );

        // when & then
        mockMvc.perform(post("/api/reservations/cancel")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(testReservation.getId()))
                .andExpect(jsonPath("$.status").value("CANCELED"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.refundAmount").value(150000))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("예약 취소 - 존재하지 않는 예약")
    void cancelReservation_NotFound() throws Exception {
        // given
        CancelReservationRequest request = new CancelReservationRequest(
                99999L,
                "개인 사정으로 인한 취소"
        );

        // when & then
        mockMvc.perform(post("/api/reservations/cancel")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("내 예약 조회 - 빈 목록")
    void getMyReservations_EmptyList() throws Exception {
        // given - 다른 사용자의 토큰 생성
        User otherUser = User.builder()
                .username("other")
                .email("other@test.com")
                .password(passwordEncoder.encode("password"))
                .role(UserRole.USER)
                .build();
        otherUser = userRepository.save(otherUser);

        String otherToken = jwtTokenProvider.createToken(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        otherUser, null, otherUser.getAuthorities()
                )
        );

        // when & then
        mockMvc.perform(get("/api/reservations/my")
                        .header("Authorization", "Bearer " + otherToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }
}