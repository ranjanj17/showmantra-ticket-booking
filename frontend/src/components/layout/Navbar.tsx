import React, { useState } from 'react';
import { Search, ChevronDown, Menu } from 'lucide-react';
import { useAppStore } from '../../store/useAppStore';
import { useAuthStore } from '../../store/useAuthStore';
import { Link } from 'react-router-dom';
import logo from '../../assets/logo.png';
import { CITIES } from '../../data/cities';

export const Navbar = () => {
  const { city, setCity } = useAppStore();
  const { user, logout, setAuthModalOpen } = useAuthStore();
  const [isCityDropdownOpen, setIsCityDropdownOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [userMenuOpen, setUserMenuOpen] = useState(false);

  const filteredCities = CITIES.filter(c => c.toLowerCase().includes(searchQuery.toLowerCase()));

  return (
    <header className="w-full bg-white border-b shadow-sm sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
        
        {/* Left Section: Logo & Search */}
        <div className="flex items-center gap-8 flex-1">
          <Link to="/" className="flex items-center gap-3">
            <img src={logo} alt="ShowMantra Logo" className="h-10 w-10 object-cover rounded-xl shadow-sm" />
            <span className="text-2xl font-bold tracking-tight text-gray-900">Show<span className="text-red-500">Mantra</span></span>
          </Link>
          
          <div className="hidden md:flex items-center bg-gray-50 border rounded-sm px-3 py-2 flex-1 max-w-xl text-gray-500 focus-within:bg-white focus-within:border-blue-400 focus-within:shadow-sm transition-colors">
            <Search size={18} className="text-gray-400" />
            <input 
              type="text" 
              placeholder="Search for Movies, Events, Plays, Sports and Activities" 
              className="bg-transparent border-none outline-none w-full ml-3 text-sm placeholder-gray-400 text-gray-800"
            />
          </div>
        </div>

        {/* Right Section: City, SignIn, Menu */}
        <div className="flex items-center gap-6 ml-4">
          
          {/* City Dropdown */}
          <div className="relative">
            <button 
              onClick={() => {
                setIsCityDropdownOpen(!isCityDropdownOpen);
                if (!isCityDropdownOpen) setSearchQuery('');
              }}
              className="flex items-center gap-1 text-sm font-medium hover:text-gray-600 transition-colors"
            >
              {city} <ChevronDown size={16} />
            </button>
            
            {isCityDropdownOpen && (
              <div className="absolute right-0 mt-3 w-56 bg-white border rounded-md shadow-lg py-2 z-50">
                <div className="px-3 pb-2 mb-2 border-b flex flex-col gap-2">
                  <div className="text-xs text-gray-400 uppercase tracking-wider font-semibold">Select City</div>
                  <div className="flex items-center bg-gray-100 rounded px-2 py-1.5 focus-within:bg-white focus-within:ring-1 focus-within:ring-red-300">
                    <Search size={14} className="text-gray-400" />
                    <input 
                      type="text" 
                      placeholder="Search for your city" 
                      className="bg-transparent border-none outline-none w-full ml-2 text-sm text-gray-800 placeholder-gray-400"
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      autoFocus
                    />
                  </div>
                </div>
                <div className="max-h-60 overflow-y-auto">
                  {filteredCities.length > 0 ? (
                    filteredCities.map((c) => (
                      <button
                        key={c}
                        onClick={() => {
                          setCity(c);
                          setIsCityDropdownOpen(false);
                          setSearchQuery('');
                        }}
                        className={`w-full text-left px-4 py-2 text-sm hover:bg-gray-50 ${city === c ? 'text-red-500 font-medium' : 'text-gray-700'}`}
                      >
                        {c}
                      </button>
                    ))
                  ) : (
                    <div className="px-4 py-3 text-sm text-gray-500 text-center">No cities found</div>
                  )}
                </div>
              </div>
            )}
          </div>

          {/* User Auth Section */}
          {user ? (
            <div className="relative">
              <button 
                onClick={() => setUserMenuOpen(!userMenuOpen)}
                className="flex items-center gap-2 text-sm font-medium hover:text-gray-600 transition-colors focus:outline-none"
              >
                <div className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center text-gray-700 font-semibold border border-gray-300 uppercase">
                  {user.email.charAt(0)}
                </div>
                <ChevronDown size={16} />
              </button>
              
              {userMenuOpen && (
                <div className="absolute right-0 mt-3 w-56 bg-white border rounded-md shadow-lg py-2 z-50">
                  <div className="px-4 py-3 border-b mb-1">
                    <p className="text-sm font-medium text-gray-900 truncate">{user.email}</p>
                    <p className="text-xs text-gray-500 capitalize mt-0.5">{user.role.toLowerCase()}</p>
                  </div>
                  <button 
                    onClick={() => {
                      logout();
                      setUserMenuOpen(false);
                    }}
                    className="w-full text-left px-4 py-2.5 text-sm text-red-600 hover:bg-red-50 font-medium transition-colors"
                  >
                    Sign Out
                  </button>
                </div>
              )}
            </div>
          ) : (
            <button 
              onClick={() => setAuthModalOpen(true)}
              className="bg-red-500 hover:bg-red-600 text-white text-xs font-semibold px-4 py-1.5 rounded-sm transition-colors"
            >
              Sign in
            </button>
          )}
          
          <button className="text-gray-600 hover:text-gray-900 transition-colors">
            <Menu size={24} />
          </button>
        </div>
      </div>
      
      {/* Secondary Navigation (Categories) */}
      <div className="bg-gray-100 border-b hidden md:block">
        <div className="max-w-7xl mx-auto px-4 h-10 flex items-center justify-between text-sm text-gray-700">
          <div className="flex gap-4">
            <Link to="#" className="hover:text-red-500 transition-colors">Movies</Link>
            <Link to="#" className="hover:text-red-500 transition-colors">Stream</Link>
            <Link to="#" className="hover:text-red-500 transition-colors">Events</Link>
            <Link to="#" className="hover:text-red-500 transition-colors">Plays</Link>
            <Link to="#" className="hover:text-red-500 transition-colors">Sports</Link>
            <Link to="#" className="hover:text-red-500 transition-colors">Activities</Link>
          </div>
          <div className="flex gap-4 text-xs">
            <Link to="#" className="hover:text-red-500 transition-colors">ListYourShow</Link>
            <Link to="#" className="hover:text-red-500 transition-colors">Corporates</Link>
            <Link to="#" className="hover:text-red-500 transition-colors">Offers</Link>
            <Link to="#" className="hover:text-red-500 transition-colors">Gift Cards</Link>
          </div>
        </div>
      </div>
    </header>
  );
};
