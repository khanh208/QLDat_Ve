package com.example.QLDatVe.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardStatsDTO {
    private BigDecimal totalRevenue; // Tổng doanh thu
    private long totalBookingsConfirmed; // Tổng số đơn vé (đã xác nhận)
    private long totalTicketsSold; // Tổng số vé (ghế) đã bán
    private long totalUsers; // Tổng số người dùng
    private List<TopRouteDTO> topRoutes; // Top các tuyến đường
}