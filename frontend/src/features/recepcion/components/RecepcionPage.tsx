import { useState } from 'react';
import { Alert, PageHeader } from '@/shared/components';
import { errorMessage } from '@/shared/utils/errorMessage';
import type { Bolsin } from '@/features/bolsines';
import { useRecibirBolsin } from '../hooks/useRecepcion';
import type { OpcionRecepcion } from '../types/recepcion.types';
import { BolsinesPendientesTable } from './BolsinesPendientesTable';
import { ConfirmarRecepcionModal } from './ConfirmarRecepcionModal';
import { DetalleRemitoPanel } from './DetalleRemitoPanel';
import { RecepcionExitosaModal } from './RecepcionExitosaModal';
import { SesionCard } from './SesionCard';

/** CU 28 - Registrar recepcion de bolsin. */
export const RecepcionPage = () => {
  const [seleccionado, setSeleccionado] = useState<number | null>(null);
  const [confirmando, setConfirmando] = useState<{ numero: number; opcion: OpcionRecepcion } | null>(null);
  const [recibido, setRecibido] = useState<Bolsin | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const recibir = useRecibirBolsin();

  const seleccionar = (numero: number) => {
    setAviso(null);
    setSeleccionado(numero);
  };

  // Paso 8: opcion elegida. Solo la 1 sigue a la confirmacion (paso 9); las otras (A3, A4, A5) no estan implementadas.
  const continuar = (opcion: OpcionRecepcion) => {
    if (seleccionado === null) return;
    if (opcion !== 1) {
      setAviso('Opción no implementada');
      return;
    }
    setAviso(null);
    setConfirmando({ numero: seleccionado, opcion });
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

  // Flujo alternativo A6: el usuario no confirma la seleccion.
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
        <DetalleRemitoPanel numero={seleccionado} onContinuar={continuar} />
      </div>
      <ConfirmarRecepcionModal
        numero={confirmando?.numero ?? null}
        loading={recibir.isPending}
        error={recibir.error ? errorMessage(recibir.error) : null}
        onConfirmar={confirmar}
        onCancelar={cancelar}
      />
      <RecepcionExitosaModal bolsin={recibido} onCerrar={() => setRecibido(null)} />
    </>
  );
};
