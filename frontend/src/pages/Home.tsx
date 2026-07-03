import React from 'react';
import { Link } from 'react-router-dom';
import { ChevronRight } from 'lucide-react';
import { useAppStore } from '../store/useAppStore';
import { MOCK_MOVIES } from '../data/mockMovies';
import { MovieList } from '../components/movies/MovieList';
import { UpcomingMoviesSection } from '../components/movies/UpcomingMoviesSection';

export const Home = () => {
  const { city } = useAppStore();

  return (
    <div className="bg-gray-50 min-h-screen pb-12">
      <div className="max-w-7xl mx-auto px-4 lg:px-10 pt-6">
        {/* Promotional Carousel Banner (Placeholder) */}
        <div className="w-full bg-gray-200 h-64 md:h-80 flex items-center justify-center text-gray-400 mb-10 rounded-2xl">
          Banner Carousel Placeholder
        </div>

        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Recommended Movies in {city}</h2>
          <Link to="/movies" className="text-red-500 text-sm font-medium hover:underline flex items-center gap-1">
            See All <ChevronRight size={16} />
          </Link>
        </div>

        <MovieList movies={MOCK_MOVIES} />
        
        <UpcomingMoviesSection />
      </div>
    </div>
  );
};
