package com.example.QLDatVe.services;

import com.example.QLDatVe.dtos.RouteRequest;
import com.example.QLDatVe.entities.Route;
import com.example.QLDatVe.repositories.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;

    // 1. Lấy tất cả
    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    // 2. Lấy 1 theo ID
    public Optional<Route> getRouteById(int id) {
        return routeRepository.findById(id);
    }

    // 3. Tạo mới
    public Route createRoute(RouteRequest request) {
        Route route = new Route();
        route.setStartLocation(request.getStartLocation());
        route.setEndLocation(request.getEndLocation());
        route.setDistanceKm(request.getDistanceKm());
        return routeRepository.save(route);
    }

    // 4. Cập nhật
    public Route updateRoute(int id, RouteRequest request) {
        Route existingRoute = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Route với ID: " + id));
        
        existingRoute.setStartLocation(request.getStartLocation());
        existingRoute.setEndLocation(request.getEndLocation());
        existingRoute.setDistanceKm(request.getDistanceKm());
        
        return routeRepository.save(existingRoute);
    }

    // 5. Xóa
    public void deleteRoute(int id) {
        if (!routeRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy Route với ID: " + id);
        }
        routeRepository.deleteById(id);
    }
}