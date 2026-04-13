package com.xyz.movieticket.service;

import com.xyz.movieticket.model.Booking;
import com.xyz.movieticket.model.User;
import com.xyz.movieticket.model.enums.NotificationType;

public interface NotificationService {

    void sendBookingConfirmation(Booking booking);

    void sendBookingCancellation(Booking booking);

    void sendPaymentConfirmation(Booking booking);

    void sendPaymentReminder(Booking booking);

    void sendSeatReleaseNotification(User user, Booking booking);

    void sendWelcomeEmail(User user);

    void sendPasswordResetEmail(User user, String resetToken);

    void sendPromotionalNotification(User user, String message);

    void sendTheatreOnboardingConfirmation(String theatreEmail, String theatreName);

    void sendShowReminder(Booking booking, int minutesBeforeShow);

    void sendBulkBookingNotification(String theatreEmail, int numberOfBookings);
}