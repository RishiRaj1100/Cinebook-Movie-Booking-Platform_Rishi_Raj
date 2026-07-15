package com.cinebook.domain.repository;

import com.cinebook.domain.entity.Booking;
import com.cinebook.domain.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.show s
        JOIN FETCH s.movie
        JOIN FETCH s.screen sc
        JOIN FETCH sc.theater
        WHERE b.user.id = :userId
        ORDER BY b.createdAt DESC
        """)
    List<Booking> findByUserIdWithDetails(@Param("userId") UUID userId);

    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.show s
        JOIN FETCH s.movie
        JOIN FETCH s.screen sc
        JOIN FETCH sc.theater
        JOIN FETCH b.bookingSeats bs
        JOIN FETCH bs.showSeat ss
        JOIN FETCH ss.seat seat
        WHERE b.id = :id AND b.user.id = :userId
        """)
    Optional<Booking> findByIdAndUserIdWithDetails(
        @Param("id")     UUID id,
        @Param("userId") UUID userId
    );

    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.show s
        JOIN FETCH s.movie
        JOIN FETCH s.screen sc
        JOIN FETCH sc.theater
        JOIN FETCH b.bookingSeats bs
        JOIN FETCH bs.showSeat ss
        JOIN FETCH ss.seat seat
        WHERE b.id = :id
        """)
    Optional<Booking> findByIdWithDetails(@Param("id") UUID id);

    /** Expire bookings whose seat locks have all expired. Replaces pg_cron logic. */
    @Modifying
    @Query("""
        UPDATE Booking b SET b.status = 'EXPIRED'
        WHERE b.status = 'CREATED'
          AND NOT EXISTS (
            SELECT 1 FROM BookingSeat bs
            JOIN ShowSeat ss ON ss.id = bs.showSeat.id
            WHERE bs.booking.id = b.id
              AND ss.status = 'LOCKED'
              AND ss.lockedBy = b.user.id
          )
        """)
    int expireOrphanedBookings();

    long countByStatus(BookingStatus status);
}
