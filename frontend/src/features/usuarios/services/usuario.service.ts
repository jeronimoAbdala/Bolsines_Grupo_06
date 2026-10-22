import { api } from '@/shared/lib/axios';
import type { CrearUsuarioPayload, Usuario } from '../types/usuario.types';

export const usuarioService = {
  getAll: async (): Promise<Usuario[]> => {
    const { data } = await api.get<Usuario[]>('/usuarios');
    return data;
  },

  create: async (payload: CrearUsuarioPayload): Promise<Usuario> => {
    const { data } = await api.post<Usuario>('/usuarios', payload);
    return data;
  },
};
