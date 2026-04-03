package com.example.QLDatVe.controllers;

import com.example.QLDatVe.entities.Trip;
import com.example.QLDatVe.services.BookingService; // Needed for booked seats
import com.example.QLDatVe.services.TripService;    // Needed for trip data
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Consolidated imports

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/trips") // Base path for public trip APIs
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;
    private final BookingService bookingService; // Injected to get booked seats

    /**
     * API to get all available trips (public).
     * GET /api/trips
     * @return List of all trips.
     */
    @GetMapping
    public ResponseEntity<List<Trip>> getAllTrips() {
        List<Trip> trips = tripService.getAllTrips();
        return ResponseEntity.ok(trips);
    }

    /**
     * API to get details of a specific trip by ID (public).
     * GET /api/trips/{id}
     * @param id The ID of the trip.
     * @return Trip details or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripById(@PathVariable int id) {
        return tripService.getTripById(id)
                .map(ResponseEntity::ok) // If found, return 200 OK with trip
                .orElse(ResponseEntity.notFound().build()); // If not found, return 404
    }

    /**
     * API to get the set of booked/pending seat numbers for a specific trip (public).
     * GET /api/trips/{id}/booked-seats
     * @param id The ID of the trip.
     * @return A Set of seat numbers (e.g., {"A1", "B5"}).
     */
    @GetMapping("/{id}/booked-seats")
    public ResponseEntity<Set<String>> getBookedSeats(@PathVariable int id) {
        // Assumes getBookedSeatsForTrip in BookingService returns confirmed + pending seats
        Set<String> bookedSeats = bookingService.getBookedSeatsForTrip(id);
        return ResponseEntity.ok(bookedSeats);
    }

    /**
     * API to search for trips based on start location, end location, and date (public).
     * GET /api/trips/search?start_location=...&end_location=...&date=YYYY-MM-DD
     * @param startLocation The starting location.
     * @param endLocation   The ending location.
     * @param date          The departure date (ISO format YYYY-MM-DD).
     * @return List of matching trips.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Trip>> searchTrips(
            @RequestParam("start_location") String startLocation,
            @RequestParam("end_location") String endLocation,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Trip> trips = tripService.searchTrips(startLocation, endLocation, date);
        return ResponseEntity.ok(trips);
    }
}