package com.cinebook.controller;

import com.cinebook.domain.enums.BookingStatus;
import com.cinebook.domain.repository.BookingRepository;
import com.cinebook.domain.repository.MovieRepository;
import com.cinebook.domain.repository.TheaterRepository;

import com.cinebook.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final MovieRepository   movieRepository;
    private final TheaterRepository theaterRepository;
    private final BookingRepository bookingRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMovies", movieRepository.count());
        stats.put("totalTheaters", theaterRepository.count());
        stats.put("confirmedBookings", bookingRepository.countByStatus(BookingStatus.CONFIRMED));

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
