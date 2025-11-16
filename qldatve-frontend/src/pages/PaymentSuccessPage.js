import React, { useEffect, useState } from 'react';
import { Container, Typography, Alert, CircularProgress, Button, Box, Paper } from '@mui/material';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import api from '../api'; // ⭐ THÊM IMPORT api

function PaymentSuccessPage() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const [statusMessage, setStatusMessage] = useState('Đang xử lý kết quả thanh toán...');
    const [isSuccess, setIsSuccess] = useState(null);
    const [details, setDetails] = useState({});
    const [error, setError] = useState('');
    const [countdown, setCountdown] = useState(5);
    const [isProcessing, setIsProcessing] = useState(true);

    useEffect(() => {
        console.log("🔍 PaymentSuccessPage loaded");
        console.log("📦 URL Search Params:", Object.fromEntries(searchParams));

        const verifyAndConfirmPayment = async () => {
            try {
                // ⭐ Lấy tất cả params từ MoMo redirect
                const partnerCode = searchParams.get('partnerCode');
                const orderId = searchParams.get('orderId');
                const requestId = searchParams.get('requestId');
                const amount = searchParams.get('amount');
                const orderInfo = searchParams.get('orderInfo');
                const orderType = searchParams.get('orderType');
                const transId = searchParams.get('transId');
                const resultCode = searchParams.get('resultCode');
                const message = searchParams.get('message');
                const payType = searchParams.get('payType');
                const responseTime = searchParams.get('responseTime');
                const extraData = searchParams.get('extraData');
                const signature = searchParams.get('signature');

                console.log("📊 Extracted MoMo Data:", {
                    partnerCode,
                    orderId,
                    requestId,
                    amount,
                    resultCode,
                    message,
                    transId
                });

                // Lưu details để hiển thị
                setDetails({
                    partnerCode,
                    orderId,
                    requestId,
                    amount,
                    orderInfo,
                    transId,
                    message,
                    payType,
                    resultCode
                });

                // ⭐ GIẢI PHÁP 3: Call API backend để confirm payment
                if (resultCode === '0' && orderId && requestId) {
                    console.log("✅ ResultCode = 0, calling backend to confirm...");
                    setStatusMessage('✅ Thanh toán thành công! Đang xác nhận với hệ thống...');
                    
                    try {
                        const confirmResponse = await api.post('/payment/momo/confirm', {
                            orderId,
                            requestId,
                            resultCode,
                            amount,
                            transId,
                            message,
                            orderInfo
                        });

                        console.log("✅ Backend confirm response:", confirmResponse.data);
                        
                        if (confirmResponse.data.status === 'success') {
                            setIsSuccess(true);
                            setStatusMessage('🎉 Thanh toán MoMo thành công!');
                            setError('');

                            // Xóa pending payment
                            localStorage.removeItem('pendingPayment');
                            localStorage.removeItem('paymentStartTime');

                            // Countdown và redirect
                            setIsProcessing(false);
                            startCountdown();

                        } else {
                            throw new Error('Backend confirmation failed');
                        }

                    } catch (confirmError) {
                        console.error("❌ Error confirming with backend:", confirmError);
                        
                        // Vẫn coi là thành công từ MoMo, nhưng có lỗi xác nhận
                        setIsSuccess(true);
                        setStatusMessage('⚠️ Thanh toán thành công nhưng có lỗi xác nhận');
                        setError('Vui lòng kiểm tra lại tại trang "Lịch sử đặt vé" sau 1-2 phút.');
                        setIsProcessing(false);
                        startCountdown();
                    }

                } else if (resultCode !== '0') {
                    // Thanh toán thất bại
                    console.log("❌ Payment failed with resultCode:", resultCode);
                    setIsSuccess(false);
                    setStatusMessage('❌ Thanh toán MoMo thất bại');
                    setError(message || 'Giao dịch không thành công. Vui lòng thử lại.');
                    setIsProcessing(false);
                    startCountdown();

                } else {
                    // Thiếu thông tin
                    console.error("❌ Missing orderId or requestId");
                    setIsSuccess(false);
                    setStatusMessage('❌ Thông tin thanh toán không đầy đủ');
                    setError('Vui lòng liên hệ hỗ trợ.');
                    setIsProcessing(false);
                }

            } catch (err) {
                console.error("❌ Error processing payment result:", err);
                setIsSuccess(false);
                setStatusMessage('❌ Có lỗi xảy ra khi xử lý kết quả thanh toán');
                setError(err.message || 'Lỗi không xác định');
                setIsProcessing(false);
            }
        };

        const startCountdown = () => {
            let timeLeft = 5;
            const timer = setInterval(() => {
                timeLeft--;
                setCountdown(timeLeft);
                
                if (timeLeft <= 0) {
                    clearInterval(timer);
                    console.log("🔄 Redirecting to My Bookings...");
                    
                    if (isSuccess) {
                        navigate('/my-bookings?payment=success&orderId=' + details.orderId);
                    } else {
                        navigate('/my-bookings?payment=failed');
                    }
                }
            }, 1000);
        };

        verifyAndConfirmPayment();
    }, [searchParams, navigate]); // ⭐ Removed isSuccess from dependencies

    // Format amount to VND
    const formatVND = (amount) => {
        if (!amount) return 'N/A';
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(parseInt(amount, 10));
    };

    return (
        <Container maxWidth="sm" sx={{ mt: 5, mb: 5 }}>
            <Paper elevation={3} sx={{ p: 4 }}>
                <Typography variant="h4" gutterBottom align="center" sx={{ fontWeight: 'bold' }}>
                    Kết quả Thanh toán MoMo
                </Typography>

                {/* Loading/Processing state */}
                {isProcessing && isSuccess === null && (
                    <Box sx={{ textAlign: 'center', my: 4 }}>
                        <CircularProgress size={60} />
                        <Typography sx={{ mt: 2 }}>{statusMessage}</Typography>
                        <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                            Đang xác nhận với hệ thống...
                        </Typography>
                    </Box>
                )}

                {/* Success state */}
                {!isProcessing && isSuccess === true && (
                    <Box sx={{ textAlign: 'center', my: 3 }}>
                        <CheckCircleOutlineIcon 
                            sx={{ fontSize: 80, color: 'success.main' }} 
                        />
                        <Alert severity="success" sx={{ mt: 2, mb: 2 }}>
                            {statusMessage}
                        </Alert>
                        <Typography variant="body1" color="text.secondary">
                            Vé của bạn đã được xác nhận thành công!
                        </Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                            Email xác nhận sẽ được gửi tới hộp thư của bạn.
                        </Typography>
                        {error && (
                            <Alert severity="warning" sx={{ mt: 2 }}>
                                {error}
                            </Alert>
                        )}
                    </Box>
                )}

                {/* Failed state */}
                {!isProcessing && isSuccess === false && (
                    <Box sx={{ textAlign: 'center', my: 3 }}>
                        <ErrorOutlineIcon 
                            sx={{ fontSize: 80, color: 'error.main' }} 
                        />
                        <Alert severity="error" sx={{ mt: 2, mb: 2 }}>
                            {statusMessage}
                        </Alert>
                        {error && (
                            <Typography variant="body1" color="error" sx={{ mt: 1 }}>
                                <strong>Lý do:</strong> {error}
                            </Typography>
                        )}
                        <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
                            Vui lòng thử lại hoặc liên hệ hỗ trợ nếu vấn đề vẫn tiếp diễn.
                        </Typography>
                    </Box>
                )}

                {/* Payment Details */}
                {details.orderId && (
                    <Box sx={{ 
                        mt: 3, 
                        p: 2, 
                        border: '1px solid #e0e0e0', 
                        borderRadius: 2,
                        bgcolor: '#f9f9f9'
                    }}>
                        <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold' }}>
                            📄 Chi tiết giao dịch
                        </Typography>
                        
                        {details.orderId && (
                            <Typography variant="body2" sx={{ mb: 1 }}>
                                <strong>Mã đơn hàng:</strong> {details.orderId}
                            </Typography>
                        )}
                        
                        {details.transId && (
                            <Typography variant="body2" sx={{ mb: 1 }}>
                                <strong>Mã giao dịch MoMo:</strong> {details.transId}
                            </Typography>
                        )}
                        
                        {details.amount && (
                            <Typography variant="body2" sx={{ mb: 1 }}>
                                <strong>Số tiền:</strong> {formatVND(details.amount)}
                            </Typography>
                        )}
                        
                        {details.orderInfo && (
                            <Typography variant="body2" sx={{ mb: 1 }}>
                                <strong>Nội dung:</strong> {details.orderInfo}
                            </Typography>
                        )}
                        
                        {details.payType && (
                            <Typography variant="body2" sx={{ mb: 1 }}>
                                <strong>Phương thức:</strong> {details.payType}
                            </Typography>
                        )}
                        
                        <Typography variant="body2" sx={{ mb: 1 }}>
                            <strong>Mã kết quả:</strong> {details.resultCode}
                        </Typography>
                    </Box>
                )}

                {/* Countdown */}
                {!isProcessing && isSuccess !== null && (
                    <Box sx={{ textAlign: 'center', mt: 3 }}>
                        <Typography variant="body2" color="text.secondary">
                            Tự động chuyển về trang Lịch sử đặt vé sau {countdown} giây...
                        </Typography>
                    </Box>
                )}

                {/* Action Buttons */}
                <Box sx={{ mt: 3, display: 'flex', justifyContent: 'center', gap: 2, flexWrap: 'wrap' }}>
                    <Button 
                        component={Link} 
                        to="/my-bookings" 
                        variant="contained" 
                        size="large"
                        sx={{ minWidth: '180px' }}
                    >
                        📋 Xem Lịch sử Đặt vé
                    </Button>
                    
                    <Button 
                        component={Link} 
                        to="/" 
                        variant="outlined"
                        size="large"
                    >
                        🏠 Về Trang chủ
                    </Button>
                </Box>

                {/* Debug Info (only in development) */}
                {process.env.NODE_ENV === 'development' && (
                    <Box sx={{ 
                        mt: 4, 
                        p: 2, 
                        bgcolor: '#f5f5f5', 
                        borderRadius: 1,
                        fontSize: '12px',
                        fontFamily: 'monospace'
                    }}>
                        <Typography variant="caption" sx={{ fontWeight: 'bold' }}>
                            🔍 DEBUG INFO (Dev Only):
                        </Typography>
                        <pre style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-all' }}>
                            {JSON.stringify({
                                isSuccess,
                                isProcessing,
                                statusMessage,
                                error,
                                details
                            }, null, 2)}
                        </pre>
                    </Box>
                )}
            </Paper>
        </Container>
    );
}

export default PaymentSuccessPage;