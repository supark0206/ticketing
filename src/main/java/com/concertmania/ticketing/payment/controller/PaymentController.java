package com.concertmania.ticketing.payment.controller;

import com.concertmania.ticketing.payment.dto.PaymentRequest;
import com.concertmania.ticketing.payment.dto.PaymentResponse;
import com.concertmania.ticketing.payment.service.PaymentService;
import com.concertmania.ticketing.user.dto.UserResponse;
import com.concertmania.ticketing.user.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Payment", description = "결제 관련 API")
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    @PostMapping()
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentResponse> payment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal User user) {
        PaymentResponse response = paymentService.paymentProcess(request, user);
        return ResponseEntity.ok(response);
    }

}