package com.xyz.movieticket.model;

import com.xyz.movieticket.model.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String bookingReference;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Column(nullable = false)
    private Integer numberOfSeats;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    private BigDecimal discountAmount;
    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private String seatsBooked;
    private LocalDateTime bookingTime;
    private LocalDateTime paymentDeadline;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;

    private String paymentId;

    @PrePersist
    protected void onCreate() {
        bookingTime = LocalDateTime.now();
        paymentDeadline = LocalDateTime.now().plusMinutes(15);
        status = BookingStatus.PENDING;
    }
}