 // src/components/FeedbackForm.js
import React, { useState } from 'react';
import api from '../api';
import { Box, Typography, TextField, Button, Rating, Alert, CircularProgress } from '@mui/material';

// Function component receiving tripId and callback onSubmitSuccess
function FeedbackForm({ tripId, onSubmitSuccess }) {
    const [rating, setRating] = useState(0); // Rating state (0 to 5)
    const [comment, setComment] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        if (rating === 0) {
            setError("Vui lòng chọn số sao đánh giá.");
            return;
        }
        setLoading(true);

        try {
            const payload = {
                tripId: tripId,
                rating: rating,
                comment: comment
            };
            // Call API to submit feedback (requires user token)
            const response = await api.post('/feedback', payload);
            console.log("Feedback submitted:", response.data);
            // Reset form and call success callback
            setRating(0);
            setComment('');
            if (onSubmitSuccess) {
                onSubmitSuccess(response.data); // Pass new feedback data back
            }
        } catch (err) {
            console.error("Lỗi khi gửi phản hồi:", err.response || err);
            setError(err.response?.data?.message || "Không thể gửi phản hồi. Vui lòng thử lại.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <Box component="form" onSubmit={handleSubmit} sx={{ mt: 3, p: 2, border: '1px solid #eee', borderRadius: '8px' }}>
            <Typography variant="h6" gutterBottom>Để lại phản hồi</Typography>
            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            <Box sx={{ mb: 2, display: 'flex', alignItems: 'center' }}>
                <Typography component="legend" sx={{ mr: 1 }}>Đánh giá:</Typography>
                <Rating
                    name="rating"
                    value={rating}
                    onChange={(event, newValue) => {
                        setRating(newValue || 0); // Handle null case if user clears rating
                    }}
                    size="large"
                />
            </Box>

            <TextField
                label="Bình luận (tùy chọn)"
                name="comment"
                multiline
                rows={3}
                fullWidth
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                variant="outlined"
                margin="normal"
            />
            <Button
                type="submit"
                variant="contained"
                disabled={loading || rating === 0} // Disable if loading or no rating
                sx={{ mt: 1 }}
            >
                {loading ? <CircularProgress size={24} color="inherit" /> : "Gửi phản hồi"}
            </Button>
        </Box>
    );
}

export default FeedbackForm;