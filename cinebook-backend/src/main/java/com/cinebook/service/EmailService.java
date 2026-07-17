package com.cinebook.service;

import com.cinebook.domain.entity.Booking;
import com.cinebook.util.QrCodeService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final QrCodeService  qrCodeService;

    @Async
    public void sendBookingConfirmation(Booking booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("Ticket Confirmed: " + booking.getShow().getMovie().getTitle());

            String html = "<h1>Booking Confirmed!</h1>" +
                    "<p>Thank you, " + booking.getUser().getFullName() + "!</p>" +
                    "<p><strong>Movie:</strong> " + booking.getShow().getMovie().getTitle() + "</p>" +
                    "<p><strong>Theater:</strong> " + booking.getShow().getScreen().getTheater().getName() + "</p>" +
                    "<p><strong>Showtime:</strong> " + booking.getShow().getStartTime() + "</p>" +
                    "<p><strong>Booking ID:</strong> " + booking.getId() + "</p>" +
                    "<br/><img src='cid:qrCode'/>";

            helper.setText(html, true);

            byte[] qrBytes = qrCodeService.generateQrCodeImage(booking.getId().toString(), 250, 250);
            helper.addInline("qrCode", new ByteArrayResource(qrBytes), "image/png");

            mailSender.send(message);
            log.info("Booking confirmation email sent to {}", booking.getUser().getEmail());

        } catch (Exception e) {
            log.error("Failed to send booking confirmation email: ", e);
        }
    }
}
