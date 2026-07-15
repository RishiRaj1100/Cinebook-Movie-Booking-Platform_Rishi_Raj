package com.cinebook.domain.repository;

import com.cinebook.domain.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {
    List<Seat> findByScreenIdOrderByRowLabelAscSeatNumberAsc(UUID screenId);
}
