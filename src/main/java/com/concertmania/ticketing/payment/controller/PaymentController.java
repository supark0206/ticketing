package com.concertmania.ticketing.payment.controller;

import com.concertmania.ticketing.payment.docs.PaymentControllerDocs;
import com.concertmania.ticketing.payment.dto.ConfirmPaymentRequest;
import com.concertmania.ticketing.payment.dto.ConfirmPaymentResponse;
import com.concertmania.ticketing.payment.dto.PaymentRequest;
import com.concertmania.ticketing.payment.dto.PaymentResponse;
import com.concertmania.ticketing.payment.service.PaymentService;
import com.concertmania.ticketing.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController implements PaymentControllerDocs {

    private final PaymentService paymentService;


    @PostMapping()
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentResponse> startPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal User user) {
        PaymentResponse response = paymentService.startPayment(request, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<ConfirmPaymentResponse> confirmPayment(@Valid @RequestBody ConfirmPaymentRequest request) {

        // PG 결제 모킹 → 임의로 성공 or 실패 랜덤하게 만듦
        boolean paymentSuccess = mockPaymentProcess();

        // 결제 완료 처리
        ConfirmPaymentResponse confirmPaymentResponse = paymentService.confirmPayment(request.getTransactionId(), paymentSuccess);

        return ResponseEntity.ok(confirmPaymentResponse);
    }

    private boolean mockPaymentProcess() {
        // 50% 성공률
        return Math.random() < 0.5;
    }

}