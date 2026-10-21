import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { bolsinKeys } from '@/features/bolsines';
import { recepcionService } from '../services/recepcion.service';

// Cuelgan de bolsinKeys.all: crear un bolsin o agregar un remito refresca la recepcion.
export const recepcionKeys = {
  all: [...bolsinKeys.all, 'recepcion'] as const,
  sesion: () => ['sesion'] as const,
  pendientes: () => [...recepcionKeys.all, 'pendientes'] as const,
  detalle: (numero: number) => [...recepcionKeys.all, 'detalle', numero] as const,
};

export const useSesion = () =>
  useQuery({
    queryKey: recepcionKeys.sesion(),
    queryFn: recepcionService.getSesion,
    staleTime: Infinity,
  });

export const useBolsinesEnviados = () =>
  useQuery({
    queryKey: recepcionKeys.pendientes(),
    queryFn: recepcionService.getBolsinesEnviados,
  });

export const useDetalleRecepcion = (numero: number | null) =>
  useQuery({
    queryKey: recepcionKeys.detalle(numero ?? 0),
    queryFn: () => recepcionService.seleccionarBolsin(numero as number),
    enabled: numero !== null,
    staleTime: 0,
  });

export const useRecibirBolsin = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: recepcionService.recibir,
    // No se invalida el detalle: el bolsin recibido ya no esta pendiente y la consulta daria 409.
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: recepcionKeys.pendientes() });
      queryClient.invalidateQueries({ queryKey: bolsinKeys.lists() });
    },
  });
};
