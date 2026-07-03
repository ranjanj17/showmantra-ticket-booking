import { api } from './api';

export interface Showtime {
  showId: number;
  startTime: string;
  movieTitle: string;
  theaterName: string;
  screenName: string;
}

export const showService = {
  getShowsByMovieAndDate: async (movieId: number, date: string): Promise<Showtime[]> => {
    const response = await api.get(`/shows/movie/${movieId}?date=${date}`);
    return response.data;
  }
};
