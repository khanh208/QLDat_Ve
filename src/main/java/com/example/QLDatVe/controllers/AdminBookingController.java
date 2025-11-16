package com.example.QLDatVe.controllers;

import com.example.QLDatVe.entities.Booking;
import com.example.QLDatVe.services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import PreAuthorize
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingService bookingService;

    /**
     * API lấy tất cả vé đã đặt (cho admin)
     * URL: GET /api/admin/bookings
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')") // Sửa: Dùng hasAuthority hoặc hasRole('ROLE_ADMIN')
    public ResponseEntity<List<Booking>> getAllBookings() {
        // Sửa: Gọi hàm mới đã tối ưu và sắp xếp
        List<Booking> bookings = bookingService.getAllBookingsSorted(); 
        return ResponseEntity.ok(bookings);
    }

    /**
     * API lấy chi tiết một booking theo ID
     * URL: GET /api/admin/bookings/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')") // Sửa: Dùng hasAuthority
    public ResponseEntity<?> getBookingById(@PathVariable("id") int bookingId) {
        try {
            Booking booking = bookingService.getBookingById(bookingId);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * API hủy vé (admin có quyền hủy mọi vé)
     * URL: PUT /api/admin/bookings/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('ADMIN')") // Sửa: Dùng hasAuthority
    public ResponseEntity<?> cancelBooking(@PathVariable("id") int bookingId) {
        try {
            Booking cancelledBooking = bookingService.cancelBookingByAdmin(bookingId);
            return ResponseEntity.ok(cancelledBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}