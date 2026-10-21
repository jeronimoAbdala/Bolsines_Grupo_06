import { Alert, Button, Card, EmptyState, Spinner } from '@/shared/components';
import { errorMessage } from '@/shared/utils/errorMessage';
import { formatPeso } from '@/shared/utils/format';
import { useDetalleRecepcion } from '../hooks/useRecepcion';
import { RemitosList } from './RemitosList';

interface DetalleRemitoPanelProps {
  numero: number | null;
  onRecibir: () => void;
  onRechazar: () => void;
}

export const DetalleRemitoPanel = ({ numero, onRecibir, onRechazar }: DetalleRemitoPanelProps) => {
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
          <RemitosList remitos={data.bolsin.remitos} />
          <div className="acciones">
            <Button variant="danger" onClick={onRechazar}>Rechazar</Button>
            <Button variant="success" onClick={onRecibir}>Recibir bolsin</Button>
          </div>
        </>
      )}
    </Card>
  );
};
