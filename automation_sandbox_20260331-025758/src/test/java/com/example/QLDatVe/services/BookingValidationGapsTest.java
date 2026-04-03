package com.example.QLDatVe.services;

import com.example.QLDatVe.dtos.BookingRequest;
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
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BookingValidationGapsTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingDetailRepository bookingDetailRepository;
    @Mock private TripRepository tripRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;

    private Trip validTrip;
    private User validUser;

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

        validUser = new User();
        validUser.setUserId(1);
        validUser.setUsername("validation-user");
        validUser.setFullName("Validation User");
        validUser.setEmail("validation@example.com");
        validUser.setRole("ROLE_USER");
        validUser.setEnabled(true);

        when(tripRepository.findById(1)).thenReturn(Optional.of(validTrip));
        when(bookingDetailRepository.findDuplicateSeats(anyInt(), anySet())).thenReturn(new HashSet<>());
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    public void testValGap_PaymentMethodNull_ShouldReject() {
        assertShouldReject(buildRequest(null, "J01"));
    }

    @Test
    public void testValGap_PaymentMethodUnsupported_ShouldReject() {
        assertShouldReject(buildRequest("BANKING", "J02"));
    }

    @Test
    public void testValGap_SeatContainsNull_ShouldReject() {
        assertShouldReject(buildRequest("CASH", (String) null));
    }

    @Test
    public void testValGap_SeatContainsBlank_ShouldReject() {
        assertShouldReject(buildRequest("CASH", ""));
    }

    @Test
    public void testValGap_SeatContainsValidAndBlank_ShouldReject() {
        assertShouldReject(buildRequest("CASH", "J05", ""));
    }

    private BookingRequest buildRequest(String paymentMethod, String... seats) {
        BookingRequest request = new BookingRequest();
        request.setTripId(1);
        request.setPaymentMethod(paymentMethod);
        request.setSeatNumbers(Arrays.asList(seats));
        return request;
    }

    private void assertShouldReject(BookingRequest request) {
        try {
            bookingService.createBooking(request, validUser);
            Assert.fail("Expected the request to be rejected, but booking was still created.");
        } catch (RuntimeException expected) {
            // Current service may throw different RuntimeException subtypes.
            // The test only cares that invalid data must not be accepted.
        }
    }
}
