package com.cinebook.domain.repository;

import com.cinebook.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByBookingId(UUID bookingId);
    Optional<Payment> findByProviderOrderId(String providerOrderId);
}
