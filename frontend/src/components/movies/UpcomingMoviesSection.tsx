
import { Link } from 'react-router-dom';
import { ChevronRight } from 'lucide-react';
import { MovieList } from './MovieList';
import { MOCK_UPCOMING_MOVIES } from '../../data/mockMovies';

export const UpcomingMoviesSection = () => {
  return (
    <div className="mt-16">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Upcoming Movies</h2>
        <Link to="/movies?type=upcoming" className="text-red-500 text-sm font-medium hover:underline flex items-center gap-1">
          Explore All <ChevronRight size={16} />
        </Link>
      </div>

      <MovieList movies={MOCK_UPCOMING_MOVIES.slice(0, 4)} disableClick={true} />
    </div>
  );
};
