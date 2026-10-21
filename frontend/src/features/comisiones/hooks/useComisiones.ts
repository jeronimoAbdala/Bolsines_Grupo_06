import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { comisionService } from '../services/comision.service';

export const comisionKeys = {
  all: ['comisiones'] as const,
  lists: () => [...comisionKeys.all, 'list'] as const,
};

export const useComisiones = () =>
  useQuery({
    queryKey: comisionKeys.lists(),
    queryFn: comisionService.getAll,
  });

export const useCrearComision = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: comisionService.create,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: comisionKeys.all }),
  });
};
