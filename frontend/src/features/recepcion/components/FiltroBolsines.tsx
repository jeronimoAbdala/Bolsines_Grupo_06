import type { FormEvent } from 'react';
import { Button } from '@/shared/components';
import { CRITERIOS_FILTRO, type CriterioFiltro, type FiltroBolsin } from '../utils/filtroBolsines';

interface FiltroBolsinesProps {
  /** Lo que el usuario esta escribiendo (todavia no aplicado). */
  borrador: FiltroBolsin;
  onCambiar: (borrador: FiltroBolsin) => void;
  /** Se llama al tocar Filtrar o con Enter: recien ahi se aplica el filtro. */
  onFiltrar: () => void;
  /** Vacia el campo y quita el filtro: vuelve al listado completo. */
  onLimpiar: () => void;
  /** Hay un filtro aplicado (habilita Limpiar filtro aunque el campo este vacio). */
  hayFiltro: boolean;
}

const EJEMPLO: Record<CriterioFiltro, string> = {
  precinto: 'Ej.: PREC-001',
  cmOrigen: 'Ej.: Buenos Aires',
};

/** Paso 4 del CU 28: filtrar la lista por numero de precinto o por CM origen (un criterio a la vez). */
export const FiltroBolsines = ({ borrador, onCambiar, onFiltrar, onLimpiar, hayFiltro }: FiltroBolsinesProps) => {
  const vacio = borrador.valor.trim() === '';

  const filtrar = (e: FormEvent) => {
    e.preventDefault();
    if (!vacio) onFiltrar();
  };

  return (
    <form className="filtro-bolsines" onSubmit={filtrar} role="search">
      <label htmlFor="filtro-criterio">Filtrar por</label>
      <select
        id="filtro-criterio"
        value={borrador.criterio}
        onChange={(e) => onCambiar({ ...borrador, criterio: e.target.value as CriterioFiltro })}
      >
        {CRITERIOS_FILTRO.map((c) => <option key={c.valor} value={c.valor}>{c.texto}</option>)}
      </select>
      <input
        id="filtro-valor"
        aria-label="Valor a buscar"
        placeholder={EJEMPLO[borrador.criterio]}
        value={borrador.valor}
        onChange={(e) => onCambiar({ ...borrador, valor: e.target.value })}
      />
      <Button type="submit" disabled={vacio}>Filtrar</Button>
      <Button type="button" variant="ghost" onClick={onLimpiar} disabled={vacio && !hayFiltro}>
        Limpiar filtro
      </Button>
    </form>
  );
};
