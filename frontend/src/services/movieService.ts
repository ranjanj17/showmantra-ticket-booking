import { api } from './api';

export interface Movie {
  id: number;
  title: string;
  genre: string;
  language: string;
  image?: string;
  description?: string;
  durationMinutes?: number;
  releaseDate?: string;
}

export const movieService = {
  getAllMovies: async (cityId?: number): Promise<Movie[]> => {
    const url = cityId ? `/movies?cityId=${cityId}` : '/movies';
    const response = await api.get(url);
    // The backend returns an array of Movie entities
    return response.data;
  },
  searchMovies: async (query: string): Promise<Movie[]> => {
    const response = await api.get(`/movies/search?q=${encodeURIComponent(query)}`);
    return response.data;
  }
};
