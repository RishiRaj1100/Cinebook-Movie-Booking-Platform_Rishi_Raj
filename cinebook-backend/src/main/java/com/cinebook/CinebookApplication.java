package com.cinebook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CineBook — Movie Ticket Booking System
 * Spring Boot 3.3 | Java 21 | PostgreSQL | JWT Auth | Razorpay | Spring AI
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class CinebookApplication {
    public static void main(String[] args) {
        SpringApplication.run(CinebookApplication.class, args);
    }
}
