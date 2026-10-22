import type { ComisionMedica } from '@/features/comisiones';

export interface Documento {
  numero: number;
  asunto: string;
  descripcion: string;
  tipo: string;
  estado: string;
}

export interface Remito {
  numero: number;
  fecha: string;
  estado: string;
  documentos: Documento[];
}

export interface Bolsin {
  numero: number;
  fechaGeneracion: string;
  nroPrecinto: string | null;
  peso: number;
  cantRemitos: number;
  estado: string;
  cmOrigen: ComisionMedica;
  cmDestino: ComisionMedica;
  remitos: Remito[];
}

export interface CrearBolsinPayload {
  nroPrecinto: string;
  peso: number;
  cmOrigenCodigo: number;
  cmDestinoCodigo: number;
}

export const TIPOS_DOCUMENTO = ['EXPEDIENTE', 'INFORME', 'CERTIFICADO', 'OTRO'] as const;
export type TipoDocumento = (typeof TIPOS_DOCUMENTO)[number];

export interface CrearRemitoPayload {
  asunto: string;
  descripcion: string;
  tipoDocumento: TipoDocumento;
}
