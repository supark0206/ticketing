package com.concertmania.ticketing.queue.service;


import com.concertmania.ticketing.concert.entity.Concert;
import com.concertmania.ticketing.concert.repository.ConcertRepository;
import com.concertmania.ticketing.queue.dto.QueueRegisterResponse;
import com.concertmania.ticketing.queue.dto.QueueStatusResponse;
import com.concertmania.ticketing.seat.entity.Seat;
import com.concertmania.ticketing.seat.repository.SeatRepository;
import com.concertmania.ticketing.user.entity.User;
import com.concertmania.ticketing.utils.exception.CustomException;
import com.concertmania.ticketing.utils.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {


    private final RedisTemplate<String, String> redisTemplate;
    private final ConcertRepository concertRepository;
    private final SeatRepository seatRepository;
    private static final String QUEUE_KEY = "concert:queue:";
    private static final String USER_POSITION_KEY = "concert:user:position:";

    /**
     * 대기열에 사용자 등록
     */
    public QueueRegisterResponse registerToQueue(Long concertId, User user) {
        String queueKey = QUEUE_KEY + concertId;
        String userPositionKey = USER_POSITION_KEY + concertId + ":" + user.getId();

        // 콘서트 검증 및 조회
        concertRepository.findByIdAndDeletedAtIsNull(concertId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND));

        // 해당 콘서트의 예약 가능한 좌석 목록 조회
        List<Seat> seats = seatRepository.findByAvailableSeat(concertId);

        // 레디스에서 예약 가능한 좌석 목록중 선점된 좌석 확인
        boolean hasAvailableSeats = false;
        if (!seats.isEmpty()) {
            for (Seat seat : seats) {
                String seatLockKey = "seat_lock:" + seat.getId();
                // 선점되지 않은 좌석이 있는지 확인
                if (!redisTemplate.hasKey(seatLockKey)) {
                    hasAvailableSeats = true;
                    break;
                }
            }
        }

        // 예약 가능한 좌석이 없거나 모든 좌석이 선점된 경우에만 대기열에 등록
        if (seats.isEmpty() || !hasAvailableSeats) {
            // 이미 대기열에 있는지 확인
            if (redisTemplate.hasKey(userPositionKey)) {
                Long position = getCurrentPosition(concertId, user.getId());
                return QueueRegisterResponse.builder()
                        .concertId(concertId)
                        .userId(user.getId())
                        .position(position)
                        .isRegister(false)
                        .message("이미 대기열에 등록되어 있습니다. 현재 순번: " + position)
                        .build();
            }

            // 현재 시간을 점수로 하여 대기열에 추가 (FIFO 보장)
            double score = System.currentTimeMillis();
            redisTemplate.opsForZSet().add(queueKey, user.getId().toString(), score);

            // 사용자별 위치 정보 저장 (빠른 조회를 위해)
            redisTemplate.opsForValue().set(userPositionKey, "registered", Duration.ofHours(24));

            // 현재 순번 조회
            Long position = getCurrentPosition(concertId, user.getId());

            log.info("사용자 {}가 콘서트 {} 대기열에 등록됨. 순번: {}", user.getId(), concertId, position);

            return QueueRegisterResponse.builder()
                    .concertId(concertId)
                    .userId(user.getId())
                    .position(position)
                    .isRegister(true)
                    .message("대기열에 등록되었습니다. 현재 순번: " + position)
                    .build();
        } else {
            // 선점되지 않은 좌석이 있으면 대기열에 등록하지 않음
            return QueueRegisterResponse.builder()
                    .concertId(concertId)
                    .userId(user.getId())
                    .position(0L)
                    .isRegister(false)
                    .message("예약 가능한 좌석이 있습니다. 좌석을 선택해주세요.")
                    .build();
        }
    }

    /**
     * 현재 대기 순번 조회
     */
    public Long getCurrentPosition(Long concertId, Long userId) {
        String queueKey = QUEUE_KEY + concertId;
        Long rank = redisTemplate.opsForZSet().rank(queueKey, userId.toString());
        return rank != null ? rank + 1 : 0; // rank는 0부터 시작하므로 +1
    }

    /**
     * 대기열 전체 크기 조회
     */
    public Long getQueueSize(Long concertId) {
        String queueKey = QUEUE_KEY + concertId;
        return redisTemplate.opsForZSet().zCard(queueKey);
    }

    /**
     * 대기열에서 사용자 제거 (예매 완료 시)
     */
    public void removeFromQueue(Long concertId, Long userId) {
        String queueKey = QUEUE_KEY + concertId;
        String userPositionKey = USER_POSITION_KEY + concertId + ":" + userId;

        redisTemplate.opsForZSet().remove(queueKey, userId.toString());
        redisTemplate.delete(userPositionKey);

        log.info("사용자 {}가 콘서트 {} 대기열에서 제거됨", userId, concertId);
    }


    public QueueStatusResponse getQueueStatus(Long concertId, Long userId) {
        // 예약 가능한 좌석이 있고 선점되지 않은 좌석이 있는지 확인
        List<Seat> seats = seatRepository.findByAvailableSeat(concertId);
        boolean hasAvailableSeats = false;
        
        if (!seats.isEmpty()) {
            for (Seat seat : seats) {
                String seatLockKey = "seat_lock:" + seat.getId();
                // 선점되지 않은 좌석이 있는지 확인
                if (!redisTemplate.hasKey(seatLockKey)) {
                    hasAvailableSeats = true;
                    break;
                }
            }
        }
        
        // 예약 가능하고 선점되지 않은 좌석이 있으면 대기열에서 한명씩 빼기
        if (!seats.isEmpty() && hasAvailableSeats) {
            processQueue(concertId);
        }

        Long position = getCurrentPosition(concertId, userId);
        Long totalQueue = getQueueSize(concertId);

        // 입장 가능 여부 판단: 예약 가능한 좌석이 있고 선점되지 않은 좌석이 있으면 true
        boolean canEnter = !seats.isEmpty() && hasAvailableSeats;

        return QueueStatusResponse.builder()
                .concertId(concertId)
                .userId(userId)
                .position(position.intValue())
                .totalQueue(totalQueue.intValue())
                .estimatedWaitTime(position.intValue() * 30) // 인원수 * 30초 로 임시 지정
                .canEnter(canEnter)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 대기열에서 한명씩 처리 (예약 가능한 좌석이 있을 때)
     */
    private void processQueue(Long concertId) {
        String queueKey = QUEUE_KEY + concertId;
        
        // 대기열 첫 번째 사용자 조회
        Set<String> firstUser = redisTemplate.opsForZSet().range(queueKey, 0, 0);
        
        if (firstUser != null && !firstUser.isEmpty()) {
            String userId = firstUser.iterator().next();
            String userPositionKey = USER_POSITION_KEY + concertId + ":" + userId;
            
            // 대기열에서 제거
            redisTemplate.opsForZSet().remove(queueKey, userId);
            redisTemplate.delete(userPositionKey);
            
            log.info("예약 가능한 좌석이 있어 사용자 {}를 콘서트 {} 대기열에서 제거함", userId, concertId);
        }
    }

}
