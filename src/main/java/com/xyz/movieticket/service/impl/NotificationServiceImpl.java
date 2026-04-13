package com.xyz.movieticket.service.impl;

import com.xyz.movieticket.model.Booking;
import com.xyz.movieticket.model.User;
import com.xyz.movieticket.model.enums.NotificationType;
import com.xyz.movieticket.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;

    @Value("${app.notification.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${app.notification.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${app.notification.whatsapp.enabled:false}")
    private boolean whatsappEnabled;

    @Value("${app.notification.email.from:noreply@movieticket.com}")
    private String fromEmail;

    @Value("${app.notification.sms.api.key:}")
    private String smsApiKey;

    @Value("${app.notification.sms.api.url:}")
    private String smsApiUrl;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    @Override
    @Async
    public void sendBookingConfirmation(Booking booking) {
        log.info("Sending booking confirmation for booking: {}", booking.getBookingReference());

        String subject = "🎬 Movie Ticket Booking Confirmation - " + booking.getBookingReference();
        String emailBody = buildBookingConfirmationEmail(booking);
        String smsBody = buildBookingConfirmationSMS(booking);

        // Send email
        if (emailEnabled) {
            sendEmail(booking.getUser().getEmail(), subject, emailBody);
        }

        // Send SMS
        if (smsEnabled && booking.getUser().getPhoneNumber() != null) {
            sendSMS(booking.getUser().getPhoneNumber(), smsBody);
        }

        // Send WhatsApp (optional)
        if (whatsappEnabled && booking.getUser().getPhoneNumber() != null) {
            sendWhatsAppMessage(booking.getUser().getPhoneNumber(), smsBody);
        }

        log.info("Booking confirmation sent successfully for: {}", booking.getBookingReference());
    }

    @Override
    @Async
    public void sendBookingCancellation(Booking booking) {
        log.info("Sending booking cancellation notification for: {}", booking.getBookingReference());

        String subject = "❌ Booking Cancellation Confirmation - " + booking.getBookingReference();
        String body = buildCancellationEmail(booking);

        if (emailEnabled) {
            sendEmail(booking.getUser().getEmail(), subject, body);
        }

        if (smsEnabled && booking.getUser().getPhoneNumber() != null) {
            sendSMS(booking.getUser().getPhoneNumber(), "Your booking " + booking.getBookingReference() + " has been cancelled.");
        }
    }

    @Override
    @Async
    public void sendPaymentConfirmation(Booking booking) {
        log.info("Sending payment confirmation for booking: {}", booking.getBookingReference());

        String subject = "💰 Payment Confirmation - " + booking.getBookingReference();
        String body = buildPaymentConfirmationEmail(booking);

        if (emailEnabled) {
            sendEmail(booking.getUser().getEmail(), subject, body);
        }
    }

    @Override
    @Async
    public void sendPaymentReminder(Booking booking) {
        log.info("Sending payment reminder for booking: {}", booking.getBookingReference());

        String subject = "⏰ Payment Reminder - Complete your booking";
        String body = buildPaymentReminderEmail(booking);

        if (emailEnabled) {
            sendEmail(booking.getUser().getEmail(), subject, body);
        }

        if (smsEnabled && booking.getUser().getPhoneNumber() != null) {
            String smsBody = String.format(
                    "Reminder: Complete payment for booking %s within %s minutes to confirm your seats.",
                    booking.getBookingReference(),
                    "15"
            );
            sendSMS(booking.getUser().getPhoneNumber(), smsBody);
        }
    }

    @Override
    @Async
    public void sendSeatReleaseNotification(User user, Booking booking) {
        log.info("Sending seat release notification for user: {}", user.getEmail());

        String subject = "🎫 Seats Released - " + booking.getBookingReference();
        String body = buildSeatReleaseEmail(booking);

        if (emailEnabled) {
            sendEmail(user.getEmail(), subject, body);
        }
    }

    @Override
    @Async
    public void sendWelcomeEmail(User user) {
        log.info("Sending welcome email to: {}", user.getEmail());

        String subject = "Welcome to MovieTicket Platform! 🎬";
        String body = buildWelcomeEmail(user);

        if (emailEnabled) {
            sendEmail(user.getEmail(), subject, body);
        }
    }

    @Override
    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        log.info("Sending password reset email to: {}", user.getEmail());

        String subject = "Password Reset Request";
        String body = buildPasswordResetEmail(user, resetToken);

        if (emailEnabled) {
            sendEmail(user.getEmail(), subject, body);
        }
    }

    @Override
    @Async
    public void sendPromotionalNotification(User user, String message) {
        log.info("Sending promotional notification to: {}", user.getEmail());

        String subject = "🎉 Special Offer Just for You!";

        if (emailEnabled) {
            sendEmail(user.getEmail(), subject, message);
        }

        if (smsEnabled && user.getPhoneNumber() != null) {
            sendSMS(user.getPhoneNumber(), message);
        }
    }

    @Override
    @Async
    public void sendTheatreOnboardingConfirmation(String theatreEmail, String theatreName) {
        log.info("Sending theatre onboarding confirmation to: {}", theatreEmail);

        String subject = "Welcome to MovieTicket Platform - Theatre Partner";
        String body = buildTheatreOnboardingEmail(theatreName);

        if (emailEnabled) {
            sendEmail(theatreEmail, subject, body);
        }
    }

    @Override
    @Async
    public void sendShowReminder(Booking booking, int minutesBeforeShow) {
        log.info("Sending show reminder for booking: {}, minutes before: {}",
                booking.getBookingReference(), minutesBeforeShow);

        String subject = "🎬 Show Reminder - " + booking.getShow().getMovie().getTitle();
        String body = buildShowReminderEmail(booking, minutesBeforeShow);

        if (emailEnabled) {
            sendEmail(booking.getUser().getEmail(), subject, body);
        }

        if (smsEnabled && booking.getUser().getPhoneNumber() != null) {
            String smsBody = String.format(
                    "Reminder: %s starts at %s. Show ID: %s",
                    booking.getShow().getMovie().getTitle(),
                    booking.getShow().getShowTime().format(DATE_FORMATTER),
                    booking.getBookingReference()
            );
            sendSMS(booking.getUser().getPhoneNumber(), smsBody);
        }
    }

    @Override
    @Async
    public void sendBulkBookingNotification(String theatreEmail, int numberOfBookings) {
        log.info("Sending bulk booking notification to theatre: {}", theatreEmail);

        String subject = "Bulk Booking Summary";
        String body = String.format(
                "Dear Theatre Partner,\n\n" +
                        "You have received %d new bookings in the last hour.\n" +
                        "Login to your dashboard to view details.\n\n" +
                        "Best regards,\nMovieTicket Platform",
                numberOfBookings
        );

        if (emailEnabled) {
            sendEmail(theatreEmail, subject, body);
        }
    }

    // ========== Private Helper Methods ==========

    private void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML email

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}. Error: {}", to, e.getMessage());
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        try {
            // Using SMS gateway API (e.g., Twilio, AWS SNS, etc.)
            Map<String, String> request = new HashMap<>();
            request.put("to", phoneNumber);
            request.put("message", message);
            request.put("apiKey", smsApiKey);

            // Uncomment when SMS API is configured
            // restTemplate.postForEntity(smsApiUrl, request, String.class);

            log.info("SMS sent successfully to: {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}. Error: {}", phoneNumber, e.getMessage());
        }
    }

    private void sendWhatsAppMessage(String phoneNumber, String message) {
        try {
            // WhatsApp Business API integration
            log.info("WhatsApp message sent to: {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to: {}", phoneNumber);
        }
    }

    // ========== Email Template Builders ==========

    private String buildBookingConfirmationEmail(Booking booking) {
        return String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head><style>" +
                        "body { font-family: Arial, sans-serif; }" +
                        ".container { max-width: 600px; margin: auto; padding: 20px; }" +
                        ".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }" +
                        ".content { padding: 20px; }" +
                        ".booking-details { background-color: #f9f9f9; padding: 15px; border-radius: 5px; }" +
                        ".ticket { border: 2px dashed #4CAF50; padding: 15px; margin: 10px 0; }" +
                        ".footer { text-align: center; color: #666; font-size: 12px; }" +
                        "</style></head>" +
                        "<body>" +
                        "<div class='container'>" +
                        "<div class='header'>" +
                        "<h2>Booking Confirmed! 🎬</h2>" +
                        "</div>" +
                        "<div class='content'>" +
                        "<p>Dear <strong>%s</strong>,</p>" +
                        "<p>Your movie tickets have been successfully booked!</p>" +
                        "<div class='booking-details'>" +
                        "<h3>Booking Details:</h3>" +
                        "<p><strong>Booking Reference:</strong> %s</p>" +
                        "<p><strong>Movie:</strong> %s</p>" +
                        "<p><strong>Theatre:</strong> %s</p>" +
                        "<p><strong>Date & Time:</strong> %s</p>" +
                        "<p><strong>Seats:</strong> %s</p>" +
                        "<p><strong>Number of Seats:</strong> %d</p>" +
                        "<p><strong>Total Amount:</strong> ₹%.2f</p>" +
                        "<p><strong>Discount Applied:</strong> ₹%.2f</p>" +
                        "<p><strong>Final Amount:</strong> ₹%.2f</p>" +
                        "</div>" +
                        "<div class='ticket'>" +
                        "<h3>🎫 E-Ticket</h3>" +
                        "<p>Show this ticket at the theatre entrance</p>" +
                        "<p><strong>QR Code:</strong> %s</p>" +
                        "</div>" +
                        "<p><strong>Important:</strong> Please arrive 15 minutes before the show.</p>" +
                        "</div>" +
                        "<div class='footer'>" +
                        "<p>Thank you for choosing MovieTicket Platform!</p>" +
                        "<p>© 2024 MovieTicket Platform. All rights reserved.</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                booking.getUser().getFullName(),
                booking.getBookingReference(),
                booking.getShow().getMovie().getTitle(),
                booking.getShow().getTheatre().getName(),
                booking.getShow().getShowTime().format(DATE_FORMATTER),
                booking.getSeatsBooked(),
                booking.getNumberOfSeats(),
                booking.getTotalAmount(),
                booking.getDiscountAmount(),
                booking.getFinalAmount(),
                generateQRCodeData(booking.getBookingReference())
        );
    }

    private String buildBookingConfirmationSMS(Booking booking) {
        return String.format(
                "Booking Confirmed! %s | %s | Seats: %s | Amount: ₹%.2f | Ref: %s",
                booking.getShow().getMovie().getTitle(),
                booking.getShow().getShowTime().format(DATE_FORMATTER),
                booking.getSeatsBooked(),
                booking.getFinalAmount(),
                booking.getBookingReference()
        );
    }

    private String buildCancellationEmail(Booking booking) {
        return String.format(
                "<h2>Booking Cancelled</h2>" +
                        "<p>Dear %s,</p>" +
                        "<p>Your booking with reference <strong>%s</strong> has been cancelled.</p>" +
                        "<p><strong>Cancelled Seats:</strong> %s</p>" +
                        "<p><strong>Refund Amount:</strong> ₹%.2f</p>" +
                        "<p>Refund will be processed within 5-7 business days.</p>",
                booking.getUser().getFullName(),
                booking.getBookingReference(),
                booking.getSeatsBooked(),
                booking.getFinalAmount()
        );
    }

    private String buildPaymentConfirmationEmail(Booking booking) {
        return String.format(
                "<h2>Payment Confirmed! ✅</h2>" +
                        "<p>Dear %s,</p>" +
                        "<p>Your payment of <strong>₹%.2f</strong> for booking <strong>%s</strong> has been received.</p>" +
                        "<p>Your tickets are now confirmed. Enjoy the show!</p>",
                booking.getUser().getFullName(),
                booking.getFinalAmount(),
                booking.getBookingReference()
        );
    }

    private String buildPaymentReminderEmail(Booking booking) {
        return String.format(
                "<h2>⏰ Complete Your Booking</h2>" +
                        "<p>Dear %s,</p>" +
                        "<p>You have pending payment for booking <strong>%s</strong>.</p>" +
                        "<p><strong>Amount Due:</strong> ₹%.2f</p>" +
                        "<p><strong>Payment Deadline:</strong> %s</p>" +
                        "<p>Please complete payment before the deadline to confirm your seats.</p>" +
                        "<a href='https://movieticket.com/payment/%s' style='background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Pay Now</a>",
                booking.getUser().getFullName(),
                booking.getBookingReference(),
                booking.getFinalAmount(),
                booking.getPaymentDeadline().format(DATE_FORMATTER),
                booking.getBookingReference()
        );
    }

    private String buildSeatReleaseEmail(Booking booking) {
        return String.format(
                "<h2>Seats Released ⚠️</h2>" +
                        "<p>Dear %s,</p>" +
                        "<p>Your booking <strong>%s</strong> has expired due to non-payment.</p>" +
                        "<p>The following seats have been released: <strong>%s</strong></p>" +
                        "<p>You can book again if seats are still available.</p>",
                booking.getUser().getFullName(),
                booking.getBookingReference(),
                booking.getSeatsBooked()
        );
    }

    private String buildWelcomeEmail(User user) {
        return String.format(
                "<h2>Welcome to MovieTicket Platform! 🎬</h2>" +
                        "<p>Dear %s,</p>" +
                        "<p>Thank you for joining MovieTicket Platform!</p>" +
                        "<p>You can now:</p>" +
                        "<ul>" +
                        "<li>Browse movies across multiple cities</li>" +
                        "<li>Book tickets in advance</li>" +
                        "<li>Get exclusive discounts</li>" +
                        "<li>Earn loyalty points</li>" +
                        "</ul>" +
                        "<p>Get started by exploring movies near you!</p>" +
                        "<a href='https://movieticket.com/explore' style='background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Explore Movies</a>",
                user.getFullName()
        );
    }

    private String buildPasswordResetEmail(User user, String resetToken) {
        String resetLink = "https://movieticket.com/reset-password?token=" + resetToken;
        return String.format(
                "<h2>Password Reset Request</h2>" +
                        "<p>Dear %s,</p>" +
                        "<p>We received a request to reset your password.</p>" +
                        "<p>Click the link below to reset your password:</p>" +
                        "<a href='%s' style='background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Reset Password</a>" +
                        "<p>This link will expire in 1 hour.</p>" +
                        "<p>If you didn't request this, please ignore this email.</p>",
                user.getFullName(),
                resetLink
        );
    }

    private String buildTheatreOnboardingEmail(String theatreName) {
        return String.format(
                "<h2>Welcome to MovieTicket Platform! 🎭</h2>" +
                        "<p>Dear %s,</p>" +
                        "<p>Your theatre has been successfully onboarded to our platform!</p>" +
                        "<p>You can now:</p>" +
                        "<ul>" +
                        "<li>Create and manage shows</li>" +
                        "<li>Update seat inventory</li>" +
                        "<li>Track bookings in real-time</li>" +
                        "<li>Access analytics dashboard</li>" +
                        "</ul>" +
                        "<p>Login to your partner dashboard to get started.</p>",
                theatreName
        );
    }

    private String buildShowReminderEmail(Booking booking, int minutesBeforeShow) {
        return String.format(
                "<h2>Show Starting Soon! 🎬</h2>" +
                        "<p>Dear %s,</p>" +
                        "<p>Your show <strong>%s</strong> starts in %d minutes at <strong>%s</strong>.</p>" +
                        "<div class='booking-details'>" +
                        "<p><strong>Date & Time:</strong> %s</p>" +
                        "<p><strong>Seats:</strong> %s</p>" +
                        "<p><strong>Booking Reference:</strong> %s</p>" +
                        "</div>" +
                        "<p>Please carry a valid ID proof and arrive on time.</p>",
                booking.getUser().getFullName(),
                booking.getShow().getMovie().getTitle(),
                minutesBeforeShow,
                booking.getShow().getTheatre().getName(),
                booking.getShow().getShowTime().format(DATE_FORMATTER),
                booking.getSeatsBooked(),
                booking.getBookingReference()
        );
    }

    private String generateQRCodeData(String bookingReference) {
        // This would generate actual QR code in production
        // For now, return a simple URL
        return "https://movieticket.com/ticket/" + bookingReference;
    }
}