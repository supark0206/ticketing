package com.concertmania.ticketing.seat.controller;

import com.concertmania.ticketing.seat.dto.SeatCreateRequest;
import com.concertmania.ticketing.seat.dto.SeatResponse;
import com.concertmania.ticketing.seat.dto.SeatUpdateRequest;
import com.concertmania.ticketing.seat.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/seat")
@RequiredArgsConstructor
public class SeatController implements SeatControllerDocs {

    private final SeatService seatService;

    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SeatResponse>> createSeats(@Valid @RequestBody List<SeatCreateRequest> requests) {
        List<SeatResponse> responses = seatService.createSeats(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping("/concert/{concertId}")
    public ResponseEntity<Page<SeatResponse>> getSeatsByConcert(@PathVariable Long concertId, Pageable pageable) {
        Page<SeatResponse> seats = seatService.getSeatsByConcert(concertId, pageable);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/concert/{concertId}/seat-map")
    public ResponseEntity<List<SeatResponse>> getSeatSeatMapByConcert(@PathVariable Long concertId) {
        List<SeatResponse> seats = seatService.getSeatSeatMapByConcert(concertId);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/concert/{concertId}/seats")
    public ResponseEntity<List<SeatResponse>> getSeatsByGrade(@PathVariable Long concertId,
                                                              @RequestParam String grade) {
        List<SeatResponse> seats = seatService.getSeatsByGrade(concertId, grade);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatResponse> getSeat(@PathVariable Long id) {
        SeatResponse seat = seatService.getSeat(id);
        return ResponseEntity.ok(seat);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeatResponse> updateSeat(@PathVariable Long id, @Valid @RequestBody SeatUpdateRequest request) {
        SeatResponse response = seatService.updateSeat(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSeat(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return ResponseEntity.noContent().build();
    }
}