import { useEffect, useState } from 'react';
import { Alert, Button, Card, EmptyState, Spinner } from '@/shared/components';
import { errorMessage } from '@/shared/utils/errorMessage';
import { formatPeso } from '@/shared/utils/format';
import { useDetalleRecepcion } from '../hooks/useRecepcion';
import { OPCIONES_RECEPCION, type OpcionRecepcion } from '../types/recepcion.types';
import { DatosRemito } from './DatosRemito';

interface DetalleRemitoPanelProps {
  numero: number | null;
  /** Pasos 7 y 8 del CU 28: se muestran las opciones de recepcion y el usuario elige una y continua. */
  onContinuar: (opcion: OpcionRecepcion) => void;
}

export const DetalleRemitoPanel = ({ numero, onContinuar }: DetalleRemitoPanelProps) => {
  const { data, isFetching, error } = useDetalleRecepcion(numero);
  const [opcion, setOpcion] = useState<OpcionRecepcion | null>(null);

  // Al cambiar de bolsin, la opcion elegida se limpia.
  useEffect(() => setOpcion(null), [numero]);

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
          <DatosRemito informacionRemito={data.informacionRemito} />
          {/* Paso 7 (mostrarOpcionesRecepcion): las 4 opciones de recepcion, despues de los datos. */}
          <fieldset className="opciones-recepcion">
            <legend>Opcion de recepcion</legend>
            {OPCIONES_RECEPCION.map((o) => (
              <label key={o.valor}>
                <input
                  type="radio"
                  name="opcion-recepcion"
                  value={o.valor}
                  checked={opcion === o.valor}
                  onChange={() => setOpcion(o.valor)}
                />
                <span>{o.valor}. {o.texto}</span>
              </label>
            ))}
          </fieldset>
          <div className="acciones">
            <Button variant="primary" onClick={() => opcion !== null && onContinuar(opcion)} disabled={opcion === null}>
              Continuar
            </Button>
          </div>
        </>
      )}
    </Card>
  );
};
