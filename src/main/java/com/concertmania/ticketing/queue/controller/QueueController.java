package com.concertmania.ticketing.queue.controller;


import com.concertmania.ticketing.queue.dto.QueueRegisterResponse;
import com.concertmania.ticketing.queue.dto.QueueStatusResponse;
import com.concertmania.ticketing.user.entity.User;
import com.concertmania.ticketing.queue.dto.QueueRequest;
import com.concertmania.ticketing.queue.service.QueueService;
import com.concertmania.ticketing.utils.exception.CustomException;
import com.concertmania.ticketing.utils.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
@Slf4j
public class QueueController {

    private final QueueService queueService;

    @PostMapping("")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<QueueRegisterResponse> registerToQueue(@Valid @RequestBody QueueRequest queueRequest,
                                                                 @AuthenticationPrincipal User user) {
        try {
            QueueRegisterResponse result = queueService.registerToQueue(queueRequest.getConcertId(), user);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("[대기열] 등록 실패", e);
            throw new CustomException(ErrorCode.FAIL_REGISTER_QUEUE);
        }
    }

    // SSE 엔드포인트 - 실시간 순번 스트리밍
    @GetMapping(value = "/{concertId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('USER')")
    public SseEmitter streamQueueStatus(@PathVariable Long concertId,
                                        @AuthenticationPrincipal User user) {
        SseEmitter emitter = new SseEmitter(0L); // 무제한 타임아웃

        // 비동기로 데이터 전송
        CompletableFuture.runAsync(() -> {
            try {
                while (true) {
                    QueueStatusResponse response = queueService.getQueueStatus(concertId, user.getId());

                    if (response.getPosition() > 0) {
                        emitter.send(SseEmitter.event()
                                .name("queue-update")
                                .data(response));
                    } else {
                        // 대기열에 없으면 연결 종료
                        emitter.send(SseEmitter.event()
                                .name("queue-complete")
                                .data(response));
                        emitter.complete();
                        break;
                    }

                    Thread.sleep(3000); // 3초마다 업데이트
                }
            } catch (Exception e) {
                log.error("SSE 스트리밍 오류", e);
                emitter.completeWithError(e);
            }
        });

        emitter.onCompletion(() -> log.info("SSE 연결 완료: 콘서트 {}, 사용자 {}", concertId, user.getId()));
        emitter.onTimeout(() -> log.info("SSE 연결 타임아웃: 콘서트 {}, 사용자 {}", concertId, user.getId()));

        return emitter;
    }
}
