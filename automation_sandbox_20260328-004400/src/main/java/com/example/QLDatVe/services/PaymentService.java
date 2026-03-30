package com.example.QLDatVe.services;

import com.example.QLDatVe.entities.Booking;
import com.example.QLDatVe.repositories.BookingRepository;
import com.example.QLDatVe.services.BookingService; // ⭐ THÊM IMPORT NÀY
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class PaymentService {

    @Value("${momo.partnerCode}")
    private String partnerCode;
    @Value("${momo.accessKey}")
    private String accessKey;
    @Value("${momo.secretKey}")
    private String secretKey;
    @Value("${momo.api.endpoint}")
    private String momoApiEndpoint;
    @Value("${momo.redirectUrl}")
    private String redirectUrl;
    @Value("${momo.ipnUrl}")
    private String ipnUrl;

    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate;
    private final BookingService bookingService; // ⭐ THÊM DEPENDENCY NÀY

    // ⭐ SỬA CONSTRUCTOR: Thêm BookingService
    public PaymentService(BookingRepository bookingRepository, RestTemplate restTemplate, BookingService bookingService) {
        this.bookingRepository = bookingRepository;
        this.restTemplate = restTemplate;
        this.bookingService = bookingService; // ⭐ THÊM DÒNG NÀY
    }

    @PostConstruct
    public void logConfig() {
        System.out.println("🟡 MoMo Config Loaded:");
        System.out.println("  partnerCode: " + partnerCode);
        System.out.println("  accessKey: " + accessKey);
        System.out.println("  api.endpoint: " + momoApiEndpoint);
        System.out.println("  redirectUrl: " + redirectUrl);
        System.out.println("  ipnUrl: " + ipnUrl);
        
        // ⭐ KIỂM TRA XEM CONFIG CÓ HỢP LỆ KHÔNG
        if (partnerCode == null || partnerCode.isEmpty() || 
            accessKey == null || accessKey.isEmpty() ||
            secretKey == null || secretKey.isEmpty()) {
            System.err.println("❌ MoMo configuration is missing or incomplete!");
        }
    }

    /**
     * Creates a MoMo Payment Request for a given booking.
     */
    public Map<String, Object> createMoMoPayment(int bookingId) throws Exception { // ⭐ ĐỔI RETURN TYPE THÀNH Map
        System.out.println("🟡 Creating MoMo payment for booking: " + bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        if (!"PENDING".equals(booking.getStatus())) {
            throw new RuntimeException("Booking " + bookingId + " is not in PENDING state. Current status: " + booking.getStatus());
        }

        String requestId = UUID.randomUUID().toString();
        String orderId = partnerCode + "_" + booking.getBookingId() + "_" + System.currentTimeMillis();
        long amount = booking.getTotalAmount().longValue();
        String orderInfo = "Thanh toan ve xe #" + booking.getBookingId() + " (" + booking.getUser().getUsername() + ")";
        String requestType = "captureWallet";
        String extraData = "";

        // ⭐ FIX: Sử dụng đúng format raw signature theo MoMo docs
        String rawSignature = "accessKey=" + accessKey +
                              "&amount=" + amount +
                              "&extraData=" + extraData +
                              "&ipnUrl=" + ipnUrl +
                              "&orderId=" + orderId +
                              "&orderInfo=" + orderInfo +
                              "&partnerCode=" + partnerCode +
                              "&redirectUrl=" + redirectUrl +
                              "&requestId=" + requestId +
                              "&requestType=" + requestType;

        System.out.println("🔐 MoMo Raw Signature Data: " + rawSignature);
        String signature = generateHmacSHA256(rawSignature, secretKey);
        System.out.println("🔐 MoMo Calculated Signature: " + signature);

        // --- Prepare Request Body ---
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("partnerCode", partnerCode);
        requestBody.put("accessKey", accessKey);
        requestBody.put("requestId", requestId);
        requestBody.put("amount", amount); // ⭐ FIX: Gửi amount như number, không phải string
        requestBody.put("orderId", orderId);
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("redirectUrl", redirectUrl);
        requestBody.put("ipnUrl", ipnUrl);
        requestBody.put("lang", "vi");
        requestBody.put("extraData", extraData);
        requestBody.put("requestType", requestType);
        requestBody.put("signature", signature);
        requestBody.put("autoCapture", true); // ⭐ THÊM: Auto capture

        // --- Call MoMo API ---
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        System.out.println("📤 Calling MoMo API Endpoint: " + momoApiEndpoint);
        System.out.println("📤 MoMo Request Body: " + requestBody);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(momoApiEndpoint, entity, Map.class);
            System.out.println("📥 MoMo Response Status: " + response.getStatusCode());
            System.out.println("📥 MoMo Response Body: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Integer resultCode = getIntegerFromResult(responseBody, "resultCode");

                if (resultCode != null && resultCode == 0) {
                    // ⭐ FIX: Cập nhật booking với MoMo info
                    booking.setMomoOrderId(orderId);
                    booking.setMomoRequestId(requestId);
                    bookingRepository.save(booking);
                    System.out.println("✅ MoMo order info saved for booking: " + bookingId);

                    String payUrl = (String) responseBody.get("payUrl");
                    if (payUrl == null || payUrl.isEmpty()) {
                        payUrl = (String) responseBody.get("deeplink");
                    }
                    
                    if (payUrl == null || payUrl.isEmpty()) {
                        throw new RuntimeException("MoMo API Error: payUrl/deeplink is missing");
                    }

                    System.out.println("✅ MoMo Payment URL generated: " + payUrl);
                    
                    // ⭐ FIX: Trả về cả payUrl và orderId để frontend tracking
                    Map<String, Object> result = new HashMap<>();
                    result.put("payUrl", payUrl);
                    result.put("orderId", orderId);
                    result.put("requestId", requestId);
                    return result;

                } else {
                    String message = (String) responseBody.get("message");
                    System.err.println("❌ MoMo API returned error. ResultCode: " + resultCode + ", Message: " + message);
                    throw new RuntimeException("MoMo API Error: " + message + " (ResultCode: " + resultCode + ")");
                }
            } else {
                throw new RuntimeException("Failed to call MoMo API. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Error calling MoMo API: " + e.getMessage());
            throw new RuntimeException("MoMo API call failed: " + e.getMessage());
        }
    }

    /**
     * Handles the Instant Payment Notification (IPN) callback from MoMo.
     */
    @Transactional
    public void handleMomoIPN(Map<String, Object> payload) {
        System.out.println("🟡 Received MoMo IPN: " + payload);

        try {
            // ⭐ FIX: Extract data với null checks
            String partnerCodeIPN = (String) payload.get("partnerCode");
            String orderIdIPN = (String) payload.get("orderId");
            String requestIdIPN = (String) payload.get("requestId");
            Long amountIPN = getLongFromResult(payload, "amount");
            String orderInfoIPN = (String) payload.get("orderInfo");
            String orderTypeIPN = (String) payload.get("orderType");
            Long transIdIPN = getLongFromResult(payload, "transId");
            Integer resultCodeIPN = getIntegerFromResult(payload, "resultCode");
            String messageIPN = (String) payload.get("message");
            String payTypeIPN = (String) payload.get("payType");
            Long responseTimeIPN = getLongFromResult(payload, "responseTime");
            String extraDataIPN = (String) payload.get("extraData");
            String signatureIPN = (String) payload.get("signature");

            // ⭐ FIX: Validate required fields
            if (orderIdIPN == null || requestIdIPN == null || resultCodeIPN == null || signatureIPN == null) {
                System.err.println("❌ MoMo IPN Error: Missing required fields");
                return; // Không throw exception, chỉ log và return
            }

            // ⭐ FIX: Verify Signature - Sử dụng đúng format IPN signature
            String rawSignature = "accessKey=" + accessKey +
                                  "&amount=" + (amountIPN != null ? amountIPN : "") +
                                  "&extraData=" + (extraDataIPN != null ? extraDataIPN : "") +
                                  "&message=" + (messageIPN != null ? messageIPN : "") +
                                  "&orderId=" + orderIdIPN +
                                  "&orderInfo=" + (orderInfoIPN != null ? orderInfoIPN : "") +
                                  "&orderType=" + (orderTypeIPN != null ? orderTypeIPN : "") +
                                  "&partnerCode=" + partnerCodeIPN +
                                  "&payType=" + (payTypeIPN != null ? payTypeIPN : "") +
                                  "&requestId=" + requestIdIPN +
                                  "&responseTime=" + (responseTimeIPN != null ? responseTimeIPN : "") +
                                  "&resultCode=" + resultCodeIPN +
                                  "&transId=" + (transIdIPN != null ? transIdIPN : "");

            System.out.println("🔐 MoMo IPN Raw Signature Data: " + rawSignature);
            String calculatedSignature = generateHmacSHA256(rawSignature, secretKey);
            System.out.println("🔐 MoMo IPN Calculated Signature: " + calculatedSignature);
            System.out.println("🔐 MoMo IPN Received Signature: " + signatureIPN);

            if (!calculatedSignature.equals(signatureIPN)) { // ⭐ FIX: Không dùng equalsIgnoreCase
                System.err.println("❌ MoMo IPN Signature Verification Failed!");
                return; // Không throw exception, chỉ return
            }
            System.out.println("✅ MoMo IPN Signature Verification Successful!");

            // ⭐ FIX: Tìm booking bằng orderId và requestId
            Optional<Booking> bookingOpt = bookingRepository.findByMomoOrderIdAndMomoRequestId(orderIdIPN, requestIdIPN);

            if (bookingOpt.isEmpty()) {
                System.err.println("❌ MoMo IPN Error: Booking not found for orderId: " + orderIdIPN + ", requestId: " + requestIdIPN);
                
                // ⭐ DEBUG: In tất cả bookings để kiểm tra
                List<Booking> allBookings = bookingRepository.findAll();
                System.out.println("🔍 All bookings with MoMo IDs:");
                for (Booking b : allBookings) {
                    System.out.println(" - Booking ID: " + b.getBookingId() + 
                                     ", MoMoOrderId: " + b.getMomoOrderId() + 
                                     ", MoMoRequestId: " + b.getMomoRequestId() +
                                     ", Status: " + b.getStatus());
                }
                return;
            }

            Booking booking = bookingOpt.get();
            System.out.println("🟡 Found booking: " + booking.getBookingId() + ", Current status: " + booking.getStatus());

            // ⭐ FIX: Kiểm tra trạng thái hiện tại
            if ("CONFIRMED".equals(booking.getStatus())) {
                System.out.println("ℹ️ Booking " + booking.getBookingId() + " already confirmed");
                return;
            }

            if (resultCodeIPN == 0) { // Payment Success
                System.out.println("✅ MoMo IPN Success for Booking ID: " + booking.getBookingId());
                
                // ⭐ FIX: Sử dụng BookingService để confirm booking (để gửi email)
                bookingService.confirmBookingAfterPayment(orderIdIPN, requestIdIPN);
                System.out.println("✅ Booking ID " + booking.getBookingId() + " status updated to CONFIRMED via IPN.");
                
            } else { // Payment Failed
                System.out.println("❌ MoMo IPN Failed for Booking ID: " + booking.getBookingId() + ". ResultCode: " + resultCodeIPN + ", Reason: " + messageIPN);
                booking.setStatus("FAILED");
                bookingRepository.save(booking);
                System.out.println("❌ Booking ID " + booking.getBookingId() + " status updated to FAILED.");
            }

        } catch (Exception e) {
            System.err.println("❌ Error processing MoMo IPN: " + e.getMessage());
            e.printStackTrace();
            // ⭐ QUAN TRỌNG: Không throw exception ở đây để MoMo không gửi lại IPN
        }
    }

    // ⭐ THÊM PHƯƠNG THỨC MỚI: Kiểm tra trạng thái thanh toán
    public Map<String, Object> checkPaymentStatus(String orderId) {
        try {
            Optional<Booking> bookingOpt = bookingRepository.findByMomoOrderId(orderId);
            if (bookingOpt.isPresent()) {
                Booking booking = bookingOpt.get();
                Map<String, Object> status = new HashMap<>();
                status.put("orderId", orderId);
                status.put("bookingId", booking.getBookingId());
                status.put("status", booking.getStatus());
                status.put("amount", booking.getTotalAmount());
                return status;
            } else {
                throw new RuntimeException("Order not found: " + orderId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error checking payment status: " + e.getMessage());
        }
    }

    // Các helper methods giữ nguyên...
    private String generateHmacSHA256(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private Integer getIntegerFromResult(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try { return Integer.parseInt((String) value); } 
            catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    private Long getLongFromResult(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof String) {
            try { return Long.parseLong((String) value); } 
            catch (NumberFormatException e) { return null; }
        }
        return null;
    }
}