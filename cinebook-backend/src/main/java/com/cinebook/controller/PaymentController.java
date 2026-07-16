package com.cinebook.controller;

import com.cinebook.domain.entity.User;
import com.cinebook.dto.request.PaymentVerifyRequest;
import com.cinebook.dto.response.ApiResponse;
import com.cinebook.dto.response.PaymentOrderResponse;
import com.cinebook.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> createOrder(
            @RequestParam UUID bookingId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.createOrder(bookingId, user.getId()), "Payment order created"));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request,
            @AuthenticationPrincipal User user) {
        boolean success = paymentService.verifyAndConfirmPayment(request, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(success, "Payment verified and booking confirmed successfully"));
    }
}
