package com.example.QLDatVe.services;

import com.example.QLDatVe.dtos.BookingRequest;
import com.example.QLDatVe.entities.Trip;
import com.example.QLDatVe.entities.User;
import com.example.QLDatVe.repositories.BookingRepository;
import com.example.QLDatVe.repositories.TripRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BookingValidationTest {

    @InjectMocks private BookingService bookingService;
    @Mock private TripRepository tripRepository;
    @Mock private BookingRepository bookingRepository;

    private BookingRequest request;
    private User user;

    @Before
    public void setUp() {
        user = new User(); 
        user.setUserId(1);
        request = new BookingRequest();
        request.setTripId(1);
        request.setSeatNumbers(Arrays.asList("A01"));
        request.setPaymentMethod("CASH");
    }

    // =========================================================================
    // NHÓM VALIDATION TEST: ĐẠI DIỆN CHO RULES 9 ĐẾN 16 TRONG BẢNG QUYẾT ĐỊNH
    // Tình huống: Dữ liệu (C1) = False -> Hệ thống luôn báo lỗi, bỏ qua C2, C3, C4
    // =========================================================================

    @Test(expected = RuntimeException.class)
    public void testVal_EmptySeats() {
        Trip trip = new Trip(); trip.setDepartureTime(LocalDateTime.now().plusDays(1));
        when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
        
        request.setSeatNumbers(new ArrayList<>()); // Danh sách ghế rỗng
        bookingService.createBooking(request, user);
    }

    @Test(expected = RuntimeException.class)
    public void testVal_TripDeparted() {
        Trip oldTrip = new Trip(); 
        oldTrip.setDepartureTime(LocalDateTime.now().minusHours(2)); // Xe đã chạy
        when(tripRepository.findById(1)).thenReturn(Optional.of(oldTrip));
        
        bookingService.createBooking(request, user);
    }

    @Test(expected = RuntimeException.class)
    public void testVal_TripNotFound() {
        when(tripRepository.findById(999)).thenReturn(Optional.empty()); // ID không có trong DB
        request.setTripId(999);
        bookingService.createBooking(request, user);
    }

    @Test(expected = RuntimeException.class)
    public void testVal_NegativeTripId() {
        when(tripRepository.findById(-1)).thenReturn(Optional.empty()); // ID số âm
        request.setTripId(-1);
        bookingService.createBooking(request, user);
    }

    @Test(expected = RuntimeException.class)
    public void testVal_UserNull() {
        Trip trip = new Trip(); trip.setDepartureTime(LocalDateTime.now().plusDays(1));
        when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
        
        bookingService.createBooking(request, null); // Thiếu User
    }

    @Test(expected = RuntimeException.class)
    public void testVal_DuplicateSeatsInRequest() {
        Trip trip = new Trip(); trip.setDepartureTime(LocalDateTime.now().plusDays(1));
        when(tripRepository.findById(1)).thenReturn(Optional.of(trip));
        
        request.setSeatNumbers(Arrays.asList("A01", "A01")); // Trùng ghế
        bookingService.createBooking(request, user);
    }

    @Test(expected = NullPointerException.class)
    public void testVal_RequestNull() {
        bookingService.createBooking(null, user); // Request rỗng hoàn toàn
    }
}