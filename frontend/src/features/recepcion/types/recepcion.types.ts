import type { Bolsin } from '@/features/bolsines';

/** Bolsin seleccionado en el CU 28 con la informacion de remitos que arma el gestor. */
export interface DetalleRecepcion {
  bolsin: Bolsin;
  informacionRemito: string;
}

/** Paso 7 del CU 28: opciones de recepcion de la ERS. Solo la 1 esta implementada. */
export type OpcionRecepcion = 1 | 2 | 3 | 4;

export const OPCIONES_RECEPCION: { valor: OpcionRecepcion; texto: string }[] = [
  { valor: 1, texto: 'El contenido del bolsín es igual al registrado' },
  { valor: 2, texto: 'No se recibe toda la documentación asociada a los remitos que contiene el bolsín' },
  { valor: 3, texto: 'Existe documentación que no corresponde al destino (CM del usuario logueado)' },
  { valor: 4, texto: 'La documentación se debe redirigir a otra área' },
];
