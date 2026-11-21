import { useEffect, useState } from 'react';
import { Alert, PageHeader } from '@/shared/components';
import { errorMessage } from '@/shared/utils/errorMessage';
import type { RecepcionRegistrada } from '../types/recepcion.types';
import { useConfirmarRecepcion, useElegirOpcion, useSeleccionarBolsin } from '../hooks/useRecepcion';
import { BolsinesPendientesTable } from './BolsinesPendientesTable';
import { ConfirmarRecepcionModal } from './ConfirmarRecepcionModal';
import { DetalleRemitoPanel } from './DetalleRemitoPanel';
import { RecepcionExitosaModal } from './RecepcionExitosaModal';
import { SesionCard } from './SesionCard';

/** CU 28 - Registrar recepcion de bolsin. Cada accion del usuario llama a un endpoint de PantallaRegistrarRecepcionBolsin (backend). */
export const RecepcionPage = () => {
  const [seleccionado, setSeleccionado] = useState<number | null>(null);
  const [opcion, setOpcion] = useState<number | null>(null);
  const [confirmando, setConfirmando] = useState<number | null>(null);
  const [recibido, setRecibido] = useState<RecepcionRegistrada | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const seleccion = useSeleccionarBolsin();
  const eleccion = useElegirOpcion();
  const confirmacion = useConfirmarRecepcion();

  // Al cambiar de bolsin, la opcion elegida se limpia.
  useEffect(() => setOpcion(null), [seleccionado]);

  // Paso 5: el usuario elige un bolsin del listado.
  const seleccionarBolsin = (numero: number) => {
    setAviso(null);
    setSeleccionado(numero);
    seleccion.mutate(numero);
  };

  // Paso 8: Continuar con la opcion elegida. Las opciones 2, 3 y 4 (A3, A4, A5) responden "Opción no implementada".
  const continuar = () => {
    if (opcion === null) return;
    setAviso(null);
    eleccion.mutate(opcion, {
      onSuccess: (numero) => setConfirmando(numero),
      onError: (error) => setAviso(errorMessage(error)),
    });
  };

  // Paso 10: el usuario confirma la recepcion.
  const confirmar = () => {
    confirmacion.mutate(undefined, {
      onSuccess: (recepcion) => {
        setConfirmando(null);
        setSeleccionado(null);
        seleccion.reset();
        setAviso(null);
        setRecibido(recepcion);
      },
    });
  };

  // Flujo alternativo A6: el usuario no confirma (no se llama a la API).
  const cancelar = () => {
    setConfirmando(null);
    confirmacion.reset();
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
        <BolsinesPendientesTable seleccionado={seleccionado} onSeleccionar={seleccionarBolsin} />
        <DetalleRemitoPanel
          numero={seleccionado}
          detalle={seleccion.data}
          cargando={seleccion.isPending}
          error={seleccion.error}
          opcion={opcion}
          onCambiarOpcion={setOpcion}
          onContinuar={continuar}
        />
      </div>
      <ConfirmarRecepcionModal
        numero={confirmando}
        loading={confirmacion.isPending}
        error={confirmacion.error ? errorMessage(confirmacion.error) : null}
        onConfirmar={confirmar}
        onCancelar={cancelar}
      />
      <RecepcionExitosaModal recepcion={recibido} onCerrar={() => setRecibido(null)} />
    </>
  );
};
