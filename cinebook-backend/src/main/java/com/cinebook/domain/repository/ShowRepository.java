package com.cinebook.domain.repository;

import com.cinebook.domain.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShowRepository extends JpaRepository<Show, UUID> {

    @Query("""
        SELECT s FROM Show s
        JOIN FETCH s.movie
        JOIN FETCH s.screen sc
        JOIN FETCH sc.theater
        WHERE s.movie.id = :movieId
          AND s.isActive = true
          AND s.startTime >= :from
          AND s.startTime <= :to
        ORDER BY s.startTime ASC
        """)
    List<Show> findShowsForMovie(
        @Param("movieId") UUID movieId,
        @Param("from")    LocalDateTime from,
        @Param("to")      LocalDateTime to
    );

    @Query("""
        SELECT s FROM Show s
        JOIN FETCH s.movie
        JOIN FETCH s.screen sc
        JOIN FETCH sc.theater
        WHERE s.isActive = true
          AND s.startTime > :now
        ORDER BY s.startTime ASC
        """)
    List<Show> findUpcomingShows(@Param("now") LocalDateTime now);

    long countByScreenId(UUID screenId);
}
