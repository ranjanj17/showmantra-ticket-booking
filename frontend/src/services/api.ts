import axios from 'axios';
import { useAuthStore } from '../store/useAuthStore';

// Create central axios instance
export const api = axios.create({
  baseURL: '/api', // Proxied by Vite to backend
});

// Add interceptor to inject Authorization header
api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
// Add interceptor to handle 401/403 responses
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      // Token is invalid or expired
      useAuthStore.getState().logout();
      useAuthStore.getState().setAuthModalOpen(true);
    }
    return Promise.reject(error);
  }
);
