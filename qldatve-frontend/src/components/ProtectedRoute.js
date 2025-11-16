import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

const useAuthToken = () => {
    return localStorage.getItem('token');
};

function ProtectedRoute() {
    const token = useAuthToken();

    // Nếu không có token -> Chuyển về trang đăng nhập
    if (!token) {
        return <Navigate to="/login" />;
    }

    // Nếu có token -> Cho phép hiển thị trang con
    return <Outlet />;
}

export default ProtectedRoute;