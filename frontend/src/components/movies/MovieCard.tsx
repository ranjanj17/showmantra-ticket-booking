import React from 'react';

import { Movie } from '../../services/movieService';

interface MovieCardProps {
  movie: Movie;
  onClick?: () => void;
}

export const MovieCard: React.FC<MovieCardProps> = ({ movie, onClick }) => {
  return (
    <div className="cursor-pointer group" onClick={onClick}>
      <div className="rounded-lg overflow-hidden mb-3 aspect-[2/3] bg-gray-200">
        <img 
          src={movie.image} 
          alt={movie.title}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" 
        />
      </div>
      <h3 className="font-semibold text-gray-900 text-lg">{movie.title}</h3>
      <p className="text-gray-500 text-sm">{movie.genre}</p>
    </div>
  );
};
