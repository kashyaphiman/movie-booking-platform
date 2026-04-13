package com.xyz.movieticket.controller;

import com.xyz.movieticket.dto.request.BookingRequest;
import com.xyz.movieticket.dto.response.ApiResponse;
import com.xyz.movieticket.dto.response.BookingResponse;
import com.xyz.movieticket.dto.response.TheatreShowResponse;
import com.xyz.movieticket.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Booking API", description = "Movie ticket booking endpoints")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/theatres/by-movie")
    @Operation(summary = "Browse theatres running a specific movie")
    public ResponseEntity<ApiResponse<List<TheatreShowResponse>>> browseTheatres(
            @RequestParam String city,
            @RequestParam String movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("REST request to browse theatres - city: {}, movieId: {}, date: {}", city, movieId, date);
        List<TheatreShowResponse> theatres = bookingService.browseTheatresByMovie(city, movieId, date);
        return ResponseEntity.ok(ApiResponse.success(theatres));
    }

    @PostMapping("/book")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Book movie tickets")
    public ResponseEntity<ApiResponse<BookingResponse>> bookTickets(@Valid @RequestBody BookingRequest request) {
        log.info("REST request to book tickets: {}", request);
        BookingResponse response = bookingService.bookTickets(request);
        return ResponseEntity.ok(ApiResponse.success("Tickets booked successfully. Please complete payment within 15 minutes.", response));
    }
}