import { Alert, Card, EmptyState, PageHeader, Spinner } from '@/shared/components';
import { errorMessage } from '@/shared/utils/errorMessage';
import { useComisiones } from '../hooks/useComisiones';
import { CrearComisionForm } from './CrearComisionForm';

export const ComisionesPage = () => {
  const { data: comisiones, isLoading, error } = useComisiones();

  return (
    <>
      <PageHeader title="Comisiones medicas" />
      <div className="grid-2">
        <Card title="Nueva comision">
          <CrearComisionForm />
        </Card>
        <Card title="Registradas">
          {isLoading && <Spinner />}
          {error && <Alert tone="error">{errorMessage(error)}</Alert>}
          {comisiones?.length === 0 && <EmptyState title="No hay comisiones" />}
          {comisiones && comisiones.length > 0 && (
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr><th className="num">Codigo</th><th>Nombre</th><th>Email</th><th>Telefono</th></tr>
                </thead>
                <tbody>
                  {comisiones.map((cm) => (
                    <tr key={cm.codigo}>
                      <td className="num">{cm.codigo}</td>
                      <td><strong>{cm.nombre}</strong><div className="muted small">{cm.direccion}</div></td>
                      <td>{cm.email}</td>
                      <td>{cm.telefono}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Card>
      </div>
    </>
  );
};
