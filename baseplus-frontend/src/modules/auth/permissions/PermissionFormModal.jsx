import { useEffect, useState } from 'react';
import { Button, Input, Modal } from '../../../shared/components/index.js';

export function PermissionFormModal({ isOpen, loading = false, mode = 'create', onClose, onSubmit, permission }) {
  const [form, setForm] = useState({
    name: '',
    description: '',
  });

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    setForm({
      name: permission?.name ?? '',
      description: permission?.description ?? '',
    });
  }, [isOpen, permission]);

  function handleSubmit(event) {
    event.preventDefault();
    onSubmit({
      name: form.name.trim(),
      description: form.description.trim(),
    });
  }

  return (
    <Modal
      footer={
        <>
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" form="permission-form" disabled={loading}>
            {loading ? 'Salvando...' : mode === 'edit' ? 'Salvar' : 'Criar'}
          </Button>
        </>
      }
      isOpen={isOpen}
      onClose={onClose}
      title={mode === 'edit' ? 'Editar permission' : 'Nova permission'}
    >
      <form id="permission-form" className="bp-form-grid" onSubmit={handleSubmit}>
        <Input
          id="permission-name"
          label="Nome"
          value={form.name}
          onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))}
        />
        <Input
          id="permission-description"
          label="Descrição"
          value={form.description}
          onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))}
        />
      </form>
    </Modal>
  );
}
