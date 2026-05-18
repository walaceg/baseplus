import { useEffect, useMemo, useState } from 'react';
import { Button, Input, Modal, Select } from '../../shared/components/index.js';

export function OrganizationUnitModal({ isOpen, loading = false, mode = 'create', onClose, onSubmit, unit, types = [], units = [] }) {
  const [form, setForm] = useState({ typeId: '', code: '', name: '', parentId: '', active: true });

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    setForm({
      typeId: unit?.type?.id ? String(unit.type.id) : '',
      code: unit?.code ?? '',
      name: unit?.name ?? '',
      parentId: unit?.parent?.id ? String(unit.parent.id) : '',
      active: unit?.active ?? true,
    });
  }, [isOpen, unit]);

  const typeOptions = useMemo(
    () => types.map((type) => ({ value: String(type.id), label: `${type.code} - ${type.name}` })),
    [types]
  );

  const parentOptions = useMemo(
    () =>
      units
        .filter((item) => item.id !== unit?.id)
        .map((item) => ({ value: String(item.id), label: `${item.type?.code ?? 'TIPO'} ${item.code} - ${item.name}` })),
    [unit?.id, units]
  );

  function handleSubmit(event) {
    event.preventDefault();
    onSubmit({
      typeId: Number(form.typeId),
      code: form.code.trim(),
      name: form.name.trim(),
      parentId: form.parentId ? Number(form.parentId) : null,
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
          <Button disabled={loading} form="organization-unit-form" type="submit">
            {loading ? 'Salvando...' : mode === 'edit' ? 'Salvar' : 'Criar'}
          </Button>
        </>
      }
      isOpen={isOpen}
      onClose={onClose}
      title={mode === 'edit' ? 'Editar unidade' : 'Nova unidade'}
    >
      <form className="bp-form-grid" id="organization-unit-form" onSubmit={handleSubmit}>
        <Select id="organization-unit-type" label="Tipo" options={typeOptions} placeholder="Selecione um tipo" value={form.typeId} onChange={(event) => setForm((current) => ({ ...current, typeId: event.target.value }))} />
        <Input id="organization-unit-code" label="Codigo" placeholder="0001" value={form.code} onChange={(event) => setForm((current) => ({ ...current, code: event.target.value }))} />
        <Input id="organization-unit-name" label="Nome" placeholder="Matriz" value={form.name} onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} />
        <Select id="organization-unit-parent" label="Unidade superior" options={parentOptions} placeholder="Sem unidade superior" value={form.parentId} onChange={(event) => setForm((current) => ({ ...current, parentId: event.target.value }))} />
        <label className="bp-organization-switch" htmlFor="organization-unit-active">
          <input checked={form.active} id="organization-unit-active" type="checkbox" onChange={(event) => setForm((current) => ({ ...current, active: event.target.checked }))} />
          <span>Ativo</span>
        </label>
      </form>
    </Modal>
  );
}
