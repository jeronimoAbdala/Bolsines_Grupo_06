import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { bolsinKeys } from '@/features/bolsines';
import { recepcionService } from '../services/recepcion.service';

// Cuelgan de bolsinKeys.all: crear un bolsin o agregar un remito refresca la recepcion.
export const recepcionKeys = {
  all: [...bolsinKeys.all, 'recepcion'] as const,
  ventana: () => [...recepcionKeys.all, 'ventana'] as const,
};

// useVentanaRecepcion, useSesion y useBolsinesEnviados comparten la misma consulta (un solo POST /iniciar).
export const useVentanaRecepcion = () =>
  useQuery({
    queryKey: recepcionKeys.ventana(),
    queryFn: recepcionService.iniciar,
  });

// Cada uno toma su parte de la ventana.
export const useSesion = () =>
  useQuery({
    queryKey: recepcionKeys.ventana(),
    queryFn: recepcionService.iniciar,
    select: (ventana) => ventana.usuario,
  });

export const useBolsinesEnviados = () =>
  useQuery({
    queryKey: recepcionKeys.ventana(),
    queryFn: recepcionService.iniciar,
    select: (ventana) => ventana.bolsines,
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
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: recepcionKeys.ventana() });
      queryClient.invalidateQueries({ queryKey: bolsinKeys.lists() });
    },
  });
};
