package com.xyz.movieticket.repository;

import com.xyz.movieticket.model.Seat;
import com.xyz.movieticket.model.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.show.id = :showId AND s.seatNumber IN :seatNumbers")
    List<Seat> findByShowIdAndSeatNumberInWithPessimisticLock(
            @Param("showId") String showId,
            @Param("seatNumbers") List<String> seatNumbers
    );

    List<Seat> findByShowIdAndStatus(String showId, SeatStatus status);

    @Modifying
    @Query("UPDATE Seat s SET s.status = 'AVAILABLE', s.blockedBy = NULL, s.blockedUntil = NULL " +
            "WHERE s.blockedUntil < :now AND s.status = 'BLOCKED'")
    int releaseExpiredBlocks(@Param("now") LocalDateTime now);
}