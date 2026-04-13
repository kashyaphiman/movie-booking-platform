package com.xyz.movieticket.repository;

import com.xyz.movieticket.model.Booking;
import com.xyz.movieticket.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByUserIdAndStatus(String userId, BookingStatus status);

    @Modifying
    @Query("UPDATE Booking b SET b.status = 'EXPIRED' WHERE b.status = 'PENDING' AND b.paymentDeadline < :now")
    int expirePendingBookings(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.show.id = :showId AND b.status = 'CONFIRMED'")
    int countConfirmedBookingsByShow(@Param("showId") String showId);
}