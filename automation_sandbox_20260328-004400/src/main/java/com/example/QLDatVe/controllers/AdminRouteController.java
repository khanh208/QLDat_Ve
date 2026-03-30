package com.example.QLDatVe.controllers;

import com.example.QLDatVe.dtos.RouteRequest;
import com.example.QLDatVe.entities.Route;
import com.example.QLDatVe.services.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/routes") // <-- Bảo vệ bởi SecurityConfig
@RequiredArgsConstructor
public class AdminRouteController {

    private final RouteService routeService;

    // 1. Tạo mới (POST /api/admin/routes)
    @PostMapping
    public ResponseEntity<Route> createRoute(@RequestBody RouteRequest request) {
        return ResponseEntity.ok(routeService.createRoute(request));
    }

    // 2. Lấy tất cả (GET /api/admin/routes)
    @GetMapping
    public ResponseEntity<List<Route>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    // 3. Lấy 1 (GET /api/admin/routes/1)
    @GetMapping("/{id}")
    public ResponseEntity<Route> getRouteById(@PathVariable int id) {
        return routeService.getRouteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. Cập nhật (PUT /api/admin/routes/1)
    @PutMapping("/{id}")
    public ResponseEntity<Route> updateRoute(@PathVariable int id, @RequestBody RouteRequest request) {
        try {
            return ResponseEntity.ok(routeService.updateRoute(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 5. Xóa (DELETE /api/admin/routes/1)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable int id) {
        try {
            routeService.deleteRoute(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}