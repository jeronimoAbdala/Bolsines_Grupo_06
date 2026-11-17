import { useEffect, useState } from 'react';
import { Alert, Button, PageHeader } from '@/shared/components';
import { errorMessage } from '@/shared/utils/errorMessage';
import type { Bolsin } from '@/features/bolsines';
import { useRecibirBolsin } from '../hooks/useRecepcion';
import { OPCIONES_RECEPCION, type OpcionRecepcion } from '../types/recepcion.types';
import { BolsinesPendientesTable } from './BolsinesPendientesTable';
import { ConfirmarRecepcionModal } from './ConfirmarRecepcionModal';
import { DatosRemito } from './DatosRemito';
import { DetalleRemitoPanel } from './DetalleRemitoPanel';
import { RecepcionExitosaModal } from './RecepcionExitosaModal';
import { SesionCard } from './SesionCard';

/** CU 28 - Registrar recepcion de bolsin: PantallaRegistrarRecepcionBolsin del diagrama de secuencia. */
export const PantallaRegistrarRecepcionBolsin = () => {
  const [seleccionado, setSeleccionado] = useState<number | null>(null);
  const [opcion, setOpcion] = useState<OpcionRecepcion | null>(null);
  const [confirmando, setConfirmando] = useState<{ numero: number; opcion: OpcionRecepcion } | null>(null);
  const [recibido, setRecibido] = useState<Bolsin | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const recibir = useRecibirBolsin();

  // Al cambiar de bolsin, la opcion elegida se limpia.
  useEffect(() => setOpcion(null), [seleccionado]);

  // opcRegistrarRecepcionBolsin() — paso 1: el EB elige "Registrar recepcion" en el menu.
  const opcRegistrarRecepcionBolsin = () => habilitarVentana();

  // habilitarVentana() — paso 1.
  const habilitarVentana = () => (
    <>
      <PageHeader
        title="Registrar recepcion de bolsin"
        subtitle="Selecciona un bolsin enviado a tu comision medica, revisa su documentacion y registra la recepcion."
      />
      {mostrarCM()}
      {aviso && <Alert tone="info">{aviso}</Alert>}
      <div className="grid-recepcion">
        {mostrarBolsinesParaSeleccion()}
        <DetalleRemitoPanel
          numero={seleccionado}
          mostrarDatosRemito={mostrarDatosRemito}
          mostrarOpcionesRecepcion={mostrarOpcionesRecepcion}
        />
      </div>
      {solicitarConfirmacion()}
      {informarRecepcion()}
    </>
  );

  // mostrarCM() — paso 2.
  const mostrarCM = () => <SesionCard />;

  // mostrarBolsinesParaSeleccion() — paso 4.
  const mostrarBolsinesParaSeleccion = () => (
    <BolsinesPendientesTable
      {...solicitarSeleccionBolsin()}
      mostrarCMOrigen={mostrarCMOrigen}
      mostrarNroPrecinto={mostrarNroPrecinto}
    />
  );

  // mostrarCMOrigen() — paso 4.
  const mostrarCMOrigen = (bolsin: Bolsin) => bolsin.cmOrigen.nombre;

  // mostrarNroPrecinto() — paso 4.
  const mostrarNroPrecinto = (bolsin: Bolsin) => bolsin.nroPrecinto ?? 'N/A';

  // solicitarSeleccionBolsin() — paso 4: habilita la seleccion de un bolsin del listado.
  const solicitarSeleccionBolsin = () => ({ seleccionado, onSeleccionar: tomarSeleccionBolsin });

  // tomarSeleccionBolsin() — paso 5 (con parametro, como en el backend: P1).
  const tomarSeleccionBolsin = (numero: number) => {
    setAviso(null);
    setSeleccionado(numero);
  };

  // mostrarDatosRemito() — paso 6: lo que armo el Iterator en el backend.
  const mostrarDatosRemito = (informacionRemito: string) => <DatosRemito informacionRemito={informacionRemito} />;

  // mostrarOpcionesRecepcion() — paso 7.
  const mostrarOpcionesRecepcion = () => (
    <>
      <fieldset className="opciones-recepcion">
        <legend>Opcion de recepcion</legend>
        {OPCIONES_RECEPCION.map((o) => (
          <label key={o.valor}>
            <input
              type="radio"
              name="opcion-recepcion"
              value={o.valor}
              checked={opcion === o.valor}
              onChange={() => setOpcion(o.valor)}
            />
            <span>{o.valor}. {o.texto}</span>
          </label>
        ))}
      </fieldset>
      <div className="acciones">
        <Button variant="primary" onClick={() => opcion !== null && tomarOpcionDeRecepcion(opcion)} disabled={opcion === null}>
          Continuar
        </Button>
      </div>
    </>
  );

  // tomarOpcionDeRecepcion() — paso 8: solo la 1 sigue a la confirmacion; las otras (A3, A4, A5) no estan implementadas.
  const tomarOpcionDeRecepcion = (opcionElegida: OpcionRecepcion) => {
    if (seleccionado === null) return;
    if (opcionElegida !== 1) {
      setAviso('Opción no implementada');
      return;
    }
    setAviso(null);
    setConfirmando({ numero: seleccionado, opcion: opcionElegida });
  };

  // solicitarConfirmacion() — paso 9.
  const solicitarConfirmacion = () => (
    <ConfirmarRecepcionModal
      numero={confirmando?.numero ?? null}
      loading={recibir.isPending}
      error={recibir.error ? errorMessage(recibir.error) : null}
      onConfirmar={tomarConfirmacion}
      onCancelar={tomarCancelacion}
    />
  );

  // tomarConfirmacion() — paso 10.
  const tomarConfirmacion = () => {
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

  // tomarCancelacion() — flujo alternativo A6: el EB no confirma la seleccion.
  const tomarCancelacion = () => {
    setConfirmando(null);
    recibir.reset();
  };

  // informarRecepcion() — paso 12.
  const informarRecepcion = () => <RecepcionExitosaModal bolsin={recibido} onCerrar={() => setRecibido(null)} />;

  return opcRegistrarRecepcionBolsin();
};
