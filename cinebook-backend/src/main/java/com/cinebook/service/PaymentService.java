package com.cinebook.service;

import com.cinebook.domain.entity.Booking;
import com.cinebook.domain.entity.Payment;
import com.cinebook.domain.enums.PaymentStatus;
import com.cinebook.domain.repository.BookingRepository;
import com.cinebook.domain.repository.PaymentRepository;
import com.cinebook.dto.request.PaymentVerifyRequest;
import com.cinebook.dto.response.PaymentOrderResponse;
import com.cinebook.exception.BadRequestException;
import com.cinebook.exception.ResourceNotFoundException;
import com.cinebook.util.SignatureVerifier;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingService   bookingService;
    private final EmailService     emailService;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    @Transactional
    public PaymentOrderResponse createOrder(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized payment request");
        }

        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", booking.getTotalAmount()); // Amount in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", booking.getId().toString());

            Order order = razorpay.orders.create(orderRequest);
            String razorpayOrderId = order.get("id");

            Payment payment = Payment.builder()
                    .booking(booking)
                    .amount(booking.getTotalAmount())
                    .status(PaymentStatus.PENDING)
                    .providerOrderId(razorpayOrderId)
                    .build();

            paymentRepository.save(payment);

            return PaymentOrderResponse.builder()
                    .orderId(razorpayOrderId)
                    .amount(booking.getTotalAmount())
                    .currency("INR")
                    .razorpayKeyId(razorpayKeyId)
                    .bookingId(bookingId)
                    .build();

        } catch (Exception e) {
            log.error("Error creating Razorpay order: ", e);
            throw new BadRequestException("Failed to initiate payment: " + e.getMessage());
        }
    }

    @Transactional
    public boolean verifyAndConfirmPayment(PaymentVerifyRequest request, UUID userId) {
        boolean isValid = SignatureVerifier.verifyRazorpaySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                razorpayKeySecret
        );

        if (!isValid) {
            throw new BadRequestException("Invalid payment signature verification failed");
        }

        Payment payment = paymentRepository.findByProviderOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment order record not found"));

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setProviderPaymentId(request.getRazorpayPaymentId());
        payment.setProviderSignature(request.getRazorpaySignature());
        paymentRepository.save(payment);

        Booking confirmedBooking = bookingService.confirmBooking(request.getBookingId(), userId);

        // Async email confirmation
        emailService.sendBookingConfirmation(confirmedBooking);

        return true;
    }
}
