import { useState } from 'react';
import { Alert, PageHeader } from '@/shared/components';
import { errorMessage } from '@/shared/utils/errorMessage';
import type { Bolsin } from '@/features/bolsines';
import { useRecibirBolsin } from '../hooks/useRecepcion';
import { BolsinesPendientesTable } from './BolsinesPendientesTable';
import { ConfirmarRecepcionModal } from './ConfirmarRecepcionModal';
import { DetalleRemitoPanel } from './DetalleRemitoPanel';
import { RecepcionExitosaModal } from './RecepcionExitosaModal';
import { SesionCard } from './SesionCard';

/** CU 28 - Registrar recepcion de bolsin. */
export const RecepcionPage = () => {
  const [seleccionado, setSeleccionado] = useState<number | null>(null);
  const [confirmando, setConfirmando] = useState<number | null>(null);
  const [recibido, setRecibido] = useState<Bolsin | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const recibir = useRecibirBolsin();

  const seleccionar = (numero: number) => {
    setAviso(null);
    setSeleccionado(numero);
  };

  const confirmar = () => {
    if (confirmando === null) return;
    recibir.mutate(confirmando, {
      onSuccess: (bolsin) => {
        setConfirmando(null);
        setSeleccionado(null);
        setAviso(null);
        setRecibido(bolsin);
      },
    });
  };

  const cancelar = () => {
    setConfirmando(null);
    recibir.reset();
  };

  return (
    <>
      <PageHeader
        title="Registrar recepcion de bolsin"
        subtitle="Selecciona un bolsin enviado a tu comision medica, revisa su documentacion y registra la recepcion."
      />
      <SesionCard />
      {aviso && <Alert tone="info">{aviso}</Alert>}
      <div className="grid-recepcion">
        <BolsinesPendientesTable seleccionado={seleccionado} onSeleccionar={seleccionar} />
        <DetalleRemitoPanel
          numero={seleccionado}
          onRecibir={() => setConfirmando(seleccionado)}
          onRechazar={() => setAviso('El rechazo de un bolsin se resuelve en otro caso de uso y todavia no esta implementado.')}
        />
      </div>
      <ConfirmarRecepcionModal
        numero={confirmando}
        loading={recibir.isPending}
        error={recibir.error ? errorMessage(recibir.error) : null}
        onConfirmar={confirmar}
        onCancelar={cancelar}
      />
      <RecepcionExitosaModal bolsin={recibido} onCerrar={() => setRecibido(null)} />
    </>
  );
};
