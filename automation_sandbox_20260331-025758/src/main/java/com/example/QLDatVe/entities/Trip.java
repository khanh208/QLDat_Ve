package com.example.QLDatVe.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Integer tripId;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    @Column(name = "base_price", precision = 18, scale = 2)
    private BigDecimal basePrice;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "route_id")
    @JsonIgnoreProperties("trips")
    private Route route;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id")
    @JsonIgnoreProperties("trips")
    private Vehicle vehicle;

    // Lưu ý: Cần có file Route.java và Vehicle.java
}