import { Badge, Button, Modal } from '@/shared/components';
import type { Bolsin } from '@/features/bolsines';
import { RemitosList } from './RemitosList';

interface RecepcionExitosaModalProps {
  bolsin: Bolsin | null;
  onCerrar: () => void;
}

export const RecepcionExitosaModal = ({ bolsin, onCerrar }: RecepcionExitosaModalProps) => (
  <Modal
    title="Recepcion registrada"
    open={bolsin !== null}
    onClose={onCerrar}
    footer={<Button onClick={onCerrar}>Listo</Button>}
  >
    {bolsin && (
      <>
        <p className="exito">
          Bolsin #{bolsin.numero} <Badge label={bolsin.estado} />
        </p>
        <RemitosList remitos={bolsin.remitos} />
      </>
    )}
  </Modal>
);
