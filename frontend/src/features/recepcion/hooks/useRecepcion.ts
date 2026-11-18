import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { recepcionService } from '../services/recepcion.service';

export const recepcionKeys = {
  all: ['recepcion'] as const,
  ventana: () => [...recepcionKeys.all, 'ventana'] as const,
};

// useVentanaRecepcion y useSesion comparten la misma consulta (un solo POST /iniciar, opcRegistrarRecepcionBolsin).
// staleTime 0: cada vez que se entra a la pantalla se vuelve a iniciar el CU (paso 1).
export const useVentanaRecepcion = () =>
  useQuery({
    queryKey: recepcionKeys.ventana(),
    queryFn: recepcionService.iniciar,
    staleTime: 0,
  });

// SesionCard toma solo el usuario logueado.
export const useSesion = () =>
  useQuery({
    queryKey: recepcionKeys.ventana(),
    queryFn: recepcionService.iniciar,
    staleTime: 0,
    select: (ventana) => ventana.usuario,
  });

// Mutaciones: cada accion del usuario cambia el estado del CU en el Gestor.
export const useSeleccionarBolsin = () =>
  useMutation({ mutationFn: recepcionService.seleccionarBolsin });

export const useElegirOpcion = () =>
  useMutation({ mutationFn: recepcionService.elegirOpcion });

export const useConfirmarRecepcion = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => recepcionService.confirmar(),
    // Se vuelve a pedir la ventana: el bolsin recibido ya no esta pendiente.
    onSuccess: () => queryClient.invalidateQueries({ queryKey: recepcionKeys.ventana() }),
  });
};
