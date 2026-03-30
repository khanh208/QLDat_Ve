package com.example.QLDatVe.repositories;

import com.example.QLDatVe.dtos.TopRouteDTO;
import com.example.QLDatVe.entities.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Integer> {

    /**
     * SỬA LẠI: Thêm điều kiện "departureTime > :now"
     */
    @Query("SELECT t FROM Trip t WHERE t.route.startLocation = :startLocation " +
           "AND t.route.endLocation = :endLocation " +
           "AND t.departureTime >= :startOfDay " + // startOfDay (đã bao gồm 'now' nếu là ngày hôm nay)
           "AND t.departureTime < :endOfDay " +
           "AND t.departureTime > :now") // <-- Lọc các chuyến đã chạy trong ngày
    List<Trip> searchTripsOptimized(
            @Param("startLocation") String startLocation,
            @Param("endLocation") String endLocation,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("now") LocalDateTime now
    );
    
    /**
     * HÀM MỚI: Lấy tất cả các chuyến đi CÒN CHẠY (chưa khởi hành)
     */
    @Query("SELECT t FROM Trip t WHERE t.departureTime > :now ORDER BY t.departureTime ASC")
    List<Trip> findAllAvailable(@Param("now") LocalDateTime now);

    // --- (Hàm findTopRoutes giữ nguyên) ---
    @Query("SELECT new com.example.QLDatVe.dtos.TopRouteDTO(CONCAT(r.startLocation, ' - ', r.endLocation), COUNT(bd.id)) " +
           "FROM BookingDetail bd " +
           "JOIN bd.booking b " +
           "JOIN b.trip t " +
           "JOIN t.route r " +
           "WHERE b.status = 'CONFIRMED' " +
           "GROUP BY r.startLocation, r.endLocation " +
           "ORDER BY COUNT(bd.id) DESC " +
           "LIMIT 5")
    List<TopRouteDTO> findTopRoutes();
}