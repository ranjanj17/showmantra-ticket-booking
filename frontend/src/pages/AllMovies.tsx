import React from 'react';
import { useSearchParams } from 'react-router-dom';
import { MOCK_MOVIES, MOCK_UPCOMING_MOVIES } from '../data/mockMovies';
import { MovieList } from '../components/movies/MovieList';

export const AllMovies = () => {
  const [searchParams] = useSearchParams();
  const isUpcoming = searchParams.get('type') === 'upcoming';
  
  const title = isUpcoming ? "Upcoming Movies" : "Recommended Movies";
  const movies = isUpcoming ? MOCK_UPCOMING_MOVIES : MOCK_MOVIES;

  return (
    <div className="bg-gray-50 min-h-screen pb-12 pt-8">
      <div className="max-w-7xl mx-auto px-4 lg:px-10">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">{title}</h1>
        <MovieList movies={movies} />
      </div>
    </div>
  );
};
