import axios from 'axios';
import type { ApiError } from '../types/api.types';

/** Devuelve el mensaje que manda el backend, o uno generico si no hay respuesta. */
export const errorMessage = (error: unknown): string => {
  if (axios.isAxiosError<ApiError>(error)) {
    if (error.response?.data?.mensaje) return error.response.data.mensaje;
    if (!error.response) return 'No se pudo conectar con el servidor. ¿Esta corriendo el backend?';
  }
  return 'Ocurrio un error inesperado.';
};
