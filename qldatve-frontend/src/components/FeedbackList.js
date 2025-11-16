// src/components/FeedbackList.js
import React, { useEffect, useState } from 'react';
import api from '../api';
import { Box, Typography, List, ListItem, ListItemText, Divider, Rating, CircularProgress, Alert } from '@mui/material';

// Function component receiving tripId
function FeedbackList({ tripId, newFeedback }) { // Accept newFeedback prop
    const [feedbackList, setFeedbackList] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const fetchFeedback = async () => {
        if (!tripId) return;
        setLoading(true);
        setError('');
        try {
            const response = await api.get(`/trips/${tripId}/feedback`); // Public API
            setFeedbackList(Array.isArray(response.data) ? response.data : []);
        } catch (err) {
            console.error("Lỗi khi tải phản hồi:", err);
            setError("Không thể tải phản hồi.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchFeedback();
    }, [tripId]); // Fetch when tripId changes

     // --- Add new feedback to the top when submitted ---
     useEffect(() => {
        if (newFeedback) {
            setFeedbackList(prevList => [newFeedback, ...prevList]);
        }
    }, [newFeedback]); // Run when newFeedback prop changes

    if (loading) {
        return <Box sx={{ display: 'flex', justifyContent: 'center', my: 3 }}><CircularProgress size={30}/></Box>;
    }
    if (error) {
        return <Alert severity="warning" sx={{ mt: 2 }}>{error}</Alert>;
    }

    return (
        <Box sx={{ mt: 4 }}>
            <Typography variant="h6" gutterBottom>Phản hồi từ khách hàng</Typography>
            {feedbackList.length === 0 ? (
                <Typography color="text.secondary">Chưa có phản hồi nào cho chuyến đi này.</Typography>
            ) : (
                <List sx={{ bgcolor: 'background.paper', borderRadius: '8px' }}>
                    {feedbackList.map((fb, index) => (
                        <React.Fragment key={fb.feedbackId}>
                            <ListItem alignItems="flex-start">
                                <ListItemText
                                    primary={ // User and Rating
                                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 0.5 }}>
                                            <Typography sx={{ fontWeight: 'medium', mr: 1 }}>
                                                {fb.user?.username || 'Ẩn danh'}
                                            </Typography>
                                            <Rating value={fb.rating} readOnly size="small" />
                                        </Box>
                                    }
                                    secondary={ // Comment and Time
                                        <>
                                            <Typography
                                                component="span"
                                                variant="body2"
                                                color="text.primary"
                                                sx={{ display: 'block' }}
                                            >
                                                {fb.comment}
                                            </Typography>
                                             <Typography variant="caption" color="text.secondary">
                                                {new Date(fb.feedbackTime).toLocaleString('vi-VN')}
                                             </Typography>
                                        </>
                                    }
                                />
                            </ListItem>
                            {index < feedbackList.length - 1 && <Divider variant="inset" component="li" />}
                        </React.Fragment>
                    ))}
                </List>
            )}
        </Box>
    );
}

export default FeedbackList;