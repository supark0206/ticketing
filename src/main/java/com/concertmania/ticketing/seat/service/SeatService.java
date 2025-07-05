package com.concertmania.ticketing.seat.service;

import com.concertmania.ticketing.concert.entity.Concert;
import com.concertmania.ticketing.concert.repository.ConcertRepository;
import com.concertmania.ticketing.seat.dto.SeatCreateRequest;
import com.concertmania.ticketing.seat.dto.SeatResponse;
import com.concertmania.ticketing.seat.dto.SeatUpdateRequest;
import com.concertmania.ticketing.seat.entity.Seat;
import com.concertmania.ticketing.seat.repository.SeatRepository;
import com.concertmania.ticketing.utils.exception.CustomException;
import com.concertmania.ticketing.utils.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;
    private final ConcertRepository concertRepository;

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

        // 3. Concert 존재 여부 확인 (getReferenceById는 지연 로딩이므로 미리 검증)
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

        return savedSeats.stream()
                .map(SeatResponse::from)
                .collect(Collectors.toList());
    }

    public Page<SeatResponse> getSeatsByConcert(Long concertId, Pageable pageable) {
        if (!concertRepository.findByIdAndDeletedAtIsNull(concertId).isPresent()) {
            throw new CustomException(ErrorCode.CONCERT_NOT_FOUND);
        }

        Page<Seat> seats = seatRepository.findByConcertIdNotDeletedPage(concertId, pageable);
        return seats.map(SeatResponse::from);
    }

    public List<SeatResponse> getSeatSeatMapByConcert(Long concertId) {
        if (!concertRepository.findByIdAndDeletedAtIsNull(concertId).isPresent()) {
            throw new CustomException(ErrorCode.CONCERT_NOT_FOUND);
        }

        List<Seat> seats = seatRepository.findByConcertIdNotDeleted(concertId);
        return seats.stream()
                .map(SeatResponse::from)
                .collect(Collectors.toList());
    }

    public List<SeatResponse> getSeatsByGrade(Long concertId, String grade) {
        if (!concertRepository.findByIdAndDeletedAtIsNull(concertId).isPresent()) {
            throw new CustomException(ErrorCode.CONCERT_NOT_FOUND);
        }

        List<Seat> seats = seatRepository.findByConcertIdAndGradeNotDeleted(concertId, grade);
        return seats.stream()
                .map(SeatResponse::from)
                .collect(Collectors.toList());
    }

    public SeatResponse getSeat(Long id) {
        Seat seat = seatRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));
        return SeatResponse.from(seat);
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

        return SeatResponse.from(updatedSeat);
    }

    @Transactional
    public void deleteSeat(Long id) {
        Seat seat = seatRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

        if (!seat.getReservationSeats().isEmpty()) {
            throw new CustomException(ErrorCode.CANNOT_DELETE_RESERVED_SEAT);
        }

        seat.delete();
    }
}