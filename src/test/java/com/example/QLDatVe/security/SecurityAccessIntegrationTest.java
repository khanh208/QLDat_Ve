package com.example.QLDatVe.security;

import com.example.QLDatVe.config.JwtAuthenticationFilter;
import com.example.QLDatVe.config.SecurityConfig;
import com.example.QLDatVe.controllers.AdminBookingController;
import com.example.QLDatVe.controllers.AuthController;
import com.example.QLDatVe.controllers.BookingController;
import com.example.QLDatVe.controllers.PaymentController;
import com.example.QLDatVe.controllers.TripController;
import com.example.QLDatVe.dtos.AuthResponse;
import com.example.QLDatVe.entities.User;
import com.example.QLDatVe.services.AuthService;
import com.example.QLDatVe.services.BookingService;
import com.example.QLDatVe.services.JwtService;
import com.example.QLDatVe.services.PaymentService;
import com.example.QLDatVe.services.TripService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {
        AuthController.class,
        TripController.class,
        BookingController.class,
        AdminBookingController.class,
        PaymentController.class
})
@Import({SecurityConfig.class, SecurityAccessIntegrationTest.TestSecurityBeans.class})
public class SecurityAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private TripService tripService;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private PaymentService paymentService;

    @Test
    @WithAnonymousUser
    public void publicTripsEndpointShouldAllowAnonymousAccess() throws Exception {
        when(tripService.getAllTrips()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/trips"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void loginEndpointShouldAllowAnonymousAccess() throws Exception {
        when(authService.login(any()))
                .thenReturn(new AuthResponse("mock-token", "Login success", 1, "selenium-user", "USER"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"selenium-user\",\"password\":\"123456\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void bookingsEndpointShouldRejectAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/bookings/my-bookings"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void bookingsEndpointShouldAllowAuthenticatedUser() throws Exception {
        when(bookingService.getMyBookings(any(User.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/bookings/my-bookings")
                        .with(authentication(buildAuthentication("USER"))))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getMyBookings(any(User.class));
    }

    @Test
    public void adminEndpointShouldRejectRegularUser() throws Exception {
        mockMvc.perform(get("/api/admin/bookings")
                        .with(authentication(buildAuthentication("USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void adminEndpointShouldAllowAdminUser() throws Exception {
        when(bookingService.getAllBookingsSorted()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/bookings")
                        .with(authentication(buildAuthentication("ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void momoIpnEndpointShouldAllowAnonymousWebhook() throws Exception {
        mockMvc.perform(post("/api/payment/momo-ipn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":\"ORD-001\",\"requestId\":\"REQ-001\"}"))
                .andExpect(status().isOk());

        verify(paymentService, times(1)).handleMomoIPN(anyMap());
    }

    @Test
    @WithAnonymousUser
    public void momoCheckoutEndpointShouldRejectAnonymousAccess() throws Exception {
        mockMvc.perform(post("/api/payment/momo/checkout/123"))
                .andExpect(status().is4xxClientError());
    }

    private Authentication buildAuthentication(String role) {
        User user = User.builder()
                .userId(1)
                .username("security-user")
                .passwordHash("encoded-password")
                .email("security@example.com")
                .role(role)
                .enabled(true)
                .build();

        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @TestConfiguration
    static class TestSecurityBeans {

        @Bean
        AuthenticationProvider authenticationProvider() {
            return new AuthenticationProvider() {
                @Override
                public Authentication authenticate(Authentication authentication) {
                    return authentication;
                }

                @Override
                public boolean supports(Class<?> authentication) {
                    return true;
                }
            };
        }

        @Bean
        UserDetailsService userDetailsService() {
            return username -> User.builder()
                    .userId(99)
                    .username(username)
                    .passwordHash("encoded-password")
                    .role("USER")
                    .enabled(true)
                    .build();
        }

        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter(UserDetailsService userDetailsService) {
            JwtService jwtService = Mockito.mock(JwtService.class);

            return new JwtAuthenticationFilter(jwtService, userDetailsService) {
                @Override
                protected void doFilterInternal(HttpServletRequest request,
                                                HttpServletResponse response,
                                                FilterChain filterChain) throws ServletException, IOException {
                    filterChain.doFilter(request, response);
                }
            };
        }
    }
}
