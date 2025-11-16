// src/pages/TripDetailPage.js
import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate, useSearchParams } from 'react-router-dom';
import FeedbackList from '../components/FeedbackList';
import FeedbackForm from '../components/FeedbackForm';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import api from '../api';
import { useNotification } from '../contexts/NotificationContext';
import {
    Container, Typography, Paper, Box, Grid, Divider,
    Button, Chip, Radio, RadioGroup, FormControlLabel, FormControl, FormLabel,
    CircularProgress, Alert, Accordion, AccordionSummary, AccordionDetails
} from '@mui/material';

// ===== Helpers (Defined outside the component) =====
const useAuth = () => {
  const userString = localStorage.getItem('user');
  if (!userString) return null;
  try {
    return JSON.parse(userString);
  } catch (e) {
    console.error('Error parsing user data from localStorage:', e);
    localStorage.removeItem('user'); localStorage.removeItem('token');
    return null;
  }
};

const fmtVND = (n) => Number(n || 0).toLocaleString('vi-VN');

// Helper to normalize API response data
const asData = (res) => {
  if (!res) return {};
  // Handle data that might be a JSON string inside res.data
  if (res.data && typeof res.data === 'string') {
    try {
      const parsed = JSON.parse(res.data);
      console.log('✅ Parsed JSON string from res.data:', parsed);
      return parsed;
    } catch (e) {
      console.error('❌ Error parsing JSON from res.data:', e);
      return {}; // Return empty object on parse error
    }
  }
  // Handle data that is already an object
  if (res.data && typeof res.data === 'object') {
    return res.data;
  }
  // Handle case where res itself is the object
  if (typeof res === 'object' && !res.data) {
    return res;
  }
  return {};
};

// Helper to extract bookingId from various possible response structures
const extractBookingId = (obj) => {
  if (!obj) return null;
  console.log('🔍 extractBookingId - obj:', obj);

  // Handle string (try parsing as JSON first)
  if (typeof obj === 'string') {
    try {
      const parsed = JSON.parse(obj);
      return extractBookingId(parsed); // Recursive call on parsed object
    } catch (e) {
      console.error('❌ Error parsing string in extractBookingId:', e);
      // Fallback: regex search in the raw string
      const match = obj.match(/"bookingId":\s*(\d+)/);
      if (match && match[1]) {
        console.log('✅ Found bookingId via regex:', match[1]);
        return Number(match[1]);
      }
      return null;
    }
  }

  // Handle object
  if (typeof obj === 'object') {
    // Check common paths where bookingId might be
    const possiblePaths = [
      'bookingId', 'id',
      'booking.bookingId', 'booking.id',
      'data.bookingId', 'data.id',
      'result.bookingId', 'result.id',
      'object.bookingId', 'object.id'
    ];
    
    for (const path of possiblePaths) {
      // Safely access nested properties
      const value = path.split('.').reduce((acc, key) => acc?.[key], obj);
      if (value && !isNaN(Number(value))) {
        console.log(`✅ Found bookingId at path "${path}":`, value);
        return Number(value);
      }
    }
  }
  return null; // Not found
};

// ===== Main Component =====
function TripDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const loggedInUser = useAuth();
  const { showNotification } = useNotification(); // Snackbar hook

  // --- States ---
  const [newFeedbackSubmitted, setNewFeedbackSubmitted] = useState(null);
  const [trip, setTrip] = useState(null);
  const [bookedSeats, setBookedSeats] = useState(new Set());
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('MOMO');
  const [cashBookingSuccess, setCashBookingSuccess] = useState(false); // To disable button after cash success
  const [isBookingOrPaying, setIsBookingOrPaying] = useState(false);

  // --- Feedback Success Handler ---
  const handleFeedbackSuccess = (newFeedback) => {
    console.log("New feedback received in parent:", newFeedback);
    setNewFeedbackSubmitted(newFeedback); // Triggers list update
    showNotification("Cảm ơn bạn đã gửi phản hồi!", "success");
  };

  // --- Check for Payment Status from URL (MoMo Redirect) ---
  useEffect(() => {
    const paymentStatus = searchParams.get('payment');
    if (paymentStatus === 'success') {
        showNotification('🎉 Thanh toán MoMo thành công! Vé đã được xác nhận.', 'success');
        // Clear URL params after showing message
        setTimeout(() => setSearchParams({}), 5000);
        // Refetch seats to show updated status
        setTimeout(() => fetchBookedSeatsAgain(), 1000); // Short delay
    } else if (paymentStatus === 'fail') {
         showNotification('Thanh toán MoMo thất bại. Vui lòng thử lại.', 'error');
         setTimeout(() => setSearchParams({}), 5000);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams, setSearchParams, showNotification]); // Add deps

  // --- Load Trip Data and Booked Seats ---
  useEffect(() => {
    const fetchTripData = async () => {
      if (!id || isNaN(Number(id))) { setError('Trip ID không hợp lệ.'); setLoading(false); return; }
      setLoading(true); setError('');
      try {
        const [tripRes, seatsRes] = await Promise.all([
          api.get(`/trips/${id}`),
          api.get(`/trips/${id}/booked-seats`), // Fetches CONFIRMED + PENDING seats
        ]);
        const tripData = asData(tripRes);
        const seatsData = asData(seatsRes);

        if (!tripData?.tripId) throw new Error('Không tìm thấy thông tin chuyến đi.');
        setTrip(tripData);
        const seats = Array.isArray(seatsData) ? seatsData : [];
        setBookedSeats(new Set(seats));
      } catch (err) {
        console.error('Lỗi khi tải dữ liệu chuyến đi:', err);
        setError(err?.response?.data?.message || 'Không thể tải thông tin chuyến đi.');
        setTrip(null);
      } finally { setLoading(false); }
    };
    fetchTripData();
  }, [id]); // Dependency: id

  // --- Seat Selection Handler ---
  const handleSeatClick = (seatNumber) => {
    if (bookedSeats.has(seatNumber)) return;
    setSelectedSeats((prev) =>
      prev.includes(seatNumber)
        ? prev.filter((s) => s !== seatNumber)
        : [...prev, seatNumber]
    );
    setCashBookingSuccess(false); // Reset success state
  };

  // --- Refetch Seats Helper ---
  const fetchBookedSeatsAgain = async () => {
    try {
      const res = await api.get(`/trips/${id}/booked-seats`);
      const data = asData(res);
      const seats = Array.isArray(data) ? data : [];
      setBookedSeats(new Set(seats));
    } catch (err) { 
      console.error('Lỗi khi tải lại ghế đã đặt:', err); 
    }
  };

  // --- MoMo Checkout Handler ---
  const handleMoMoCheckout = async (bookingId) => {
    try {
      console.log('🟡 Gọi MoMo checkout với bookingId:', bookingId);
      const res = await api.post(`/payment/momo/checkout/${bookingId}`);
      const data = asData(res);
      const payUrl = data?.payUrl ?? data?.deeplink ?? data?.url;
      if (!payUrl) throw new Error('Không nhận được URL thanh toán MoMo.');
      window.location.href = payUrl; // Redirect current tab
    } catch (err) {
      console.error('❌ Lỗi khi tạo yêu cầu thanh toán MoMo:', err);
      const errorMsg = err?.response?.data?.error || err?.response?.data?.message || err?.message || 'Không thể tạo yêu cầu thanh toán MoMo.';
      showNotification(errorMsg, "error");
      setIsBookingOrPaying(false); // Stop loading on error
    }
  };

  // --- Main Booking Handler (Cash/MoMo) ---
  const handleBooking = async () => {
    if (!loggedInUser) { 
        showNotification('Vui lòng đăng nhập để đặt vé.', 'error');
        navigate('/login', { state: { from: `/trips/${id}` } }); 
        return; 
    }
    if (selectedSeats.length === 0) { 
        showNotification('Vui lòng chọn ít nhất một ghế.', 'error');
        return; 
    }

    setIsBookingOrPaying(true);
    setCashBookingSuccess(false);
    try {
      const payload = { 
        tripId: Number(id), 
        seatNumbers: selectedSeats, 
        paymentMethod 
      };
      
      console.log('🟡 Gửi request booking:', payload);
      const res = await api.post('/bookings', payload);
      let data = asData(res);
      console.log('🟡 Data sau khi asData:', data);

      if (paymentMethod === 'CASH') {
        const successMessage = data?.message || '💰 Đặt vé (Tiền mặt) thành công! Email xác nhận đã được gửi.';
        showNotification(successMessage, "success");
        setCashBookingSuccess(true);
        setSelectedSeats([]); 
        await fetchBookedSeatsAgain(); 
        setIsBookingOrPaying(false); 
        return;
      }

      // Handle MoMo
      let bookingId = extractBookingId(data);
      // Fallback search
      if (!bookingId && res.data && typeof res.data === 'string') {
        bookingId = extractBookingId(res.data);
      }
      // ... (Add other fallbacks if needed) ...

      if (!bookingId) {
        console.error('❌ Không thể extract bookingId từ response');
        throw new Error('Đã xảy ra lỗi khi tạo đơn hàng MoMo. Không nhận được ID đặt chỗ từ server.');
      }
      console.log('✅ BookingId cuối cùng:', bookingId);
      await handleMoMoCheckout(bookingId); // Proceed to payment
      
    } catch (err) {
      console.error('❌ Lỗi khi đặt vé/tạo thanh toán:', err);
      const errorMsg = err?.response?.data?.message || err?.response?.data?.error || err?.response?.data || err?.message || 'Đặt vé thất bại.';
      showNotification(errorMsg, "error");
      setIsBookingOrPaying(false);
    }
  };

  // --- Seat Rendering Function ---
  const renderSeats = () => {
    if (!trip?.vehicle?.totalSeats) {
      return <Typography color="text.secondary" sx={{ textAlign: 'center', mt: 2 }}>Không có thông tin ghế.</Typography>;
    }
    const totalSeats = trip.vehicle.totalSeats;
    const seatsPerRow = 4;
    const numRows = Math.ceil(totalSeats / seatsPerRow);
    const seatComponents = [];

    const getSeatNumber = (index) => {
      const row = Math.floor(index / seatsPerRow) + 1;
      const seatInRow = index % seatsPerRow;
      if (seatInRow === 0) return `A${row}`;
      if (seatInRow === 1) return `B${row}`;
      if (seatInRow === 2) return `C${row}`;
      return `D${row}`;
    };

    for (let i = 0; i < totalSeats; i++) {
      const seatNumber = getSeatNumber(i);
      const isBookedOrPending = bookedSeats.has(seatNumber);
      const isSelected = selectedSeats.includes(seatNumber);

      let chipColor = 'default';
      let chipVariant = 'outlined';
      let clickable = !isBookedOrPending;
      if (isBookedOrPending) { chipColor = 'error'; clickable = false; }
      if (isSelected) { chipColor = 'primary'; chipVariant = 'filled'; }

      const row = Math.floor(i / seatsPerRow) + 1;
      const seatInRow = i % seatsPerRow;
      const gridColumn = seatInRow < 2 ? seatInRow + 1 : seatInRow + 2;

      seatComponents.push(
        <Chip
          key={seatNumber} label={seatNumber} clickable={clickable}
          color={chipColor} variant={chipVariant}
          onClick={() => clickable && handleSeatClick(seatNumber)}
          sx={{
            m: 0.5, width: '55px', height: '35px', fontSize: '0.8rem',
            gridRow: row, gridColumn,
            fontWeight: isSelected ? 'bold' : 'normal',
            cursor: clickable ? 'pointer' : 'not-allowed',
            opacity: isBookedOrPending ? 0.6 : 1,
          }}
        />
      );
    }

    return (
      <Box sx={{
        mt: 3, mb: 3, display: 'grid',
        gridTemplateColumns: 'repeat(5, minmax(55px, auto))',
        gap: '8px 12px', justifyContent: 'center', maxWidth: '400px',
        mx: 'auto', p: 2, border: '1px dashed grey', borderRadius: '8px',
      }}>
        <Typography sx={{
          gridRow: `1 / ${numRows + 1}`, gridColumn: 3,
          writingMode: 'vertical-rl', textAlign: 'center', opacity: 0.6, alignSelf: 'center',
        }}>
          LỐI ĐI
        </Typography>
        {seatComponents}
      </Box>
    );
  };

  // --- Main Render ---
  if (loading) return (
    <Box sx={{ display: 'flex', justifyContent: 'center', my: 5 }}>
      <CircularProgress />
    </Box>
  );

  if (error) {
    return (
      <Container maxWidth="sm" sx={{ mt: 4 }}>
        <Alert severity="error">{error}</Alert>
        <Button component={Link} to="/" sx={{ mt: 2 }}>Quay lại Trang chủ</Button>
      </Container>
    );
  }

  if (!trip) {
    return (
      <Container maxWidth="sm" sx={{ mt: 4 }}>
        <Alert severity="warning">Không tìm thấy thông tin chuyến đi.</Alert>
        <Button component={Link} to="/" sx={{ mt: 2 }}>Quay lại Trang chủ</Button>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
      {/* --- Section 1: Trip Info & Booking --- */}
      <Paper sx={{ p: 3 }} elevation={3}>
        <Typography variant="h4" gutterBottom component="h1">
          🚌 Chi tiết chuyến đi #{trip.tripId}
        </Typography>

        {/* Fixed Grid props: add 'item' */}
        <Grid container spacing={2} sx={{ mt: 2, mb: 3 }}>
          <Grid item xs={12} sm={6}>
            <Typography variant="h6">📍 Tuyến đường:</Typography>
            <Typography>🚩 Điểm đi: {trip.route?.startLocation}</Typography>
            <Typography>🎯 Điểm đến: {trip.route?.endLocation}</Typography>
            <Typography variant="body2" color="text.secondary">
              📏 Khoảng cách: {trip.route?.distanceKm} km
            </Typography>
          </Grid>
          <Grid item xs={12} sm={6}>
            <Typography variant="h6">🚗 Thông tin xe:</Typography>
            <Typography>🔢 Biển số: {trip.vehicle?.licensePlate}</Typography>
            <Typography>🚐 Loại xe: {trip.vehicle?.vehicleType}</Typography>
            <Typography variant="body2" color="text.secondary">
              💺 Số ghế: {trip.vehicle?.totalSeats}
            </Typography>
          </Grid>
          <Grid item xs={12} sm={6}>
            <Typography variant="h6">⏰ Thời gian:</Typography>
            <Typography>🕒 Khởi hành: {new Date(trip.departureTime).toLocaleString('vi-VN')}</Typography>
            <Typography>🕓 Dự kiến đến: {new Date(trip.arrivalTime).toLocaleString('vi-VN')}</Typography>
          </Grid>
          <Grid item xs={12} sm={6}>
            <Typography variant="h6" color="primary">💰 Giá vé / ghế:</Typography>
            <Typography variant="h5" color="primary" sx={{ fontWeight: 'medium' }}>
              {fmtVND(trip.basePrice)} VND
            </Typography>
          </Grid>
        </Grid>

        <Divider sx={{ my: 3 }} />

        <Box sx={{ pt: 2 }}>
          <Typography variant="h6" align="center">💺 Chọn ghế:</Typography>
          <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 1, mb: 2 }}>
            <Chip label="Trống" variant="outlined" size="small" />
            <Chip label="Đang chọn" color="primary" size="small" />
            <Chip label="Đã đặt / Chờ TT" color="error" size="small" />
          </Box>
          {renderSeats()}
          {selectedSeats.length > 0 && (
            <Typography sx={{ mt: 2, textAlign: 'center', fontWeight: 'medium' }}>
              Ghế đang chọn: <strong>{selectedSeats.join(', ')}</strong> | Tổng tiền:{' '}
              <strong>{fmtVND(Number(trip.basePrice) * selectedSeats.length)} VND</strong>
            </Typography>
          )}
          <FormControl component="fieldset" sx={{ mt: 3, display: 'flex', justifyContent: 'center' }}>
            <FormLabel component="legend">💳 Chọn phương thức thanh toán:</FormLabel>
            <RadioGroup row name="paymentMethod" value={paymentMethod} onChange={(e) => setPaymentMethod(e.target.value)}>
              <FormControlLabel value="MOMO" control={<Radio />} label="Ví MoMo" />
              <FormControlLabel value="CASH" control={<Radio />} label="Tiền mặt (Thanh toán khi lên xe)" />
            </RadioGroup>
          </FormControl>
          {paymentMethod === 'MOMO' && (
                        <Alert severity="warning" sx={{ mt: 2, textAlign: 'center' }}>
                            <Typography variant="body2">
                                <strong>Lưu ý:</strong> Bạn sẽ có **15 phút** để hoàn tất thanh toán MoMo sau khi đặt.
                                <br/> Vé sẽ tự động hủy nếu quá hạn.
                            </Typography>
                        </Alert>
                    )}

          {/* Alerts: Removed bookingError/bookingSuccess, handled by Snackbar */}
          <Alert severity="info" sx={{ mt: 2 }}>
            <Typography variant="body2">
              <strong>📧 Thông báo:</strong> Hệ thống sẽ tự động gửi email xác nhận khi đặt vé thành công!
            </Typography>
          </Alert>
          {paymentMethod === 'MOMO' && (
            <Alert severity="info" sx={{ mt: 2 }}>
              <Typography variant="body2">
                <strong>🧪 Hướng dẫn test MoMo Sandbox:</strong><br />
                1. Chọn "Thanh toán với MoMo"<br />
                2. Trang MoMo sẽ mở ra<br />
                3. Dùng app MoMo Sandbox để quét QR hoặc chọn "Thanh toán bằng tài khoản MoMo Sandbox"<br />
                4. Sau khi thanh toán thành công, hệ thống sẽ tự động gửi email xác nhận
              </Typography>
            </Alert>
          )}
          
          <Box sx={{ textAlign: 'center' }}>
            <Button
              variant="contained" size="large"
              sx={{
                mt: 2, minWidth: '200px',
                backgroundColor: paymentMethod === 'MOMO' ? '#a50064' : '#1976d2',
                '&:hover': {
                  backgroundColor: paymentMethod === 'MOMO' ? '#8b0053' : '#1565c0',
                }
              }}
              onClick={handleBooking}
              disabled={isBookingOrPaying || selectedSeats.length === 0 || cashBookingSuccess}
            >
              {isBookingOrPaying
                ? <CircularProgress size={24} color="inherit" />
                : (paymentMethod === 'MOMO'
                  ? `💳 Thanh toán với MoMo (${selectedSeats.length} ghế)`
                  : `💰 Xác nhận đặt ${selectedSeats.length} ghế (Tiền mặt)`)
              }
            </Button>
          </Box>
        </Box>
        <Button component={Link} to="/" sx={{ mt: 3 }}>
          Quay lại danh sách
        </Button>
      </Paper>
      {/* --- END Section 1 --- */}

      {/* --- Section 2: Feedback --- */}
      <Paper sx={{ p: 3, mt: 3 }} elevation={2}>
        <Accordion defaultExpanded>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="h6">Đánh giá & Phản hồi</Typography>
            </AccordionSummary>
            <AccordionDetails sx={{ p: { xs: 1, sm: 2 } }}>
                <FeedbackList 
                    tripId={Number(id)} 
                    newFeedback={newFeedbackSubmitted}
                />
                <Divider sx={{ my: 3 }} />
                {loggedInUser ? (
                    <FeedbackForm 
                        tripId={Number(id)} 
                        onSubmitSuccess={handleFeedbackSuccess}
                    />
                ) : (
                    <Alert severity="info" sx={{ mt: 2 }}>
                        Vui lòng <Link to="/login" state={{ from: `/trips/${id}` }}>Đăng nhập</Link> để để lại phản hồi.
                    </Alert>
                )}
            </AccordionDetails>
        </Accordion>
      </Paper>
      {/* --- END Section 2 --- */}
    </Container>
  );
}

export default TripDetailPage;