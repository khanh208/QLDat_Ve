package com.example.QLDatVe.repositories;

import com.example.QLDatVe.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.user " +
           "LEFT JOIN FETCH b.trip t " +
           "LEFT JOIN FETCH t.route " +
           "LEFT JOIN FETCH t.vehicle " +
           "LEFT JOIN FETCH b.bookingDetails " +
           "WHERE b.user.userId = :userId " +
           "ORDER BY b.bookingTime DESC")
    List<Booking> findByUserWithDetails(@Param("userId") Integer userId);

    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.user u " +
           "LEFT JOIN FETCH b.trip t " +
           "LEFT JOIN FETCH t.route " +
           "LEFT JOIN FETCH t.vehicle " +
           "LEFT JOIN FETCH b.bookingDetails " +
           "ORDER BY b.bookingTime DESC")
    List<Booking> findAllWithDetails();
    
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.status = 'CONFIRMED'")
    BigDecimal sumTotalRevenueConfirmed();

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CONFIRMED'")
    long countConfirmedBookings();

    Optional<Booking> findByMomoOrderIdAndMomoRequestId(String momoOrderId, String momoRequestId);

    Optional<Booking> findByMomoOrderId(String momoOrderId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Booking b SET b.status = 'CANCELLED' " +
           "WHERE b.status = 'PENDING' " +
           "AND b.bookingTime < :expiryTime")
    int cancelExpiredPendingBookings(@Param("expiryTime") LocalDateTime expiryTime);
}