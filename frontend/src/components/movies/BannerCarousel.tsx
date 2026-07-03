import React, { useEffect, useState } from 'react';
import { Movie } from '../../services/movieService';

interface BannerCarouselProps {
  movies: Movie[];
}

export const BannerCarousel: React.FC<BannerCarouselProps> = ({ movies }) => {
  const [currentIndex, setCurrentIndex] = useState(0);

  useEffect(() => {
    if (movies.length === 0) return;
    
    const interval = setInterval(() => {
      setCurrentIndex((prevIndex) => (prevIndex + 1) % movies.length);
    }, 3000);

    return () => clearInterval(interval);
  }, [movies.length]);

  if (movies.length === 0) {
    return (
      <div className="w-full bg-gray-200 h-64 md:h-80 flex items-center justify-center text-gray-400 mb-10 rounded-2xl">
        Loading Carousel...
      </div>
    );
  }

  return (
    <div className="relative w-full h-64 md:h-[400px] mb-10 rounded-2xl overflow-hidden shadow-lg group">
      {movies.map((movie, index) => (
        <div
          key={movie.id}
          className={`absolute inset-0 transition-opacity duration-1000 ease-in-out ${
            index === currentIndex ? 'opacity-100' : 'opacity-0'
          }`}
        >
          <img
            src={movie.image}
            alt={movie.title}
            className="absolute inset-0 w-full h-full object-cover object-center"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-gray-900 via-gray-900/40 to-transparent z-10" />
          <div className="absolute bottom-0 left-0 p-6 md:p-12 z-20 w-full">
            <h2 className="text-3xl md:text-5xl font-bold text-white mb-2 md:mb-4">{movie.title}</h2>
            <div className="flex items-center gap-4 text-gray-300 text-sm md:text-base font-medium">
              <span className="bg-red-600 text-white px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider">{movie.genre}</span>
              <span>{movie.language}</span>
              <span>{movie.durationMinutes} min</span>
            </div>
          </div>
        </div>
      ))}


    </div>
  );
};
