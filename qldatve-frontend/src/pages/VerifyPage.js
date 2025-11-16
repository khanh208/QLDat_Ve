import React, { useState, useEffect } from 'react'; // Add useEffect
import api from '../api';
import { useLocation, useNavigate } from 'react-router-dom';
// --- IMPORT MUI COMPONENTS ---
import {
    Container,
    Box,
    Typography,
    TextField,
    Button,
    Alert,
    CircularProgress,
    Link as MuiLink // Use MuiLink for potential links
} from '@mui/material';
// ----------------------------

function VerifyPage() {
    const [code, setCode] = useState('');
    const [error, setError] = useState('');
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(false); // Add loading state
    const navigate = useNavigate();
    const location = useLocation();

    // Lấy email từ state (nếu không có, quay về đăng ký)
    const email = location.state?.email;
    useEffect(() => {
        if (!email) {
            console.warn("VerifyPage: No email found in location state, redirecting to register.");
            navigate('/register');
        }
    }, [email, navigate]); // Dependency array includes email and navigate


    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setMessage('');
        setLoading(true); // Start loading

        if (!email) {
             setError('Không tìm thấy email để xác thực.');
             setLoading(false);
             return;
        }

        try {
            const response = await api.post('/auth/verify', {
                email: email,
                code: code
            });
            setMessage(response.data); // "Tài khoản đã được kích hoạt thành công!"

            // Automatically navigate to login after a delay
            setTimeout(() => {
                navigate('/login');
            }, 3000);

        } catch (err) {
            console.error("Verify error:", err.response || err);
            setError(err.response?.data || 'Mã OTP không chính xác hoặc đã hết hạn.');
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
                    Xác thực tài khoản
                </Typography>
                <Typography component="p" sx={{ mt: 1, textAlign: 'center' }}>
                    Một mã OTP 5 số đã được gửi đến email: <br/><strong>{email || '...'}</strong>
                </Typography>
                <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
                    {/* Display error */}
                    {error && <Alert severity="error" sx={{ width: '100%', mb: 2 }}>{error}</Alert>}
                    {/* Display success message */}
                    {message && <Alert severity="success" sx={{ width: '100%', mb: 2 }}>{message} Đang chuyển đến trang đăng nhập...</Alert>}

                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        id="code"
                        label="Mã OTP (5 số)"
                        name="code"
                        autoFocus
                        value={code}
                        onChange={(e) => setCode(e.target.value)}
                        disabled={loading || !!message} // Disable if loading or success
                        inputProps={{ maxLength: 5 }} // Limit input length
                    />

                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        sx={{ mt: 3, mb: 2 }}
                        disabled={loading || !!message} // Disable if loading or success
                    >
                        {loading ? <CircularProgress size={24} color="inherit" /> : "Xác thực"}
                    </Button>
                    {/* TODO: Add a "Resend Code" button/link */}
                </Box>
            </Box>
        </Container>
    );
}

export default VerifyPage;