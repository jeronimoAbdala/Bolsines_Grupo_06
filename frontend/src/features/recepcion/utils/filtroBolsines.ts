import type { Bolsin } from '@/features/bolsines';

/** Paso 4 del CU 28: dato por el que se filtra la lista de bolsines pendientes (uno a la vez). */
export type CriterioFiltro = 'precinto' | 'cmOrigen';

/** Criterio elegido y valor ingresado por el usuario. */
export interface FiltroBolsin {
  criterio: CriterioFiltro;
  valor: string;
}

export const CRITERIOS_FILTRO: { valor: CriterioFiltro; texto: string }[] = [
  { valor: 'precinto', texto: 'Número de precinto' },
  { valor: 'cmOrigen', texto: 'CM origen' },
];

export const FILTRO_VACIO: FiltroBolsin = { criterio: 'precinto', valor: '' };

/** Minusculas, sin tildes y sin espacios al principio o al final: "  Córdoba " -> "cordoba". */
const normalizar = (texto: string): string =>
  texto.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().trim();

/**
 * Filtra en la pantalla la lista que ya devolvio el backend (pasos 3 y 4): no hay endpoint ni metodo del Gestor.
 * - Precinto: coincidencia exacta (es un identificador). Un bolsin sin precinto nunca coincide.
 * - CM origen: coincidencia parcial sobre el nombre ("buenos" encuentra "Buenos Aires").
 * Sin filtro aplicado (null) devuelve la lista completa.
 */
export const filtrarBolsines = (bolsines: Bolsin[], filtro: FiltroBolsin | null): Bolsin[] => {
  if (filtro === null) return bolsines;
  const buscado = normalizar(filtro.valor);
  if (filtro.criterio === 'precinto') {
    return bolsines.filter((b) => b.nroPrecinto !== null && normalizar(b.nroPrecinto) === buscado);
  }
  return bolsines.filter((b) => normalizar(b.cmOrigen.nombre).includes(buscado));
};

/**
 * Flujos alternativos del CU 28 cuando el filtro no encuentra ningun bolsin:
 * - A1: no se encuentra bolsin con el numero de precinto ingresado.
 * - A2: no se encuentra bolsin con la CM de origen ingresada.
 */
export const mensajeSinResultados = (filtro: FiltroBolsin): string =>
  filtro.criterio === 'precinto'
    ? `No se encontró ningún bolsín con el número de precinto "${filtro.valor}".`
    : `No se encontró ningún bolsín con la CM de origen "${filtro.valor}".`;

/** Resumen del filtro aplicado cuando si hay resultados: "Filtrado por CM origen "buenos": 2 de 2 bolsines." */
export const resumenFiltro = (filtro: FiltroBolsin, encontrados: number, total: number): string => {
  const dato = filtro.criterio === 'precinto' ? 'número de precinto' : 'CM origen';
  return `Filtrado por ${dato} "${filtro.valor}": ${encontrados} de ${total} ${total === 1 ? 'bolsín' : 'bolsines'}.`;
};
