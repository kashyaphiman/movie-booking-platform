package com.xyz.movieticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TheatreShowResponse {

    // Theatre Information
    private String theatreId;
    private String theatreName;
    private String theatreAddress;
    private String theatreCity;

    // Show Information
    private String showId;
    private LocalDateTime showTime;
    private String formattedShowTime; // Pre-formatted date/time for display
    private String showType;

    // Movie Information
    private String movieId;
    private String movieName;
    private String movieLanguage;
    private String movieGenre;

    // Pricing
    private BigDecimal basePrice;
    private BigDecimal discountedPrice;
    private String priceDisplay; // Formatted price string

    // Availability
    private Integer availableSeats;
    private Integer totalSeats;
    private String availabilityStatus; // AVAILABLE, FILLING_FAST, HOUSE_FULL

    // Helper method to format show time
    public String getFormattedShowTime() {
        if (showTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
            return showTime.format(formatter);
        }
        return null;
    }

    // Helper method to format price display
    public String getPriceDisplay() {
        if (discountedPrice != null && discountedPrice.compareTo(basePrice) < 0) {
            return String.format("₹%.2f (Was ₹%.2f)", discountedPrice, basePrice);
        }
        return String.format("₹%.2f", basePrice);
    }

    // Helper method to determine availability status
    public String getAvailabilityStatus() {
        if (availableSeats == null || totalSeats == null) {
            return "UNKNOWN";
        }
        double percentage = (double) availableSeats / totalSeats * 100;
        if (availableSeats == 0) {
            return "HOUSE_FULL";
        } else if (percentage < 20) {
            return "FILLING_FAST";
        } else {
            return "AVAILABLE";
        }
    }
}