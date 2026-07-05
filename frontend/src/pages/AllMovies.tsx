import React, { useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { MOCK_UPCOMING_MOVIES } from '../data/mockMovies';
import { MovieList } from '../components/movies/MovieList';
import { NoMoviesState } from '../components/movies/NoMoviesState';
import { useAppStore } from '../store/useAppStore';

export const AllMovies = () => {
  const { city, movies: allMovies, isMoviesLoading, fetchMovies } = useAppStore();
  const [searchParams] = useSearchParams();
  const isUpcoming = searchParams.get('type') === 'upcoming';
  
  useEffect(() => {
    if (!isUpcoming) {
      fetchMovies();
    }
  }, [isUpcoming, fetchMovies]);

  const cityMovies = allMovies;

  const isLoading = isUpcoming ? false : isMoviesLoading;
  const title = isUpcoming ? "Upcoming Movies" : `Recommended Movies in ${city}`;
  const displayMovies = isUpcoming ? MOCK_UPCOMING_MOVIES : cityMovies;

  return (
    <div className="bg-gray-50 min-h-screen pb-12 pt-8">
      <div className="max-w-7xl mx-auto px-4 lg:px-10">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">{title}</h1>
        {isLoading ? (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-8 mb-10">
            {[1, 2, 3, 4].map(n => <div key={n} className="bg-gray-200 h-64 md:h-80 rounded-xl animate-pulse"></div>)}
          </div>
        ) : displayMovies.length === 0 ? (
          <NoMoviesState city={city} />
        ) : (
          <MovieList movies={displayMovies} disableClick={isUpcoming} />
        )}
      </div>
    </div>
  );
};
