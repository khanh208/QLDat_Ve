// src/api.js
import axios from 'axios';

const apiBaseUrl = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8081/api';

const api = axios.create({
  baseURL: apiBaseUrl,
  withCredentials: false,
  headers: {
    'Content-Type': 'application/json'
  },
  // ✅ FIX: Chấp nhận mọi status code, để xử lý trong interceptor
  validateStatus: (status) => status >= 200 && status < 500, // Hoặc thậm chí bỏ hoàn toàn
});

// Request: gắn token + xin JSON
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log('🔑 Sending token:', token.substring(0, 20) + '...');
    } else {
      console.warn('⚠️ No token found in localStorage');
    }
    if (!config.headers.Accept) {
      config.headers.Accept = 'application/json';
    }
    return config;
  },
  (error) => {
    console.error('❌ Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Response: parse JSON string + xử lý lỗi 401/403
api.interceptors.response.use(
  (response) => {
    // Parse string JSON
    if (typeof response.data === 'string' && response.data) {
      try { 
        response.data = JSON.parse(response.data);
        console.log('✅ Parsed JSON string to object');
      } catch (e) { 
        console.warn('⚠️ Could not parse response as JSON:', response.data);
      }
    }
    
    // Xử lý response rỗng nhưng có responseText
    if (
      (response.data == null || response.data === '' || 
       (typeof response.data === 'object' && Object.keys(response.data).length === 0)) &&
      response?.request?.responseText
    ) {
      const txt = response.request.responseText;
      try { 
        response.data = JSON.parse(txt);
        console.log('✅ Parsed responseText to object');
      } catch (e) { 
        console.warn('⚠️ Could not parse responseText:', txt);
      }
    }
    
    console.log(`✅ API Response [${response.status}]:`, response.config.url, response.data);
    return response;
  },
  (error) => {
    // ✅ FIX QUAN TRỌNG: Chỉ xử lý lỗi network, không xử lý lỗi HTTP status ở đây
    if (!error.response) {
      console.error('❌ Network error:', error.message);
      return Promise.reject(error);
    }

    const { status, data } = error.response;
    
    console.error('❌ API Error:', {
      status,
      statusText: error.response?.statusText,
      data,
      url: error.config?.url
    });
    
    // ✅ FIX: Chỉ redirect khi thực sự là 401/403
    if (status === 401 || status === 403) {
      console.error('🚫 Unauthorized - Removing token and redirecting to login');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      
      // ✅ FIX: Chỉ redirect nếu không phải đang ở trang login và có pathname hợp lệ
      const currentPath = window.location.pathname;
      if (!currentPath.includes('/login') && currentPath !== '/login') {
        // Sử dụng setTimeout để tránh lỗi redirect trong promise rejection
        setTimeout(() => {
          window.location.href = '/login?redirect=' + encodeURIComponent(currentPath);
        }, 100);
      }
    }
    
    return Promise.reject(error);
  }
);

export default api;
