package com.cinebook.controller;

import com.cinebook.domain.entity.Screen;
import com.cinebook.domain.entity.Theater;
import com.cinebook.dto.response.ApiResponse;
import com.cinebook.service.TheaterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/theaters")
@RequiredArgsConstructor
public class TheaterController {

    private final TheaterService theaterService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Theater>>> getTheaters(@RequestParam(required = false) String city) {
        if (city != null && !city.isBlank()) {
            return ResponseEntity.ok(ApiResponse.ok(theaterService.getTheatersByCity(city)));
        }
        return ResponseEntity.ok(ApiResponse.ok(theaterService.getAllTheaters()));
    }

    @GetMapping("/cities")
    public ResponseEntity<ApiResponse<List<String>>> getCities() {
        return ResponseEntity.ok(ApiResponse.ok(theaterService.getCities()));
    }

    @GetMapping("/{id:^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$}")
    public ResponseEntity<ApiResponse<Theater>> getTheaterById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(theaterService.getTheaterById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Theater>> createTheater(@RequestBody Theater theater) {
        return ResponseEntity.ok(ApiResponse.ok(theaterService.createTheater(theater), "Theater created successfully"));
    }

    @PostMapping("/{theaterId}/screens")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Screen>> createScreen(@PathVariable UUID theaterId, @RequestBody Screen screen) {
        return ResponseEntity.ok(ApiResponse.ok(theaterService.createScreen(theaterId, screen), "Screen and seats generated successfully"));
    }
}
