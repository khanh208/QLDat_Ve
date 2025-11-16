import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import 'react-datepicker/dist/react-datepicker.css';
import { NotificationProvider } from './contexts/NotificationContext';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <LocalizationProvider dateAdapter={AdapterDateFns}> {/* <-- BỌC Ở ĐÂY */}
    <NotificationProvider> {/* <-- 2. Bọc App */}
        <App />
    </NotificationProvider> {/* <-- 3. Đóng thẻ */}
    </LocalizationProvider>
  </React.StrictMode>
);

reportWebVitals();