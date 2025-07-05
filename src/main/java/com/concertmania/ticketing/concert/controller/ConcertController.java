package com.concertmania.ticketing.concert.controller;

import com.concertmania.ticketing.concert.dto.ConcertCreateRequest;
import com.concertmania.ticketing.concert.dto.ConcertResponse;
import com.concertmania.ticketing.concert.dto.ConcertUpdateRequest;
import com.concertmania.ticketing.concert.service.ConcertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/concert")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    // 콘서트 생성
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConcertResponse> createConcert(@Valid @RequestBody ConcertCreateRequest request) {
        ConcertResponse response = concertService.createConcert(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 전체 콘서트 조회
    @GetMapping
    public ResponseEntity<Page<ConcertResponse>> getAllConcerts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String venue,
            Pageable pageable) {
        Page<ConcertResponse> concerts = concertService.getAllConcerts(title, venue, pageable);
        return ResponseEntity.ok(concerts);
    }

    // 콘서트 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ConcertResponse> getConcert(@PathVariable Long id) {
        ConcertResponse concert = concertService.getConcert(id);
        return ResponseEntity.ok(concert);
    }

    // 콘서트 수정
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConcertResponse> updateConcert(@PathVariable Long id, @Valid @RequestBody ConcertUpdateRequest request) {
        ConcertResponse response = concertService.updateConcert(id, request);
        return ResponseEntity.ok(response);
    }

    // 콘서트 삭제
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteConcert(@PathVariable Long id) {
        concertService.deleteConcert(id);
        return ResponseEntity.noContent().build();
    }
}