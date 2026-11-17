import type { ReactNode } from 'react';
import { Alert, Card, EmptyState, Spinner } from '@/shared/components';
import { errorMessage } from '@/shared/utils/errorMessage';
import { formatPeso } from '@/shared/utils/format';
import { useDetalleRecepcion } from '../hooks/useRecepcion';

interface DetalleRemitoPanelProps {
  numero: number | null;
  /** mostrarDatosRemito() de la Pantalla (paso 6). */
  mostrarDatosRemito: (informacionRemito: string) => ReactNode;
  /** mostrarOpcionesRecepcion() de la Pantalla (paso 7). */
  mostrarOpcionesRecepcion: () => ReactNode;
}

export const DetalleRemitoPanel = ({ numero, mostrarDatosRemito, mostrarOpcionesRecepcion }: DetalleRemitoPanelProps) => {
  const { data, isFetching, error } = useDetalleRecepcion(numero);

  if (numero === null) {
    return (
      <Card title="Informacion del remito">
        <EmptyState title="Selecciona un bolsin" description="Vas a ver su origen, precinto y la documentacion de cada remito." />
      </Card>
    );
  }

  return (
    <Card title={`Bolsin #${numero}`}>
      {isFetching && !data && <Spinner />}
      {error && <Alert tone="error">{errorMessage(error)}</Alert>}
      {data && (
        <>
          <dl className="resumen">
            <div><dt>Origen</dt><dd>{data.bolsin.cmOrigen.nombre}</dd></div>
            <div><dt>Destino</dt><dd>{data.bolsin.cmDestino.nombre}</dd></div>
            <div><dt>Precinto</dt><dd>{data.bolsin.nroPrecinto ?? 'N/A'}</dd></div>
            <div><dt>Peso</dt><dd>{formatPeso(data.bolsin.peso)}</dd></div>
          </dl>
          {/* Paso 6 (mostrarDatosRemito): remitos y documentaciones armados por el Iterator en el backend. */}
          {mostrarDatosRemito(data.informacionRemito)}
          {/* Paso 7 (mostrarOpcionesRecepcion): las 4 opciones de recepcion, despues de los datos. */}
          {mostrarOpcionesRecepcion()}
        </>
      )}
    </Card>
  );
};
