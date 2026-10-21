import { Link } from 'react-router-dom';
import { Alert, Card, EmptyState, PageHeader, Spinner } from '@/shared/components';
import { errorMessage } from '@/shared/utils/errorMessage';
import { useBolsines } from '../hooks/useBolsines';
import { BolsinCard } from './BolsinCard';

export const BolsinesPage = () => {
  const { data: bolsines, isLoading, error } = useBolsines();

  return (
    <>
      <PageHeader title="Bolsines" subtitle="Todos los bolsines registrados, con sus remitos y documentacion." />
      <Card
        title="Listado"
        actions={
          <div className="acciones-inline">
            <Link className="btn btn-ghost" to="/bolsines/remito">Agregar remito</Link>
            <Link className="btn btn-primary" to="/bolsines/nuevo">Nuevo bolsin</Link>
          </div>
        }
      >
        {isLoading && <Spinner />}
        {error && <Alert tone="error">{errorMessage(error)}</Alert>}
        {bolsines?.length === 0 && <EmptyState title="Todavia no hay bolsines" />}
        {bolsines && bolsines.length > 0 && (
          <ul className="bolsines">
            {bolsines.map((b) => <BolsinCard key={b.numero} bolsin={b} />)}
          </ul>
        )}
      </Card>
    </>
  );
};
