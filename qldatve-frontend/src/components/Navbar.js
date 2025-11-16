import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material'; // Ensure Box is imported

function Navbar() {
    const navigate = useNavigate();
    const userString = localStorage.getItem('user');
    
    // Sửa lỗi cú pháp: Xóa ký tự lạ (nếu có)
    const user = userString ? JSON.parse(userString) : null;

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        navigate('/login');
        window.location.reload(); // Tải lại để đảm bảo Navbar cập nhật
    };

    return (
        <AppBar position="static" sx={{ bgcolor: 'primary.dark' }}>
            <Toolbar>
                {/* --- LOGO AND TITLE SECTION --- */}
                <Box
                    component={Link}
                    to="/"
                    sx={{
                        display: 'flex',
                        alignItems: 'center',
                        textDecoration: 'none', // Bỏ gạch chân
                        color: 'inherit',        // Kế thừa màu
                        mr: 3,                   // Margin-right
                        '&:hover': {
                            opacity: 0.9,        // Hiệu ứng hover
                        },
                    }}
                >
                    {/* (Bạn có thể thêm <img> logo của bạn ở đây) */}
                    {/* <img src="/logo.png" alt="Logo" style={{ height: '35px', marginRight: '10px' }} /> */}
                    
                    <Typography
                        variant="h5" // Hơi lớn hơn
                        component="div"
                        sx={{
                            fontWeight: 'bold', // In đậm
                            letterSpacing: 1,  // Giãn cách chữ
                            color: 'white',
                            display: { xs: 'none', sm: 'block' } // Ẩn trên màn hình quá nhỏ
                        }}
                    >
                        Vé Xe Online
                    </Typography>
                </Box>
                {/* --- END LOGO AND TITLE SECTION --- */}

                {/* Box này đẩy các link sang phải */}
                <Box sx={{ flexGrow: 1 }} />

                {/* Các link tùy theo trạng thái đăng nhập */}
                {user ? (
                    // Đã đăng nhập
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                        <Button color="inherit" component={Link} to="/my-bookings" sx={{ whiteSpace: 'nowrap' }}>
                            Lịch sử đặt vé
                        </Button>
                        
                        {/* Link Profile */}
                         <Button color="inherit" component={Link} to="/profile" sx={{ whiteSpace: 'nowrap' }}>
                            Thông tin cá nhân
                        </Button>

                        {/* --- SỬA LỖI: BỎ COMMENT VÀ THÊM BUTTON THẬT --- */}
                        {user.role === 'ADMIN' && (
                            <Button color="inherit" component={Link} to="/admin/users" sx={{ whiteSpace: 'nowrap' }}>
                                Admin Dashboard
                            </Button>
                        )}
                        {/* ------------------------------------------- */}

                        <Typography component="span" sx={{ mx: 2, whiteSpace: 'nowrap', display: { xs: 'none', md: 'block' } }}>
                            Xin chào, {user.username}!
                         </Typography>
                        <Button color="inherit" onClick={handleLogout} sx={{ whiteSpace: 'nowrap' }}>
                            Đăng xuất
                        </Button>
                    </Box>
                ) : (
                    // Chưa đăng nhập
                    <Box>
                        <Button color="inherit" component={Link} to="/login">
                            Đăng nhập
                        </Button>
                        <Button color="inherit" component={Link} to="/register">
                            Đăng ký
                        </Button>
                    </Box>
                )}
            </Toolbar>
        </AppBar>
    );
}

export default Navbar;