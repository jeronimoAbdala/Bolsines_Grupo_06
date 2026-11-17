interface DatosRemitoProps {
  /**
   * Paso 6 del CU 28: texto que arma el backend con el patron Iterator (Bolsin -> Remito -> DetalleRemito).
   * Una linea "Remito #N" por remito y, debajo, una linea con 2 espacios de sangria por documentacion
   * ("  Asunto: ... | Tipo: ...").
   */
  informacionRemito: string;
}

interface RemitoInformado {
  titulo: string;
  documentaciones: string[];
}

/** Agrupa las lineas del texto: "Remito #" abre un remito; una linea con sangria es una documentacion. */
const separarRemitos = (informacionRemito: string): RemitoInformado[] => {
  const remitos: RemitoInformado[] = [];
  for (const linea of informacionRemito.split('\n')) {
    if (linea.startsWith('Remito #')) {
      remitos.push({ titulo: linea, documentaciones: [] });
    } else if (linea.startsWith('  ') && remitos.length > 0) {
      remitos[remitos.length - 1].documentaciones.push(linea.trim());
    }
  }
  return remitos;
};

/** mostrarDatosRemito() del diagrama de secuencia: muestra la informacion de remitos tal como la armo el Iterator. */
export const DatosRemito = ({ informacionRemito }: DatosRemitoProps) => {
  const remitos = separarRemitos(informacionRemito);
  if (remitos.length === 0) return <p className="muted">El bolsín no tiene remitos.</p>;

  return (
    <ul className="remitos">
      {remitos.map((remito) => (
        <li key={remito.titulo} className="remito">
          <div className="remito-header">
            <strong>{remito.titulo}</strong>
          </div>
          {remito.documentaciones.length === 0 ? (
            <p className="muted small">Sin documentación.</p>
          ) : (
            <ul className="documentos">
              {remito.documentaciones.map((documentacion, i) => (
                <li key={i}>{documentacion}</li>
              ))}
            </ul>
          )}
        </li>
      ))}
    </ul>
  );
};
