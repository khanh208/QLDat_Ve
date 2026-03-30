package com.example.QLDatVe.services;

import com.example.QLDatVe.dtos.TripRequest;
import com.example.QLDatVe.entities.Route;
import com.example.QLDatVe.entities.Trip;
import com.example.QLDatVe.entities.Vehicle;
import com.example.QLDatVe.repositories.RouteRepository;
import com.example.QLDatVe.repositories.TripRepository;
import com.example.QLDatVe.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final VehicleRepository vehicleRepository;
    
    /**
     * Lọc các chuyến đi đã khởi hành (cho API /search)
     */
    public List<Trip> searchTrips(String startLocation, String endLocation, LocalDate departureDate) {
        LocalDateTime now = LocalDateTime.now();
        
        if (departureDate.isBefore(LocalDate.now())) {
            return new ArrayList<>(); // Không tìm ngày quá khứ
        }

        LocalDateTime startOfDaySearch;
        if (departureDate.isEqual(LocalDate.now())) {
            startOfDaySearch = now; // Tìm từ giờ hiện tại
        } else {
            startOfDaySearch = departureDate.atStartOfDay(); // Tìm từ 0h
        }
        
        LocalDateTime endOfDaySearch = departureDate.plusDays(1).atStartOfDay();

        return tripRepository.searchTripsOptimized(
            startLocation, 
            endLocation, 
            startOfDaySearch, 
            endOfDaySearch,
            now
        );
    }

    /**
     * Chỉ lấy các chuyến đi CÒN CHẠY (cho API /trips - Trang chủ)
     */
    public List<Trip> getAllTrips() {
        return tripRepository.findAllAvailable(LocalDateTime.now());
    }

    /**
     * Lấy 1 chuyến theo ID (Cho phép xem chi tiết chuyến cũ)
     */
    public Optional<Trip> getTripById(int id) {
        return tripRepository.findById(id);
    }

    /**
     * Tạo chuyến xe mới (Dùng cho Admin)
     */
    public Trip createTrip(TripRequest request) {
        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Route với ID: " + request.getRouteId()));
        
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Vehicle với ID: " + request.getVehicleId()));

        Trip trip = new Trip();
        trip.setRoute(route);
        trip.setVehicle(vehicle);
        trip.setDepartureTime(request.getDepartureTime());
        trip.setArrivalTime(request.getArrivalTime());
        trip.setBasePrice(request.getBasePrice());

        return tripRepository.save(trip);
    }

    /**
     * Cập nhật chuyến xe (Dùng cho Admin)
     */
    public Trip updateTrip(int id, TripRequest request) {
        Trip existingTrip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Trip với ID: " + id));

        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Route với ID: " + request.getRouteId()));
        
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Vehicle với ID: " + request.getVehicleId()));

        existingTrip.setRoute(route);
        existingTrip.setVehicle(vehicle);
        existingTrip.setDepartureTime(request.getDepartureTime());
        existingTrip.setArrivalTime(request.getArrivalTime());
        existingTrip.setBasePrice(request.getBasePrice());

        return tripRepository.save(existingTrip);
    }

    /**
     * Xóa chuyến (Dùng cho Admin)
     */
    public void deleteTrip(int id) {
        if (!tripRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy Trip với ID: " + id + " để xóa");
        }
        tripRepository.deleteById(id);
    }
}