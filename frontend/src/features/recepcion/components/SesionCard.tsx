import { Alert, Card, Spinner } from '@/shared/components';
import { errorMessage } from '@/shared/utils/errorMessage';
import { useSesion } from '../hooks/useRecepcion';

export const SesionCard = () => {
  const { data: usuario, isLoading, error } = useSesion();

  if (isLoading) return <Card><Spinner /></Card>;
  if (error || !usuario) return <Alert tone="error">{errorMessage(error)}</Alert>;

  return (
    <Card className="sesion-card">
      <div className="dato">
        <span>Usuario</span>
        <strong>{usuario.nombre} {usuario.apellido}</strong>
      </div>
      <div className="dato">
        <span>Comision medica</span>
        <strong>{usuario.comisionMedica.nombre}</strong>
      </div>
      <div className="dato">
        <span>Fecha</span>
        <strong>{new Date().toLocaleDateString('es-AR')}</strong>
      </div>
    </Card>
  );
};
