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
