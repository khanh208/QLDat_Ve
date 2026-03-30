package com.example.QLDatVe.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicles") // Ánh xạ tới bảng 'vehicles'
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Integer vehicleId;

    @Column(name = "license_plate", nullable = false, unique = true, length = 15)
    private String licensePlate;

    @Column(name = "vehicle_type", length = 100)
    private String vehicleType;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    // Sẽ thêm quan hệ với Trip sau
}