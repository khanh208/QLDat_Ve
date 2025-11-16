// src/components/FeedbackSection.js
import React, { useEffect, useState } from 'react';
import api from '../api';
import {
    Container,
    Typography,
    Box,
    Grid,
    Card,
    CardContent,
    Rating,
    Avatar,
    CircularProgress,
    Alert,
    Button,
    Chip
} from '@mui/material';
import FormatQuoteIcon from '@mui/icons-material/FormatQuote';
import PersonIcon from '@mui/icons-material/Person';

function FeedbackSection() {
    const [feedbacks, setFeedbacks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [displayCount, setDisplayCount] = useState(6); // Số feedback hiển thị ban đầu

    useEffect(() => {
        fetchAllFeedbacks();
    }, []);

    const fetchAllFeedbacks = async () => {
        setLoading(true);
        setError('');
        try {
            // ⭐ Gọi endpoint public (không cần authentication)
            const response = await api.get('/feedback/public');
            
            // Data đã được filter và sort ở backend
            const feedbacksData = response.data || [];
            
            setFeedbacks(feedbacksData);
            console.log('✅ Loaded public feedbacks:', feedbacksData.length);
            
        } catch (err) {
            console.error("❌ Error loading feedbacks:", err);
            // Nếu có lỗi, không hiển thị section (return null)
            setError('Unable to load feedbacks');
            setFeedbacks([]);
        } finally {
            setLoading(false);
        }
    };

    const formatDate = (dateTime) => {
        if (!dateTime) return '';
        return new Date(dateTime).toLocaleDateString('vi-VN', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        });
    };

    const getAvatarColor = (name) => {
        const colors = ['#1976d2', '#388e3c', '#d32f2f', '#f57c00', '#7b1fa2', '#0097a7'];
        const index = name ? name.charCodeAt(0) % colors.length : 0;
        return colors[index];
    };

    const handleLoadMore = () => {
        setDisplayCount(prev => prev + 6);
    };

    if (loading) {
        return (
            <Box sx={{ py: 8, textAlign: 'center', backgroundColor: '#f5f5f5' }}>
                <CircularProgress />
                <Typography sx={{ mt: 2 }}>Đang tải đánh giá...</Typography>
            </Box>
        );
    }

    // Nếu không có feedback hoặc có lỗi, không hiển thị section này
    if (error || feedbacks.length === 0) {
        return null;
    }

    return (
        <Box sx={{ py: 8, backgroundColor: '#f5f5f5' }}>
            <Container maxWidth="lg">
                {/* Header */}
                <Box sx={{ textAlign: 'center', mb: 6 }}>
                    <Typography 
                        variant="h4" 
                        gutterBottom 
                        sx={{ fontWeight: 'bold', color: '#1a237e' }}
                    >
                        💬 Khách hàng nói gì về chúng tôi
                    </Typography>
                    <Typography variant="body1" color="text.secondary">
                        Hàng ngàn khách hàng đã tin tưởng và sử dụng dịch vụ của chúng tôi
                    </Typography>
                    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', mt: 2 }}>
                        <Rating value={4.8} precision={0.1} readOnly size="large" />
                        <Typography variant="h6" sx={{ ml: 2, fontWeight: 'bold' }}>
                            4.8/5
                        </Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ ml: 1 }}>
                            ({feedbacks.length}+ đánh giá)
                        </Typography>
                    </Box>
                </Box>

                {/* Feedback Grid */}
                <Grid container spacing={3}>
                    {feedbacks.slice(0, displayCount).map((feedback, index) => (
                        <Grid item xs={12} sm={6} md={4} key={feedback.feedbackId || index}>
                            <Card 
                                elevation={2}
                                sx={{ 
                                    height: '100%',
                                    display: 'flex',
                                    flexDirection: 'column',
                                    transition: 'transform 0.2s, box-shadow 0.2s',
                                    '&:hover': {
                                        transform: 'translateY(-4px)',
                                        boxShadow: 6
                                    },
                                    position: 'relative',
                                    overflow: 'visible'
                                }}
                            >
                                {/* Quote Icon */}
                                <FormatQuoteIcon 
                                    sx={{ 
                                        position: 'absolute',
                                        top: -10,
                                        left: 20,
                                        fontSize: 50,
                                        color: '#1976d2',
                                        opacity: 0.2
                                    }} 
                                />

                                <CardContent sx={{ flexGrow: 1, pt: 3 }}>
                                    {/* Rating */}
                                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                        <Rating 
                                            value={feedback.rating} 
                                            readOnly 
                                            size="small"
                                        />
                                        <Chip 
                                            label={`${feedback.rating}/5`}
                                            size="small"
                                            color="primary"
                                            sx={{ ml: 1, fontWeight: 'bold' }}
                                        />
                                    </Box>

                                    {/* Comment */}
                                    <Typography 
                                        variant="body2" 
                                        color="text.secondary"
                                        sx={{ 
                                            mb: 2,
                                            minHeight: '80px',
                                            fontStyle: 'italic',
                                            lineHeight: 1.6
                                        }}
                                    >
                                        "{feedback.comment || 'Dịch vụ tốt, tôi rất hài lòng!'}"
                                    </Typography>

                                    {/* User Info */}
                                    <Box sx={{ display: 'flex', alignItems: 'center', mt: 'auto' }}>
                                        <Avatar 
                                            sx={{ 
                                                bgcolor: getAvatarColor(feedback.user?.fullName || 'A'),
                                                width: 40,
                                                height: 40
                                            }}
                                        >
                                            {feedback.user?.fullName?.charAt(0).toUpperCase() || <PersonIcon />}
                                        </Avatar>
                                        <Box sx={{ ml: 1.5 }}>
                                            <Typography 
                                                variant="subtitle2" 
                                                sx={{ fontWeight: 'bold' }}
                                            >
                                                {feedback.user?.fullName || 'Khách hàng'}
                                            </Typography>
                                            <Typography variant="caption" color="text.secondary">
                                                {formatDate(feedback.feedbackTime)}
                                            </Typography>
                                        </Box>
                                    </Box>

                                    {/* Trip Info (nếu có) */}
                                    {feedback.trip && (
                                        <Box 
                                            sx={{ 
                                                mt: 2, 
                                                pt: 2, 
                                                borderTop: '1px solid #eee' 
                                            }}
                                        >
                                            <Typography 
                                                variant="caption" 
                                                color="primary"
                                                sx={{ fontWeight: 'medium' }}
                                            >
                                                🚌 {feedback.trip.route?.startLocation} → {feedback.trip.route?.endLocation}
                                            </Typography>
                                        </Box>
                                    )}
                                </CardContent>
                            </Card>
                        </Grid>
                    ))}
                </Grid>

                {/* Load More Button */}
                {displayCount < feedbacks.length && (
                    <Box sx={{ textAlign: 'center', mt: 4 }}>
                        <Button 
                            variant="outlined" 
                            size="large"
                            onClick={handleLoadMore}
                        >
                            Xem thêm đánh giá ({feedbacks.length - displayCount} đánh giá khác)
                        </Button>
                    </Box>
                )}

                {/* CTA Section */}
                
            </Container>
        </Box>
    );
}

export default FeedbackSection;