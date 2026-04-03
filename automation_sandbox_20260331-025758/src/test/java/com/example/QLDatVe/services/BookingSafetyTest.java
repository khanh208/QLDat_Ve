package com.example.QLDatVe.services;

import com.example.QLDatVe.dtos.BookingRequest;
import com.example.QLDatVe.entities.Booking;
import com.example.QLDatVe.entities.Route;
import com.example.QLDatVe.entities.Trip;
import com.example.QLDatVe.entities.User;
import com.example.QLDatVe.repositories.BookingDetailRepository;
import com.example.QLDatVe.repositories.BookingRepository;
import com.example.QLDatVe.repositories.TripRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BookingSafetyTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingDetailRepository bookingDetailRepository;
    @Mock private TripRepository tripRepository;
    @Mock private EmailService emailService;

    private Trip validTrip;
    private User validUser;

    @Before
    public void init() {
        // Setup dữ liệu chuẩn để test logic chạy thật
        validUser = new User();
        validUser.setUserId(1);
        validUser.setUsername("test-user");
        validUser.setEmail("test@email.com");
        validUser.setFullName("Test User");
        validUser.setRole("ROLE_USER");
        validUser.setEnabled(true);

        Route route = new Route();
        route.setStartLocation("Hanoi");
        route.setEndLocation("Da Nang");

        validTrip = new Trip();
        validTrip.setTripId(1);
        validTrip.setBasePrice(BigDecimal.valueOf(100000));
        validTrip.setDepartureTime(LocalDateTime.now().plusDays(1));
        validTrip.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(6));
        validTrip.setRoute(route);
    }

    @After
    public void tearDown() {
        validTrip = null;
    }

    // =========================================================================
    // NHÓM SAFETY RACE (ĐA LUỒNG): ĐẠI DIỆN CHO RULE 1 VÀ RULE 2
    // -> Khi chạy sẽ ra kết quả FAILED (Vì phát hiện ra Bug hệ thống)
    // =========================================================================

    // --- CASE 1: 2 NGƯỜI ĐẶT 1 GHẾ - TIỀN MẶT (Ánh xạ RULE 1) ---
    @Test
    public void testSafe_RaceCondition_CASH_OneSeat() throws InterruptedException {
        System.out.println("--- Testing: 2 Users book same seat (CASH) ---");
        setupMockForSuccess(); // Giả lập DB ban đầu trống

        BookingRequest req = new BookingRequest();
        req.setTripId(1);
        req.setSeatNumbers(Arrays.asList("A01"));
        req.setPaymentMethod("CASH");

        runConcurrentTest(req, 2); // KHI CHẠY, HÀM NÀY SẼ BÁO FAILED VÌ LỖI DOUBLE BOOKING
    }

    // --- CASE 2: 2 NGƯỜI ĐẶT 1 GHẾ - MOMO (Ánh xạ RULE 2) ---
    @Test
    public void testSafe_RaceCondition_MOMO_OneSeat() throws InterruptedException {
        System.out.println("--- Testing: 2 Users book same seat (MOMO) ---");
        setupMockForSuccess();

        BookingRequest req = new BookingRequest();
        req.setTripId(1);
        req.setSeatNumbers(Arrays.asList("A02"));
        req.setPaymentMethod("MOMO");

        runConcurrentTest(req, 2); // KHI CHẠY, HÀM NÀY SẼ BÁO FAILED VÌ BUG API
    }

    // =========================================================================
    // NHÓM SAFETY SEQUENTIAL: ĐẠI DIỆN CHO RULE 5, 6, 7, 8
    // -> Dữ liệu đúng nhưng ghế đã bán, phải chặn lại (PASSED)
    // =========================================================================

    // --- CASE 3: CHẶN ĐẶT GHẾ ĐÃ CÓ NGƯỜI MUA (Ánh xạ R5, R7) ---
    @Test(expected = RuntimeException.class)
    public void testSafe_Sequential_SeatAlreadySold() {
        BookingRequest req = new BookingRequest();
        req.setTripId(1);
        req.setSeatNumbers(Arrays.asList("A01"));

        when(tripRepository.findById(1)).thenReturn(Optional.of(validTrip));
        // Mấu chốt: DB báo về ghế A01 ĐÃ CÓ người (Set không rỗng)
        when(bookingDetailRepository.findDuplicateSeats(anyInt(), anySet()))
                .thenReturn(new HashSet<>(Collections.singletonList("A01")));

        // Thực thi logic: Code phải ném lỗi ngay lập tức
        bookingService.createBooking(req, validUser);
    }

    // --- CASE 7: 2 NGƯỜI ĐẶT NHIỀU VÉ - CÓ TRÙNG (Ánh xạ R6, R8) ---
    @Test
    public void testSafe_RaceCondition_OverlapSeats() throws InterruptedException {
        System.out.println("--- Testing: Overlap Booking ([D01, D02] vs [D02, D03]) ---");
        setupMockForSuccess();

        // Thread 1: User A
        Runnable taskA = () -> {
            BookingRequest reqA = new BookingRequest();
            reqA.setTripId(1);
            reqA.setSeatNumbers(Arrays.asList("D01", "D02"));
            reqA.setPaymentMethod("CASH");
            try { bookingService.createBooking(reqA, validUser); } catch (Exception e) {}
        };

        // Thread 2: User B
        Runnable taskB = () -> {
            BookingRequest reqB = new BookingRequest();
            reqB.setTripId(1);
            reqB.setSeatNumbers(Arrays.asList("D02", "D03")); // Trùng D02
            reqB.setPaymentMethod("CASH");
            try { bookingService.createBooking(reqB, buildUser(2, "User B")); } catch (Exception e) {}
        };

        runCustomRace(taskA, taskB); // KHI CHẠY SẼ BÁO FAILED NẾU CODE CHO LƯU TRÙNG
    }

    // =========================================================================
    // NHÓM FUNCTIONAL (HAPPY PATH): ĐẠI DIỆN CHO RULE 3 VÀ RULE 4
    // -> Giao dịch bình thường, hệ thống xử lý đúng (PASSED)
    // =========================================================================

    // --- CASE 4: ĐẶT VÉ THÀNH CÔNG - TIỀN MẶT (Ánh xạ RULE 3) ---
    @Test
    public void testFunc_Success_CASH() {
        setupMockForSuccess();
        BookingRequest req = new BookingRequest();
        req.setTripId(1);
        req.setSeatNumbers(Arrays.asList("B01"));
        req.setPaymentMethod("CASH");

        Booking result = bookingService.createBooking(req, validUser);

        Assert.assertEquals("CONFIRMED", result.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    // --- CASE 5: ĐẶT VÉ THÀNH CÔNG - MOMO (Ánh xạ RULE 4) ---
    @Test
    public void testFunc_Success_MOMO() {
        setupMockForSuccess();
        BookingRequest req = new BookingRequest();
        req.setTripId(1);
        req.setSeatNumbers(Arrays.asList("B02"));
        req.setPaymentMethod("MOMO");

        Booking result = bookingService.createBooking(req, validUser);

        Assert.assertEquals("PENDING", result.getStatus());
    }

    // --- CASE 6: 1 NGƯỜI ĐẶT NHIỀU VÉ ---
    @Test
    public void testFunc_OneUser_MultipleSeats() {
        setupMockForSuccess();
        BookingRequest req = new BookingRequest();
        req.setTripId(1);
        req.setSeatNumbers(Arrays.asList("C01", "C02", "C03")); // 3 ghế
        req.setPaymentMethod("CASH");

        Booking result = bookingService.createBooking(req, validUser);

        Assert.assertEquals("CONFIRMED", result.getStatus());
        Assert.assertEquals(0, BigDecimal.valueOf(300000).compareTo(result.getTotalAmount()));
        Assert.assertEquals(3, result.getBookingDetails().size());
    }

    // --- CASE 8: CRON JOB TỰ ĐỘNG HỦY VÉ QUÁ HẠN ---
    @Test
    public void testFunc_CronJob_AutoCancel() {
        bookingService.checkAndCancelExpiredBookings();
        verify(bookingRepository, times(1)).cancelExpiredPendingBookings(any(LocalDateTime.class));
    }

    // --- HÀM HỖ TRỢ BÊN DƯỚI (GIỮ NGUYÊN GỐC) ---
    private void setupMockForSuccess() {
        when(tripRepository.findById(1)).thenReturn(Optional.of(validTrip));
        when(bookingDetailRepository.findDuplicateSeats(anyInt(), anySet())).thenReturn(new HashSet<>());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));
    }

    private void runConcurrentTest(BookingRequest req, int threads) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            final int userId = i + 1;
            executor.submit(() -> {
                try {
                    bookingService.createBooking(req, buildUser(userId, "Concurrent User " + userId));
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(2, TimeUnit.SECONDS);

        // ĐÂY LÀ CHỖ QUYẾT ĐỊNH ĐỎ HAY XANH: 
        // Nếu Bug xảy ra, save() chạy 2 lần -> Báo FAILED.
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    private void runCustomRace(Runnable t1, Runnable t2) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        executor.submit(() -> { t1.run(); latch.countDown(); });
        executor.submit(() -> { t2.run(); latch.countDown(); });
        latch.await(2, TimeUnit.SECONDS);

        try {
            verify(bookingRepository, times(1)).save(any(Booking.class));
        } catch (AssertionError e) {
            System.err.println("!!! BUG DETECTED: Overlap Booking Failed - Cả 2 người đều mua được ghế trùng nhau !!!");
            throw e;
        } 
    }

    private User buildUser(int userId, String fullName) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername("user" + userId);
        user.setFullName(fullName);
        user.setEmail("user" + userId + "@example.com");
        user.setRole("ROLE_USER");
        user.setEnabled(true);
        return user;
    }
}
