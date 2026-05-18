import { useEffect, useState } from 'react';
import { Button, Input, Modal } from '../../shared/components/index.js';

export function OrganizationTypeModal({ isOpen, loading = false, mode = 'create', onClose, onSubmit, type }) {
  const [form, setForm] = useState({ code: '', name: '', active: true });

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    setForm({
      code: type?.code ?? '',
      name: type?.name ?? '',
      active: type?.active ?? true,
    });
  }, [isOpen, type]);

  function handleSubmit(event) {
    event.preventDefault();
    onSubmit({
      code: form.code.trim(),
      name: form.name.trim(),
      active: form.active,
    });
  }

  return (
    <Modal
      footer={
        <>
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
          <Button disabled={loading} form="organization-type-form" type="submit">
            {loading ? 'Salvando...' : mode === 'edit' ? 'Salvar' : 'Criar'}
          </Button>
        </>
      }
      isOpen={isOpen}
      onClose={onClose}
      title={mode === 'edit' ? 'Editar tipo' : 'Novo tipo'}
    >
      <form className="bp-form-grid" id="organization-type-form" onSubmit={handleSubmit}>
        <Input
          id="organization-type-code"
          label="Codigo"
          placeholder="EMPRESA"
          value={form.code}
          onChange={(event) => setForm((current) => ({ ...current, code: event.target.value }))}
        />
        <Input
          id="organization-type-name"
          label="Nome"
          placeholder="Empresa"
          value={form.name}
          onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))}
        />
        <label className="bp-organization-switch" htmlFor="organization-type-active">
          <input
            checked={form.active}
            id="organization-type-active"
            type="checkbox"
            onChange={(event) => setForm((current) => ({ ...current, active: event.target.checked }))}
          />
          <span>Ativo</span>
        </label>
      </form>
    </Modal>
  );
}
