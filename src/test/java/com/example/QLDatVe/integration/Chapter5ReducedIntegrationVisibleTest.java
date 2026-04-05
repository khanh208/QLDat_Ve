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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
public class Chapter5ReducedIntegrationVisibleTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private TripService tripService;

    @Test
    public void tc31_createBookingCash_shouldReturnConfirmedBooking() throws Exception {
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
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(bookingService, times(1)).createBooking(any(), any(User.class));
    }

    @Test
    public void tc32_createBookingConflict_shouldReturnBadRequest() throws Exception {
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
}
