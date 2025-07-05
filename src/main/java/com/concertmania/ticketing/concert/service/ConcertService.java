package com.concertmania.ticketing.concert.service;

import com.concertmania.ticketing.concert.dto.ConcertCreateRequest;
import com.concertmania.ticketing.concert.dto.ConcertResponse;
import com.concertmania.ticketing.concert.dto.ConcertUpdateRequest;
import com.concertmania.ticketing.concert.entity.Concert;
import com.concertmania.ticketing.concert.enums.ConcertStatus;
import com.concertmania.ticketing.concert.repository.ConcertRepository;
import com.concertmania.ticketing.utils.exception.CustomException;
import com.concertmania.ticketing.utils.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcertService {

    private final ConcertRepository concertRepository;

    @Transactional
    public ConcertResponse createConcert(ConcertCreateRequest request) {

        // 생성 일자 검증
        validateCreateDate(request);

        Concert concert = Concert.builder()
                .title(request.getTitle())
                .concertDate(request.getConcertDate())
                .venue(request.getVenue())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .status(ConcertStatus.SCHEDULED)
                .build();

        Concert savedConcert = concertRepository.save(concert);
        return ConcertResponse.from(savedConcert);
    }

    private static void validateCreateDate(ConcertCreateRequest request) {
        if (!request.getConcertDate().isAfter(request.getCloseTime())) {
            throw new CustomException(ErrorCode.CONCERT_DATE_AFTER_CLOSE_TIME);
        }
        if (!request.getCloseTime().isAfter(request.getOpenTime())) {
            throw new CustomException(ErrorCode.CLOSE_TIME_AFTER_OPEN_TIME);
        }
    }

    public Page<ConcertResponse> getAllConcerts(String title, String venue, Pageable pageable) {
        Page<Concert> concerts = concertRepository.findAllNotDeletedWithSearch(title, venue, pageable);
        return concerts.map(ConcertResponse::from);
    }

    public ConcertResponse getConcert(Long id) {
        Concert concert = concertRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND));
        return ConcertResponse.from(concert);
    }

    @Transactional
    public ConcertResponse updateConcert(Long id, ConcertUpdateRequest request) {
        Concert concert = concertRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND));

        Concert updatedConcert = concert.update(
                request.getTitle(),
                request.getConcertDate(),
                request.getVenue(),
                request.getOpenTime(),
                request.getCloseTime()
        );

        return ConcertResponse.from(updatedConcert);
    }

    @Transactional
    public void deleteConcert(Long id) {
        Concert concert = concertRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND));

        // TODO 예약자가 있으면 삭제 불가 처리
        
        concert.delete();
    }

}