package com.cinebook.controller;

import com.cinebook.domain.entity.Movie;
import com.cinebook.dto.response.ApiResponse;
import com.cinebook.service.TmdbService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tmdb")
@RequiredArgsConstructor
public class TmdbController {

    private final TmdbService tmdbService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<String>> searchTmdb(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.ok(tmdbService.searchMoviesFromTmdb(q)));
    }

    @PostMapping("/import/{tmdbId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Movie>> importMovie(@PathVariable Integer tmdbId) {
        return ResponseEntity.ok(ApiResponse.ok(tmdbService.importMovieFromTmdb(tmdbId), "Movie imported from TMDB successfully"));
    }

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<List<Movie>>> syncPopularMovies(@RequestParam(defaultValue = "2") int pages) {
        List<Movie> imported = tmdbService.syncPopularMoviesFromTmdb(pages);
        return ResponseEntity.ok(ApiResponse.ok(imported, "Synced " + imported.size() + " popular movies from TMDB"));
    }
}
