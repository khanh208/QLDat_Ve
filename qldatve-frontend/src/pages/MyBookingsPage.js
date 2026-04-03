import React, { useEffect, useState, useCallback } from 'react';
import api from '../api';
import { Link, useSearchParams } from 'react-router-dom';
import {
    Container, Typography, Paper, Button,
    CircularProgress, Box, Alert, Chip,
    Tabs, Tab, Card, CardContent, Grid
} from '@mui/material';

function MyBookingsPage() {
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [cancelError, setCancelError] = useState('');
    const [cancelSuccess, setCancelSuccess] = useState('');
    const [selectedTab, setSelectedTab] = useState(0);
    const [paymentLoading, setPaymentLoading] = useState(null);
    const [searchParams, setSearchParams] = useSearchParams();
    const [autoRefreshCount, setAutoRefreshCount] = useState(0);

    // ⭐ Hàm Fetch Lịch sử Đặt vé với useCallback để tránh re-render loop
    const fetchMyBookings = useCallback(async (silent = false) => {
        try {
            if (!silent) {
                setError('');
                setCancelError('');
                setCancelSuccess('');
                setLoading(true);
            }
            
            const response = await api.get('/bookings/my-bookings');
            console.log("📦 MyBookingsPage - API Response:", response.data);
            
            let bookingsData = response.data;
            
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
            
            const sortedBookings = bookingsData.sort((a, b) => 
                new Date(b.bookingTime || 0) - new Date(a.bookingTime || 0)
            );
            
            setBookings(sortedBookings);
            console.log("✅ Loaded " + sortedBookings.length + " bookings");
            
        } catch (err) {
            console.error("❌ Lỗi khi tải lịch sử đặt vé:", err);
            if (err.response && (err.response.status === 401 || err.response.status === 403)) {
                setError("Vui lòng đăng nhập để xem lịch sử đặt vé.");
            } else {
                setError(err.response?.data?.message || "Không thể tải lịch sử đặt vé.");
            }
        } finally {
            if (!silent) {
                setLoading(false);
            }
        }
    }, []);

    // ⭐ useEffect chính: Xử lý payment callback và khởi tạo auto-refresh
    useEffect(() => {
        fetchMyBookings();
        
        const paymentStatus = searchParams.get('payment');
        const resultCode = searchParams.get('resultCode');
        const orderId = searchParams.get('orderId');
        
        console.log("🔍 Payment callback params:", { paymentStatus, resultCode, orderId });
        
        if (paymentStatus === 'success' || resultCode === '0') {
            setCancelSuccess('🎉 Thanh toán thành công! Đang cập nhật trạng thái vé...');
            
            // ⭐ Auto-refresh để kiểm tra trạng thái từ backend
            let refreshAttempts = 0;
            const maxAttempts = 5;
            const refreshInterval = setInterval(() => {
                refreshAttempts++;
                console.log(`🔄 Auto-refresh attempt ${refreshAttempts}/${maxAttempts}`);
                
                fetchMyBookings(true); // Silent refresh
                setAutoRefreshCount(refreshAttempts);
                
                if (refreshAttempts >= maxAttempts) {
                    clearInterval(refreshInterval);
                    console.log("✅ Auto-refresh completed");
                    setCancelSuccess('✅ Vé đã được xác nhận! Nếu trạng thái chưa cập nhật, vui lòng bấm "Tải lại".');
                }
            }, 3000); // Refresh mỗi 3 giây
            
            // Xóa params sau khi hiển thị thông báo
            setTimeout(() => {
                setSearchParams({});
            }, 5000);
            
            return () => clearInterval(refreshInterval);
        }
        
        if (paymentStatus === 'failed' || (resultCode && resultCode !== '0')) {
            setCancelError('❌ Thanh toán thất bại. Vui lòng thử lại.');
            setTimeout(() => setSearchParams({}), 5000);
        }
        
        // ⭐ Kiểm tra localStorage để xử lý payment pending
        const pendingPayment = localStorage.getItem('pendingPayment');
        if (pendingPayment) {
            console.log("📋 Found pending payment:", pendingPayment);
            setCancelSuccess('🔄 Đang kiểm tra trạng thái thanh toán...');
            
            // Refresh sau 2 giây
            setTimeout(() => {
                fetchMyBookings();
                localStorage.removeItem('pendingPayment');
            }, 2000);
        }
    }, [searchParams, setSearchParams, fetchMyBookings]);

    // ⭐ Hàm Hủy Vé
    const handleCancelBooking = async (bookingId) => {
        setCancelError('');
        setCancelSuccess('');
        if (!window.confirm(`Bạn có chắc muốn hủy vé #${bookingId} không?`)) {
            return;
        }
        try {
            await api.put(`/bookings/${bookingId}/cancel`);
            setCancelSuccess(`✅ Đã hủy vé #${bookingId} thành công.`);
            setTimeout(() => fetchMyBookings(), 1000);
        } catch (err) {
            console.error("❌ Lỗi khi hủy vé:", err);
            setCancelError(err.response?.data?.message || `❌ Lỗi khi hủy vé #${bookingId}.`);
        }
    };

    // ⭐ Hàm xử lý Thanh toán MoMo - Improved
    const handleMoMoPayment = async (bookingId) => {
        setPaymentLoading(bookingId);
        setError('');
        setCancelError('');
        setCancelSuccess('');
        
        try {
            console.log("💳 Initiating MoMo payment for booking:", bookingId);
            const response = await api.post(`/payment/momo/checkout/${bookingId}`);
            const payUrl = response.data.payUrl;
            
            if (!payUrl) {
                throw new Error("Không nhận được URL thanh toán từ MoMo");
            }
            
            console.log('🔗 MoMo Payment URL:', payUrl);
            
            // Lưu bookingId để tracking
            localStorage.setItem('pendingPayment', bookingId);
            localStorage.setItem('paymentStartTime', Date.now().toString());
            
            // Mở trong tab mới
            const paymentWindow = window.open(payUrl, '_blank', 'width=800,height=600');
            
            if (!paymentWindow) {
                console.warn("⚠️ Popup blocked, redirecting in current tab");
                window.location.href = payUrl;
                return;
            }
            
            // ⭐ Monitor payment window
            const checkInterval = setInterval(() => {
                try {
                    if (paymentWindow.closed) {
                        clearInterval(checkInterval);
                        console.log("🔍 Payment window closed, checking status...");
                        
                        setCancelSuccess('🔄 Đang kiểm tra kết quả thanh toán...');
                        
                        // Đợi 3 giây để backend xử lý IPN
                        setTimeout(() => {
                            fetchMyBookings();
                            
                            // Retry sau 3 giây nữa
                            setTimeout(() => {
                                fetchMyBookings();
                                localStorage.removeItem('pendingPayment');
                            }, 3000);
                        }, 3000);
                    }
                } catch (e) {
                    // Cross-origin error, continue checking
                }
            }, 1000);
            
            // Timeout sau 10 phút
            setTimeout(() => {
                clearInterval(checkInterval);
                if (!paymentWindow.closed) {
                    console.warn("⚠️ Payment timeout");
                }
            }, 600000);
            
        } catch (err) {
            console.error("❌ MoMo payment error:", err);
            setCancelError(
                err.response?.data?.error || 
                err.response?.data?.message || 
                "❌ Không thể tạo yêu cầu thanh toán MoMo."
            );
        } finally {
            setPaymentLoading(null);
        }
    };

    const handleTabChange = (event, newValue) => {
        setSelectedTab(newValue);
        setCancelError('');
        setCancelSuccess('');
    };

    // Lọc bookings theo trạng thái
    const allBookings = bookings || [];
    const pendingBookings = allBookings.filter(b => b.status === 'PENDING');
    const confirmedBookings = allBookings.filter(b => b.status === 'CONFIRMED');
    const cancelledBookings = allBookings.filter(b => b.status === 'CANCELLED');
    const failedBookings = allBookings.filter(b => b.status === 'FAILED');
    const cancelledOrFailedBookings = [...cancelledBookings, ...failedBookings];

    const formatVND = (amount) => {
        return new Intl.NumberFormat('vi-VN', { 
            style: 'currency', 
            currency: 'VND' 
        }).format(amount || 0);
    };

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
                        <Card
                            key={booking.bookingId}
                            data-testid={`booking-card-${booking.bookingId}`}
                            sx={{ mb: 2, border: 1, borderColor: 'divider' }}
                        >
                            <CardContent>
                                <Grid container spacing={2} alignItems="center">
                                    <Grid item xs={12} sm={8}>
                                        <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 1 }}>
                                            Mã vé: #{booking.bookingId}
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
                                                data-testid={`booking-status-${booking.bookingId}`}
                                                label={statusLabel}
                                                color={statusColor}
                                                variant={statusVariant}
                                                size="small"
                                                sx={{ fontWeight: 'bold', minWidth: '140px' }}
                                            />
                                            
                                            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, mt: 1 }}>
                                                {booking.status === 'CONFIRMED' && (
                                                    <Button
                                                        data-testid={`booking-cancel-${booking.bookingId}`}
                                                        variant="outlined"
                                                        color="error"
                                                        size="small"
                                                        onClick={() => handleCancelBooking(booking.bookingId)}
                                                        sx={{ minWidth: '120px' }}
                                                    >
                                                        Hủy vé
                                                    </Button>
                                                )}
                                                
                                                {booking.status === 'PENDING' && (
                                                    <Button
                                                        data-testid={`booking-momo-pay-${booking.bookingId}`}
                                                        variant="contained"
                                                        sx={{
                                                            backgroundColor: '#a50064',
                                                            color: 'white',
                                                            '&:hover': { backgroundColor: '#8b0053' },
                                                            minWidth: '120px'
                                                        }}
                                                        size="small"
                                                        onClick={() => handleMoMoPayment(booking.bookingId)}
                                                        disabled={paymentLoading === booking.bookingId}
                                                    >
                                                        {paymentLoading === booking.bookingId 
                                                            ? <CircularProgress size={16} color="inherit"/> 
                                                            : "💳 Thanh toán MoMo"
                                                        }
                                                    </Button>
                                                )}

                                                <Button
                                                    variant="outlined"
                                                    size="small"
                                                    component={Link}
                                                    to={`/trips/${booking.trip?.tripId}`}
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

    const renderStats = () => (
        <Box sx={{ mb: 3 }}>
            <Grid container spacing={2}>
                <Grid item xs={6} sm={3}>
                    <Paper sx={{ p: 2, textAlign: 'center', bgcolor: 'primary.main', color: 'white' }}>
                        <Typography variant="h6">{allBookings.length}</Typography>
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

    if (loading) {
        return ( 
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh', flexDirection: 'column' }}>
                <CircularProgress />
                <Typography sx={{ mt: 2 }}>Đang tải lịch sử đặt vé...</Typography>
            </Box> 
        );
    }

    if (error) {
        return (
            <Container maxWidth="sm" sx={{ mt: 4 }}>
                <Alert severity="error">{error}</Alert>
                <Button 
                    variant="contained"
                    onClick={() => fetchMyBookings()}
                    sx={{ mt: 2, mr: 2 }}
                >
                    🔄 Thử lại
                </Button>
                <Button 
                    component={Link} 
                    to={localStorage.getItem('token') ? "/" : "/login"} 
                    sx={{ mt: 2 }}
                >
                    {localStorage.getItem('token') ? "🏠 Quay lại Trang chủ" : "🔑 Đi đến trang Đăng nhập"}
                </Button>
            </Container>
        );
    }

    return (
        <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
            <Typography variant="h4" gutterBottom component="h1" sx={{ fontWeight: 'bold' }} data-testid="my-bookings-title">
                📋 Lịch sử Đặt vé của tôi
            </Typography>

            <Button 
                data-testid="my-bookings-refresh-button"
                variant="outlined" 
                onClick={() => fetchMyBookings()}
                disabled={loading}
                sx={{ mb: 2 }}
                startIcon={<span>🔄</span>}
            >
                {loading ? 'Đang tải...' : 'Tải lại danh sách'}
            </Button>

            {autoRefreshCount > 0 && (
                <Alert severity="info" sx={{ mb: 2 }}>
                    🔄 Đang tự động kiểm tra trạng thái thanh toán... (Lần {autoRefreshCount}/5)
                </Alert>
            )}

            {cancelError && (
                <Alert data-testid="my-bookings-error-alert" severity="error" sx={{ mb: 2 }} onClose={() => setCancelError('')}>
                    {cancelError}
                </Alert>
            )}
            {cancelSuccess && (
                <Alert data-testid="my-bookings-success-alert" severity="success" sx={{ mb: 2 }} onClose={() => setCancelSuccess('')}>
                    {cancelSuccess}
                </Alert>
            )}

            {renderStats()}

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

            <Paper elevation={2}>
                <Box role="tabpanel" hidden={selectedTab !== 0}>
                    {selectedTab === 0 && renderBookingList(allBookings)}
                </Box>
                
                <Box role="tabpanel" hidden={selectedTab !== 1}>
                    {selectedTab === 1 && renderBookingList(pendingBookings)}
                </Box>
                
                <Box role="tabpanel" hidden={selectedTab !== 2}>
                    {selectedTab === 2 && renderBookingList(confirmedBookings)}
                </Box>
                
                <Box role="tabpanel" hidden={selectedTab !== 3}>
                    {selectedTab === 3 && renderBookingList(cancelledOrFailedBookings)}
                </Box>
            </Paper>

            <Button component={Link} to="/" sx={{ mt: 3 }}>
                ← Quay lại Trang chủ
            </Button>
        </Container>
    );
}

export default MyBookingsPage;
