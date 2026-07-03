import React from 'react';
import { useNavigate } from 'react-router-dom';
import { MovieCard } from './MovieCard';

import { Movie } from '../../services/movieService';

interface MovieListProps {
  movies: Movie[];
}

export const MovieList: React.FC<MovieListProps> = ({ movies }) => {
  const navigate = useNavigate();

  return (
    <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
      {movies.map((movie) => (
        <MovieCard key={movie.id} movie={movie} onClick={() => navigate(`/movies/${movie.id}`)} />
      ))}
    </div>
  );
};
