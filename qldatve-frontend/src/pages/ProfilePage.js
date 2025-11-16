import React, { useEffect, useState } from 'react';
import api from '../api';
import {
    Container, Typography, Paper, Box, Grid, TextField, Button,
    CircularProgress, Alert, Divider
} from '@mui/material';

function ProfilePage() {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // State cho Form Cập nhật Thông tin
    const [formData, setFormData] = useState({ fullName: '', phone: '' });
    const [updateSuccess, setUpdateSuccess] = useState('');
    const [updateError, setUpdateError] = useState('');
    const [isUpdating, setIsUpdating] = useState(false);

    // State cho Form Đổi Mật khẩu
    const [passwordData, setPasswordData] = useState({ oldPassword: '', newPassword: '' });
    const [passSuccess, setPassSuccess] = useState('');
    const [passError, setPassError] = useState('');
    const [isChangingPassword, setIsChangingPassword] = useState(false);

    // 1. Tải thông tin user khi vào trang
    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const response = await api.get('/profile'); // Gọi API mới
                setUser(response.data);
                // Điền thông tin vào form
                setFormData({
                    fullName: response.data.fullName || '',
                    phone: response.data.phone || ''
                });
            } catch (err) {
                console.error("Lỗi khi tải profile:", err);
                setError("Không thể tải thông tin tài khoản.");
            } finally {
                setLoading(false);
            }
        };
        fetchProfile();
    }, []);

    // 2. Xử lý input cho form thông tin
    const handleInfoChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    // 3. Xử lý input cho form mật khẩu
    const handlePasswordChange = (e) => {
        setPasswordData({
            ...passwordData,
            [e.target.name]: e.target.value
        });
    };

    // 4. Submit Form Cập nhật Thông tin
    const handleInfoSubmit = async (e) => {
        e.preventDefault();
        setIsUpdating(true);
        setUpdateError('');
        setUpdateSuccess('');
        try {
            const response = await api.put('/profile', formData); // Gọi API PUT /profile
            setUser(response.data); // Cập nhật state user
            setUpdateSuccess("Cập nhật thông tin thành công!");
        } catch (err) {
            console.error("Lỗi cập nhật profile:", err);
            setUpdateError(err.response?.data?.message || "Lỗi khi cập nhật.");
        } finally {
            setIsUpdating(false);
        }
    };

    // 5. Submit Form Đổi Mật khẩu
    const handlePasswordSubmit = async (e) => {
        e.preventDefault();
        setIsChangingPassword(true);
        setPassError('');
        setPassSuccess('');

        // (Tùy chọn: Thêm kiểm tra mật khẩu mới tại đây)

        try {
            const response = await api.put('/profile/change-password', passwordData); // Gọi API PUT /change-password
            setPassSuccess(response.data.message);
            // Xóa form mật khẩu sau khi thành công
            setPasswordData({ oldPassword: '', newPassword: '' });
        } catch (err) {
            console.error("Lỗi đổi mật khẩu:", err);
            setPassError(err.response?.data?.message || "Lỗi khi đổi mật khẩu.");
        } finally {
            setIsChangingPassword(false);
        }
    };

    // --- Giao diện ---
    if (loading) {
        return <Box sx={{ display: 'flex', justifyContent: 'center', my: 5 }}><CircularProgress /></Box>;
    }
    if (error) {
        return <Container maxWidth="sm" sx={{ mt: 4 }}><Alert severity="error">{error}</Alert></Container>;
    }

    return (
        <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
            <Typography variant="h4" gutterBottom component="h1">
                Thông tin Tài khoản
            </Typography>
            
            {/* --- Form 1: Cập nhật Thông tin --- */}
            <Paper sx={{ p: 3, mb: 4 }}>
                <Box component="form" onSubmit={handleInfoSubmit}>
                    <Typography variant="h6" gutterBottom>Thông tin cá nhân</Typography>
                    <Grid container spacing={2}>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                label="Tên đăng nhập"
                                value={user?.username || ''}
                                disabled // Không cho sửa username
                                fullWidth
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                label="Email"
                                value={user?.email || ''}
                                disabled // Không cho sửa email
                                fullWidth
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                label="Tên đầy đủ"
                                name="fullName"
                                value={formData.fullName}
                                onChange={handleInfoChange}
                                fullWidth
                                required
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                label="Số điện thoại"
                                name="phone"
                                value={formData.phone}
                                onChange={handleInfoChange}
                                fullWidth
                                required
                            />
                        </Grid>
                    </Grid>
                    {updateError && <Alert severity="error" sx={{ mt: 2 }}>{updateError}</Alert>}
                    {updateSuccess && <Alert severity="success" sx={{ mt: 2 }}>{updateSuccess}</Alert>}
                    <Button
                        type="submit"
                        variant="contained"
                        sx={{ mt: 2 }}
                        disabled={isUpdating}
                    >
                        {isUpdating ? <CircularProgress size={24} /> : "Lưu thay đổi"}
                    </Button>
                </Box>
            </Paper>

            {/* --- Form 2: Đổi Mật khẩu --- */}
             <Paper sx={{ p: 3 }}>
                <Box component="form" onSubmit={handlePasswordSubmit}>
                    <Typography variant="h6" gutterBottom>Đổi mật khẩu</Typography>
                     <Grid container spacing={2}>
                        <Grid item xs={12}>
                            <TextField
                                label="Mật khẩu cũ"
                                name="oldPassword"
                                type="password"
                                value={passwordData.oldPassword}
                                onChange={handlePasswordChange}
                                fullWidth
                                required
                            />
                        </Grid>
                         <Grid item xs={12}>
                            <TextField
                                label="Mật khẩu mới"
                                name="newPassword"
                                type="password"
                                value={passwordData.newPassword}
                                onChange={handlePasswordChange}
                                fullWidth
                                required
                            />
                        </Grid>
                         {/* (Tùy chọn: Thêm ô "Xác nhận mật khẩu mới") */}
                    </Grid>
                    {passError && <Alert severity="error" sx={{ mt: 2 }}>{passError}</Alert>}
                    {passSuccess && <Alert severity="success" sx={{ mt: 2 }}>{passSuccess}</Alert>}
                    <Button
                        type="submit"
                        variant="contained"
                        sx={{ mt: 2 }}
                        disabled={isChangingPassword}
                    >
                        {isChangingPassword ? <CircularProgress size={24} /> : "Đổi mật khẩu"}
                    </Button>
                </Box>
            </Paper>

        </Container>
    );
}

export default ProfilePage;