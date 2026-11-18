import { Alert, Button, Card, EmptyState, Spinner } from '@/shared/components';
import { errorMessage } from '@/shared/utils/errorMessage';
import { formatPeso } from '@/shared/utils/format';
import type { DetalleRecepcion } from '../types/recepcion.types';
import { DatosRemito } from './DatosRemito';

interface DetalleRemitoPanelProps {
  numero: number | null;
  /** Respuesta de la seleccion del bolsin (pasos 5, 6 y 7). */
  detalle: DetalleRecepcion | undefined;
  cargando: boolean;
  error: unknown;
  opcion: number | null;
  onCambiarOpcion: (opcion: number) => void;
  onContinuar: () => void;
}

export const DetalleRemitoPanel = ({
  numero,
  detalle,
  cargando,
  error,
  opcion,
  onCambiarOpcion,
  onContinuar,
}: DetalleRemitoPanelProps) => {
  if (numero === null) {
    return (
      <Card title="Informacion del remito">
        <EmptyState title="Selecciona un bolsin" description="Vas a ver su origen, precinto y la documentacion de cada remito." />
      </Card>
    );
  }

  return (
    <Card title={`Bolsin #${numero}`}>
      {cargando && <Spinner />}
      {error !== null && <Alert tone="error">{errorMessage(error)}</Alert>}
      {detalle && (
        <>
          <dl className="resumen">
            <div><dt>Origen</dt><dd>{detalle.bolsin.cmOrigen.nombre}</dd></div>
            <div><dt>Destino</dt><dd>{detalle.bolsin.cmDestino.nombre}</dd></div>
            <div><dt>Precinto</dt><dd>{detalle.bolsin.nroPrecinto ?? 'N/A'}</dd></div>
            <div><dt>Peso</dt><dd>{formatPeso(detalle.bolsin.peso)}</dd></div>
          </dl>
          {/* Paso 6: remitos y documentaciones armados por el Iterator en el backend. */}
          <DatosRemito informacionRemito={detalle.informacionRemito} />
          {/* Paso 7: las 4 opciones de recepcion que devuelve el backend, despues de los datos. */}
          <fieldset className="opciones-recepcion">
            <legend>Opcion de recepcion</legend>
            {detalle.opcionesRecepcion.map((texto, i) => (
              <label key={i + 1}>
                <input
                  type="radio"
                  name="opcion-recepcion"
                  value={i + 1}
                  checked={opcion === i + 1}
                  onChange={() => onCambiarOpcion(i + 1)}
                />
                <span>{i + 1}. {texto}</span>
              </label>
            ))}
          </fieldset>
          <div className="acciones">
            <Button variant="primary" onClick={onContinuar} disabled={opcion === null}>
              Continuar
            </Button>
          </div>
        </>
      )}
    </Card>
  );
};
