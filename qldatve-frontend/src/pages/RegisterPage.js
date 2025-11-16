import React, { useState } from 'react';
import api from '../api';
import { useNavigate, Link } from 'react-router-dom'; // Import Link
// --- IMPORT MUI COMPONENTS ---
import {
    Container,
    Box,
    Typography,
    TextField,
    Button,
    Alert,
    CircularProgress,
    Grid // To potentially link to Login
} from '@mui/material';
// ----------------------------

function RegisterPage() {
    const [formData, setFormData] = useState({
        username: '',
        password: '',
        email: '',
        fullName: '',
        phone: ''
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false); // Add loading state
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true); // Start loading

        try {
            // Role is set automatically on the backend
            const payload = {
                username: formData.username,
                password: formData.password,
                email: formData.email,
                fullName: formData.fullName,
                phone: formData.phone
            };
            await api.post('/auth/register', payload);

            // Navigate to Verify page on success
            navigate('/verify', { state: { email: formData.email } });

        } catch (err) {
            console.error("Register error:", err.response || err);
            setError(err.response?.data || 'Đăng ký thất bại. Vui lòng thử lại.');
        } finally {
            setLoading(false); // Stop loading
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
                    Đăng Ký Tài Khoản
                </Typography>
                <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 3 }}> {/* Increased margin top */}
                    {/* Display error using Alert */}
                    {error && <Alert severity="error" sx={{ width: '100%', mb: 2 }}>{error}</Alert>}

                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        id="username"
                        label="Tên đăng nhập"
                        name="username"
                        autoComplete="username"
                        autoFocus
                        value={formData.username}
                        onChange={handleChange}
                        disabled={loading}
                    />
                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        name="password"
                        label="Mật khẩu"
                        type="password"
                        id="password"
                        autoComplete="new-password" // Use new-password for registration
                        value={formData.password}
                        onChange={handleChange}
                        disabled={loading}
                    />
                     <TextField
                        margin="normal"
                        required
                        fullWidth
                        id="email"
                        label="Địa chỉ Email"
                        name="email"
                        autoComplete="email"
                        value={formData.email}
                        onChange={handleChange}
                        disabled={loading}
                    />
                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        id="fullName"
                        label="Tên đầy đủ"
                        name="fullName"
                        autoComplete="name"
                        value={formData.fullName}
                        onChange={handleChange}
                        disabled={loading}
                    />
                     <TextField
                        margin="normal"
                        required
                        fullWidth
                        id="phone"
                        label="Số điện thoại"
                        name="phone"
                        autoComplete="tel"
                        value={formData.phone}
                        onChange={handleChange}
                        disabled={loading}
                    />

                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        sx={{ mt: 3, mb: 2 }}
                        disabled={loading}
                    >
                        {loading ? <CircularProgress size={24} color="inherit" /> : "Đăng ký"}
                    </Button>

                    {/* Link back to Login Page */}
                    <Grid container justifyContent="flex-end">
                        <Grid item>
                            <Link to="/login" style={{ textDecoration: 'none' }}>
                                <Typography variant="body2">
                                    Đã có tài khoản? Đăng nhập
                                </Typography>
                            </Link>
                        </Grid>
                    </Grid>
                </Box>
            </Box>
        </Container>
    );
}

export default RegisterPage;