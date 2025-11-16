package com.example.QLDatVe.services;

// --- TẤT CẢ IMPORT CẦN THIẾT ---
import com.example.QLDatVe.dtos.BookingRequest;
import com.example.QLDatVe.entities.Booking;
import com.example.QLDatVe.entities.BookingDetail;
import com.example.QLDatVe.entities.Trip;
import com.example.QLDatVe.entities.User;
import com.example.QLDatVe.repositories.BookingDetailRepository;
import com.example.QLDatVe.repositories.BookingRepository;
import com.example.QLDatVe.repositories.TripRepository;
import com.example.QLDatVe.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
// -----------------------------------

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public Set<String> getBookedSeatsForTrip(int tripId) {
        return bookingDetailRepository.findAllBookedAndPendingSeatsByTripId(tripId);
    }

    public List<String> checkDuplicateSeats(int tripId, Set<String> seatNumbers) {
        Set<String> duplicates = bookingDetailRepository.findDuplicateSeats(tripId, seatNumbers);
        return new ArrayList<>(duplicates);
    }

    @Transactional
    public Booking createBooking(BookingRequest request, User user) {
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Trip"));
                
        if (trip.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Chuyến đi này đã khởi hành. Không thể đặt vé.");
        }

        Set<String> requestedSeats = new HashSet<>(request.getSeatNumbers());
        if (requestedSeats.isEmpty()) { 
            throw new RuntimeException("Vui lòng chọn ít nhất một ghế."); 
        }
        List<String> duplicateSeats = checkDuplicateSeats(request.getTripId(), requestedSeats);
        
        if (!duplicateSeats.isEmpty()) {
            throw new RuntimeException("Các ghế sau đã có người chọn hoặc đang chờ thanh toán: " + String.join(", ", duplicateSeats));
        }

        Booking booking = new Booking();
        booking.setTrip(trip);
        booking.setUser(user);

        int numberOfSeats = request.getSeatNumbers().size();
        BigDecimal totalAmount = trip.getBasePrice().multiply(new BigDecimal(numberOfSeats));
        booking.setTotalAmount(totalAmount);

        if ("CASH".equalsIgnoreCase(request.getPaymentMethod())) {
            booking.setStatus("CONFIRMED");
        } else {
            booking.setStatus("PENDING");
        }
        booking.setMomoOrderId(null);
        booking.setMomoRequestId(null);

        List<BookingDetail> details = new ArrayList<>();
        for (String seat : request.getSeatNumbers()) {
            BookingDetail detail = new BookingDetail();
            detail.setBooking(booking);
            detail.setSeatNumber(seat);
            details.add(detail);
        }
        booking.setBookingDetails(details);

        Booking savedBooking = bookingRepository.save(booking);
        logger.info("Booking ID {} đã được tạo với trạng thái {}.", savedBooking.getBookingId(), savedBooking.getStatus());

        if ("CONFIRMED".equals(savedBooking.getStatus()) && "CASH".equalsIgnoreCase(request.getPaymentMethod())) {
            try {
                sendBookingConfirmationEmail(savedBooking, true);
            } catch (Exception e) {
                logger.error("Lỗi khi gửi email xác nhận (Tiền mặt) cho Booking ID {}: {}", savedBooking.getBookingId(), e.getMessage());
            }
        }

        return savedBooking;
    }

    public void sendBookingConfirmationEmail(Booking booking, boolean isCashPayment) {
        // --- SỬA LỖI "final" TẠI ĐÂY ---
        // Gán 'booking' cho một biến mới (effectively final)
        Booking bookingToSend = booking;
        
        if (bookingToSend.getUser() == null || bookingToSend.getTrip() == null || bookingToSend.getTrip().getRoute() == null || bookingToSend.getBookingDetails() == null) {
            logger.warn("Không thể gửi email cho Booking ID {}: Thiếu dữ liệu liên quan. Đang thử tải lại...", bookingToSend.getBookingId());
             
             final Integer bookingIdToReload = bookingToSend.getBookingId(); // Biến final
             Booking reloadedBooking = bookingRepository.findAllWithDetails().stream()
                .filter(b -> b.getBookingId().equals(bookingIdToReload)) // Sử dụng biến final
                .findFirst()
                .orElse(null);
             
             if(reloadedBooking == null || reloadedBooking.getUser() == null) {
                 logger.error("Không thể gửi email cho Booking ID {}: Vẫn thiếu dữ liệu sau khi tải lại.", bookingIdToReload);
                 return;
             }
             bookingToSend = reloadedBooking; // Gán lại
        }
        // --- HẾT SỬA LỖI ---

        User user = bookingToSend.getUser();
        Trip trip = bookingToSend.getTrip();
        String seatNumbers = bookingToSend.getBookingDetails().stream().map(BookingDetail::getSeatNumber).collect(Collectors.joining(", "));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy");
        String subject = "Xác nhận đặt vé thành công - Mã vé #" + bookingToSend.getBookingId();

        String paymentInfo;
        if (isCashPayment) {
            paymentInfo = String.format(
                "Phương thức thanh toán: Tiền mặt\n" +
                "Trạng thái: ĐÃ XÁC NHẬN\n"+
                "Vui lòng thanh toán số tiền %,.0f VND cho nhà xe khi lên xe.",
                bookingToSend.getTotalAmount()
            );
        } else {
            paymentInfo = String.format(
                "Phương thức thanh toán: MoMo\n" +
                "Trạng thái: ĐÃ THANH TOÁN (Mã GD MoMo: %s)",
                 bookingToSend.getMomoOrderId() != null ? bookingToSend.getMomoOrderId() : "N/A"
            );
        }

        String body = String.format(
            "Kính chào %s,\n\n" +
            "Cảm ơn bạn đã đặt vé tại hệ thống của chúng tôi!\n\n" +
            "Thông tin chi tiết vé:\n" +
            "- Mã vé: #%d\n" +
            "- Tuyến đường: %s → %s\n" +
            "- Thời gian khởi hành: %s\n" +
            "- Thời gian dự kiến đến: %s\n" +
            "- Ghế đã đặt: %s\n" +
            "- Tổng tiền: %,.0f VND\n" +
            "- %s\n\n" +
            "Lưu ý:\n" +
            "- Vui lòng đến trước 30 phút để làm thủ tục lên xe\n" +
            "- Mang theo CMND/CCCD để đối chiếu khi cần\n" +
            "- Vé này đã được xác nhận và hợp lệ\n\n" +
            "Trân trọng,\n" +
            "Đội ngũ hỗ trợ hệ thống đặt vé",
            user.getFullName(), 
            bookingToSend.getBookingId(),
            trip.getRoute().getStartLocation(), 
            trip.getRoute().getEndLocation(),
            trip.getDepartureTime().format(formatter), 
            trip.getArrivalTime().format(formatter),
            seatNumbers, 
            bookingToSend.getTotalAmount(), 
            paymentInfo
        );

        emailService.sendEmail(user.getEmail(), subject, body);
        logger.info("✅ Confirmation email sent cho Booking ID: {}", bookingToSend.getBookingId());
    }

    public List<Booking> getMyBookings(User user) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("Thông tin người dùng không hợp lệ.");
        }
        try {
            List<Booking> bookings = bookingRepository.findByUserWithDetails(user.getUserId());
            logger.info("📊 User ID: {} | Email: {} | Found {} bookings", 
                         user.getUserId(), user.getEmail(), bookings.size());
            return bookings;
        } catch (Exception e) {
            logger.error("❌ Error fetching bookings cho user {}: {}", user.getUserId(), e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    public List<Booking> getAllBookingsSorted() {
        try {
            List<Booking> bookings = bookingRepository.findAllWithDetails();
            logger.info("✅ Admin: Loaded {} bookings with all details", bookings.size());
            return bookings;
        } catch (Exception e) {
            logger.error("❌ Error fetching all bookings: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public Booking getBookingById(int bookingId) {
       return bookingRepository.findById(bookingId)
               .orElseThrow(() -> new RuntimeException("Không tìm thấy Booking với ID: " + bookingId));
    }

    @Transactional
    public Booking cancelBooking(int bookingId, User user) {
        if (user == null || user.getUserId() == null) { 
             throw new IllegalArgumentException("Thông tin người dùng không hợp lệ.");
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Booking với ID: " + bookingId));

        if (!booking.getUser().getUserId().equals(user.getUserId())) { 
            throw new RuntimeException("Bạn không có quyền hủy vé này!");
        }
        
        if (booking.getTrip().getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Không thể hủy vé. Chuyến đi này đã khởi hành.");
        }

        if ("CANCELLED".equals(booking.getStatus())) { 
            throw new RuntimeException("Vé này đã được hủy trước đó.");
        }

        if ("PENDING".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus())) {
            booking.setStatus("CANCELLED");
            Booking cancelled = bookingRepository.save(booking);
            logger.info("✅ User {} cancelled booking #{}", user.getEmail(), bookingId);
            return cancelled;
        } else { 
            throw new RuntimeException("Không thể hủy vé ở trạng thái hiện tại: " + booking.getStatus());
        }
    }

    @Transactional
    public Booking cancelBookingByAdmin(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Booking với ID: " + bookingId));

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new RuntimeException("Vé này đã được hủy trước đó.");
        }
        
        if ("PENDING".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus())) {
            booking.setStatus("CANCELLED");
            Booking cancelled = bookingRepository.save(booking);
            logger.info("✅ Admin cancelled booking #{}", bookingId);
            return cancelled;
        } else {
            throw new RuntimeException("Không thể hủy vé ở trạng thái hiện tại: " + booking.getStatus());
        }
    }

    @Transactional
    public void confirmBookingAfterPayment(String momoOrderId, String momoRequestId) {
        Optional<Booking> bookingOpt = bookingRepository.findByMomoOrderIdAndMomoRequestId(momoOrderId, momoRequestId);
        
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            
            if ("PENDING".equals(booking.getStatus())) {
                booking.setStatus("CONFIRMED");
                Booking confirmedBooking = bookingRepository.save(booking);
                
                Booking bookingWithDetails = bookingRepository.findAllWithDetails().stream()
                        .filter(b -> b.getBookingId().equals(confirmedBooking.getBookingId()))
                        .findFirst()
                        .orElse(confirmedBooking);

                logger.info("✅ Booking ID {} confirmed after MoMo payment (OrderID: {})", 
                             confirmedBooking.getBookingId(), momoOrderId);
                
                try {
                    sendBookingConfirmationEmail(bookingWithDetails, false); // isCashPayment = false
                } catch (Exception e) {
                    logger.error("❌ Error sending MoMo confirmation email cho Booking ID {}: {}", 
                                  confirmedBooking.getBookingId(), e.getMessage(), e);
                }
            } else {
                logger.warn("⚠️ Booking {} was not PENDING when trying to confirm. Current status: {}", 
                             booking.getBookingId(), booking.getStatus());
            }
        } else {
            logger.error("❌ Could not find booking with MoMo OrderID: {} and RequestID: {}", 
                          momoOrderId, momoRequestId);
        }
    }
    
    @Scheduled(fixedRate = 60000) // Chạy mỗi 60 giây
    @Transactional
    public void checkAndCancelExpiredBookings() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(15); // Hết hạn sau 15 phút
        
        try {
            int cancelledCount = bookingRepository.cancelExpiredPendingBookings(expiryTime);
            if (cancelledCount > 0) {
                logger.info("✅ Scheduled Task: Cancelled {} expired PENDING bookings older than {}.", 
                             cancelledCount, expiryTime);
            }
        } catch (Exception e) {
             logger.error("❌ Error during scheduled task 'checkAndCancelExpiredBookings': {}", e.getMessage(), e);
        }
    }

    public Booking findBookingByMomoIds(String orderId, String requestId) {
        return bookingRepository.findByMomoOrderIdAndMomoRequestId(orderId, requestId)
                .orElse(null);
    }
}