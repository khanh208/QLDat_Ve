package com.example.QLDatVe.repositories;

import com.example.QLDatVe.entities.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, Integer> {

    @Query("SELECT COUNT(bd) > 0 " +
           "FROM BookingDetail bd JOIN bd.booking b " +
           "WHERE b.trip.tripId = :tripId AND bd.seatNumber = :seatNumber AND b.status = 'CONFIRMED'")
    boolean existsByTripIdAndSeatNumberAndStatusConfirmed(
            @Param("tripId") int tripId,
            @Param("seatNumber") String seatNumber
    );

    @Query("SELECT bd.seatNumber " +
           "FROM BookingDetail bd JOIN bd.booking b " +
           "WHERE b.trip.tripId = :tripId AND b.status = :status")
    Set<String> findSeatNumbersByTripIdAndStatus(
            @Param("tripId") int tripId,
            @Param("status") String status
    );

    @Query("SELECT COUNT(bd) > 0 " +
           "FROM BookingDetail bd JOIN bd.booking b " +
           "WHERE b.trip.tripId = :tripId AND bd.seatNumber = :seatNumber AND b.status = :status")
    boolean existsByTripIdAndSeatNumberAndStatus(
            @Param("tripId") int tripId,
            @Param("seatNumber") String seatNumber,
            @Param("status") String status
    );

    @Query("SELECT bd.seatNumber " +
           "FROM BookingDetail bd JOIN bd.booking b " +
           "WHERE b.trip.tripId = :tripId " +
           "AND bd.seatNumber IN :seatNumbers " +
           "AND (b.status = 'CONFIRMED' OR b.status = 'PENDING')")
    Set<String> findDuplicateSeats(
            @Param("tripId") int tripId,
            @Param("seatNumbers") Set<String> seatNumbers
    );

    @Query("SELECT bd.seatNumber " +
           "FROM BookingDetail bd JOIN bd.booking b " +
           "WHERE b.trip.tripId = :tripId AND (b.status = 'CONFIRMED' OR b.status = 'PENDING')")
    Set<String> findAllBookedAndPendingSeatsByTripId(@Param("tripId") int tripId);

    @Query("SELECT COUNT(bd) FROM BookingDetail bd JOIN bd.booking b WHERE b.status = 'CONFIRMED'")
    long countTotalTicketsSoldConfirmed();
}