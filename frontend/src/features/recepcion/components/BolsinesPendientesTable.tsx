import { Alert, Badge, Card, EmptyState, Spinner } from '@/shared/components';
import { errorMessage } from '@/shared/utils/errorMessage';
import { useBolsinesEnviados } from '../hooks/useRecepcion';

interface BolsinesPendientesTableProps {
  seleccionado: number | null;
  onSeleccionar: (numero: number) => void;
}

export const BolsinesPendientesTable = ({ seleccionado, onSeleccionar }: BolsinesPendientesTableProps) => {
  const { data: bolsines, isLoading, error } = useBolsinesEnviados();

  return (
    <Card title="Bolsines enviados a tu comision">
      {isLoading && <Spinner />}
      {error && <Alert tone="error">{errorMessage(error)}</Alert>}
      {bolsines && bolsines.length === 0 && (
        <EmptyState title="No hay bolsines pendientes" description="Todos los bolsines enviados ya fueron recibidos." />
      )}
      {bolsines && bolsines.length > 0 && (
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
              {bolsines.map((b) => (
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
