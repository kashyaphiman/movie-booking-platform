package com.xyz.movieticket.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class BookingRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Show ID is required")
    private String showId;

    @NotEmpty(message = "At least one seat must be selected")
    @Size(min = 1, max = 10, message = "Maximum 10 seats per booking")
    private List<@NotBlank String> seatNumbers;
}