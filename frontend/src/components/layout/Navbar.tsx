import React, { useState, useRef, useEffect } from 'react';
import { Search, ChevronDown, Menu, LogOut, User, Ticket } from 'lucide-react';
import { useAppStore } from '../../store/useAppStore';
import { useAuthStore } from '../../store/useAuthStore';
import { Link, useNavigate } from 'react-router-dom';
import logo from '../../assets/logo.png';
import { CITIES } from '../../data/cities';
import { MovieSearch } from './MovieSearch';

export const Navbar = () => {
  const navigate = useNavigate();
  const { city, setCity } = useAppStore();
  const { user, logout, setAuthModalOpen } = useAuthStore();
  const [isCityDropdownOpen, setIsCityDropdownOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const [isVisible, setIsVisible] = useState(true);
  const [lastScrollY, setLastScrollY] = useState(0);
  
  const cityDropdownRef = useRef<HTMLDivElement>(null);
  const userMenuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleScroll = () => {
      const currentScrollY = window.scrollY;
      
      // Hide when scrolling down, show when scrolling up
      if (currentScrollY > lastScrollY && currentScrollY > 100) {
        setIsVisible(false);
      } else {
        setIsVisible(true);
      }
      
      setLastScrollY(currentScrollY);
    };

    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, [lastScrollY]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (cityDropdownRef.current && !cityDropdownRef.current.contains(event.target as Node)) {
        setIsCityDropdownOpen(false);
      }
      if (userMenuRef.current && !userMenuRef.current.contains(event.target as Node)) {
        setUserMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const filteredCities = CITIES.filter(c => c.toLowerCase().includes(searchQuery.toLowerCase()));



  return (
    <header className={`w-full bg-white shadow-sm sticky top-0 z-50 transition-transform duration-300 ${isVisible ? 'translate-y-0' : '-translate-y-full'}`}>
      <div className="max-w-7xl mx-auto px-4 lg:px-10 h-16 flex items-center justify-between">
        
        {/* Left Section: Logo & Search */}
        <div className="flex items-center gap-8 flex-1">
          <Link 
            to="/" 
            className="flex items-center gap-3"
            onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
          >
            <img src={logo} alt="ShowMantra Logo" className="h-10 w-10 object-cover rounded-xl shadow-sm" />
            <span className="text-2xl font-bold tracking-tight text-gray-900">Show<span className="text-red-500">Mantra</span></span>
          </Link>
          
          <MovieSearch />
        </div>

        {/* Right Section: City, SignIn, Menu */}
        <div className="flex items-center gap-6 ml-4">
          
          {/* City Dropdown */}
          <div className="relative" ref={cityDropdownRef}>
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
            <div className="relative" ref={userMenuRef}>
              <button 
                onClick={() => setUserMenuOpen(!userMenuOpen)}
                className="flex items-center gap-2 text-sm font-medium hover:text-gray-600 transition-colors focus:outline-none"
              >
                <div className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center text-gray-700 font-semibold border border-gray-300 uppercase">
                  {user.email.charAt(0)}
                </div>
              </button>
              
              {userMenuOpen && (
                <div className="absolute right-0 mt-3 w-72 bg-white border border-gray-100 rounded-2xl shadow-[0_8px_30px_rgb(0,0,0,0.12)] p-2 z-50">
                  <div className="px-4 py-4 mb-2 bg-gradient-to-br from-red-50 to-orange-50 rounded-xl flex items-center gap-4 border border-red-100/50">
                    <div className="w-12 h-12 rounded-full bg-white shadow-sm flex items-center justify-center text-red-500 font-bold text-lg uppercase border border-red-100 shrink-0">
                      {user.email.charAt(0)}
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="text-sm font-semibold text-gray-900 truncate" title={user.email}>{user.email}</p>
                      <div className="flex items-center gap-1.5 mt-1">
                        <span className="flex w-2 h-2 rounded-full bg-green-500 shrink-0 shadow-[0_0_8px_rgba(34,197,94,0.6)]"></span>
                        <p className="text-xs font-medium text-gray-600 capitalize truncate">{user.role.toLowerCase()} Account</p>
                      </div>
                    </div>
                  </div>
                  
                  <div className="h-px bg-gray-100 w-full my-1"></div>
                  
                  <button 
                    onClick={() => {
                      navigate('/bookings');
                      setUserMenuOpen(false);
                    }}
                    className="w-full flex items-center gap-3 px-4 py-3 text-sm text-gray-700 hover:text-red-600 hover:bg-red-50 font-medium rounded-xl transition-all duration-200 group mt-1"
                  >
                    <div className="p-1.5 rounded-lg bg-gray-50 group-hover:bg-red-100 transition-colors text-gray-500 group-hover:text-red-600">
                      <Ticket size={16} />
                    </div>
                    My Bookings
                  </button>

                  <button 
                    onClick={() => {
                      logout();
                      setUserMenuOpen(false);
                    }}
                    className="w-full flex items-center gap-3 px-4 py-3 text-sm text-gray-700 hover:text-red-600 hover:bg-red-50 font-medium rounded-xl transition-all duration-200 group mt-1"
                  >
                    <div className="p-1.5 rounded-lg bg-gray-50 group-hover:bg-red-100 transition-colors text-gray-500 group-hover:text-red-600">
                      <LogOut size={16} />
                    </div>
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
          

        </div>
      </div>
      
      {/* Secondary Navigation (Categories) */}
      <div className="bg-gray-100 hidden md:block">
        <div className="max-w-7xl mx-auto px-4 lg:px-10 h-10 flex items-center justify-between text-sm text-gray-700">
          <div className="flex gap-4">
            <Link to="/movies" className="hover:text-red-500 transition-colors">Movies</Link>
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
