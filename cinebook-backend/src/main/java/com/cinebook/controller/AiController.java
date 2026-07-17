package com.cinebook.controller;

import com.cinebook.dto.response.ApiResponse;
import com.cinebook.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final RecommendationService recommendationService;

    @PostMapping("/recommend")
    public ResponseEntity<ApiResponse<String>> recommendMovies(@RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.ok(recommendationService.recommendMovies(query)));
    }
}
