import { useState } from 'react';
import { Badge } from '@/shared/components';
import { formatFecha, formatPeso } from '@/shared/utils/format';
import type { Bolsin } from '../types/bolsin.types';

interface BolsinCardProps {
  bolsin: Bolsin;
}

export const BolsinCard = ({ bolsin }: BolsinCardProps) => {
  const [abierto, setAbierto] = useState(false);

  return (
    <li className="bolsin-item">
      <button className="bolsin-resumen" onClick={() => setAbierto(!abierto)} aria-expanded={abierto}>
        <strong>#{bolsin.numero}</strong>
        <span>{bolsin.cmOrigen.nombre} → {bolsin.cmDestino.nombre}</span>
        <span className="muted">{formatFecha(bolsin.fechaGeneracion)}</span>
        <span className="muted">{bolsin.cantRemitos} remito(s)</span>
        <Badge label={bolsin.estado} />
        <span className="chevron">{abierto ? '▾' : '▸'}</span>
      </button>
      {abierto && (
        <div className="bolsin-detalle">
          <p className="muted small">
            Precinto: {bolsin.nroPrecinto ?? 'N/A'} · Peso: {formatPeso(bolsin.peso)}
          </p>
          {bolsin.remitos.length === 0 && <p className="muted small">Sin remitos.</p>}
          {bolsin.remitos.map((r) => (
            <div key={r.numero} className="remito-mini">
              <span>Remito #{r.numero}</span>
              <Badge label={r.estado} />
              {r.documentos.map((d) => (
                <span key={d.numero} className="muted small">Doc #{d.numero}: {d.asunto} ({d.tipo})</span>
              ))}
            </div>
          ))}
        </div>
      )}
    </li>
  );
};
