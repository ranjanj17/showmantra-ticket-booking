import React, { useState, useRef, useEffect } from 'react';
import { Search } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAppStore } from '../../store/useAppStore';
import { movieService, Movie } from '../../services/movieService';

export const MovieSearch = () => {
  const navigate = useNavigate();
  const { movies: storeMovies } = useAppStore();
  
  const [movieSearch, setMovieSearch] = useState('');
  const [movieResults, setMovieResults] = useState<Movie[]>([]);
  const [isSearchingMovies, setIsSearchingMovies] = useState(false);
  const [showMovieDropdown, setShowMovieDropdown] = useState(false);
  
  const searchDropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (searchDropdownRef.current && !searchDropdownRef.current.contains(event.target as Node)) {
        setShowMovieDropdown(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Debounced search logic
  useEffect(() => {
    if (!movieSearch.trim()) {
      setMovieResults([]);
      setIsSearchingMovies(false);
      return;
    }

    const timer = setTimeout(async () => {
      setIsSearchingMovies(true);
      try {
        // 1. Search in local store (fast)
        const localResults = storeMovies.filter(m => 
          m.title.toLowerCase().includes(movieSearch.toLowerCase())
        );
        
        if (localResults.length > 0) {
          setMovieResults(localResults);
        } else {
          // 2. Fallback to backend search
          const backendResults = await movieService.searchMovies(movieSearch);
          setMovieResults(backendResults);
        }
      } catch (error) {
        console.error("Failed to search movies", error);
      } finally {
        setIsSearchingMovies(false);
      }
    }, 400); // 400ms debounce

    return () => clearTimeout(timer);
  }, [movieSearch, storeMovies]);

  return (
    <div className="relative hidden md:flex items-center bg-gray-50 border rounded-sm px-3 py-2 flex-1 max-w-xl text-gray-500 focus-within:bg-white focus-within:border-blue-400 focus-within:shadow-sm transition-colors" ref={searchDropdownRef}>
      <Search size={18} className="text-gray-400" />
      <input 
        type="text" 
        placeholder="Search for Movies, Events, Plays, Sports and Activities" 
        className="bg-transparent border-none outline-none w-full ml-3 text-sm placeholder-gray-400 text-gray-800"
        value={movieSearch}
        onChange={(e) => {
          setMovieSearch(e.target.value);
          setShowMovieDropdown(true);
        }}
        onFocus={() => {
          if (movieSearch.trim()) setShowMovieDropdown(true);
        }}
      />
      {showMovieDropdown && movieSearch.trim() && (
        <div className="absolute top-[120%] left-0 right-0 mt-2 bg-white border border-gray-100 rounded-lg shadow-[0_8px_30px_rgb(0,0,0,0.12)] z-50 overflow-hidden">
          {isSearchingMovies ? (
            <div className="p-4 text-center text-sm text-gray-500">Searching...</div>
          ) : movieResults.length > 0 ? (
            <div className="max-h-80 overflow-y-auto">
              {movieResults.map(movie => (
                <div 
                  key={movie.id} 
                  className="p-3 hover:bg-gray-50 cursor-pointer border-b border-gray-100 last:border-0 flex items-center gap-3 transition-colors"
                  onMouseDown={(e) => {
                    e.preventDefault();
                    setShowMovieDropdown(false);
                    setMovieSearch('');
                    navigate(`/movies/${movie.id}`);
                  }}
                >
                  {movie.image ? (
                     <img src={movie.image} alt={movie.title} className="w-10 h-14 object-cover rounded shadow-sm" />
                  ) : (
                     <div className="w-10 h-14 bg-gray-200 rounded flex items-center justify-center text-gray-400 font-bold text-xs">
                       M
                     </div>
                  )}
                  <div>
                    <div className="font-semibold text-gray-800 text-sm">{movie.title}</div>
                    <div className="text-xs text-gray-500 mt-0.5">{movie.genre} • {movie.language}</div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="p-6 text-center text-sm text-gray-500">
              <p>No movies found matching "{movieSearch}"</p>
              <p className="text-xs mt-1 text-gray-400">Try a different keyword</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
