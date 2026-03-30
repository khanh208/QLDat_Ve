package com.example.QLDatVe.dtos;

import lombok.Data;

@Data
public class RouteRequest {
    private String startLocation;
    private String endLocation;
    private int distanceKm;
}