import React, { useEffect, useState } from 'react';
import api from '../api';
import { Link } from 'react-router-dom'; // Dùng cho link nội bộ (VD: /trips/1)
import {
    Container,
    Box,
    Grid,
    Paper,
    Card,
    CardContent,
    CardActions,
    Typography,
    CircularProgress,
    Alert,
    Button,
    Link as MuiLink // <-- 1. Import Link của MUI (dùng cho link bên ngoài)
} from '@mui/material';
import TripSearchForm from '../components/TripSearchForm';
// import FeedbackSection from '../components/FeedbackSection'; // <-- 4. Tạm thời comment out
import 'react-datepicker/dist/react-datepicker.css';

// --- Import Icons ---
import DirectionsBusIcon from '@mui/icons-material/DirectionsBus';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import PhoneIcon from '@mui/icons-material/Phone';
import EmailIcon from '@mui/icons-material/Email';
// import LocationOn from '@mui/icons-material/LocationOn'; // Bị trùng, xóa bớt 1 dòng
import FacebookIcon from '@mui/icons-material/Facebook';
import TwitterIcon from '@mui/icons-material/Twitter';
import InstagramIcon from '@mui/icons-material/Instagram';
// -------------------------------------------------------------------------------------

function HomePage() {
    // --- State variables ---
    const [allTrips, setAllTrips] = useState([]);
    const [displayedTrips, setDisplayedTrips] = useState([]); // Sửa: Dùng state này
    const [loadingInitial, setLoadingInitial] = useState(true);
    const [searching, setSearching] = useState(false);
    const [error, setError] = useState('');
    const [searchError, setSearchError] = useState('');
    const [isShowingAll, setIsShowingAll] = useState(false);
    // ----------------------

    // --- useEffect to load initial data ---
    useEffect(() => {
        const fetchAllTrips = async () => {
            setLoadingInitial(true);
            setError('');
            try {
                const response = await api.get('/trips');
                const tripsData = Array.isArray(response.data) ? response.data : [];
                setAllTrips(tripsData);
                setDisplayedTrips(tripsData.slice(0, 6)); // Hiển thị 6 chuyến đầu
                setIsShowingAll(false);
            } catch (err) {
                console.error("Lỗi khi tải tất cả chuyến đi:", err);
                setError("Không thể tải danh sách chuyến đi.");
            } finally {
                setLoadingInitial(false);
            }
        };
        fetchAllTrips();
    }, []);
    // -------------------------------------

    // --- Search handler function ---
    const handleSearch = async (searchParams) => {
        setSearching(true);
        setSearchError('');
        setIsShowingAll(true); // Kết quả tìm kiếm là 1 danh sách đầy đủ
        try {
            const response = await api.get('/trips/search', { params: searchParams });
            const results = Array.isArray(response.data) ? response.data : [];
            setDisplayedTrips(results); // Cập nhật danh sách hiển thị
            if (results.length === 0) {
                 setSearchError("Không tìm thấy chuyến đi phù hợp.");
            }
        } catch (err) {
            console.error("Lỗi khi tìm kiếm:", err);
            setSearchError("Lỗi khi tìm kiếm chuyến đi.");
            setDisplayedTrips([]);
        } finally {
            setSearching(false);
        }
    };
    // ----------------------------

    // --- Show all trips function ---
    const showAllTrips = () => {
        setDisplayedTrips(allTrips); // Hiển thị TẤT CẢ
        setIsShowingAll(true);
        setSearchError('');
    };
    // ----------------------------

    // --- JSX Rendering ---
    return (
        <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
            {/* Main Content */}
            <Box sx={{ flex: 1 }}>
                {/* --- HERO SECTION --- */}
                <Box
                    sx={{
                        pt: { xs: 6, sm: 10 },
                        pb: { xs: 8, sm: 12 },
                        backgroundImage: `linear-gradient(rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.4)), url(/hero-image.jpg)`,
                        backgroundSize: 'cover',
                        backgroundPosition: 'center',
                        color: '#fff',
                        textAlign: 'center',
                        display: 'flex',
                        alignItems: 'center',
                        minHeight: '40vh'
                    }}
                >
                    <Container maxWidth="md">
                        <Typography component="h1" variant="h2" sx={{ fontWeight: 'bold', mb: 2 }}>
                            Đặt Vé Xe Khách Dễ Dàng
                        </Typography>
                        <Typography variant="h5" color="rgba(255, 255, 255, 0.85)" paragraph sx={{ mb: 4 }}>
                            Tìm kiếm và đặt vé các tuyến xe chất lượng cao trên khắp cả nước.
                        </Typography>
                        <Paper elevation={4} sx={{ p: { xs: 2, sm: 3 }, backgroundColor: 'rgba(255, 255, 255, 0.95)', borderRadius: 2 }}>
                            <Typography variant="h6" color="primary.main" sx={{ mb: 2 }}>Tìm chuyến đi</Typography>
                            <TripSearchForm onSearch={handleSearch} />
                        </Paper>
                    </Container>
                </Box>
                {/* --- END HERO SECTION --- */}

                {/* --- ABOUT SECTION --- */}
                <Container sx={{ py: 4 }} maxWidth="md">
                    <Typography variant="h4" align="center" gutterBottom sx={{ fontWeight: 'medium' }}>
                        Tại sao chọn đặt vé xe trên Vé Xe Online?
                    </Typography>
                    <Grid container spacing={4} justifyContent="center" sx={{ mt: 2 }}>
                        <Grid item xs={12} sm={4} sx={{ textAlign: 'center' }}>
                            <Typography variant="h6" gutterBottom>Nhanh chóng</Typography>
                            <Typography color="text.secondary">Tìm kiếm và đặt vé chỉ trong vài phút.</Typography>
                        </Grid>
                        <Grid item xs={12} sm={4} sx={{ textAlign: 'center' }}>
                            <Typography variant="h6" gutterBottom>Tiện lợi</Typography>
                            <Typography color="text.secondary">Nhiều tuyến đường, nhiều nhà xe uy tín.</Typography>
                        </Grid>
                        <Grid item xs={12} sm={4} sx={{ textAlign: 'center' }}>
                            <Typography variant="h6" gutterBottom>An toàn</Typography>
                            <Typography color="text.secondary">Thông tin rõ ràng, thanh toán bảo mật.</Typography>
                        </Grid>
                    </Grid>
                </Container>
                {/* --- END ABOUT SECTION --- */}

                {/* --- RESULTS SECTION --- */}
                <Container sx={{ py: 4, backgroundColor: '#f9f9f9' }} maxWidth="lg">
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                        <Typography variant="h5">
                            {isShowingAll ? 'Tất cả chuyến đi' : 'Các chuyến đi nổi bật'}
                        </Typography>
                        {!isShowingAll && (
                             <Button onClick={showAllTrips} disabled={searching || loadingInitial} variant="outlined" size="small">
                                Xem tất cả ({allTrips.length} chuyến)
                            </Button>
                        )}
                    </Box>

                    {/* Loading/Error states */}
                    {loadingInitial && ( <Box sx={{ display: 'flex', justifyContent: 'center', my: 5 }}><CircularProgress /></Box> )}
                    {error && !loadingInitial && ( <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert> )}

                    {!loadingInitial && !error && (
                        <Box>
                            {searching && <Box sx={{ display: 'flex', justifyContent: 'center', mb: 2 }}><CircularProgress size={30} /></Box>}
                            {searchError && !searching && (<Alert severity="warning" sx={{ mt: 2, mb: 2 }}>{searchError}</Alert>)}

                            <Grid container spacing={3}>
                                {displayedTrips.length === 0 && !searching ? (
                                    <Grid item xs={12}>
                                        <Typography align="center" color="text.secondary" sx={{ mt: 5 }}>
                                            {searchError ? 'Vui lòng thử tìm kiếm khác.' : 'Hiện không có chuyến đi nào.'}
                                        </Typography>
                                    </Grid>
                                ) : (
                                    displayedTrips.map((trip) => (
                                        <Grid item key={trip.tripId} xs={12} sm={6} md={4}>
                                            <Card elevation={2} sx={{ height: '100%', display: 'flex', flexDirection: 'column', transition: 'transform 0.2s, box-shadow 0.2s', '&:hover': { transform: 'translateY(-4px)', boxShadow: 6 } }}>
                                                <CardContent sx={{ flexGrow: 1 }}>
                                                    {/* Route */}
                                                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                                                        <LocationOnIcon fontSize="small" sx={{ mr: 1, color: 'action.active' }} />
                                                        <Typography variant="h6" component="div" noWrap>
                                                            {trip.route?.startLocation} - {trip.route?.endLocation}
                                                        </Typography>
                                                    </Box>
                                                    {/* Vehicle */}
                                                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                                                        <DirectionsBusIcon fontSize="small" sx={{ mr: 1, color: 'text.secondary' }} />
                                                        <Typography variant="body2" color="text.secondary">
                                                            {trip.vehicle?.licensePlate} ({trip.vehicle?.vehicleType})
                                                        </Typography>
                                                    </Box>
                                                    {/* Times */}
                                                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 0.5 }}>
                                                        <AccessTimeIcon fontSize="small" sx={{ mr: 1, color: 'text.secondary' }} />
                                                        <Typography variant="body2">
                                                            {new Date(trip.departureTime).toLocaleString('vi-VN')} - {new Date(trip.arrivalTime).toLocaleString('vi-VN')}
                                                        </Typography>
                                                    </Box>
                                                    {/* Price */}
                                                    <Box sx={{ display: 'flex', alignItems: 'baseline', mt: 2 }}>
                                                        <AttachMoneyIcon fontSize="small" sx={{ mr: 0.5, color: 'primary.main' }} />
                                                        <Typography variant="h6" color="primary">
                                                            {trip.basePrice?.toLocaleString('vi-VN')} VND
                                                        </Typography>
                                                         <Typography variant="caption" color="text.secondary" sx={{ml: 0.5}}>/ vé</Typography>
                                                    </Box>
                                                </CardContent>
                                                <CardActions sx={{ justifyContent: 'flex-end', borderTop: '1px solid #eee' }}>
                                                    {/* Dùng Link của react-router-dom */}
                                                    <Button size="small" component={Link} to={`/trips/${trip.tripId}`} variant="contained">
                                                        Chọn chuyến
                                                    </Button>
                                                </CardActions>
                                            </Card>
                                        </Grid>
                                    ))
                                )}
                            </Grid>
                            
                             {/* Hiển thị lại nút "Xem tất cả" nếu đang hiển thị kết quả tìm kiếm */}
                             {searching && !loadingInitial && (
                                 <Box sx={{ textAlign: 'center', mt: 4 }}>
                                    <Button onClick={showAllTrips} variant="outlined">
                                        Quay lại & Xem tất cả chuyến
                                    </Button>
                                </Box>
                             )}
                        </Box>
                    )}
                </Container>
                {/* --- END RESULTS SECTION --- */}

                {/* --- (TẠM THỜI COMMENT OUT FEEDBACK SECTION) --- */}
                {/* <FeedbackSection /> */}
                {/* --- END FEEDBACK SECTION --- */}
            </Box>

            {/* --- FOOTER SECTION --- */}
            <Box
                component="footer"
                sx={{
                    backgroundColor: '#1a237e',
                    color: 'white',
                    py: 6,
                    mt: 'auto'
                }}
            >
                <Container maxWidth="lg">
                    <Grid container spacing={4}>
                        {/* Company Info */}
                        <Grid item xs={12} md={4}>
                            <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold', color: '#ffd54f' }}>
                                🚌 Vé Xe Online
                            </Typography>
                            <Typography variant="body2" sx={{ mb: 2, lineHeight: 1.6 }}>
                                Hệ thống đặt vé xe khách trực tuyến hàng đầu Việt Nam. 
                                Chúng tôi cam kết mang đến dịch vụ chất lượng cao với 
                                giá cả hợp lý và trải nghiệm tốt nhất cho khách hàng.
                            </Typography>
                            <Box sx={{ display: 'flex', gap: 2, mt: 2 }}>
                                {/* --- 2. SỬA LẠI: DÙNG MuiLink --- */}
                                <MuiLink 
                                    href="https://www.facebook.com/qknguyen208/"
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    sx={{ color: 'inherit' }}
                                >
                                    <FacebookIcon sx={{ cursor: 'pointer', '&:hover': { color: '#ffd54f' } }} />
                                </MuiLink>
                                <MuiLink 
                                    href="https://www.twitter.com/YOUR_PROFILE_URL" 
                                    target="_blank" 
                                    rel="noopener noreferrer" 
                                    sx={{ color: 'inherit' }}
                                >
                                    <TwitterIcon sx={{ cursor: 'pointer', '&:hover': { color: '#ffd54f' } }} />
                                </MuiLink>
                                <MuiLink 
                                    href="https://www.instagram.com/khanh_nq04/"
                                    target="_blank" 
                                    rel="noopener noreferrer" 
                                    sx={{ color: 'inherit' }}
                                >
                                    <InstagramIcon sx={{ cursor: 'pointer', '&:hover': { color: '#ffd54f' } }} />
                                </MuiLink>
                                {/* ------------------------- */}
                            </Box>
                        </Grid>

                        {/* Contact Info */}
                        <Grid item xs={12} md={4}>
                            {/* --- 3. SỬA LỖI gutterBottom --- */}
                            <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold', color: '#ffd54f' }}>
                                📞 Liên Hệ
                            </Typography>
                            {/* ----------------------------- */}
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                <PhoneIcon sx={{ mr: 1, fontSize: '20px' }} />
                                <Typography variant="body2">
                                    Hotline: <strong>1900 1000</strong>
                                </Typography>
                            </Box>
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                <EmailIcon sx={{ mr: 1, fontSize: '20px' }} />
                                <Typography variant="body2">
                                    Email: <strong>khenng208@gmail.com</strong>
                                </Typography>
                            </Box>
                            <Box sx={{ display: 'flex', alignItems: 'flex-start', mb: 2 }}>
                                <LocationOnIcon sx={{ mr: 1, fontSize: '20px', mt: 0.5 }} />
                                <Typography variant="body2">
                                    Địa chỉ: <strong>Số 88, Đường Huỳnh Tấn Phát, Phường Tân Hưng, Quận 7,<br />Thành phố Hồ Chí Minh</strong>
                                </Typography>
                            </Box>
                        </Grid>
                    </Grid>

                    {/* Copyright */}
                    <Box 
                        sx={{ 
                            borderTop: '1px solid rgba(255, 255, 255, 0.1)', 
                            mt: 4, 
                            pt: 3, 
                            textAlign: 'center' 
                        }}
                    >
                        <Typography variant="body2" sx={{ opacity: 0.8 }}>
                            © {new Date().getFullYear()} Vé Xe Online. Tất cả các quyền được bảo lưu.
                        </Typography>
                        <Typography variant="caption" sx={{ opacity: 0.6, mt: 1, display: 'block' }}>
                            Giấy phép kinh doanh số: 0123456789 do Sở Kế hoạch & Đầu tư TP.HCM cấp ngày 01/01/2023
                        </Typography>
                    </Box>
                </Container>
            </Box>
            {/* --- END FOOTER SECTION --- */}
        </Box>
    );
}

export default HomePage;