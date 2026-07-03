import { create } from 'zustand';
import { movieService, Movie } from '../services/movieService';
import { CITY_TO_ID_MAP } from '../data/cities';
import { generateDynamicPoster } from '../utils/imageGenerator';

const OMDB_API_KEY = 'c644388f';

interface AppState {
  city: string;
  setCity: (city: string) => void;
  movies: Movie[];
  isMoviesLoading: boolean;
  fetchMovies: (forceRefetch?: boolean) => Promise<void>;
}

export const useAppStore = create<AppState>((set, get) => ({
  city: 'Bengaluru',
  setCity: (city) => {
    set({ city });
    get().fetchMovies(true); // Refetch backend when city changes
  },
  movies: [],
  isMoviesLoading: false,
  fetchMovies: async (forceRefetch = false) => {
    // Prevent duplicate fetches if already loaded or loading
    if ((get().movies.length > 0 && !forceRefetch) || get().isMoviesLoading) return;
    
    set({ isMoviesLoading: true });
    try {
      const cityId = CITY_TO_ID_MAP[get().city];
      const data = await movieService.getAllMovies(cityId);
      
      // Fetch real posters dynamically from OMDB for every movie
      const moviesWithImages = await Promise.all(data.map(async (m) => {
        try {
          // Some titles might need tweaking for OMDB to find them accurately, but using raw title usually works
          const title = m.title === 'Harry Potter' ? "Harry Potter and the Sorcerer's Stone" : m.title;
          
          const res = await fetch(`https://www.omdbapi.com/?t=${encodeURIComponent(title)}&apikey=${OMDB_API_KEY}`);
          const omdbData = await res.json();
          
          if (omdbData.Poster && omdbData.Poster !== 'N/A') {
            // Get high resolution poster
            return {
              ...m,
              image: omdbData.Poster.replace('SX300', 'SX1000')
            };
          }
        } catch (e) {
          console.error("Failed to fetch OMDB poster for", m.title);
        }
        
        // Fallback to our dynamic SVG generator if OMDB doesn't have it
        return {
          ...m,
          image: generateDynamicPoster(m.title, m.genre)
        };
      }));

      set({ movies: moviesWithImages });
    } catch (error) {
      console.error("Failed to fetch movies globally:", error);
    } finally {
      set({ isMoviesLoading: false });
    }
  }
}));
