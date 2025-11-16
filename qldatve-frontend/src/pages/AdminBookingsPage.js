import React, { useEffect, useState } from 'react';
import api from '../api';
import { Link } from 'react-router-dom';
import {
    Container, Typography, Paper, Button,
    CircularProgress, Box, Alert, Chip,
    Tabs, 
    Tab,
    Card,
    CardContent,
    Grid,
    TextField,
    InputAdornment,
    MenuItem,
    Select,
    FormControl,
    InputLabel
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import FilterListIcon from '@mui/icons-material/FilterList';

function AdminBookingsPage() {
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [cancelError, setCancelError] = useState('');
    const [cancelSuccess, setCancelSuccess] = useState('');
    const [selectedTab, setSelectedTab] = useState(0);
    const [searchTerm, setSearchTerm] = useState('');
    const [filterStatus, setFilterStatus] = useState('ALL');
    const [sortBy, setSortBy] = useState('NEWEST');

    // --- Hàm Fetch Tất cả Booking (Admin) ---
    const fetchAllBookings = async () => {
        try {
            setError('');
            setCancelError('');
            setCancelSuccess('');
            setLoading(true);
            
            const response = await api.get('/admin/bookings');
            console.log("AdminBookingsPage - API Response:", response.data);
            
            let bookingsData = response.data;
            
            // Xử lý nhiều định dạng response khác nhau
            if (!Array.isArray(bookingsData)) {
                if (bookingsData.content && Array.isArray(bookingsData.content)) {
                    bookingsData = bookingsData.content;
                } else if (bookingsData.data && Array.isArray(bookingsData.data)) {
                    bookingsData = bookingsData.data;
                } else if (bookingsData.bookings && Array.isArray(bookingsData.bookings)) {
                    bookingsData = bookingsData.bookings;
                } else {
                    console.warn("Response không chứa mảng bookings:", bookingsData);
                    bookingsData = [];
                }
            }
            
            // Sắp xếp theo thời gian đặt vé mới nhất
            const sortedBookings = bookingsData.sort((a, b) => 
                new Date(b.bookingTime || 0) - new Date(a.bookingTime || 0)
            );
            
            setBookings(sortedBookings);
            console.log("Số lượng bookings (admin) đã tải:", sortedBookings.length);
            
        } catch (err) {
            console.error("Lỗi khi tải danh sách booking (admin):", err);
            if (err.response && (err.response.status === 401 || err.response.status === 403)) {
                 setError("Bạn không có quyền truy cập trang quản lý booking. Vui lòng đăng nhập với tài khoản admin.");
            } else {
                setError(err.response?.data?.message || "Không thể tải danh sách booking.");
            }
        } finally {
            setLoading(false);
        }
    };

    // --- useEffect: Gọi fetchAllBookings khi component được mount ---
    useEffect(() => {
        fetchAllBookings();
    }, []);

    
    // --- Hàm Hủy Vé (Admin) ---
    const handleCancelBooking = async (bookingId) => {
        setCancelError('');
        setCancelSuccess('');
        if (!window.confirm(`Bạn có chắc muốn hủy vé #${bookingId} không?`)) {
            return;
        }
        try {
            await api.put(`/admin/bookings/${bookingId}/cancel`);
            setCancelSuccess(`✅ Đã hủy vé #${bookingId} thành công.`);
            
            // Fetch lại dữ liệu sau 1 giây
            setTimeout(() => {
                fetchAllBookings();
            }, 1000);
            
        } catch (err) {
            console.error("Lỗi khi hủy vé (admin):", err.response || err);
            setCancelError(err.response?.data?.message || err.response?.data || `❌ Lỗi khi hủy vé #${bookingId}.`);
        }
    };

    // --- Hàm xử lý khi chuyển Tab ---
    const handleTabChange = (event, newValue) => {
        setSelectedTab(newValue);
        setCancelError('');
        setCancelSuccess('');
    };

    // --- Lọc và sắp xếp dữ liệu ---
    const filterAndSortBookings = () => {
        let filtered = [...bookings];

        // Lọc theo search term
        if (searchTerm) {
            const term = searchTerm.toLowerCase();
            filtered = filtered.filter(booking => 
                booking.bookingId?.toString().includes(term) ||
                booking.user?.fullName?.toLowerCase().includes(term) ||
                booking.user?.email?.toLowerCase().includes(term) ||
                booking.trip?.route?.startLocation?.toLowerCase().includes(term) ||
                booking.trip?.route?.endLocation?.toLowerCase().includes(term) ||
                booking.bookingDetails?.some(detail => 
                    detail.seatNumber?.toLowerCase().includes(term)
                )
            );
        }

        // Lọc theo trạng thái
        if (filterStatus !== 'ALL') {
            filtered = filtered.filter(booking => booking.status === filterStatus);
        }

        // Sắp xếp
        filtered.sort((a, b) => {
            switch (sortBy) {
                case 'NEWEST':
                    return new Date(b.bookingTime) - new Date(a.bookingTime);
                case 'OLDEST':
                    return new Date(a.bookingTime) - new Date(b.bookingTime);
                case 'PRICE_HIGH':
                    return (b.totalAmount || 0) - (a.totalAmount || 0);
                case 'PRICE_LOW':
                    return (a.totalAmount || 0) - (b.totalAmount || 0);
                default:
                    return new Date(b.bookingTime) - new Date(a.bookingTime);
            }
        });

        return filtered;
    };

    const processedBookings = filterAndSortBookings();

    // --- Lọc danh sách booking theo trạng thái ---
    const allBookings = processedBookings || [];
    const pendingBookings = allBookings.filter(b => b.status === 'PENDING');
    const confirmedBookings = allBookings.filter(b => b.status === 'CONFIRMED');
    const cancelledBookings = allBookings.filter(b => b.status === 'CANCELLED');
    const failedBookings = allBookings.filter(b => b.status === 'FAILED');
    
    const cancelledOrFailedBookings = [...cancelledBookings, ...failedBookings];

    // --- Hàm định dạng tiền VND ---
    const formatVND = (amount) => {
        return new Intl.NumberFormat('vi-VN', { 
            style: 'currency', 
            currency: 'VND' 
        }).format(amount || 0);
    };

    // --- Hàm định dạng ngày giờ ---
    const formatDateTime = (dateTime) => {
        if (!dateTime) return 'N/A';
        return new Date(dateTime).toLocaleString('vi-VN', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    // --- Hàm Helper để render danh sách vé ---
    const renderBookingList = (bookingList) => {
        if (!Array.isArray(bookingList) || bookingList.length === 0) {
            return (
                <Box sx={{ p: 4, textAlign: 'center' }}>
                    <Typography variant="body1" color="text.secondary">
                        Không có vé nào trong mục này.
                    </Typography>
                </Box>
            );
        }

        return (
            <Box sx={{ p: 2 }}>
                {bookingList.map((booking) => {
                    const seatNumbersString = Array.isArray(booking.bookingDetails)
                        ? booking.bookingDetails.map(d => d.seatNumber).join(', ')
                        : 'N/A';

                    // Xác định label và màu cho Chip trạng thái
                    let statusLabel = booking.status;
                    let statusColor = 'default';
                    let statusVariant = 'outlined';
                    
                    switch (booking.status) {
                        case 'CONFIRMED':
                            statusLabel = 'ĐÃ XÁC NHẬN';
                            statusColor = 'success';
                            statusVariant = 'filled';
                            break;
                        case 'PENDING':
                            statusLabel = 'CHỜ THANH TOÁN';
                            statusColor = 'warning';
                            statusVariant = 'filled';
                            break;
                        case 'CANCELLED':
                            statusLabel = 'ĐÃ HỦY';
                            statusColor = 'error';
                            statusVariant = 'filled';
                            break;
                        case 'FAILED':
                            statusLabel = 'THANH TOÁN LỖI';
                            statusColor = 'error';
                            break;
                        default:
                            statusLabel = booking.status;
                    }

                    return (
                        <Card key={booking.bookingId} sx={{ mb: 2, border: 1, borderColor: 'divider' }}>
                            <CardContent>
                                <Grid container spacing={2} alignItems="center">
                                    <Grid item xs={12} sm={8}>
                                        <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 1 }}>
                                            Mã vé: #{booking.bookingId}
                                        </Typography>

                                        {/* Thông tin người dùng */}
                                        <Typography variant="body1" sx={{ mb: 1 }}>
                                            <strong>👤 Khách hàng:</strong> {booking.user?.fullName} ({booking.user?.email})
                                        </Typography>
                                        
                                        <Typography variant="body1" sx={{ mb: 1 }}>
                                            <strong>🚌 Tuyến:</strong> {booking.trip?.route?.startLocation} → {booking.trip?.route?.endLocation}
                                        </Typography>
                                        
                                        <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>
                                            <strong>📅 Ngày đi:</strong> {formatDateTime(booking.trip?.departureTime)}
                                        </Typography>
                                        
                                        <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>
                                            <strong>💺 Ghế:</strong> {seatNumbersString}
                                        </Typography>
                                        
                                        <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>
                                            <strong>💰 Tổng tiền:</strong> {formatVND(booking.totalAmount)}
                                        </Typography>
                                        
                                        <Typography variant="body2" color="text.secondary">
                                            <strong>⏰ Ngày đặt:</strong> {formatDateTime(booking.bookingTime)}
                                        </Typography>

                                        {booking.trip?.vehicle && (
                                            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                                                <strong>🚗 Xe:</strong> {booking.trip.vehicle.licensePlate} - {booking.trip.vehicle.vehicleType}
                                            </Typography>
                                        )}
                                    </Grid>
                                    
                                    <Grid item xs={12} sm={4}>
                                        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: { xs: 'flex-start', sm: 'flex-end' }, gap: 1 }}>
                                            <Chip
                                                label={statusLabel}
                                                color={statusColor}
                                                variant={statusVariant}
                                                size="small"
                                                sx={{ fontWeight: 'bold', minWidth: '140px' }}
                                            />
                                            
                                            {/* Nút hành động */}
                                            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, mt: 1 }}>
                                                {/* Nút hủy vé cho vé chưa hủy */}
                                                {(booking.status === 'CONFIRMED' || booking.status === 'PENDING') && (
                                                    <Button
                                                        variant="outlined"
                                                        color="error"
                                                        size="small"
                                                        onClick={() => handleCancelBooking(booking.bookingId)}
                                                        sx={{ minWidth: '120px' }}
                                                    >
                                                        Hủy vé
                                                    </Button>
                                                )}

                                                {/* Nút xem chi tiết chuyến */}
                                                <Button
                                                    variant="outlined"
                                                    size="small"
                                                    component={Link}
                                                    to={`/admin/trips/${booking.trip?.tripId}`}
                                                    sx={{ minWidth: '120px' }}
                                                >
                                                    📋 Xem chi tiết
                                                </Button>
                                            </Box>
                                        </Box>
                                    </Grid>
                                </Grid>
                            </CardContent>
                        </Card>
                    );
                })}
            </Box>
        );
    };

    // --- Hiển thị thống kê ---
    const renderStats = () => (
        <Box sx={{ mb: 3 }}>
            <Grid container spacing={2}>
                <Grid item xs={6} sm={3}>
                    <Paper sx={{ p: 2, textAlign: 'center', bgcolor: 'primary.main', color: 'white' }}>
                        <Typography variant="h6">{bookings.length}</Typography>
                        <Typography variant="body2">📊 Tổng số vé</Typography>
                    </Paper>
                </Grid>
                <Grid item xs={6} sm={3}>
                    <Paper sx={{ p: 2, textAlign: 'center', bgcolor: 'success.main', color: 'white' }}>
                        <Typography variant="h6">{confirmedBookings.length}</Typography>
                        <Typography variant="body2">✅ Đã xác nhận</Typography>
                    </Paper>
                </Grid>
                <Grid item xs={6} sm={3}>
                    <Paper sx={{ p: 2, textAlign: 'center', bgcolor: 'warning.main', color: 'white' }}>
                        <Typography variant="h6">{pendingBookings.length}</Typography>
                        <Typography variant="body2">⏳ Chờ thanh toán</Typography>
                    </Paper>
                </Grid>
                <Grid item xs={6} sm={3}>
                    <Paper sx={{ p: 2, textAlign: 'center', bgcolor: 'error.main', color: 'white' }}>
                        <Typography variant="h6">{cancelledOrFailedBookings.length}</Typography>
                        <Typography variant="body2">❌ Đã hủy/Lỗi</Typography>
                    </Paper>
                </Grid>
            </Grid>
        </Box>
    );

    // --- Thanh công cụ tìm kiếm và lọc ---
    const renderToolbar = () => (
        <Paper sx={{ p: 2, mb: 2 }}>
            <Grid container spacing={2} alignItems="center">
                <Grid item xs={12} md={4}>
                    <TextField
                        fullWidth
                        variant="outlined"
                        placeholder="Tìm kiếm theo mã vé, tên khách hàng, email, tuyến đường, số ghế..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        InputProps={{
                            startAdornment: (
                                <InputAdornment position="start">
                                    <SearchIcon />
                                </InputAdornment>
                            ),
                        }}
                    />
                </Grid>
                <Grid item xs={6} md={2}>
                    <FormControl fullWidth>
                        <InputLabel>Trạng thái</InputLabel>
                        <Select
                            value={filterStatus}
                            label="Trạng thái"
                            onChange={(e) => setFilterStatus(e.target.value)}
                        >
                            <MenuItem value="ALL">Tất cả</MenuItem>
                            <MenuItem value="PENDING">Chờ thanh toán</MenuItem>
                            <MenuItem value="CONFIRMED">Đã xác nhận</MenuItem>
                            <MenuItem value="CANCELLED">Đã hủy</MenuItem>
                            <MenuItem value="FAILED">Lỗi</MenuItem>
                        </Select>
                    </FormControl>
                </Grid>
                <Grid item xs={6} md={2}>
                    <FormControl fullWidth>
                        <InputLabel>Sắp xếp</InputLabel>
                        <Select
                            value={sortBy}
                            label="Sắp xếp"
                            onChange={(e) => setSortBy(e.target.value)}
                        >
                            <MenuItem value="NEWEST">Mới nhất</MenuItem>
                            <MenuItem value="OLDEST">Cũ nhất</MenuItem>
                            <MenuItem value="PRICE_HIGH">Giá cao → thấp</MenuItem>
                            <MenuItem value="PRICE_LOW">Giá thấp → cao</MenuItem>
                        </Select>
                    </FormControl>
                </Grid>
                <Grid item xs={12} md={4}>
                    <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
                        <Button 
                            variant="outlined" 
                            onClick={fetchAllBookings}
                            disabled={loading}
                            startIcon={<span>🔄</span>}
                        >
                            {loading ? 'Đang tải...' : 'Tải lại'}
                        </Button>
                        <Button 
                            variant="outlined"
                            onClick={() => {
                                setSearchTerm('');
                                setFilterStatus('ALL');
                                setSortBy('NEWEST');
                            }}
                            startIcon={<FilterListIcon />}
                        >
                            Đặt lại
                        </Button>
                    </Box>
                </Grid>
            </Grid>
        </Paper>
    );

    // --- Giao diện chính ---
    if (loading) {
        return ( 
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh', flexDirection: 'column' }}>
                <CircularProgress />
                <Typography sx={{ mt: 2 }}>Đang tải danh sách booking...</Typography>
            </Box> 
        );
    }

    if (error) {
        return (
            <Container maxWidth="sm" sx={{ mt: 4 }}>
                <Alert severity="error">{error}</Alert>
                <Button 
                    variant="contained"
                    onClick={fetchAllBookings}
                    sx={{ mt: 2, mr: 2 }}
                >
                    🔄 Thử lại
                </Button>
                <Button 
                    component={Link} 
                    to={localStorage.getItem('token') ? "/admin" : "/login"} 
                    sx={{ mt: 2 }}
                >
                    {localStorage.getItem('token') ? "🏠 Quay lại Trang quản trị" : "🔑 Đi đến trang Đăng nhập"}
                </Button>
            </Container>
        );
    }

    return (
        <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
            <Typography variant="h4" gutterBottom component="h1" sx={{ fontWeight: 'bold' }}>
                📋 Quản lý Đặt vé (Admin)
            </Typography>

            {/* Thông báo Lỗi/Thành công */}
            {cancelError && (
                <Alert severity="error" sx={{ mb: 2 }} onClose={() => setCancelError('')}>
                    {cancelError}
                </Alert>
            )}
            {cancelSuccess && (
                <Alert severity="success" sx={{ mb: 2 }} onClose={() => setCancelSuccess('')}>
                    {cancelSuccess}
                </Alert>
            )}

            {/* Thống kê */}
            {renderStats()}

            {/* Thanh công cụ */}
            {renderToolbar()}

            {/* Thanh Tabs */}
            <Paper sx={{ mb: 2 }}>
                <Tabs 
                    value={selectedTab} 
                    onChange={handleTabChange} 
                    variant="scrollable"
                    scrollButtons="auto"
                >
                    <Tab label={`📊 Tất cả (${allBookings.length})`} />
                    <Tab label={`⏳ Chờ thanh toán (${pendingBookings.length})`} />
                    <Tab label={`✅ Đã xác nhận (${confirmedBookings.length})`} />
                    <Tab label={`❌ Đã hủy/Lỗi (${cancelledOrFailedBookings.length})`} />
                </Tabs>
            </Paper>

            {/* Nội dung Tab */}
            <Paper elevation={2}>
                {/* Tab Tất cả */}
                <Box role="tabpanel" hidden={selectedTab !== 0}>
                    {selectedTab === 0 && renderBookingList(allBookings)}
                </Box>
                
                {/* Tab Chờ thanh toán */}
                <Box role="tabpanel" hidden={selectedTab !== 1}>
                    {selectedTab === 1 && renderBookingList(pendingBookings)}
                </Box>
                
                {/* Tab Đã xác nhận */}
                <Box role="tabpanel" hidden={selectedTab !== 2}>
                    {selectedTab === 2 && renderBookingList(confirmedBookings)}
                </Box>
                
                {/* Tab Đã hủy/Lỗi */}
                <Box role="tabpanel" hidden={selectedTab !== 3}>
                    {selectedTab === 3 && renderBookingList(cancelledOrFailedBookings)}
                </Box>
            </Paper>

            <Button component={Link} to="/admin" sx={{ mt: 3 }}>
                ← Quay lại Trang quản trị
            </Button>
        </Container>
    );
}

export default AdminBookingsPage;