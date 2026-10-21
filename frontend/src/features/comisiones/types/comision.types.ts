export interface ComisionMedica {
  codigo: number;
  nombre: string;
  direccion: string;
  email: string;
  telefono: string;
}

export type CrearComisionPayload = ComisionMedica;
