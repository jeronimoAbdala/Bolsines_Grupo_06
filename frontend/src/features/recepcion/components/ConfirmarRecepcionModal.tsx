import { Alert, Button, Modal } from '@/shared/components';

interface ConfirmarRecepcionModalProps {
  numero: number | null;
  loading: boolean;
  error: string | null;
  onConfirmar: () => void;
  onCancelar: () => void;
}

export const ConfirmarRecepcionModal = ({ numero, loading, error, onConfirmar, onCancelar }: ConfirmarRecepcionModalProps) => (
  <Modal
    title="Confirmar recepcion"
    open={numero !== null}
    onClose={onCancelar}
    footer={
      <>
        <Button variant="ghost" onClick={onCancelar} disabled={loading}>Cancelar</Button>
        <Button variant="success" onClick={onConfirmar} loading={loading}>Confirmar recepcion</Button>
      </>
    }
  >
    <p>
      ¿Confirmas la recepcion del <strong>Bolsin #{numero}</strong>? Todos sus remitos y documentos
      quedaran como recibidos y aceptados.
    </p>
    {error && <Alert tone="error">{error}</Alert>}
  </Modal>
);
