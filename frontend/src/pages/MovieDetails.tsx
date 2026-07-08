import React, { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Clock, Calendar as CalendarIcon, ArrowLeft } from 'lucide-react';
import { useAppStore } from '../store/useAppStore';
import { movieService, Movie } from '../services/movieService';
import { showService, Showtime } from '../services/showService';
import { theaterService, Theater } from '../services/theaterService';
import { CITY_TO_ID_MAP } from '../data/cities';
import { generateDynamicPoster } from '../utils/imageGenerator';

export const MovieDetails = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { city, movies: storeMovies } = useAppStore();
  
  const [movie, setMovie] = useState<Movie | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  
  const today = new Date();
  const dates = Array.from({ length: 30 }).map((_, i) => {
    return new Date(today.getTime() + i * 24 * 60 * 60 * 1000);
  });
  const [selectedDate, setSelectedDate] = useState<Date>(today);
  
  const [groupedShows, setGroupedShows] = useState<Record<string, Showtime[]>>({});
  const [isShowsLoading, setIsShowsLoading] = useState(false);
  const theaterListRef = useRef<HTMLDivElement>(null);

  const handleDateSelect = (d: Date) => {
    setSelectedDate(d);
    // Auto-scroll slightly so the theater list is more prominent
    if (theaterListRef.current) {
      const offset = 80; // Offset for sticky header
      const elementPosition = theaterListRef.current.getBoundingClientRect().top;
      const offsetPosition = elementPosition + window.scrollY - offset;
      
      window.scrollTo({
        top: offsetPosition,
        behavior: 'smooth'
      });
    }
  };

  // Fetch movie details
  useEffect(() => {
    const loadMovie = async () => {
      setIsLoading(true);
      const storeMovie = storeMovies.find(m => m.id === Number(id));
      if (storeMovie) {
        setMovie(storeMovie);
      } else {
        // Fallback fetch if not in store
        try {
          const allMovies = await movieService.getAllMovies();
          let foundMovie = allMovies.find(m => m.id === Number(id));
          if (foundMovie) {
            // Try fetching poster
            try {
              const res = await fetch(`https://www.omdbapi.com/?t=${encodeURIComponent(foundMovie.title)}&apikey=c644388f`);
              const omdbData = await res.json();
              if (omdbData.Poster && omdbData.Poster !== 'N/A') {
                foundMovie.image = omdbData.Poster.replace('SX300', 'SX1000');
              } else {
                foundMovie.image = generateDynamicPoster(foundMovie.title, foundMovie.genre);
              }
            } catch (e) {
              foundMovie.image = generateDynamicPoster(foundMovie.title, foundMovie.genre);
            }
            setMovie(foundMovie);
          }
        } catch (error) {
          console.error("Failed to load movie", error);
        }
      }
      setIsLoading(false);
    };
    loadMovie();
  }, [id, storeMovies]);

  // Fetch shows and theaters when city or date changes
  useEffect(() => {
    if (!movie) return;

    const loadShowsAndTheaters = async () => {
      setIsShowsLoading(true);
      try {
        const offset = selectedDate.getTimezoneOffset();
        const localDate = new Date(selectedDate.getTime() - (offset * 60 * 1000));
        const dateStr = localDate.toISOString().split('T')[0];
        const [shows, theaters] = await Promise.all([
          showService.getShowsByMovieAndDate(movie.id, dateStr),
          theaterService.getTheatersByCityId(CITY_TO_ID_MAP[city])
        ]);

        const cityTheaterNames = theaters.map(t => t.name);
        const filteredShows = shows.filter(s => cityTheaterNames.includes(s.theaterName));

        const grouped = filteredShows.reduce((acc, show) => {
          if (!acc[show.theaterName]) acc[show.theaterName] = [];
          acc[show.theaterName].push(show);
          return acc;
        }, {} as Record<string, Showtime[]>);

        setGroupedShows(grouped);
      } catch (error) {
        console.error("Failed to load shows", error);
      } finally {
        setIsShowsLoading(false);
      }
    };

    loadShowsAndTheaters();
  }, [movie, city, selectedDate]);

  if (isLoading) {
    return <div className="min-h-screen bg-gray-50 flex items-center justify-center text-gray-500">Loading movie details...</div>;
  }

  if (!movie) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-4">
        <h2 className="text-2xl font-bold text-gray-800 mb-4">Movie not found</h2>
        <button onClick={() => navigate('/')} className="px-6 py-2 bg-red-500 text-white rounded-lg font-medium">Go Home</button>
      </div>
    );
  }

  return (
    <div className="bg-gray-50 min-h-screen pb-20">
      {/* Movie Banner */}
      <div className="w-full bg-gray-900 text-white relative border-b border-gray-800">
        <div className="absolute inset-0 overflow-hidden opacity-20">
          <img src={movie.image} alt={movie.title} className="w-full h-full object-cover blur-xl" />
        </div>
        <div className="max-w-7xl mx-auto px-4 lg:px-10 py-10 md:py-16 relative z-10 flex flex-col md:flex-row gap-8 items-start">
          <button 
            onClick={() => navigate(-1)}
            className="md:hidden absolute top-4 left-4 p-2 bg-black/40 rounded-full text-white"
          >
            <ArrowLeft size={20} />
          </button>
          
          <img src={movie.image} alt={movie.title} className="w-48 md:w-64 rounded-xl shadow-2xl shrink-0 border border-gray-700" />
          
          <div className="flex-1 pt-2 md:pt-8">
            <h1 className="text-4xl md:text-5xl font-bold mb-4">{movie.title}</h1>
            <div className="flex flex-wrap items-center gap-3 md:gap-4 mb-6">
              <span className="px-3 py-1 bg-white/20 rounded-full text-sm font-medium backdrop-blur-md">{movie.genre}</span>
              <span className="flex items-center gap-1.5 text-sm font-medium text-gray-300 bg-black/20 px-3 py-1 rounded-full backdrop-blur-sm">
                <Clock size={16} /> {movie.durationMinutes} min
              </span>
              <span className="text-sm font-medium text-gray-300 bg-black/20 px-3 py-1 rounded-full backdrop-blur-sm">{movie.language}</span>
            </div>
            <p className="text-gray-300 max-w-3xl leading-relaxed text-sm md:text-base mb-8">
              {movie.description || "No description available."}
            </p>
            
            <button 
              onClick={() => {
                if (theaterListRef.current) {
                  const offset = 80;
                  const elementPosition = theaterListRef.current.getBoundingClientRect().top;
                  const offsetPosition = elementPosition + window.scrollY - offset;
                  window.scrollTo({ top: offsetPosition, behavior: 'smooth' });
                }
              }}
              className="hidden md:inline-flex px-8 py-3 bg-red-600 hover:bg-red-700 text-white rounded-lg font-bold shadow-lg shadow-red-500/30 transition-all transform hover:scale-105"
            >
              Book Tickets
            </button>
          </div>
        </div>
      </div>

      {/* Sticky Date Selector Strip */}
      <div className="sticky top-0 z-40 bg-white border-b border-gray-200 shadow-sm" ref={theaterListRef}>
        <div className="max-w-7xl mx-auto px-4 lg:px-10">
          <div className="flex items-center gap-4 overflow-x-auto no-scrollbar py-4">
            {dates.map((d, i) => {
              const isSelected = selectedDate.getDate() === d.getDate() && selectedDate.getMonth() === d.getMonth();
              return (
                <button
                  key={i}
                  onClick={() => handleDateSelect(d)}
                  className={`flex flex-col items-center min-w-[65px] py-2 px-3 rounded-xl transition-all duration-200 ${
                    isSelected 
                      ? 'bg-red-500 text-white shadow-md' 
                      : 'bg-transparent text-gray-600 hover:bg-gray-100'
                  }`}
                >
                  <span className={`text-[10px] font-semibold uppercase tracking-wider ${isSelected ? 'text-red-100' : 'text-gray-500'}`}>
                    {d.toLocaleDateString('en-US', { weekday: 'short' })}
                  </span>
                  <span className="text-xl font-bold my-0.5">{d.getDate()}</span>
                  <span className={`text-[10px] uppercase tracking-wider ${isSelected ? 'text-red-100' : 'text-gray-500'}`}>
                    {d.toLocaleDateString('en-US', { month: 'short' })}
                  </span>
                </button>
              );
            })}
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 lg:px-10 mt-8">
        {isShowsLoading ? (
          <div className="text-center text-gray-500 py-12">Loading theaters and shows...</div>
        ) : Object.keys(groupedShows).length === 0 ? (
          <div className="bg-white rounded-2xl p-12 text-center shadow-sm border border-gray-100">
            <CalendarIcon size={48} className="mx-auto text-gray-300 mb-4" />
            <h3 className="text-xl font-bold text-gray-900 mb-2">No Shows Available</h3>
            <p className="text-gray-500 max-w-md mx-auto">
              There are no shows playing for this movie in {city} on {selectedDate.toLocaleDateString()}. Try selecting a different date or city.
            </p>
          </div>
        ) : (
          <div className="space-y-6">
            {Object.entries(groupedShows).map(([theater, shows]) => (
              <div key={theater} className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
                <div className="flex items-start justify-between mb-6">
                  <div>
                    <h3 className="text-lg font-bold text-gray-900 mb-1">{theater}</h3>
                    <p className="text-sm text-gray-500">Tickets available • Multiple screens</p>
                  </div>
                </div>
                
                <div className="flex flex-wrap gap-3">
                  {shows.sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime()).map(show => {
                    const showTime = new Date(show.startTime);
                    const formattedTime = showTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
                    
                    return (
                      <button 
                        key={show.showId}
                        onClick={() => navigate(`/book/${show.showId}`, { state: { movieTitle: movie.title } })}
                        className="px-5 py-2.5 rounded-lg border border-green-500 text-green-600 font-medium text-sm hover:bg-green-50 transition-colors flex flex-col items-center"
                      >
                        {formattedTime}
                        <span className="text-[10px] text-gray-400 mt-0.5">{show.screenName}</span>
                      </button>
                    );
                  })}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
