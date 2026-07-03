import  { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ChevronRight } from 'lucide-react';
import { useAppStore } from '../store/useAppStore';
import { MovieList } from '../components/movies/MovieList';
import { UpcomingMoviesSection } from '../components/movies/UpcomingMoviesSection';
import { BannerCarousel } from '../components/movies/BannerCarousel';
import { NoMoviesState } from '../components/movies/NoMoviesState';
import { Movie } from '../services/movieService';

export const Home = () => {
  const { city, movies: allMovies, isMoviesLoading: isLoading, fetchMovies } = useAppStore();
  const [cityMovies, setCityMovies] = useState<Movie[]>([]);

  useEffect(() => {
    fetchMovies();
  }, [fetchMovies]);

  useEffect(() => {
    setCityMovies(allMovies);
  }, [allMovies]);

  return (
    <div className="bg-gray-50 min-h-screen pb-12">
      <div className="max-w-7xl mx-auto px-4 lg:px-10 pt-6">
        
        {isLoading ? (
          <>
            <div className="w-full bg-gray-200 h-64 md:h-[400px] flex items-center justify-center text-gray-400 mb-10 rounded-2xl animate-pulse">
              Loading...
            </div>
            <div className="flex justify-between items-center mb-6">
              <div className="h-8 bg-gray-200 w-64 rounded-md animate-pulse"></div>
            </div>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-8 mb-10">
              {[1, 2, 3, 4].map(n => <div key={n} className="bg-gray-200 h-64 md:h-80 rounded-xl animate-pulse"></div>)}
            </div>
          </>
        ) : cityMovies.length === 0 ? (
          <NoMoviesState city={city} />
        ) : (
          <>
            <BannerCarousel movies={cityMovies} />
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-gray-900">Recommended Movies in {city}</h2>
              <Link to="/movies" className="text-red-500 text-sm font-medium hover:underline flex items-center gap-1">
                See All <ChevronRight size={16} />
              </Link>
            </div>
            <MovieList movies={cityMovies.slice(0, 4)} />
          </>
        )}
        
        <UpcomingMoviesSection />
      </div>
    </div>
  );
};
