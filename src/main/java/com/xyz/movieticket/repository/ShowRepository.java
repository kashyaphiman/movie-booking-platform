package com.xyz.movieticket.repository;

import com.xyz.movieticket.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowRepository extends JpaRepository<Show, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Show s WHERE s.id = :id")
    Optional<Show> findByIdWithLock(@Param("id") String id);

    @Query("SELECT s FROM Show s WHERE s.movie.id = :movieId AND s.theatre.city = :city " +
            "AND s.showTime BETWEEN :startDate AND :endDate AND s.status = 'ACTIVE'")
    List<Show> findByMovieIdAndTheatreCityAndShowTimeBetween(
            @Param("movieId") String movieId,
            @Param("city") String city,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<Show> findByTheatreIdAndShowTimeBetween(String theatreId, LocalDateTime start, LocalDateTime end);
}