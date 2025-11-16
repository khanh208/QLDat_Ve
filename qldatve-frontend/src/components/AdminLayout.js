import React from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';

function AdminLayout() {
    const navigate = useNavigate();
    const userString = localStorage.getItem('user');
    const user = userString ? JSON.parse(userString) : null;

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        navigate('/login');
    };

    // Định nghĩa style cho các Link trong sidebar
    const linkStyle = {
        color: '#ecf0f1',       // Màu chữ trắng mờ
        textDecoration: 'none' // Bỏ gạch chân
    };

    // Sửa lỗi: Xóa từ "về" ở đây
    return (
        <div style={{ display: 'flex', minHeight: '100vh' }}>
            {/* Sidebar */}
            <nav style={{
                width: '220px',
                backgroundColor: '#2c3e50',
                color: '#ecf0f1',
                padding: '1.5rem 1rem',
                display: 'flex',
                flexDirection: 'column'
            }}>
                <h3 style={{
                    color: '#ffffff',
                    marginBottom: '2rem',
                    textAlign: 'center'
                }}>
                    Admin Dashboard
                </h3>
                <ul style={{ listStyle: 'none', padding: 0, flexGrow: 1 }}>
                    <li style={{ marginBottom: '1rem' }}>
                        {/* Sử dụng linkStyle */}
                        <Link to="/admin/users" style={linkStyle}>Quản lý Người dùng</Link>
                    </li>
                    <li style={{ marginBottom: '1rem' }}>
                        <Link to="/admin/trips" style={linkStyle}>Quản lý Chuyến đi</Link>
                    </li>
                    <li style={{ marginBottom: '1rem' }}>
                        <Link to="/admin/routes" style={linkStyle}>Quản lý Tuyến đường</Link>
                    </li>
                    <li style={{ marginBottom: '1rem' }}><Link to="/admin/vehicles" style={linkStyle}>Quản lý Xe</Link></li>
                    

                    <li style={{ marginBottom: '1rem' }}><Link to="/admin/bookings" style={linkStyle}>Quản lý Đặt vé</Link></li>
                    <li style={{ marginTop: '1rem' }}><Link to="/admin" style={linkStyle}>Lịch sử Đặt vé (User)</Link></li>
                </ul>
                {/* Phần thông tin user và logout */}
                <div style={{ marginTop: 'auto', paddingTop: '1rem', borderTop: '1px solid #34495e' }}>
                    <p style={{ textAlign: 'center', marginBottom: '0.5rem' }}>Xin chào, {user?.username}!</p>
                    <button
                        onClick={handleLogout}
                        style={{
                            display: 'block',
                            width: '100%',
                            padding: '0.5rem',
                            backgroundColor: '#e74c3c',
                            color: 'white',
                            border: 'none',
                            borderRadius: '4px',
                            cursor: 'pointer'
                        }}
                    >
                        Đăng xuất
                    </button>
                </div>
            </nav>

            {/* Main Content Area */}
            <main style={{
                flexGrow: 1,
                padding: '2rem',
                backgroundColor: '#f4f6f8'
             }}>
                <Outlet />
            </main>
        </div>
    );
}

export default AdminLayout;