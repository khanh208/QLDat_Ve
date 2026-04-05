package com.example.QLDatVe.system;

import com.example.QLDatVe.config.SecurityConfig;
import com.example.QLDatVe.controllers.BookingController;
import com.example.QLDatVe.controllers.PaymentController;
import com.example.QLDatVe.controllers.TripController;
import com.example.QLDatVe.entities.Booking;
import com.example.QLDatVe.entities.Trip;
import com.example.QLDatVe.entities.User;
import com.example.QLDatVe.services.BookingService;
import com.example.QLDatVe.services.PaymentService;
import com.example.QLDatVe.services.TripService;
import com.example.QLDatVe.support.MockSecurityTestConfig;
import com.example.QLDatVe.support.TestFixtures;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {
        TripController.class,
        BookingController.class,
        PaymentController.class
})
@Import({SecurityConfig.class, MockSecurityTestConfig.class})
public class BookingSystemFlowMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private TripService tripService;

    @Test
    public void cashBookingJourneyShouldCompleteThroughTripLookupCreateAndMyBookings() throws Exception {
        User user = TestFixtures.user(1, "system-user", "system@example.com", "USER");
        Trip trip = TestFixtures.trip(1);
        Booking confirmedBooking = TestFixtures.booking(801, "CONFIRMED", "CASH", user, trip, "B1");

        when(tripService.getTripById(1)).thenReturn(java.util.Optional.of(trip));
        when(bookingService.getBookedSeatsForTrip(1)).thenReturn(Set.of("A1"));
        when(bookingService.createBooking(any(), any(User.class))).thenReturn(confirmedBooking);
        when(bookingService.getMyBookings(any(User.class))).thenReturn(List.of(confirmedBooking));

        mockMvc.perform(get("/api/trips/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripId").value(1));

        mockMvc.perform(get("/api/trips/1/booked-seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("A1"));

        mockMvc.perform(post("/api/bookings")
                        .with(authentication(TestFixtures.authentication(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tripId\":1,\"seatNumbers\":[\"B1\"],\"paymentMethod\":\"CASH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(801))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(get("/api/bookings/my-bookings")
                        .with(authentication(TestFixtures.authentication(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingId").value(801))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));

        InOrder inOrder = inOrder(tripService, bookingService);
        inOrder.verify(tripService).getTripById(1);
        inOrder.verify(bookingService).getBookedSeatsForTrip(1);
        inOrder.verify(bookingService).createBooking(any(), any(User.class));
        inOrder.verify(bookingService).getMyBookings(any(User.class));
    }

    @Test
    public void momoBookingJourneyShouldCompleteThroughCheckoutConfirmAndHistory() throws Exception {
        User user = TestFixtures.user(2, "momo-user", "momo@example.com", "USER");
        Trip trip = TestFixtures.trip(1);
        Booking pendingBooking = TestFixtures.booking(901, "PENDING", "MOMO", user, trip, "C1");
        Booking confirmedBooking = TestFixtures.booking(901, "CONFIRMED", "MOMO", user, trip, "C1");

        when(bookingService.createBooking(any(), any(User.class))).thenReturn(pendingBooking);
        when(paymentService.createMoMoPayment(901)).thenReturn(Map.of("payUrl", "https://momo.test/pay/901"));
        when(bookingService.getMyBookings(any(User.class))).thenReturn(List.of(confirmedBooking));

        mockMvc.perform(post("/api/bookings")
                        .with(authentication(TestFixtures.authentication(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tripId\":1,\"seatNumbers\":[\"C1\"],\"paymentMethod\":\"MOMO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(901))
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(post("/api/payment/momo/checkout/901")
                        .with(authentication(TestFixtures.authentication(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payUrl").value("https://momo.test/pay/901"));

        mockMvc.perform(post("/api/payment/momo/confirm")
                        .with(authentication(TestFixtures.authentication(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":\"ORD-901\",\"requestId\":\"REQ-901\",\"resultCode\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        mockMvc.perform(get("/api/bookings/my-bookings")
                        .with(authentication(TestFixtures.authentication(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingId").value(901))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));

        InOrder inOrder = inOrder(bookingService, paymentService);
        inOrder.verify(bookingService).createBooking(any(), any(User.class));
        inOrder.verify(paymentService).createMoMoPayment(901);
        inOrder.verify(bookingService).confirmBookingAfterPayment("ORD-901", "REQ-901");
        inOrder.verify(bookingService).getMyBookings(any(User.class));
    }
}
