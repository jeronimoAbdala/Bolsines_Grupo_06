import { api } from '@/shared/lib/axios';
import type { DetalleRecepcion, RecepcionRegistrada, VentanaRecepcion } from '../types/recepcion.types';

/** Un metodo por cada endpoint de PantallaRegistrarRecepcionBolsin (backend). */
export const recepcionService = {
  iniciar: async (): Promise<VentanaRecepcion> => {
    const { data } = await api.post<VentanaRecepcion>('/recepcion/iniciar');
    return data;
  },

  seleccionarBolsin: async (numero: number): Promise<DetalleRecepcion> => {
    const { data } = await api.post<DetalleRecepcion>(`/recepcion/seleccion/${numero}`);
    return data;
  },

  /** Devuelve el numero del bolsin cuya recepcion hay que confirmar. */
  elegirOpcion: async (opcion: number): Promise<number> => {
    const { data } = await api.post<number>('/recepcion/opcion', { opcion });
    return data;
  },

  confirmar: async (): Promise<RecepcionRegistrada> => {
    const { data } = await api.post<RecepcionRegistrada>('/recepcion/confirmacion');
    return data;
  },
};
