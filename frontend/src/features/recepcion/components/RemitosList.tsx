import { Badge } from '@/shared/components';
import { formatFecha } from '@/shared/utils/format';
import type { Remito } from '@/features/bolsines';

interface RemitosListProps {
  remitos: Remito[];
}

export const RemitosList = ({ remitos }: RemitosListProps) => {
  if (remitos.length === 0) return <p className="muted">El bolsin no tiene remitos.</p>;

  return (
    <ul className="remitos">
      {remitos.map((remito) => (
        <li key={remito.numero} className="remito">
          <div className="remito-header">
            <strong>Remito #{remito.numero}</strong>
            <span className="muted">{formatFecha(remito.fecha)}</span>
            <Badge label={remito.estado} />
          </div>
          {remito.documentos.length === 0 ? (
            <p className="muted small">Sin documentacion.</p>
          ) : (
            <ul className="documentos">
              {remito.documentos.map((doc) => (
                <li key={doc.numero}>
                  <span>Doc #{doc.numero} · {doc.asunto}</span>
                  <span className="muted small">{doc.tipo}</span>
                  <Badge label={doc.estado} />
                </li>
              ))}
            </ul>
          )}
        </li>
      ))}
    </ul>
  );
};
