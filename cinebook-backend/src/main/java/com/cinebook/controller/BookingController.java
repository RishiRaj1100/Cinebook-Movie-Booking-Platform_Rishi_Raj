package com.cinebook.controller;

import com.cinebook.domain.entity.Booking;
import com.cinebook.domain.entity.User;
import com.cinebook.dto.request.CreateBookingRequest;
import com.cinebook.dto.response.ApiResponse;
import com.cinebook.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<Booking>> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.createBooking(request, user.getId()), "Booking created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Booking>>> getUserBookings(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.getUserBookings(user.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Booking>> getBookingById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.getBookingById(id, user.getId())));
    }

    @GetMapping("/public/{id:^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$}")
    public ResponseEntity<ApiResponse<Booking>> getPublicBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.getPublicBooking(id)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        bookingService.cancelBooking(id, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Booking cancelled successfully"));
    }
}
