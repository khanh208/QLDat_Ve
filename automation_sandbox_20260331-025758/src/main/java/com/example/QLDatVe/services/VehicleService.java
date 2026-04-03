package com.example.QLDatVe.services;

import com.example.QLDatVe.dtos.VehicleRequest;
import com.example.QLDatVe.entities.Vehicle;
import com.example.QLDatVe.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    // 1. Lấy tất cả
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    // 2. Lấy 1 theo ID
    public Optional<Vehicle> getVehicleById(int id) {
        return vehicleRepository.findById(id);
    }

    // 3. Tạo mới
    public Vehicle createVehicle(VehicleRequest request) {
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setVehicleType(request.getVehicleType());
        vehicle.setTotalSeats(request.getTotalSeats());
        return vehicleRepository.save(vehicle);
    }

    // 4. Cập nhật
    public Vehicle updateVehicle(int id, VehicleRequest request) {
        Vehicle existingVehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Vehicle với ID: " + id));
        
        existingVehicle.setLicensePlate(request.getLicensePlate());
        existingVehicle.setVehicleType(request.getVehicleType());
        existingVehicle.setTotalSeats(request.getTotalSeats());
        
        return vehicleRepository.save(existingVehicle);
    }

    // 5. Xóa
    public void deleteVehicle(int id) {
        if (!vehicleRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy Vehicle với ID: " + id);
        }
        vehicleRepository.deleteById(id);
    }
}