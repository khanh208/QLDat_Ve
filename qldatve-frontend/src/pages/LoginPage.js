import React, { useState } from 'react';
import api from '../api';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import {
    Container,
    Box,
    Typography,
    TextField,
    Button,
    Alert,
    CircularProgress,
    Grid
} from '@mui/material';

function LoginPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const location = useLocation();

    // ✅ Lấy redirect path từ state hoặc localStorage
    const from = location.state?.from || localStorage.getItem('redirectAfterLogin') || '/';

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            console.log('🟡 Attempting login with:', { username });
            
            const response = await api.post('/auth/login', {
                username: username,
                password: password
            });

            console.log('✅ Login response:', response.data);

            // ✅ FIX: Xử lý nhiều định dạng response khác nhau
            let token, userId, role, userData;
            
            if (response.data.token) {
                // Format 1: { token, userId, role }
                token = response.data.token;
                userId = response.data.userId;
                role = response.data.role;
                userData = { userId, username, role };
            } else if (response.data.data && response.data.data.token) {
                // Format 2: { data: { token, userId, role } }
                token = response.data.data.token;
                userId = response.data.data.userId;
                role = response.data.data.role;
                userData = { userId, username, role };
            } else if (response.data.accessToken) {
                // Format 3: { accessToken, user: { id, username, role } }
                token = response.data.accessToken;
                userId = response.data.user?.id || response.data.userId;
                role = response.data.user?.role || response.data.role;
                userData = { userId, username, role };
            } else {
                throw new Error('Định dạng phản hồi không hợp lệ từ máy chủ');
            }

            if (!token) {
                throw new Error('Không nhận được mã thông báo từ máy chủ');
            }

            // ✅ Lưu thông tin đăng nhập
            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify(userData));
            
            console.log('🔐 Login successful:', { userId, role, redirectTo: from });

            // ✅ Xóa redirectAfterLogin nếu có
            localStorage.removeItem('redirectAfterLogin');
            localStorage.removeItem('selectedSeats');

            // ✅ Redirect về trang trước đó hoặc trang chủ
            setTimeout(() => {
                if (role === 'ADMIN') {
                    navigate('/admin');
                } else {
                    navigate(from, { replace: true });
                }
            }, 100);

        } catch (err) {
            console.error('❌ Login error:', {
                status: err.response?.status,
                data: err.response?.data,
                message: err.message
            });
            
            // ✅ FIX: Xử lý lỗi chi tiết hơn
            let errorMessage = "Đăng nhập thất bại. Vui lòng kiểm tra lại.";
            
            if (err.response?.status === 401) {
                errorMessage = "Tên đăng nhập hoặc mật khẩu không đúng.";
            } else if (err.response?.status === 403) {
                errorMessage = "Tài khoản của bạn đã bị khóa.";
            } else if (err.response?.data?.message) {
                errorMessage = err.response.data.message;
            } else if (err.message) {
                errorMessage = err.message;
            }
            
            setError(errorMessage);
            
            // ✅ Xóa thông tin đăng nhập cũ nếu có lỗi
            localStorage.removeItem('token');
            localStorage.removeItem('user');
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
                <Typography component="h1" variant="h5" data-testid="login-page-title">
                    Đăng nhập
                </Typography>
                
                {/* ✅ Hiển thị thông báo redirect nếu có */}
                {from !== '/' && (
                    <Alert data-testid="login-redirect-alert" severity="info" sx={{ width: '100%', mt: 2 }}>
                        Vui lòng đăng nhập để tiếp tục
                    </Alert>
                )}

                <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1, width: '100%' }}>
                    {error && (
                        <Alert data-testid="login-error-alert" severity="error" sx={{ width: '100%', mb: 2 }}>
                            {error}
                        </Alert>
                    )}

                    <TextField
                        inputProps={{ 'data-testid': 'login-username-input' }}
                        margin="normal"
                        required
                        fullWidth
                        id="username"
                        label="Tên đăng nhập"
                        name="username"
                        autoComplete="username"
                        autoFocus
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        disabled={loading}
                    />
                    <TextField
                        inputProps={{ 'data-testid': 'login-password-input' }}
                        margin="normal"
                        required
                        fullWidth
                        name="password"
                        label="Mật khẩu"
                        type="password"
                        id="password"
                        autoComplete="current-password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        disabled={loading}
                    />

                    <Button
                        data-testid="login-submit-button"
                        type="submit"
                        fullWidth
                        variant="contained"
                        sx={{ mt: 3, mb: 2 }}
                        disabled={loading || !username || !password}
                    >
                        {loading ? <CircularProgress size={24} color="inherit" /> : "Đăng nhập"}
                    </Button>

                    <Grid container>
                        <Grid item xs>
                            <Link to="/forgot-password" style={{ textDecoration: 'none' }}>
                                <Typography variant="body2" color="primary">
                                    Quên mật khẩu?
                                </Typography>
                            </Link>
                        </Grid>
                        <Grid item>
                            <Link to="/register" style={{ textDecoration: 'none' }}>
                                <Typography variant="body2" color="primary">
                                    Chưa có tài khoản? Đăng ký
                                </Typography>
                            </Link>
                        </Grid>
                    </Grid>
                </Box>
            </Box>
        </Container>
    );
}

export default LoginPage;
