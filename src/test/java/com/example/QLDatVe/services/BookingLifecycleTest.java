package com.example.QLDatVe.services;

import com.example.QLDatVe.dtos.BookingRequest;
import com.example.QLDatVe.entities.Booking;
import com.example.QLDatVe.entities.BookingDetail;
import com.example.QLDatVe.entities.Route;
import com.example.QLDatVe.entities.Trip;
import com.example.QLDatVe.entities.User;
import com.example.QLDatVe.repositories.BookingDetailRepository;
import com.example.QLDatVe.repositories.BookingRepository;
import com.example.QLDatVe.repositories.TripRepository;
import com.example.QLDatVe.repositories.UserRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BookingLifecycleTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingDetailRepository bookingDetailRepository;
    @Mock private TripRepository tripRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;

    private Trip validTrip;
    private User validUser;
    private User anotherUser;

    @Before
    public void setUp() {
        Route route = new Route();
        route.setStartLocation("Ha Noi");
        route.setEndLocation("Da Nang");

        validTrip = new Trip();
        validTrip.setTripId(1);
        validTrip.setBasePrice(BigDecimal.valueOf(100000));
        validTrip.setDepartureTime(LocalDateTime.now().plusDays(1));
        validTrip.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(6));
        validTrip.setRoute(route);

        validUser = buildUser(1, "User A");
        anotherUser = buildUser(2, "User B");
    }

    @Test
    public void testLife_CreateBooking_Cash_EmailFailure_DoesNotRollback() {
        stubHappyPath();
        doThrow(new RuntimeException("SMTP unavailable"))
                .when(emailService).sendEmail(any(), any(), any());

        Booking result = bookingService.createBooking(buildRequest("CASH", "E01"), validUser);

        Assert.assertEquals("CONFIRMED", result.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(emailService, times(1)).sendEmail(any(), any(), any());
    }

    @Test
    public void testLife_CronJob_NoExpiredBookings() {
        when(bookingRepository.cancelExpiredPendingBookings(any(LocalDateTime.class))).thenReturn(0);

        bookingService.checkAndCancelExpiredBookings();

        verify(bookingRepository, times(1)).cancelExpiredPendingBookings(any(LocalDateTime.class));
    }

    @Test
    public void testLife_CronJob_RepositoryException_IsHandled() {
        when(bookingRepository.cancelExpiredPendingBookings(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database error"));

        bookingService.checkAndCancelExpiredBookings();

        verify(bookingRepository, times(1)).cancelExpiredPendingBookings(any(LocalDateTime.class));
    }

    @Test
    public void testLife_Concurrent_DifferentSeats_BothUsersSucceed() throws InterruptedException {
        stubHappyPath();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(2);

        Runnable userATask = () -> runConcurrentBooking(startLatch, finishLatch, buildRequest("CASH", "F01"), validUser);
        Runnable userBTask = () -> runConcurrentBooking(startLatch, finishLatch, buildRequest("CASH", "F02"), anotherUser);

        executor.submit(userATask);
        executor.submit(userBTask);
        startLatch.countDown();
        finishLatch.await(2, TimeUnit.SECONDS);
        executor.shutdownNow();

        verify(bookingRepository, times(2)).save(any(Booking.class));
    }

    @Test
    public void testLife_CancelBooking_UserOwner_Success() {
        Booking booking = buildExistingBooking(101, "PENDING", validUser, validTrip, "G01");
        when(bookingRepository.findById(101)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking cancelled = bookingService.cancelBooking(101, validUser);

        Assert.assertEquals("CANCELLED", cancelled.getStatus());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test(expected = RuntimeException.class)
    public void testLife_CancelBooking_WrongUser_Throws() {
        Booking booking = buildExistingBooking(102, "PENDING", validUser, validTrip, "G02");
        when(bookingRepository.findById(102)).thenReturn(Optional.of(booking));

        bookingService.cancelBooking(102, anotherUser);
    }

    @Test(expected = RuntimeException.class)
    public void testLife_CancelBooking_DepartedTrip_Throws() {
        Trip departedTrip = new Trip();
        departedTrip.setTripId(2);
        departedTrip.setDepartureTime(LocalDateTime.now().minusHours(1));
        departedTrip.setArrivalTime(LocalDateTime.now().plusHours(5));
        departedTrip.setBasePrice(BigDecimal.valueOf(100000));
        departedTrip.setRoute(validTrip.getRoute());

        Booking booking = buildExistingBooking(103, "PENDING", validUser, departedTrip, "G03");
        when(bookingRepository.findById(103)).thenReturn(Optional.of(booking));

        bookingService.cancelBooking(103, validUser);
    }

    @Test
    public void testLife_CancelBookingByAdmin_Confirmed_Success() {
        Booking booking = buildExistingBooking(104, "CONFIRMED", validUser, validTrip, "G04");
        when(bookingRepository.findById(104)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking cancelled = bookingService.cancelBookingByAdmin(104);

        Assert.assertEquals("CANCELLED", cancelled.getStatus());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    public void testLife_ConfirmBookingAfterPayment_Pending_Success() {
        Booking booking = buildExistingBooking(105, "PENDING", validUser, validTrip, "H01");
        booking.setMomoOrderId("ORD-01");
        booking.setMomoRequestId("REQ-01");

        when(bookingRepository.findByMomoOrderIdAndMomoRequestId("ORD-01", "REQ-01"))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.findAllWithDetails()).thenReturn(Collections.singletonList(booking));

        bookingService.confirmBookingAfterPayment("ORD-01", "REQ-01");

        Assert.assertEquals("CONFIRMED", booking.getStatus());
        verify(bookingRepository, times(1)).save(booking);
        verify(emailService, times(1)).sendEmail(any(), any(), any());
    }

    @Test
    public void testLife_ConfirmBookingAfterPayment_NotFound_NoSave() {
        when(bookingRepository.findByMomoOrderIdAndMomoRequestId("ORD-404", "REQ-404"))
                .thenReturn(Optional.empty());

        bookingService.confirmBookingAfterPayment("ORD-404", "REQ-404");

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    private void stubHappyPath() {
        when(tripRepository.findById(1)).thenReturn(Optional.of(validTrip));
        when(bookingDetailRepository.findDuplicateSeats(anyInt(), anySet())).thenReturn(new HashSet<>());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            if (booking.getBookingId() == null) {
                booking.setBookingId(999);
            }
            return booking;
        });
    }

    private BookingRequest buildRequest(String paymentMethod, String... seats) {
        BookingRequest request = new BookingRequest();
        request.setTripId(1);
        request.setPaymentMethod(paymentMethod);
        request.setSeatNumbers(Arrays.asList(seats));
        return request;
    }

    private Booking buildExistingBooking(int bookingId, String status, User user, Trip trip, String... seats) {
        Booking booking = new Booking();
        booking.setBookingId(bookingId);
        booking.setUser(user);
        booking.setTrip(trip);
        booking.setStatus(status);
        booking.setBookingTime(LocalDateTime.now());
        booking.setTotalAmount(BigDecimal.valueOf(100000L * seats.length));
        booking.setBookingDetails(Arrays.asList(buildDetail(booking, seats)));
        return booking;
    }

    private BookingDetail[] buildDetail(Booking booking, String... seats) {
        BookingDetail[] details = new BookingDetail[seats.length];
        for (int i = 0; i < seats.length; i++) {
            BookingDetail detail = new BookingDetail();
            detail.setBooking(booking);
            detail.setSeatNumber(seats[i]);
            details[i] = detail;
        }
        return details;
    }

    private User buildUser(int id, String fullName) {
        User user = new User();
        user.setUserId(id);
        user.setUsername("user" + id);
        user.setFullName(fullName);
        user.setEmail("user" + id + "@example.com");
        user.setRole("ROLE_USER");
        user.setEnabled(true);
        return user;
    }

    private void runConcurrentBooking(CountDownLatch startLatch, CountDownLatch finishLatch,
                                      BookingRequest request, User user) {
        try {
            startLatch.await();
            bookingService.createBooking(request, user);
        } catch (Exception ignored) {
        } finally {
            finishLatch.countDown();
        }
    }
}
