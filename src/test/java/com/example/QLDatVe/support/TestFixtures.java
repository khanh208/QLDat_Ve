package com.example.QLDatVe.support;

import com.example.QLDatVe.dtos.BookingRequest;
import com.example.QLDatVe.entities.Booking;
import com.example.QLDatVe.entities.BookingDetail;
import com.example.QLDatVe.entities.Route;
import com.example.QLDatVe.entities.Trip;
import com.example.QLDatVe.entities.User;
import com.example.QLDatVe.entities.Vehicle;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static User user(int id, String username, String email, String role) {
        return User.builder()
                .userId(id)
                .username(username)
                .passwordHash("encoded-password")
                .fullName(username)
                .email(email)
                .role(role)
                .enabled(true)
                .build();
    }

    public static UsernamePasswordAuthenticationToken authentication(User user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    public static Route route(int id, String start, String end, int distanceKm) {
        Route route = new Route();
        route.setRouteId(id);
        route.setStartLocation(start);
        route.setEndLocation(end);
        route.setDistanceKm(distanceKm);
        return route;
    }

    public static Vehicle vehicle(int id, String plate, String type, int totalSeats) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(id);
        vehicle.setLicensePlate(plate);
        vehicle.setVehicleType(type);
        vehicle.setTotalSeats(totalSeats);
        return vehicle;
    }

    public static Trip trip(int id) {
        Trip trip = new Trip();
        trip.setTripId(id);
        trip.setDepartureTime(LocalDateTime.now().plusDays(1));
        trip.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(5));
        trip.setBasePrice(BigDecimal.valueOf(250000));
        trip.setRoute(route(1, "TP. Ho Chi Minh", "Da Lat", 320));
        trip.setVehicle(vehicle(1, "51B-99999", "Limousine", 8));
        return trip;
    }

    public static Booking booking(int id,
                                  String status,
                                  String paymentMethod,
                                  User user,
                                  Trip trip,
                                  String... seats) {
        Booking booking = new Booking();
        booking.setBookingId(id);
        booking.setStatus(status);
        booking.setBookingTime(LocalDateTime.now());
        booking.setTotalAmount(trip.getBasePrice().multiply(BigDecimal.valueOf(seats.length)));
        booking.setTrip(trip);
        booking.setUser(user);
        booking.setMomoOrderId("ORD-" + id);
        booking.setMomoRequestId("REQ-" + id);

        List<BookingDetail> details = Arrays.stream(seats)
                .map(seat -> {
                    BookingDetail detail = new BookingDetail();
                    // Keep mocked controller responses serializable in MockMvc tests.
                    detail.setBooking(null);
                    detail.setSeatNumber(seat);
                    return detail;
                })
                .toList();
        booking.setBookingDetails(details);
        return booking;
    }

    public static BookingRequest bookingRequest(int tripId, String paymentMethod, String... seats) {
        BookingRequest request = new BookingRequest();
        request.setTripId(tripId);
        request.setPaymentMethod(paymentMethod);
        request.setSeatNumbers(Arrays.asList(seats));
        return request;
    }

    public static LocalDate departureDateFromTrip(Trip trip) {
        return trip.getDepartureTime().toLocalDate();
    }
}
