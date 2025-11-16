import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar'; // <-- 1. Import Navbar
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import VerifyPage from './pages/VerifyPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import TripDetailPage from './pages/TripDetailPage';
import AdminLayout from './components/AdminLayout';
import AdminUsersPage from './pages/AdminUsersPage';
import AdminTripsPage from './pages/AdminTripsPage';
import AdminRoutesPage from './pages/AdminRoutesPage';
import AdminVehiclesPage from './pages/AdminVehiclesPage';
import MyBookingsPage from './pages/MyBookingsPage';
import ProtectedRoute from './components/ProtectedRoute';
import 'react-datepicker/dist/react-datepicker.css';
import PaymentSuccessPage from './pages/PaymentSuccessPage';
import AdminBookingsPage from './pages/AdminBookingsPage';
import ProfilePage from './pages/ProfilePage';
import AdminDashboardPage from './pages/AdminDashboardPage';

function App() {
    return (
        <BrowserRouter>
            {/* --- 2. ĐẶT NAVBAR Ở ĐÂY --- */}
            <Navbar />
            {/* ------------------------ */}
            <Routes>
                {/* === LUỒNG PUBLIC & AUTH === */}
                <Route path="/" element={<HomePage />} />
                <Route path="/trips/:id" element={<TripDetailPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/verify" element={<VerifyPage />} />
                <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                <Route path="/reset-password" element={<ResetPasswordPage />} />
                <Route path="/payment-success" element={<PaymentSuccessPage />} />
                <Route path="/admin/bookings" element={<AdminBookingsPage />} />
                {/* === LUỒNG USER (ĐÃ ĐĂNG NHẬP) === */}
                <Route element={<ProtectedRoute />}>
                    <Route path="/my-bookings" element={<MyBookingsPage />} />
                    <Route path="/profile" element={<ProfilePage />} />
                    {/* (Thêm các trang khác cần đăng nhập ở đây) */}
                </Route>

                {/* === LUỒNG ADMIN (ĐÃ ĐĂNG NHẬP ADMIN) === */}
                <Route element={<AdminLayout />}>
                    <Route path="/admin/users" element={<AdminUsersPage />} />
                    <Route path="/admin/trips" element={<AdminTripsPage />} />
                    <Route path="/admin/routes" element={<AdminRoutesPage />} />
                    <Route path="/admin/vehicles" element={<AdminVehiclesPage />} />
                    
                    <Route path="/admin/bookings" element={<AdminBookingsPage />} />
                    <Route element={<AdminLayout />}></Route>
                    <Route path="/admin/dashboard" element={<AdminDashboardPage />} />

                    
                    <Route index element={<AdminDashboardPage />} path="/admin" />
                </Route>

            </Routes>
        </BrowserRouter>
    );
}

export default App;