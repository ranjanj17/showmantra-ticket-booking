import { api } from './api';

export interface Theater {
  id: number;
  name: string;
  cityId: number;
  address: string;
}

export const theaterService = {
  getTheatersByCityId: async (cityId: number): Promise<Theater[]> => {
    const response = await api.get(`/theaters?cityId=${cityId}`);
    return response.data;
  }
};
