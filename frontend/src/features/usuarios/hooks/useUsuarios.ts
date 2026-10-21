import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { usuarioService } from '../services/usuario.service';

export const usuarioKeys = {
  all: ['usuarios'] as const,
  lists: () => [...usuarioKeys.all, 'list'] as const,
};

export const useUsuarios = () =>
  useQuery({
    queryKey: usuarioKeys.lists(),
    queryFn: usuarioService.getAll,
  });

export const useCrearUsuario = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: usuarioService.create,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: usuarioKeys.all }),
  });
};
