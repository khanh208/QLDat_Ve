import React, { createContext, useContext, useState } from 'react';
import { Snackbar, Alert } from '@mui/material';

// Tạo Context
const NotificationContext = createContext(null);

// Tạo "Nhà cung cấp" (Provider)
export function NotificationProvider({ children }) {
    const [notification, setNotification] = useState(null); // { message, severity: 'success' | 'error' | 'info' }

    const showNotification = (message, severity = 'success') => {
        setNotification({ message, severity });
    };

    const handleClose = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }
        setNotification(null); // Đóng thông báo
    };

    return (
        <NotificationContext.Provider value={{ showNotification }}>
            {children}
            {/* Component Snackbar sẽ hiển thị ở đây */}
            {notification && (
                <Snackbar
                    open={true}
                    autoHideDuration={4000} // Tự động ẩn sau 4 giây
                    onClose={handleClose}
                    anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }} // Vị trí
                >
                    <Alert
                        data-testid="global-notification"
                        onClose={handleClose}
                        severity={notification.severity}
                        sx={{ width: '100%' }}
                    >
                        {notification.message}
                    </Alert>
                </Snackbar>
            )}
        </NotificationContext.Provider>
    );
}

// Tạo hook tùy chỉnh để dễ dàng gọi
export const useNotification = () => {
    const context = useContext(NotificationContext);
    if (!context) {
        throw new Error('useNotification must be used within a NotificationProvider');
    }
    return context;
};
