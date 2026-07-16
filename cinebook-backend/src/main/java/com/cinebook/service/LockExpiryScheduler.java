package com.cinebook.service;

import com.cinebook.domain.repository.BookingRepository;
import com.cinebook.domain.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Replaces the pg_cron scheduled job in Supabase.
 * Runs every 30 seconds to clean up expired seat locks.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LockExpiryScheduler {

    private final ShowSeatRepository showSeatRepository;
    private final BookingRepository bookingRepository;

    @Scheduled(fixedDelay = 30000) // 30 seconds
    @Transactional
    public void releaseExpiredLocks() {
        LocalDateTime now = LocalDateTime.now();

        int releasedSeats = showSeatRepository.releaseExpiredLocks(now);
        int expiredBookings = bookingRepository.expireOrphanedBookings();

        if (releasedSeats > 0 || expiredBookings > 0) {
            log.info("LockExpiryScheduler: Released {} expired seat locks and expired {} orphaned bookings.",
                    releasedSeats, expiredBookings);
        }
    }
}
