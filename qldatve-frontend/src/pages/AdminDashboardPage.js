import React, { useEffect, useState } from 'react';
import api from '../api';
import { Container, Grid, Paper, Typography, Box, CircularProgress, Alert, List, ListItem, ListItemText } from '@mui/material';

// Import các Icon
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import ConfirmationNumberIcon from '@mui/icons-material/ConfirmationNumber';
import PeopleIcon from '@mui/icons-material/People';
import ReceiptIcon from '@mui/icons-material/Receipt';
import BarChartIcon from '@mui/icons-material/BarChart'; // Icon cho biểu đồ

// Component Card thống kê
const StatCard = ({ title, value, icon, color }) => (
    <Paper
        elevation={3}
        sx={{
            p: 3,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            height: '100%'
        }}
    >
        <Box>
            <Typography color="text.secondary" gutterBottom>
                {title}
            </Typography>
            <Typography variant="h4" component="div" sx={{ fontWeight: 'bold' }}>
                {value}
            </Typography>
        </Box>
        <Box sx={{ color: color, fontSize: '3.5rem', opacity: 0.8 }}>
            {icon}
        </Box>
    </Paper>
);

// Format tiền
const fmtVND = (n) => Number(n || 0).toLocaleString('vi-VN');

function AdminDashboardPage() {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchStats = async () => {
            setLoading(true);
            setError('');
            try {
                // 1. Gọi API mới
                const response = await api.get('/admin/dashboard/stats');
                console.log("Dashboard Stats:", response.data);
                setStats(response.data);
            } catch (err) {
                console.error("Lỗi khi tải thống kê:", err);
                setError(err.response?.data?.message || "Không thể tải dữ liệu thống kê.");
            } finally {
                setLoading(false);
            }
        };
        fetchStats();
    }, []); // Chạy 1 lần

    if (loading) {
        return <Box sx={{ display: 'flex', justifyContent: 'center', my: 5 }}><CircularProgress /></Box>;
    }
    if (error) {
        return <Alert severity="error">{error}</Alert>;
    }
    if (!stats) {
        return <Typography>Không có dữ liệu thống kê.</Typography>;
    }

    return (
        <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
            <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
                Tổng quan
            </Typography>

            {/* --- 4 Thẻ Thống kê --- */}
            <Grid container spacing={3}>
                <Grid item xs={12} sm={6} md={3}>
                    <StatCard
                        title="Tổng Doanh thu (Đã xác nhận)"
                        value={`${fmtVND(stats.totalRevenue)} VND`}
                        icon={<AttachMoneyIcon fontSize="inherit" />}
                        color="success.main"
                    />
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                    <StatCard
                        title="Tổng vé đã bán (Đã xác nhận)"
                        value={stats.totalTicketsSold?.toLocaleString('vi-VN')}
                        icon={<ConfirmationNumberIcon fontSize="inherit" />}
                        color="info.main"
                    />
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                    <StatCard
                        title="Tổng số Người dùng"
                        value={stats.totalUsers?.toLocaleString('vi-VN')}
                        icon={<PeopleIcon fontSize="inherit" />}
                        color="secondary.main"
                    />
                </Grid>
            </Grid>
            {/* --- Hết 4 Thẻ --- */}

            {/* --- Biểu đồ và Top Tuyến --- */}
            <Grid container spacing={3} sx={{ mt: 4 }}>
                {/* (Phần Biểu đồ Doanh thu - Tạm để trống) */}
                

                {/* Top 5 Tuyến đường */}
                <Grid item xs={12} md={4}>
                    <Paper sx={{ p: 2, height: '400px' }}>
                        <Typography variant="h6" gutterBottom>Top 5 Tuyến đường</Typography>
                        {stats.topRoutes && stats.topRoutes.length > 0 ? (
                            <List dense>
                                {stats.topRoutes.map((route, index) => (
                                    <ListItem key={index} divider>
                                        <ListItemText
                                            primary={<Typography sx={{ fontWeight: 'medium' }}>{index + 1}. {route.routeName}</Typography>}
                                            secondary={`${route.ticketCount.toLocaleString('vi-VN')} vé`}
                                        />
                                    </ListItem>
                                ))}
                            </List>
                        ) : (
                            <Typography color="text.secondary" sx={{mt: 2}}>Chưa có dữ liệu top tuyến.</Typography>
                        )}
                    </Paper>
                </Grid>
            </Grid>
        </Container>
    );
}

export default AdminDashboardPage;