package com.xyz.movieticket.service;

import com.xyz.movieticket.dto.request.BookingRequest;
import com.xyz.movieticket.dto.response.BookingResponse;
import com.xyz.movieticket.dto.response.PriceBreakdown;
import com.xyz.movieticket.dto.response.TheatreShowResponse;
import com.xyz.movieticket.exception.*;
import com.xyz.movieticket.model.*;
import com.xyz.movieticket.model.enums.BookingStatus;
import com.xyz.movieticket.model.enums.SeatStatus;
import com.xyz.movieticket.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PricingService pricingService;
    private final NotificationService notificationService;

    @Cacheable(value = "theatreShows", key = "#city + '_' + #movieId + '_' + #date")
    public List<TheatreShowResponse> browseTheatresByMovie(String city, String movieId, LocalDate date) {
        log.info("Browsing theatres for movie: {} in city: {} on date: {}", movieId, city, date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<Show> shows = showRepository.findByMovieIdAndTheatreCityAndShowTimeBetween(
                movieId, city, startOfDay, endOfDay
        );

        return shows.stream().map(this::mapToTheatreShowResponse).collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public BookingResponse bookTickets(BookingRequest request) {
        log.info("Booking tickets for user: {}, show: {}, seats: {}",
                request.getUserId(), request.getShowId(), request.getSeatNumbers());

        // Validate user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Validate and lock show
        Show show = showRepository.findByIdWithLock(request.getShowId())
                .orElseThrow(() -> new ShowNotFoundException("Show not found"));

        if (!"ACTIVE".equals(show.getStatus())) {
            throw new ShowNotAvailableException("Show is not available for booking");
        }

        // Validate and lock seats
        List<Seat> seats = validateAndLockSeats(show, request.getSeatNumbers());

        // Calculate price with discounts
        PriceBreakdown priceBreakdown = pricingService.calculatePrice(
                show.getBasePrice(),
                seats.size(),
                show.getShowType(),
                user
        );

        // Create booking
        Booking booking = createBooking(user, show, seats, priceBreakdown);

        // Reserve seats with timeout
        reserveSeatsWithTimeout(seats, booking.getId());

        // Send notification asynchronously
        notificationService.sendBookingConfirmation(booking);

        return mapToBookingResponse(booking, seats, show);
    }

    private List<Seat> validateAndLockSeats(Show show, List<String> seatNumbers) {
        List<Seat> seats = seatRepository.findByShowIdAndSeatNumberInWithPessimisticLock(
                show.getId(), seatNumbers
        );

        if (seats.size() != seatNumbers.size()) {
            throw new SeatNotFoundException("Some seats are not available");
        }

        for (Seat seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new SeatAlreadyBookedException("Seat " + seat.getSeatNumber() + " is not available");
            }
        }

        return seats;
    }

    private void reserveSeatsWithTimeout(List<Seat> seats, String bookingId) {
        LocalDateTime blockedUntil = LocalDateTime.now().plusMinutes(15);
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.BLOCKED);
            seat.setBlockedBy(bookingId);
            seat.setBlockedUntil(blockedUntil);
        }
        seatRepository.saveAll(seats);
    }

    private Booking createBooking(User user, Show show, List<Seat> seats, PriceBreakdown priceBreakdown) {
        String seatNumbers = seats.stream()
                .map(Seat::getSeatNumber)
                .collect(Collectors.joining(","));

        Booking booking = Booking.builder()
                .bookingReference(generateBookingReference())
                .user(user)
                .show(show)
                .numberOfSeats(seats.size())
                .totalAmount(priceBreakdown.getSubtotal())
                .discountAmount(priceBreakdown.getTotalDiscount())
                .finalAmount(priceBreakdown.getFinalAmount())
                .status(BookingStatus.PENDING)
                .seatsBooked(seatNumbers)
                .bookingTime(LocalDateTime.now())
                .paymentDeadline(LocalDateTime.now().plusMinutes(15))
                .build();

        return bookingRepository.save(booking);
    }

    private String generateBookingReference() {
        return "BOOK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private TheatreShowResponse mapToTheatreShowResponse(Show show) {
        return TheatreShowResponse.builder()
                .theatreId(show.getTheatre().getId())
                .theatreName(show.getTheatre().getName())
                .showId(show.getId())
                .showTime(show.getShowTime())
                .basePrice(show.getBasePrice())
                .availableSeats(show.getAvailableSeats())
                .build();
    }

    private BookingResponse mapToBookingResponse(Booking booking, List<Seat> seats, Show show) {
        return BookingResponse.builder()
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .movieName(show.getMovie().getTitle())
                .theatreName(show.getTheatre().getName())
                .showTime(show.getShowTime())
                .seats(seats.stream().map(Seat::getSeatNumber).collect(Collectors.toList()))
                .numberOfSeats(booking.getNumberOfSeats())
                .totalAmount(booking.getTotalAmount())
                .discountAmount(booking.getDiscountAmount())
                .finalAmount(booking.getFinalAmount())
                .status(booking.getStatus().toString())
                .paymentDeadline(booking.getPaymentDeadline())
                .build();
    }
}