import { api } from '@/shared/lib/axios';
import type { Bolsin } from '@/features/bolsines';
import type { Usuario } from '@/features/usuarios';
import type { DetalleRecepcion } from '../types/recepcion.types';

export const recepcionService = {
  getSesion: async (): Promise<Usuario> => {
    const { data } = await api.get<Usuario>('/recepcion/sesion');
    return data;
  },

  getBolsinesEnviados: async (): Promise<Bolsin[]> => {
    const { data } = await api.get<Bolsin[]>('/recepcion/bolsines');
    return data;
  },

  seleccionarBolsin: async (numero: number): Promise<DetalleRecepcion> => {
    const { data } = await api.get<DetalleRecepcion>(`/recepcion/bolsines/${numero}`);
    return data;
  },

  recibir: async (numero: number): Promise<Bolsin> => {
    const { data } = await api.post<Bolsin>(`/recepcion/bolsines/${numero}/recibir`);
    return data;
  },
};
