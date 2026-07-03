import React, { useState } from 'react';
import toast from 'react-hot-toast';
import { loginApi } from '../../services/authService';
import { useAuthStore } from '../../store/useAuthStore';

interface LoginFormProps {
  onSwitchToRegister: () => void;
}

export const LoginForm: React.FC<LoginFormProps> = ({ onSwitchToRegister }) => {
  const { login } = useAuthStore();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      toast.error('Please enter both email and password');
      return;
    }

    try {
      setLoading(true);
      const data = await loginApi(email, password);
      login(
        { id: data.id, email: data.email, phone: data.phone, role: data.role },
        data.token
      );
      toast.success('Successfully logged in!');
      setEmail('');
      setPassword('');
    } catch (error: any) {
      if (error.response) {
        const backendMessage = error.response.data?.message;
        if (error.response.status === 404) {
          toast.error(backendMessage || 'Invalid email (User not found)');
        } else if (error.response.status === 500 || error.response.status === 400) {
          toast.error(backendMessage || 'Invalid password or credentials');
        } else {
          toast.error(backendMessage || 'Invalid credentials');
        }
      } else {
        toast.error('Network error. Please check your connection.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="text-center mb-8">
        <h2 className="text-2xl font-bold text-gray-900">Welcome Back</h2>
        <p className="text-gray-500 text-sm mt-1">Please sign in to continue</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Email Address</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full border border-gray-300 rounded-md px-4 py-2 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-shadow"
            placeholder="Enter your email"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full border border-gray-300 rounded-md px-4 py-2 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-shadow"
            placeholder="Enter your password"
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-red-500 hover:bg-red-600 text-white font-semibold py-2.5 rounded-md transition-colors disabled:opacity-70 disabled:cursor-not-allowed mt-4"
        >
          {loading ? 'Signing in...' : 'Sign In'}
        </button>
      </form>

      <p className="text-center text-sm text-gray-500 mt-6">
        Don't have an account? <span onClick={onSwitchToRegister} className="text-red-500 hover:underline cursor-pointer">Register</span>
      </p>
    </div>
  );
};
