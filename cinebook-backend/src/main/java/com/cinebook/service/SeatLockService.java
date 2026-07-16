package com.cinebook.service;

import com.cinebook.domain.entity.ShowSeat;
import com.cinebook.domain.enums.SeatStatus;
import com.cinebook.domain.repository.ShowSeatRepository;
import com.cinebook.dto.request.HoldSeatsRequest;
import com.cinebook.dto.response.HoldSeatsResponse;
import com.cinebook.exception.SeatsUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * THE CONCURRENCY-CRITICAL SERVICE.
 * Replaces the PostgreSQL hold_seats stored procedure using PESSIMISTIC_WRITE locks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeatLockService {

    private final ShowSeatRepository showSeatRepository;

    @Value("${app.seat-lock.duration-minutes:10}")
    private int lockDurationMinutes;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public HoldSeatsResponse holdSeats(HoldSeatsRequest request, UUID userId) {
        List<UUID> targetSeatIds = request.getShowSeatIds();

        // Step 1: Acquire PESSIMISTIC_WRITE lock on target rows
        List<ShowSeat> seats = showSeatRepository.findByIdsWithLock(targetSeatIds, request.getShowId());

        // Step 2: Validate all requested seats were found
        if (seats.size() < targetSeatIds.size()) {
            throw new SeatsUnavailableException("One or more seats are no longer available. Please try again.");
        }

        LocalDateTime now = LocalDateTime.now();

        // Step 3: Check availability (status must be AVAILABLE or expired LOCK or locked by same user)
        for (ShowSeat seat : seats) {
            if (seat.getStatus() == SeatStatus.BOOKED) {
                throw new SeatsUnavailableException("Seat " + seat.getSeat().getRowLabel() + seat.getSeat().getSeatNumber() + " is already booked.");
            }
            if (seat.getStatus() == SeatStatus.LOCKED) {
                boolean isOtherUser = !userId.equals(seat.getLockedBy());
                boolean isNotExpired = seat.getLockExpiresAt() != null && seat.getLockExpiresAt().isAfter(now);
                if (isOtherUser && isNotExpired) {
                    throw new SeatsUnavailableException("Seat " + seat.getSeat().getRowLabel() + seat.getSeat().getSeatNumber() + " is currently held by another user.");
                }
            }
        }

        // Step 4: Atomic Lock Update
        LocalDateTime expiresAt = now.plusMinutes(lockDurationMinutes);
        for (ShowSeat seat : seats) {
            seat.setStatus(SeatStatus.LOCKED);
            seat.setLockedBy(userId);
            seat.setLockedAt(now);
            seat.setLockExpiresAt(expiresAt);
        }

        showSeatRepository.saveAll(seats);

        log.info("User {} held {} seats for show {} until {}", userId, seats.size(), request.getShowId(), expiresAt);

        return HoldSeatsResponse.builder()
                .success(true)
                .lockedSeatIds(targetSeatIds)
                .expiresAt(expiresAt)
                .build();
    }
}
