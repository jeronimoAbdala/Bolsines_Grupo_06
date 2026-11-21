import { Alert, Badge, Button, Modal } from '@/shared/components';
import type { RecepcionRegistrada } from '../types/recepcion.types';
import { RemitosList } from './RemitosList';

interface RecepcionExitosaModalProps {
  recepcion: RecepcionRegistrada | null;
  onCerrar: () => void;
}

export const RecepcionExitosaModal = ({ recepcion, onCerrar }: RecepcionExitosaModalProps) => (
  <Modal
    title="Recepcion registrada"
    open={recepcion !== null}
    onClose={onCerrar}
    footer={<Button onClick={onCerrar}>Listo</Button>}
  >
    {recepcion && (
      <>
        <p className="exito">
          Bolsin #{recepcion.bolsin.numero} <Badge label={recepcion.bolsin.estado} />
        </p>
        {/* Paso 12: resultado del CU 29 Notificar recepcion de bolsin */}
        {recepcion.notificacionEnviada && (
          <Alert tone="success">Notificación enviada a la CM origen ({recepcion.bolsin.cmOrigen.nombre})</Alert>
        )}
        <RemitosList remitos={recepcion.bolsin.remitos} />
      </>
    )}
  </Modal>
);
