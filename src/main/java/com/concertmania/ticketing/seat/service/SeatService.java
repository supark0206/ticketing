package com.concertmania.ticketing.seat.service;

import com.concertmania.ticketing.concert.entity.Concert;
import com.concertmania.ticketing.concert.repository.ConcertRepository;
import com.concertmania.ticketing.seat.dto.SeatCreateRequest;
import com.concertmania.ticketing.seat.dto.SeatResponse;
import com.concertmania.ticketing.seat.dto.SeatSelectResponse;
import com.concertmania.ticketing.seat.dto.SeatUpdateRequest;
import com.concertmania.ticketing.seat.entity.Seat;
import com.concertmania.ticketing.seat.repository.SeatRepository;
import com.concertmania.ticketing.user.entity.User;
import com.concertmania.ticketing.utils.exception.CustomException;
import com.concertmania.ticketing.utils.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;
    private final ConcertRepository concertRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public List<SeatResponse> createSeats(List<SeatCreateRequest> requests) {

        // 빈 값 검증
        if (requests == null || requests.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 모든 concertId 가 동일한지 검증
        Long concertId = requests.get(0).getConcertId();
        boolean allSameConcert = requests.stream()
                .allMatch(req -> concertId.equals(req.getConcertId()));

        if (!allSameConcert) {
            throw new CustomException(ErrorCode.INVALID_CONCERT_ID);
        }

        // Concert 존재 여부 확인
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND));

        // 중복 검사
        boolean hasDuplicates = seatRepository.existsByConcertIdAndSeats(concertId, requests);
        if (hasDuplicates) {
            throw new CustomException(ErrorCode.SEAT_ALREADY_EXISTS);
        }

        // 요청 내에서 중복되는 좌석이 있는지 검사
        Set<String> seatKeys = new HashSet<>();
        for (SeatCreateRequest req : requests) {
            String seatKey = String.format("%s-%s-%s", req.getSection(), req.getRow(), req.getNumber());
            if (!seatKeys.add(seatKey)) {
                throw new CustomException(ErrorCode.DUPLICATE_SEAT_IN_REQUEST);
            }
        }

        List<Seat> seats = requests.stream()
                .map(req -> Seat.builder()
                        .concert(concert)
                        .section(req.getSection())
                        .row(req.getRow())
                        .number(req.getNumber())
                        .grade(req.getGrade())
                        .price(req.getPrice())
                        .build())
                .collect(Collectors.toList());
        List<Seat> savedSeats = seatRepository.saveAll(seats);

        // 벌크 쿼리로 예약 상태 조회
        List<Long> seatIds = savedSeats.stream()
                .map(Seat::getId)
                .collect(Collectors.toList());
        Map<Long, Boolean> reservationStatusMap = seatRepository.getActiveReservationStatusBySeatIds(seatIds);

        return savedSeats.stream()
                .map(seat -> SeatResponse.from(seat, reservationStatusMap.getOrDefault(seat.getId(), false)))
                .collect(Collectors.toList());
    }

    public Page<SeatResponse> getSeatsByConcert(Long concertId, Pageable pageable) {
        if (concertRepository.findByIdAndDeletedAtIsNull(concertId).isEmpty()) {
            throw new CustomException(ErrorCode.CONCERT_NOT_FOUND);
        }

        Page<Seat> seats = seatRepository.findByConcertIdNotDeletedPage(concertId, pageable);
        
        // 벌크 쿼리로 예약 상태 조회
        List<Long> seatIds = seats.getContent().stream()
                .map(Seat::getId)
                .collect(Collectors.toList());
        Map<Long, Boolean> reservationStatusMap = seatRepository.getActiveReservationStatusBySeatIds(seatIds);
        
        return seats.map(seat -> SeatResponse.from(seat, reservationStatusMap.getOrDefault(seat.getId(), false)));
    }

    public List<SeatResponse> getSeatSeatMapByConcert(Long concertId) {
        if (concertRepository.findByIdAndDeletedAtIsNull(concertId).isEmpty()) {
            throw new CustomException(ErrorCode.CONCERT_NOT_FOUND);
        }

        List<Seat> seats = seatRepository.findByConcertIdNotDeleted(concertId);
        
        // 벌크 쿼리로 예약 상태 조회
        List<Long> seatIds = seats.stream()
                .map(Seat::getId)
                .collect(Collectors.toList());
        Map<Long, Boolean> reservationStatusMap = seatRepository.getActiveReservationStatusBySeatIds(seatIds);
        
        return seats.stream()
                .map(seat -> SeatResponse.from(seat, reservationStatusMap.getOrDefault(seat.getId(), false)))
                .collect(Collectors.toList());
    }

    public List<SeatResponse> getSeatsByGrade(Long concertId, String grade) {
        if (concertRepository.findByIdAndDeletedAtIsNull(concertId).isEmpty()) {
            throw new CustomException(ErrorCode.CONCERT_NOT_FOUND);
        }

        List<Seat> seats = seatRepository.findByConcertIdAndGradeNotDeleted(concertId, grade);
        
        // 벌크 쿼리로 예약 상태 조회
        List<Long> seatIds = seats.stream()
                .map(Seat::getId)
                .collect(Collectors.toList());
        Map<Long, Boolean> reservationStatusMap = seatRepository.getActiveReservationStatusBySeatIds(seatIds);
        
        return seats.stream()
                .map(seat -> SeatResponse.from(seat, reservationStatusMap.getOrDefault(seat.getId(), false)))
                .collect(Collectors.toList());
    }

    public SeatResponse getSeat(Long id) {
        Seat seat = seatRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

        return SeatResponse.from(seat, seatRepository.hasActiveReservations(seat.getId()));
    }

    @Transactional
    public SeatSelectResponse selectSeat(Long seatId, User user) {
        Seat seat = seatRepository.findByIdAndDeletedAtIsNull(seatId)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

        if (seatRepository.hasActiveReservations(seatId)) {
            throw new CustomException(ErrorCode.IS_ALREADY_RESERVATION);
        }

        String redisKey = "seat_lock:" + seatId;
        String userId = String.valueOf(user.getId());
        LocalDateTime expiresAt;

        // 원자적 락
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, userId, Duration.ofMinutes(10));

        if (Boolean.TRUE.equals(locked)) {
            // 락 획득 성공
            expiresAt = LocalDateTime.now()
                    .plusMinutes(10)
                    .truncatedTo(ChronoUnit.SECONDS);
        } else {
            // 락 획득 실패 - 기존 점유자 확인
            String existingLockUserId = redisTemplate.opsForValue().get(redisKey);

            if (existingLockUserId == null) {
                throw new CustomException(ErrorCode.SEAT_LOCK_RETRY_NEEDED);
            }
            if (!existingLockUserId.equals(userId)) {
                throw new CustomException(ErrorCode.SEAT_ALREADY_LOCKED);
            }

            // 같은 사용자 - TTL 계산
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            if (ttl == null || ttl <= 0) {
                // TTL이 만료된 경우
                throw new CustomException(ErrorCode.SEAT_LOCK_EXPIRED);
            }

            expiresAt = LocalDateTime.now()
                    .plusSeconds(ttl)
                    .truncatedTo(ChronoUnit.SECONDS);
        }

        return SeatSelectResponse.from(seat, expiresAt, seatRepository.hasActiveReservations(seat.getId()));
    }

    @Transactional
    public SeatResponse updateSeat(Long id, SeatUpdateRequest request) {
        Seat seat = seatRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

        Seat updatedSeat = seat.update(
                request.getSection(),
                request.getRow(),
                request.getNumber(),
                request.getGrade(),
                request.getPrice()
        );

        return SeatResponse.from(updatedSeat, seatRepository.hasActiveReservations(seat.getId()));
    }

    @Transactional
    public void deleteSeat(Long id) {
        Seat seat = seatRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

        if (seatRepository.hasActiveReservations(id)) {
            throw new CustomException(ErrorCode.CANNOT_DELETE_RESERVED_SEAT);
        }

        seat.delete();
    }
}