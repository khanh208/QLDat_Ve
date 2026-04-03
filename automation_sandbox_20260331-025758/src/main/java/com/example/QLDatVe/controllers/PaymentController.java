package com.example.QLDatVe.controllers;

import com.example.QLDatVe.entities.Booking; // Thêm import cho hàm debug
import com.example.QLDatVe.services.BookingService;
import com.example.QLDatVe.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Thêm import
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService; // Sửa: Thêm 'final'

    /**
     * Endpoint for Frontend to initiate MoMo Checkout.
     * Yêu cầu xác thực (USER hoặc ADMIN).
     */
    @PostMapping("/momo/checkout/{bookingId}")
    @PreAuthorize("isAuthenticated()") // Bảo vệ endpoint
    public ResponseEntity<?> createMoMoPayment(@PathVariable int bookingId) {
        try {
            System.out.println("🟡 Received MoMo checkout request for booking: " + bookingId);
            
            // Hàm này trong PaymentService trả về Map<String, Object>
            Map<String, Object> paymentResult = paymentService.createMoMoPayment(bookingId);
            
            System.out.println("✅ MoMo payment created successfully: " + paymentResult);
            return ResponseEntity.ok(paymentResult); // Trả về Map
            
        } catch (Exception e) {
            System.err.println("❌ Error creating MoMo payment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể tạo thanh toán MoMo: " + e.getMessage()));
        }
    }

    /**
     * Endpoint to check payment status (nếu bạn có xây dựng hàm này trong PaymentService).
     * Yêu cầu xác thực.
     */
    @GetMapping("/momo/status/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> checkPaymentStatus(@PathVariable String orderId) {
        try {
            // Giả định bạn có hàm 'checkPaymentStatus' trong PaymentService
            Map<String, Object> status = paymentService.checkPaymentStatus(orderId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint for MoMo IPN (Instant Payment Notification).
     * PHẢI được public (permitAll trong SecurityConfig).
     * Nhận Map<String, Object> vì MoMo gửi nhiều kiểu dữ liệu.
     */
    @PostMapping("/momo-ipn")
    public ResponseEntity<Map<String, Object>> handleMoMoIPN(@RequestBody Map<String, Object> payload) {
        try {
            System.out.println("====================================");
            System.out.println("🔔 MoMo IPN Received");
            System.out.println("📦 Full Payload: " + payload);
            System.out.println("====================================");
            
            // PaymentService xử lý logic (xác thực signature, cập nhật CSDL)
            paymentService.handleMomoIPN(payload);
            
            System.out.println("✅ IPN processed successfully");
            
            // QUAN TRỌNG: Phản hồi lại MoMo theo đúng yêu cầu của họ
            // Thường là 200 OK và JSON (hoặc 204 No Content)
            return ResponseEntity.ok(Map.of(
                "resultCode", 0, // Ví dụ: trả về resultCode 0
                "message", "IPN processed successfully"
            ));
            
        } catch (Exception e) {
            System.err.println("❌ IPN Processing Error: " + e.getMessage());
            e.printStackTrace();
            
            // QUAN TRỌNG: Vẫn trả về 200 OK (hoặc theo tài liệu MoMo)
            // để MoMo không gửi lại IPN
            return ResponseEntity.ok(Map.of(
                "resultCode", 1, // Ví dụ: trả về resultCode lỗi
                "message", "IPN received but processing failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Endpoint TEST để kích hoạt xác nhận booking thủ công (dùng debug).
     * Xóa khi deploy!
     */
    @PostMapping("/test/confirm/{orderId}/{requestId}")
    @PreAuthorize("hasAuthority('ADMIN')") // Chỉ Admin được gọi
    public ResponseEntity<?> testConfirmBooking(
            @PathVariable String orderId, 
            @PathVariable String requestId) {
        try {
            System.out.println("🧪 TEST: Manually confirming order: " + orderId);
            
            // Gọi hàm trong BookingService để xác nhận và gửi email
            bookingService.confirmBookingAfterPayment(orderId, requestId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Test confirmation successful",
                "orderId", orderId,
                "requestId", requestId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Endpoint DEBUG để kiểm tra status booking bằng MoMo IDs.
     * Nên bảo vệ hoặc xóa khi deploy.
     */
    @GetMapping("/debug/booking")
    @PreAuthorize("hasAuthority('ADMIN')") // Chỉ Admin được gọi
    public ResponseEntity<?> debugBooking(
            @RequestParam String orderId,
            @RequestParam String requestId) {
        try {
            // Gọi hàm từ BookingService
            Booking booking = bookingService.findBookingByMomoIds(orderId, requestId);
            
            return ResponseEntity.ok(Map.of(
                "found", booking != null,
                "bookingId", booking != null ? booking.getBookingId() : null,
                "status", booking != null ? booking.getStatus() : "NOT_FOUND",
                "orderId", orderId,
                "requestId", requestId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint để Frontend xác nhận thanh toán (thay thế IPN khi test local).
     * Kém an toàn hơn IPN, chỉ dùng khi dev.
     */
    @PostMapping("/momo/confirm")
    @PreAuthorize("isAuthenticated()") // Yêu cầu user đăng nhập
    public ResponseEntity<?> confirmMoMoPaymentFromFrontend(@RequestBody Map<String, Object> data) {
        try {
            System.out.println("====================================");
            System.out.println("🔔 Frontend Confirm Payment Request");
            System.out.println("📦 Data: " + data);
            System.out.println("====================================");
            
            String orderId = (String) data.get("orderId");
            String requestId = (String) data.get("requestId");
            String resultCodeStr = data.get("resultCode") != null ? data.get("resultCode").toString() : null;
            
            if (orderId == null || requestId == null || resultCodeStr == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing required fields: orderId, requestId, resultCode"));
            }
            
            Integer resultCode = Integer.parseInt(resultCodeStr);
            
            if (resultCode == 0) {
                // Thanh toán thành công
                System.out.println("✅ Confirming booking for orderId: " + orderId);
                // Gọi logic xác nhận (giống như IPN)
                bookingService.confirmBookingAfterPayment(orderId, requestId);
                
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Booking confirmed successfully",
                    "orderId", orderId
                ));
            } else {
                // Thanh toán thất bại
                System.out.println("❌ Payment failed for orderId: " + orderId + ", resultCode: " + resultCode);
                // (Tùy chọn: Cập nhật status thành FAILED ở đây)
                return ResponseEntity.ok(Map.of(
                    "status", "failed",
                    "message", "Payment failed",
                    "resultCode", resultCode
                ));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error confirming payment from frontend: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}