package com.cinebook.controller;

import com.cinebook.domain.entity.Movie;
import com.cinebook.dto.response.ApiResponse;
import com.cinebook.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Movie>>> getMovies(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String language) {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getMovies(genre, language)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Movie>> getMovieById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getMovieById(id)));
    }

    @GetMapping("/genres")
    public ResponseEntity<ApiResponse<List<String>>> getGenres() {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getGenres()));
    }

    @GetMapping("/languages")
    public ResponseEntity<ApiResponse<List<String>>> getLanguages() {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getLanguages()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Movie>> createMovie(@RequestBody Movie movie) {
        return ResponseEntity.ok(ApiResponse.ok(movieService.createMovie(movie), "Movie created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Movie>> updateMovie(@PathVariable UUID id, @RequestBody Movie movie) {
        return ResponseEntity.ok(ApiResponse.ok(movieService.updateMovie(id, movie), "Movie updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable UUID id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Movie soft-deleted successfully"));
    }
}
