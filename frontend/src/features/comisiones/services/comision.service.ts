import { api } from '@/shared/lib/axios';
import type { ComisionMedica, CrearComisionPayload } from '../types/comision.types';

export const comisionService = {
  getAll: async (): Promise<ComisionMedica[]> => {
    const { data } = await api.get<ComisionMedica[]>('/comisiones');
    return data;
  },

  create: async (payload: CrearComisionPayload): Promise<ComisionMedica> => {
    const { data } = await api.post<ComisionMedica>('/comisiones', payload);
    return data;
  },
};
