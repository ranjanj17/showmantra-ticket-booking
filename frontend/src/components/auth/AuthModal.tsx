import React, { useState } from 'react';
import { X } from 'lucide-react';
import { useAuthStore } from '../../store/useAuthStore';
import { LoginForm } from './LoginForm';
import { RegisterForm } from './RegisterForm';

type AuthView = 'login' | 'register';

export const AuthModal = () => {
  const { isAuthModalOpen, setAuthModalOpen } = useAuthStore();
  const [view, setView] = useState<AuthView>('login');

  if (!isAuthModalOpen) return null;

  const handleClose = () => {
    setAuthModalOpen(false);
    // Reset view back to login after it closes
    setTimeout(() => setView('login'), 200);
  };

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/60 backdrop-blur-sm px-4">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-md relative animate-in fade-in zoom-in duration-200">
        
        {/* Close Button */}
        <button 
          onClick={handleClose}
          className="absolute right-4 top-4 text-gray-400 hover:text-gray-600 transition-colors z-10"
        >
          <X size={20} />
        </button>

        <div className="p-8">
          {view === 'login' ? (
            <LoginForm onSwitchToRegister={() => setView('register')} />
          ) : (
            <RegisterForm onSwitchToLogin={() => setView('login')} />
          )}
        </div>
      </div>
    </div>
  );
};
