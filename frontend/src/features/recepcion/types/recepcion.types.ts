import type { Bolsin } from '@/features/bolsines';
import type { Usuario } from '@/features/usuarios';

/** Paso 4: una fila de la lista (la arma la Pantalla del backend; el precinto llega 'N/A' si no hay). */
export interface BolsinParaSeleccion {
  numero: number;
  cmOrigen: string;
  nroPrecinto: string;
  cantRemitos: number;
  estado: string;
}

/** Respuesta de POST /recepcion/iniciar: usuario logueado con su CM (paso 2), bolsines para seleccionar y el texto que pide elegir uno (paso 4). */
export interface VentanaRecepcion {
  usuario: Usuario;
  bolsines: BolsinParaSeleccion[];
  solicitudSeleccion: string;
}

/** Respuesta de POST /recepcion/seleccion/{n}: encabezado, texto del Iterator (paso 6) y las 4 opciones (paso 7). */
export interface DetalleRecepcion {
  bolsin: Bolsin;
  informacionRemito: string;
  opcionesRecepcion: string[];
}
