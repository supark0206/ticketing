package com.concertmania.ticketing.reservation.controller;

import com.concertmania.ticketing.reservation.docs.ReservationControllerDocs;
import com.concertmania.ticketing.reservation.dto.CancelReservationRequest;
import com.concertmania.ticketing.reservation.dto.CancelReservationResponse;
import com.concertmania.ticketing.reservation.dto.ReservationResponse;
import com.concertmania.ticketing.reservation.service.ReservationService;
import com.concertmania.ticketing.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController implements ReservationControllerDocs {

    private final ReservationService reservationService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<ReservationResponse>> getMyReservations(
            @AuthenticationPrincipal User user, Pageable pageable) {
        
        Page<ReservationResponse> reservations = reservationService.getUserReservations(user, pageable);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReservationResponse>> getAllReservations(
            @AuthenticationPrincipal User user, Pageable pageable) {
        
        Page<ReservationResponse> reservations = reservationService.getAllReservations(user, pageable);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{reservationId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReservationResponse> getReservationDetail(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal User user) {
        
        ReservationResponse reservation = reservationService.getReservationDetail(reservationId, user);
        return ResponseEntity.ok(reservation);
    }

    @PostMapping("/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CancelReservationResponse> cancelReservation(
            @Valid @RequestBody CancelReservationRequest request,
            @AuthenticationPrincipal User user) {
        
        CancelReservationResponse response = reservationService.cancelReservation(request, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/concert/{concertId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReservationResponse>> getReservationsByConcert(
            @PathVariable Long concertId,
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        
        Page<ReservationResponse> reservations = reservationService.getReservationsByConcert(concertId, user, pageable);
        return ResponseEntity.ok(reservations);
    }
}