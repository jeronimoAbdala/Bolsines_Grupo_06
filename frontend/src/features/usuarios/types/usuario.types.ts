import type { ComisionMedica } from '@/features/comisiones';

export interface Usuario {
  nombreUsuario: string;
  nombre: string;
  apellido: string;
  mail: string;
  comisionMedica: ComisionMedica;
}

export interface CrearUsuarioPayload {
  nombreUsuario: string;
  password: string;
  nombre: string;
  apellido: string;
  mail: string;
  comisionMedicaCodigo: number;
}
