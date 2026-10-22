import { Alert, Card, EmptyState, PageHeader, Spinner } from '@/shared/components';
import { errorMessage } from '@/shared/utils/errorMessage';
import { useUsuarios } from '../hooks/useUsuarios';
import { CrearUsuarioForm } from './CrearUsuarioForm';

export const UsuariosPage = () => {
  const { data: usuarios, isLoading, error } = useUsuarios();

  return (
    <>
      <PageHeader title="Usuarios" subtitle="Cada usuario tiene un empleado asociado a una comision medica." />
      <div className="grid-2">
        <Card title="Nuevo usuario">
          <CrearUsuarioForm />
        </Card>
        <Card title="Registrados">
          {isLoading && <Spinner />}
          {error && <Alert tone="error">{errorMessage(error)}</Alert>}
          {usuarios?.length === 0 && <EmptyState title="No hay usuarios" />}
          {usuarios && usuarios.length > 0 && (
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr><th>Usuario</th><th>Empleado</th><th>Comision</th></tr>
                </thead>
                <tbody>
                  {usuarios.map((u) => (
                    <tr key={u.nombreUsuario}>
                      <td><strong>{u.nombreUsuario}</strong></td>
                      <td>{u.nombre} {u.apellido}<div className="muted small">{u.mail}</div></td>
                      <td>{u.comisionMedica.nombre}</td>
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
