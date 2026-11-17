import { useState } from 'react';
import { Alert, Badge, Button, Card, EmptyState, Spinner } from '@/shared/components';
import { errorMessage } from '@/shared/utils/errorMessage';
import { useBolsinesEnviados } from '../hooks/useRecepcion';
import {
  FILTRO_VACIO,
  filtrarBolsines,
  mensajeSinResultados,
  resumenFiltro,
  type FiltroBolsin,
} from '../utils/filtroBolsines';
import { FiltroBolsines } from './FiltroBolsines';

interface BolsinesPendientesTableProps {
  seleccionado: number | null;
  onSeleccionar: (numero: number) => void;
}

export const BolsinesPendientesTable = ({ seleccionado, onSeleccionar }: BolsinesPendientesTableProps) => {
  const { data: bolsines, isLoading, error } = useBolsinesEnviados();
  // Paso 4: "borrador" es lo que se esta escribiendo; "filtro" es lo aplicado con Filtrar (null = listado completo).
  const [borrador, setBorrador] = useState<FiltroBolsin>(FILTRO_VACIO);
  const [filtro, setFiltro] = useState<FiltroBolsin | null>(null);
  const visibles = bolsines ? filtrarBolsines(bolsines, filtro) : [];
  const sinResultados = filtro !== null && visibles.length === 0;

  // Vuelve al listado completo: vacia el campo (conserva el criterio elegido) y quita el filtro.
  const limpiar = () => {
    setBorrador({ ...borrador, valor: '' });
    setFiltro(null);
  };

  return (
    <Card title="Bolsines enviados a tu comision">
      {isLoading && <Spinner />}
      {error && <Alert tone="error">{errorMessage(error)}</Alert>}
      {bolsines && bolsines.length === 0 && (
        <EmptyState title="No hay bolsines pendientes" description="Todos los bolsines enviados ya fueron recibidos." />
      )}
      {bolsines && bolsines.length > 0 && (
        <FiltroBolsines
          borrador={borrador}
          onCambiar={setBorrador}
          onFiltrar={() => setFiltro({ criterio: borrador.criterio, valor: borrador.valor.trim() })}
          onLimpiar={limpiar}
          hayFiltro={filtro !== null}
        />
      )}
      {/* Flujos alternativos A1 (precinto) y A2 (CM origen): el filtro no encontro ningun bolsin. */}
      {sinResultados && (
        <div className="filtro-sin-resultados" role="status">
          <p>{mensajeSinResultados(filtro)}</p>
          <Button type="button" variant="ghost" onClick={limpiar}>Limpiar filtro</Button>
        </div>
      )}
      {filtro !== null && !sinResultados && bolsines && (
        <p className="muted small filtro-resumen">{resumenFiltro(filtro, visibles.length, bolsines.length)}</p>
      )}
      {bolsines && bolsines.length > 0 && !sinResultados && (
        <div className="table-wrap">
          <table className="table table-selectable">
            <thead>
              <tr>
                <th>Bolsin</th>
                <th>Origen</th>
                <th>Precinto</th>
                <th className="num">Remitos</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {visibles.map((b) => (
                <tr
                  key={b.numero}
                  className={b.numero === seleccionado ? 'selected' : ''}
                  onClick={() => onSeleccionar(b.numero)}
                  tabIndex={0}
                  onKeyDown={(e) => e.key === 'Enter' && onSeleccionar(b.numero)}
                >
                  <td><strong>#{b.numero}</strong></td>
                  <td>{b.cmOrigen.nombre}</td>
                  <td>{b.nroPrecinto ?? 'N/A'}</td>
                  <td className="num">{b.cantRemitos}</td>
                  <td><Badge label={b.estado} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Card>
  );
};
