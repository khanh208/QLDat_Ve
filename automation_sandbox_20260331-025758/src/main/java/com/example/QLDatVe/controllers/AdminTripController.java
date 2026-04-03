package com.example.QLDatVe.controllers;

import com.example.QLDatVe.dtos.TripRequest;
import com.example.QLDatVe.entities.Trip;
import com.example.QLDatVe.services.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/trips") // Đường dẫn ADMIN
@RequiredArgsConstructor
public class AdminTripController {

    private final TripService tripService;

    // 1. API Tạo chuyến xe (POST)
    // URL: POST /api/admin/trips
    @PostMapping
    public ResponseEntity<Trip> createTrip(@RequestBody TripRequest request) {
        try {
            Trip newTrip = tripService.createTrip(request);
            return ResponseEntity.ok(newTrip);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build(); // 400 Bad Request
        }
    }

    // 2. API Lấy tất cả chuyến xe (GET)
    // URL: GET /api/admin/trips
    @GetMapping
    public ResponseEntity<List<Trip>> getAllTrips() {
        return ResponseEntity.ok(tripService.getAllTrips());
    }

    // 3. API Lấy 1 chuyến xe theo ID (GET)
    // URL: GET /api/admin/trips/1
    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripById(@PathVariable int id) {
        return tripService.getTripById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build()); // 404 Not Found
    }

    // 4. API Cập nhật chuyến xe (PUT)
    // URL: PUT /api/admin/trips/1
    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable int id, @RequestBody TripRequest request) {
        try {
            Trip updatedTrip = tripService.updateTrip(id, request);
            return ResponseEntity.ok(updatedTrip);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    // 5. API Xóa chuyến xe (DELETE)
    // URL: DELETE /api/admin/trips/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable int id) {
        try {
            tripService.deleteTrip(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
}