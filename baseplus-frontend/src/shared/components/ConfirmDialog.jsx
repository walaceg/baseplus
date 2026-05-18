import { Button } from './Button.jsx';
import { Modal } from './Modal.jsx';

export function ConfirmDialog({ cancelLabel = 'Cancelar', confirmLabel = 'Confirmar', message, onCancel, onConfirm, open, title, variant = 'danger' }) {
  return (
    <Modal
      footer={
        <>
          <Button type="button" variant="secondary" onClick={onCancel}>
            {cancelLabel}
          </Button>
          <Button type="button" variant={variant} onClick={onConfirm}>
            {confirmLabel}
          </Button>
        </>
      }
      isOpen={open}
      onClose={onCancel}
      title={title}
    >
      <p className="bp-confirm-dialog__message">{message}</p>
    </Modal>
  );
}
