package com.cinebook.domain.repository;

import com.cinebook.domain.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {

    List<Movie> findByIsActiveTrueOrderByReleaseDateDesc();

    Page<Movie> findByIsActiveTrueOrderByReleaseDateDesc(Pageable pageable);

    @Query("""
        SELECT DISTINCT m FROM Movie m
        WHERE m.isActive = true
          AND (:genre IS NULL OR :genre = '' OR LOWER(m.genre) LIKE LOWER(CONCAT('%', :genre, '%')))
          AND (:language IS NULL OR :language = '' OR LOWER(m.language) = LOWER(:language))
        ORDER BY m.releaseDate DESC
        """)
    List<Movie> findByFilters(
        @Param("genre")    String genre,
        @Param("language") String language
    );

    @Query("SELECT DISTINCT m.genre FROM Movie m WHERE m.isActive = true ORDER BY m.genre")
    List<String> findDistinctGenres();

    @Query("SELECT DISTINCT m.language FROM Movie m WHERE m.isActive = true ORDER BY m.language")
    List<String> findDistinctLanguages();

    boolean existsByTmdbId(Integer tmdbId);

    java.util.Optional<Movie> findByTmdbId(Integer tmdbId);
}
