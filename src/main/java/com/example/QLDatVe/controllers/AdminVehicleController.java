package com.example.QLDatVe.controllers;

import com.example.QLDatVe.dtos.VehicleRequest;
import com.example.QLDatVe.entities.Vehicle;
import com.example.QLDatVe.services.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vehicles") // <-- Bảo vệ bởi SecurityConfig
@RequiredArgsConstructor
public class AdminVehicleController {

    private final VehicleService vehicleService;

    // 1. Tạo mới (POST /api/admin/vehicles)
    @PostMapping
    public ResponseEntity<Vehicle> createVehicle(@RequestBody VehicleRequest request) {
        return ResponseEntity.ok(vehicleService.createVehicle(request));
    }

    // 2. Lấy tất cả (GET /api/admin/vehicles)
    @GetMapping
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    // 3. Lấy 1 (GET /api/admin/vehicles/1)
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable int id) {
        return vehicleService.getVehicleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. Cập nhật (PUT /api/admin/vehicles/1)
    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable int id, @RequestBody VehicleRequest request) {
        try {
            return ResponseEntity.ok(vehicleService.updateVehicle(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 5. Xóa (DELETE /api/admin/vehicles/1)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable int id) {
        try {
            vehicleService.deleteVehicle(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}