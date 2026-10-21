import type { Bolsin } from '@/features/bolsines';

/** Bolsin seleccionado en el CU 28 con la informacion de remitos que arma el gestor. */
export interface DetalleRecepcion {
  bolsin: Bolsin;
  informacionRemito: string;
}
