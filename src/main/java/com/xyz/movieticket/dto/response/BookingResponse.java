package com.xyz.movieticket.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookingResponse {
    private String bookingId;
    private String bookingReference;
    private String movieName;
    private String theatreName;
    private LocalDateTime showTime;
    private List<String> seats;
    private Integer numberOfSeats;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String status;
    private LocalDateTime paymentDeadline;
}