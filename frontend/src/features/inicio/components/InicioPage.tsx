import { Link } from 'react-router-dom';
import { Card, PageHeader } from '@/shared/components';
import { useBolsinesEnviados, useSesion } from '@/features/recepcion';

export const InicioPage = () => {
  const { data: usuario } = useSesion();
  const { data: pendientes } = useBolsinesEnviados();

  return (
    <>
      <PageHeader
        title={usuario ? `Hola, ${usuario.nombre}` : 'Sistema Bolsines'}
        subtitle={usuario ? `Comision medica ${usuario.comisionMedica.nombre}` : 'Gestion de bolsines entre comisiones medicas.'}
      />
      <div className="grid-2">
        <Card className="kpi">
          <span className="kpi-valor">{pendientes?.length ?? '–'}</span>
          <span className="muted">bolsines enviados pendientes de recepcion</span>
          <Link className="btn btn-primary" to="/recepcion">Registrar recepcion</Link>
        </Card>
        <Card title="Accesos rapidos">
          <ul className="accesos">
            <li><Link to="/bolsines/nuevo">Crear un bolsin</Link></li>
            <li><Link to="/bolsines/remito">Agregar un remito a un bolsin</Link></li>
            <li><Link to="/comisiones">Administrar comisiones medicas</Link></li>
            <li><Link to="/usuarios">Administrar usuarios</Link></li>
          </ul>
        </Card>
      </div>
    </>
  );
};
