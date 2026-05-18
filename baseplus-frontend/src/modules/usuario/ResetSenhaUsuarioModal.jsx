import { useEffect, useState } from 'react';
import { Alert, Button, Input, Modal } from '../../shared/components/index.js';

function SwitchField({ checked, id, label, onChange }) {
  return (
    <label className="bp-usuario-switch" htmlFor={id}>
      <input
        checked={checked}
        className="bp-usuario-switch__input"
        id={id}
        type="checkbox"
        onChange={(event) => onChange(event.target.checked)}
      />
      <span className="bp-usuario-switch__control" aria-hidden="true" />
      <span className="bp-usuario-switch__label">{label}</span>
    </label>
  );
}

function getApiError(requestError, fallback) {
  const response = requestError.response?.data;
  return response?.errors?.[0] ?? response?.message ?? fallback;
}

export function ResetSenhaUsuarioModal({ loading = false, onClose, onSubmit, open }) {
  const [form, setForm] = useState({
    novaSenhaTemporaria: '',
    obrigarTrocaProximoLogin: true,
  });
  const [error, setError] = useState('');

  useEffect(() => {
    if (!open) {
      return;
    }

    setForm({
      novaSenhaTemporaria: '',
      obrigarTrocaProximoLogin: true,
    });
    setError('');
  }, [open]);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');

    if (!form.novaSenhaTemporaria.trim()) {
      setError('Informe a nova senha temporaria.');
      return;
    }

    try {
      await onSubmit({
        novaSenhaTemporaria: form.novaSenhaTemporaria,
        obrigarTrocaProximoLogin: form.obrigarTrocaProximoLogin,
      });
      setForm((current) => ({ ...current, novaSenhaTemporaria: '' }));
    } catch (requestError) {
      setError(getApiError(requestError, 'Nao foi possivel redefinir a senha.'));
    }
  }

  return (
    <Modal
      footer={
        <>
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
          <Button disabled={loading} type="submit" form="usuario-reset-senha-form" variant="danger">
            {loading ? 'Redefinindo...' : 'Redefinir senha'}
          </Button>
        </>
      }
      isOpen={open}
      onClose={onClose}
      title="Redefinir senha"
    >
      <form id="usuario-reset-senha-form" className="bp-form-grid" onSubmit={handleSubmit}>
        <Input
          hint="A senha sera temporaria. Use a opcao de troca obrigatoria para exigir alteracao no proximo login."
          id="usuario-reset-nova-senha"
          label="Nova senha temporaria"
          type="password"
          value={form.novaSenhaTemporaria}
          onChange={(event) => setForm((current) => ({ ...current, novaSenhaTemporaria: event.target.value }))}
        />
        <SwitchField
          checked={form.obrigarTrocaProximoLogin}
          id="usuario-reset-obrigar-troca"
          label="Obrigar troca no proximo login"
          onChange={(checked) => setForm((current) => ({ ...current, obrigarTrocaProximoLogin: checked }))}
        />
        {error ? <Alert variant="error">{error}</Alert> : null}
      </form>
    </Modal>
  );
}
