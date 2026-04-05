package com.example.QLDatVe.integration;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {
        BookingController.class,
        PaymentController.class,
        TripController.class
})
@Import({SecurityConfig.class, MockSecurityTestConfig.class})
public class BookingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private TripService tripService;

    @Test
    public void createBookingEndpointShouldReturnConfirmedBookingForCashFlow() throws Exception {
        User user = TestFixtures.user(1, "integration-user", "integration@example.com", "USER");
        Trip trip = TestFixtures.trip(1);
        Booking booking = TestFixtures.booking(501, "CONFIRMED", "CASH", user, trip, "B1");

        when(bookingService.createBooking(any(), any(User.class))).thenReturn(booking);

        mockMvc.perform(post("/api/bookings")
                        .with(authentication(TestFixtures.authentication(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tripId\":1,\"seatNumbers\":[\"B1\"],\"paymentMethod\":\"CASH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(501))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.trip.tripId").value(1))
                .andExpect(jsonPath("$.bookingDetails[0].seatNumber").value("B1"));

        verify(bookingService, times(1)).createBooking(any(), any(User.class));
    }

    @Test
    public void createBookingEndpointShouldReturnBadRequestWhenServiceRejectsSeatConflict() throws Exception {
        User user = TestFixtures.user(1, "integration-user", "integration@example.com", "USER");
        when(bookingService.createBooking(any(), any(User.class)))
                .thenThrow(new RuntimeException("Seat A2 is already held."));

        mockMvc.perform(post("/api/bookings")
                        .with(authentication(TestFixtures.authentication(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tripId\":1,\"seatNumbers\":[\"A2\"],\"paymentMethod\":\"MOMO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Seat A2 is already held.")));
    }

    @Test
    public void bookedSeatsEndpointShouldReturnCurrentSeatSet() throws Exception {
        when(bookingService.getBookedSeatsForTrip(1)).thenReturn(Set.of("A1", "B2"));

        mockMvc.perform(get("/api/trips/1/booked-seats"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("A1")))
                .andExpect(content().string(containsString("B2")));
    }

    @Test
    public void momoConfirmEndpointShouldReturnSuccessPayloadForAuthenticatedUser() throws Exception {
        User user = TestFixtures.user(1, "integration-user", "integration@example.com", "USER");

        mockMvc.perform(post("/api/payment/momo/confirm")
                        .with(authentication(TestFixtures.authentication(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":\"ORD-501\",\"requestId\":\"REQ-501\",\"resultCode\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.orderId").value("ORD-501"));

        verify(bookingService, times(1)).confirmBookingAfterPayment("ORD-501", "REQ-501");
    }

    @Test
    @WithAnonymousUser
    public void searchTripsEndpointShouldReturnMatchingTrips() throws Exception {
        Trip trip = TestFixtures.trip(1);
        when(tripService.searchTrips(eq("TP. Ho Chi Minh"), eq("Da Lat"), eq(TestFixtures.departureDateFromTrip(trip))))
                .thenReturn(List.of(trip));

        mockMvc.perform(get("/api/trips/search")
                        .param("start_location", "TP. Ho Chi Minh")
                        .param("end_location", "Da Lat")
                        .param("date", TestFixtures.departureDateFromTrip(trip).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tripId").value(1))
                .andExpect(jsonPath("$[0].route.startLocation").value("TP. Ho Chi Minh"))
                .andExpect(jsonPath("$[0].route.endLocation").value("Da Lat"));
    }

    @Test
    public void cancelBookingEndpointShouldReturnUpdatedBooking() throws Exception {
        User user = TestFixtures.user(1, "integration-user", "integration@example.com", "USER");
        Trip trip = TestFixtures.trip(1);
        Booking cancelledBooking = TestFixtures.booking(601, "CANCELLED", "CASH", user, trip, "B1");

        when(bookingService.cancelBooking(eq(601), any(User.class))).thenReturn(cancelledBooking);

        mockMvc.perform(put("/api/bookings/601/cancel")
                        .with(authentication(TestFixtures.authentication(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(601))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
