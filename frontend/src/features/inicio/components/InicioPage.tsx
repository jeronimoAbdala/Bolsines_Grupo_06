import { Link } from 'react-router-dom';
import { Card, PageHeader } from '@/shared/components';

/** Inicio: no llama a la API. El CU 28 empieza al entrar a "Registrar recepcion" (opcRegistrarRecepcionBolsin). */
export const InicioPage = () => (
  <>
    <PageHeader title="Sistema Bolsines" subtitle="Gestion de bolsines entre comisiones medicas." />
    <div className="grid-2">
      <Card className="inicio">
        <p>Registra la recepcion de los bolsines enviados a tu comision medica: revisa sus remitos y su documentacion y confirma que llegaron.</p>
        <Link className="btn btn-primary" to="/recepcion">Registrar recepcion</Link>
      </Card>
    </div>
  </>
);
