package com.xyz.movieticket.model;

import com.xyz.movieticket.model.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Column(nullable = false)
    private String seatNumber;

    private String rowCode;
    private Integer seatColumn;
    private String seatType;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    private String blockedBy;
    private LocalDateTime blockedUntil;

    @Version
    private Long version;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = SeatStatus.AVAILABLE;
    }
}