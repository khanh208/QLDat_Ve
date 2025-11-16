package com.example.QLDatVe.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "routes") // Ánh xạ tới bảng 'routes'
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id")
    private Integer routeId;

    @Column(name = "start_location", nullable = false, length = 200)
    private String startLocation;

    @Column(name = "end_location", nullable = false, length = 200)
    private String endLocation;

    @Column(name = "distance_km")
    private Integer distanceKm;

    // Sẽ thêm quan hệ với Trip sau
}