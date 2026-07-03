import { api } from './api';

export const loginApi = async (email: string, password: string) => {
  const response = await api.post('/users/login', { email, password });
  return response.data;
};

export const registerApi = async (email: string, password: string, phone?: string) => {
  const response = await api.post('/users/register', { email, password, phone, role: 'USER' });
  return response.data;
};
