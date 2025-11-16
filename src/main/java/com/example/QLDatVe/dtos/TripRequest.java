package com.example.QLDatVe.dtos;

import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class TripRequest {
    private int routeId;
    private int vehicleId;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal basePrice;
}