package com.example.QLDatVe.controllers;

import com.example.QLDatVe.dtos.BookingRequest;
import com.example.QLDatVe.entities.Booking;
import com.example.QLDatVe.entities.User;
import com.example.QLDatVe.services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings") // Đường dẫn chung cho việc đặt vé
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Lấy User đang đăng nhập từ Security Context (từ Token)
     */
    private User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Principal chính là đối tượng User (nếu bạn đã implement UserDetails)
        return (User) authentication.getPrincipal(); 
    }

    // 1. API ĐẶT VÉ MỚI
    // URL: POST /api/bookings
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        try {
            User currentUser = getLoggedInUser();
            Booking newBooking = bookingService.createBooking(request, currentUser);
            return ResponseEntity.ok(newBooking);
            
        } catch (RuntimeException e) {
            // Bắt các lỗi như "Ghế đã bị đặt" hoặc "Không tìm thấy chuyến đi"
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. API XEM LỊCH SỬ ĐẶT VÉ CỦA TÔI
    // URL: GET /api/bookings/my-bookings
    @GetMapping("/my-bookings")
public ResponseEntity<List<Booking>> getMyBookings() {
    try {
        User currentUser = getLoggedInUser();
        System.out.println("🔍 User: " + currentUser.getEmail() + " (ID: " + currentUser.getUserId() + ")");
        
        List<Booking> myBookings = bookingService.getMyBookings(currentUser);
        
        System.out.println("✅ Tìm thấy " + myBookings.size() + " bookings");
        
        // Log chi tiết các booking
        myBookings.forEach(b -> 
            System.out.println("  - Booking #" + b.getBookingId() + 
                             " | Status: " + b.getStatus() + 
                             " | Trip: " + b.getTrip().getTripId())
        );
        
        return ResponseEntity.ok(myBookings);
        
    } catch (Exception e) {
        System.err.println("❌ Lỗi khi lấy bookings: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(500).body(null);
    }
}
    // 3. API HỦY VÉ
    // URL: PUT /api/bookings/{id}/cancel
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable("id") int bookingId) {
        try {
            User currentUser = getLoggedInUser();
            Booking cancelledBooking = bookingService.cancelBooking(bookingId, currentUser);
            return ResponseEntity.ok(cancelledBooking);

        } catch (RuntimeException e) {
            // Bắt lỗi "Không tìm thấy" hoặc "Không có quyền"
            if (e.getMessage().contains("Không có quyền")) {
                return ResponseEntity.status(403).body(e.getMessage()); // 403 Forbidden
            }
            return ResponseEntity.status(404).body(e.getMessage()); // 404 Not Found
        }
    }
}