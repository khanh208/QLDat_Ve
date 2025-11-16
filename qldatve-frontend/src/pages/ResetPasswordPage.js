import React, { useState, useEffect } from 'react';
import api from '../api';
import { useNavigate, useLocation, Link } from 'react-router-dom'; // Added useLocation
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

function ResetPasswordPage() {
    const [formData, setFormData] = useState({
        email: '',
        code: '',
        newPassword: ''
    });
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const location = useLocation(); // To potentially get email from state

    // --- Try to get email from previous page state ---
    useEffect(() => {
        if (location.state?.email) {
            setFormData(prev => ({ ...prev, email: location.state.email }));
        }
    }, [location.state]);


    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setMessage('');
        setLoading(true);

        try {
            // Call the reset password API
            const response = await api.post('/api/auth/reset-password', formData);
            setMessage(response.data); // "Mật khẩu đã được thay đổi..."

            // Automatically navigate to login after success
            setTimeout(() => {
                navigate('/login');
            }, 3000);

        } catch (err) {
            console.error("Reset Password error:", err.response || err);
            setError(err.response?.data || 'Đặt lại mật khẩu thất bại. Mã OTP không đúng hoặc đã hết hạn.');
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
                    Đặt Lại Mật khẩu
                </Typography>
                <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
                    {error && <Alert severity="error" sx={{ width: '100%', mb: 2 }}>{error}</Alert>}
                    {message && <Alert severity="success" sx={{ width: '100%', mb: 2 }}>{message} Đang chuyển đến trang đăng nhập...</Alert>}

                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        id="email"
                        label="Địa chỉ Email"
                        name="email"
                        autoComplete="email"
                        value={formData.email} // Pre-fill if passed from previous page
                        onChange={handleChange}
                        disabled={loading || !!message}
                        autoFocus // Focus email first
                    />
                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        id="code"
                        label="Mã OTP (5 số)"
                        name="code"
                        value={formData.code}
                        onChange={handleChange}
                        disabled={loading || !!message}
                        inputProps={{ maxLength: 5 }}
                    />
                     <TextField
                        margin="normal"
                        required
                        fullWidth
                        name="newPassword"
                        label="Mật khẩu mới"
                        type="password"
                        id="newPassword"
                        autoComplete="new-password"
                        value={formData.newPassword}
                        onChange={handleChange}
                        disabled={loading || !!message}
                    />

                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        sx={{ mt: 3, mb: 2 }}
                        disabled={loading || !!message}
                    >
                        {loading ? <CircularProgress size={24} color="inherit" /> : "Đặt lại mật khẩu"}
                    </Button>

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

export default ResetPasswordPage;