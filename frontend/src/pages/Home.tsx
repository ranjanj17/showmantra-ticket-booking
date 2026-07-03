import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ChevronRight } from 'lucide-react';
import { useAppStore } from '../store/useAppStore';
import { MovieList } from '../components/movies/MovieList';
import { UpcomingMoviesSection } from '../components/movies/UpcomingMoviesSection';
import { BannerCarousel } from '../components/movies/BannerCarousel';
import { movieService, Movie } from '../services/movieService';
import { CITIES } from '../data/cities';

const MOCK_IMAGES = [
  'https://images.unsplash.com/photo-1440404653325-ab127d49abc1?w=1200&q=80',
  'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=1200&q=80',
  'https://images.unsplash.com/photo-1542204165-65bf26472b9b?w=1200&q=80',
  'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=1200&q=80',
  'https://images.unsplash.com/photo-1535016120720-40c746a51240?w=1200&q=80',
  'https://images.unsplash.com/photo-1626814026160-2237a95fc5a0?w=1200&q=80',
];

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
          <div className="w-full bg-gray-200 h-64 md:h-[400px] flex items-center justify-center text-gray-400 mb-10 rounded-2xl animate-pulse">
            Loading...
          </div>
        ) : (
          <BannerCarousel movies={cityMovies} />
        )}

        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Recommended Movies in {city}</h2>
          <Link to="/movies" className="text-red-500 text-sm font-medium hover:underline flex items-center gap-1">
            See All <ChevronRight size={16} />
          </Link>
        </div>

        {isLoading ? (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-8 mb-10">
            {[1, 2, 3, 4].map(n => <div key={n} className="bg-gray-200 h-64 md:h-80 rounded-xl animate-pulse"></div>)}
          </div>
        ) : (
          <MovieList movies={cityMovies.slice(0, 4)} />
        )}
        
        <UpcomingMoviesSection />
      </div>
    </div>
  );
};
