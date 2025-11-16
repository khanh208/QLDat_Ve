import React, { useState } from 'react';
import api from '../api';
import { useNavigate, Link } from 'react-router-dom';
// --- IMPORT MUI COMPONENTS ---
import {
    Container,
    Box,
    Typography,
    TextField,
    Button,
    Alert,
    CircularProgress,
    Grid // For link back to Login
} from '@mui/material';
// ----------------------------

function ForgotPasswordPage() {
    const [email, setEmail] = useState('');
    const [message, setMessage] = useState(''); // To show success/info message
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setMessage('');
        setLoading(true);

        try {
            // Call the forgot password API
            const response = await api.post('/auth/forgot-password', { email });
            setMessage(response.data); // Show the "If email exists..." message
            // Optionally, redirect to reset password page after a delay or keep user here
            // navigate('/reset-password', { state: { email: email } }); // Option 1: Redirect immediately
            alert("Mã OTP đã được gửi (nếu email tồn tại). Vui lòng kiểm tra email."); // Option 2: Alert

        } catch (err) {
            console.error("Forgot Password error:", err.response || err);
            // Don't show specific errors for security, just a generic one
            setError('Đã xảy ra lỗi. Vui lòng thử lại.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container component="main" maxWidth="xs">
            <Box
                sx={{
                    marginTop: 8,
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                }}
            >
                <Typography component="h1" variant="h5">
                    Quên Mật khẩu
                </Typography>
                <Typography component="p" sx={{ mt: 1, textAlign: 'center' }}>
                    Nhập địa chỉ email của bạn để nhận mã OTP đặt lại mật khẩu.
                </Typography>
                <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
                    {/* Display error */}
                    {error && <Alert severity="error" sx={{ width: '100%', mb: 2 }}>{error}</Alert>}
                    {/* Display success/info message */}
                    {message && <Alert severity="info" sx={{ width: '100%', mb: 2 }}>{message}</Alert>}

                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        id="email"
                        label="Địa chỉ Email"
                        name="email"
                        autoComplete="email"
                        autoFocus
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        disabled={loading}
                    />

                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        sx={{ mt: 3, mb: 2 }}
                        disabled={loading}
                    >
                        {loading ? <CircularProgress size={24} color="inherit" /> : "Gửi mã OTP"}
                    </Button>

                    {/* Link back to Login Page */}
                    <Grid container justifyContent="flex-end">
                        <Grid item>
                            <Link to="/login" style={{ textDecoration: 'none' }}>
                                <Typography variant="body2">
                                    Quay lại Đăng nhập
                                </Typography>
                            </Link>
                        </Grid>
                    </Grid>
                </Box>
            </Box>
        </Container>
    );
}

export default ForgotPasswordPage;