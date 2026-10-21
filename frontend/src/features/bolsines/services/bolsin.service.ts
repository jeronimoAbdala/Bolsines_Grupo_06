import { api } from '@/shared/lib/axios';
import type { Bolsin, CrearBolsinPayload, CrearRemitoPayload } from '../types/bolsin.types';

export const bolsinService = {
  getAll: async (): Promise<Bolsin[]> => {
    const { data } = await api.get<Bolsin[]>('/bolsines');
    return data;
  },

  create: async (payload: CrearBolsinPayload): Promise<Bolsin> => {
    const { data } = await api.post<Bolsin>('/bolsines', payload);
    return data;
  },

  agregarRemito: async (numero: number, payload: CrearRemitoPayload): Promise<Bolsin> => {
    const { data } = await api.post<Bolsin>(`/bolsines/${numero}/remitos`, payload);
    return data;
  },
};
