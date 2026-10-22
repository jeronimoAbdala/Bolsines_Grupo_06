import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { bolsinService } from '../services/bolsin.service';
import type { CrearRemitoPayload } from '../types/bolsin.types';

export const bolsinKeys = {
  all: ['bolsines'] as const,
  lists: () => [...bolsinKeys.all, 'list'] as const,
};

export const useBolsines = () =>
  useQuery({
    queryKey: bolsinKeys.lists(),
    queryFn: bolsinService.getAll,
  });

// Las keys de la recepcion cuelgan de bolsinKeys.all, asi que esto tambien refresca los pendientes.
const useInvalidarBolsines = () => {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: bolsinKeys.all });
};

export const useCrearBolsin = () => {
  const invalidar = useInvalidarBolsines();
  return useMutation({ mutationFn: bolsinService.create, onSuccess: invalidar });
};

export const useAgregarRemito = () => {
  const invalidar = useInvalidarBolsines();
  return useMutation({
    mutationFn: ({ numero, data }: { numero: number; data: CrearRemitoPayload }) =>
      bolsinService.agregarRemito(numero, data),
    onSuccess: invalidar,
  });
};
