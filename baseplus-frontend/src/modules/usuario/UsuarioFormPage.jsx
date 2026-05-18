import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ActionIconButton, Alert, Badge, Button, Card, ConfirmDialog, Input, Loading } from '../../shared/components/index.js';
import { KeyRound, Lock, Plus, Search, Unlock, X } from 'lucide-react';
import { useAuthorization } from '../../core/auth/useAuthorization.js';
import { PERMISSIONS } from '../../shared/auth/permissions.js';
import { useDebouncedValue } from '../../shared/hooks/useDebouncedValue.js';
import * as roleService from '../auth/roles/roleService.js';
import * as usuarioService from './usuarioService.js';
import { ResetSenhaUsuarioModal } from './ResetSenhaUsuarioModal.jsx';
import './usuario.css';

const EMPTY_FORM = {
  nome: '',
  nomeExibicao: '',
  email: '',
  senha: '',
  cargo: '',
  departamento: '',
  telefone: '',
  celular: '',
  matricula: '',
  observacoesInternas: '',
  ativo: true,
  trocarSenhaPrimeiroAcesso: true,
};

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

function toForm(user) {
  return {
    nome: user?.nome ?? '',
    nomeExibicao: user?.nomeExibicao ?? '',
    email: user?.email ?? '',
    senha: '',
    cargo: user?.cargo ?? '',
    departamento: user?.departamento ?? '',
    telefone: user?.telefone ?? '',
    celular: user?.celular ?? '',
    matricula: user?.matricula ?? '',
    observacoesInternas: user?.observacoesInternas ?? '',
    ativo: user?.ativo ?? true,
    trocarSenhaPrimeiroAcesso: user?.trocarSenhaPrimeiroAcesso ?? true,
  };
}

function toPayload(form, mode, roleIds) {
  const payload = {
    nome: form.nome.trim(),
    nomeExibicao: form.nomeExibicao.trim(),
    email: form.email.trim(),
    cargo: form.cargo.trim(),
    departamento: form.departamento.trim(),
    telefone: form.telefone.trim(),
    celular: form.celular.trim(),
    matricula: form.matricula.trim(),
    observacoesInternas: form.observacoesInternas.trim(),
    ativo: form.ativo,
    trocarSenhaPrimeiroAcesso: form.trocarSenhaPrimeiroAcesso,
  };

  if (Array.isArray(roleIds)) {
    payload.roleIds = roleIds;
  }

  if (mode === 'create') {
    payload.senha = form.senha;
    payload.bloqueado = false;
  }

  return payload;
}

function normalizeUserRoles(user) {
  if (Array.isArray(user?.roleDetails) && user.roleDetails.length) {
    return user.roleDetails;
  }

  return (user?.roles ?? []).map((roleName) => ({
    id: null,
    name: roleName,
    description: '',
    ativo: true,
    sistema: roleName === 'ADMIN',
  }));
}

function RoleTypeBadge({ role }) {
  return <Badge variant={role?.sistema ? 'primary' : 'secondary'}>{role?.sistema ? 'Sistema' : 'Customizado'}</Badge>;
}

export function UsuarioFormPage({ mode = 'create' }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const { hasPermission } = useAuthorization();
  const canEdit = hasPermission(PERMISSIONS.USERS_EDIT);
  const canResetPassword = hasPermission(PERMISSIONS.USERS_RESET_PASSWORD);
  const isEdit = mode === 'edit';
  const [form, setForm] = useState(EMPTY_FORM);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(isEdit);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [resetModalOpen, setResetModalOpen] = useState(false);
  const [resetLoading, setResetLoading] = useState(false);
  const [blockTarget, setBlockTarget] = useState(null);
  const [selectedRoles, setSelectedRoles] = useState([]);
  const [roleSearch, setRoleSearch] = useState('');
  const [availableRoles, setAvailableRoles] = useState([]);
  const [rolesLoading, setRolesLoading] = useState(false);
  const [rolesError, setRolesError] = useState('');
  const debouncedRoleSearch = useDebouncedValue(roleSearch, 300);
  const title = isEdit ? 'Editar usuario' : 'Novo usuario';

  useEffect(() => {
    if (!isEdit) {
      setForm(EMPTY_FORM);
      setUser(null);
      setLoading(false);
      return;
    }

    let active = true;

    async function loadUsuario() {
      try {
        setLoading(true);
        setError('');
        const data = await usuarioService.getUsuario(id);

        if (active) {
          setUser(data);
          setForm(toForm(data));
          setSelectedRoles(normalizeUserRoles(data));
        }
      } catch (requestError) {
        if (active) {
          setError(getApiError(requestError, 'Nao foi possivel carregar o usuario.'));
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadUsuario();

    return () => {
      active = false;
    };
  }, [id, isEdit]);

  useEffect(() => {
    if (!isEdit) {
      setSelectedRoles([]);
      setAvailableRoles([]);
      setRoleSearch('');
      setRolesError('');
      return;
    }

    let active = true;

    async function loadRoles() {
      try {
        setRolesLoading(true);
        setRolesError('');
        const data = await roleService.getRoles({
          search: debouncedRoleSearch.trim() || undefined,
          size: 20,
        });

        if (active) {
          setAvailableRoles(data.content ?? []);
        }
      } catch (requestError) {
        if (active) {
          setRolesError(getApiError(requestError, 'Nao foi possivel carregar os perfis.')); 
        }
      } finally {
        if (active) {
          setRolesLoading(false);
        }
      }
    }

    loadRoles();

    return () => {
      active = false;
    };
  }, [debouncedRoleSearch, isEdit]);

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function validate() {
    if (!form.nome.trim() || !form.email.trim()) {
      return 'Informe nome e email.';
    }

    if (!isEdit && !form.senha.trim()) {
      return 'Informe a senha inicial.';
    }

    return '';
  }

  async function handleSubmit(event) {
    event.preventDefault();
    const validationError = validate();

    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      setSaving(true);
      setError('');
      setMessage('');

      if (isEdit) {
        const roleIds = selectedRoles.map((role) => role.id).filter((roleId) => roleId !== null && roleId !== undefined);
        const updated = await usuarioService.updateUsuario(id, toPayload(form, 'edit', roleIds));
        setUser(updated);
        setForm(toForm(updated));
        setSelectedRoles(normalizeUserRoles(updated));
        setMessage('Usuario atualizado com sucesso.');
      } else {
        await usuarioService.createUsuario(toPayload(form, 'create'));
        navigate('/app/usuarios', { replace: true, state: { message: 'Usuario criado com sucesso.' } });
      }
    } catch (requestError) {
      setError(getApiError(requestError, 'Nao foi possivel salvar o usuario.'));
    } finally {
      setSaving(false);
    }
  }

  async function handleResetPassword(payload) {
    if (!user) {
      return;
    }

    try {
      setResetLoading(true);
      const response = await usuarioService.resetUsuarioSenha(user.id, payload);
      setUser(response.data);
      setForm(toForm(response.data));
      setMessage(response.message ?? 'Senha redefinida com sucesso.');
      setResetModalOpen(false);
    } finally {
      setResetLoading(false);
    }
  }

  async function confirmBlockToggle() {
    if (!user || blockTarget === null) {
      return;
    }

    try {
      setSaving(true);
      setError('');
      setMessage('');
      const updated = await usuarioService.updateUsuario(user.id, {
        ...toPayload(toForm(user), 'edit', normalizeUserRoles(user).map((role) => role.id).filter((roleId) => roleId !== null && roleId !== undefined)),
        bloqueado: blockTarget,
      });
      setUser(updated);
      setForm(toForm(updated));
      setSelectedRoles(normalizeUserRoles(updated));
      setMessage(blockTarget ? 'Usuario bloqueado com sucesso.' : 'Usuario desbloqueado com sucesso.');
    } catch (requestError) {
      setError(getApiError(requestError, 'Nao foi possivel atualizar o bloqueio do usuario.'));
    } finally {
      setSaving(false);
      setBlockTarget(null);
    }
  }

  function addRole(role) {
    if (!role?.id || selectedRoles.some((selectedRole) => selectedRole.id === role.id)) {
      return;
    }

    setSelectedRoles((current) => [...current, role].sort((first, second) => first.name.localeCompare(second.name)));
    setRolesError('');
  }

  function removeRole(roleId) {
    setSelectedRoles((current) => current.filter((role) => role.id !== roleId));
    setRolesError('');
  }

  const selectedRoleIds = new Set(selectedRoles.map((role) => role.id));
  const filteredAvailableRoles = availableRoles.filter((role) => !selectedRoleIds.has(role.id));

  return (
    <div className="bp-usuario-form-page">
      <section className="bp-list-page__header">
        <div>
          <h1>{title}</h1>
          <p>{isEdit ? 'Atualize cadastro, acesso e dados corporativos.' : 'Cadastre acesso e dados corporativos do usuario.'}</p>
        </div>
      </section>

      {message ? <Alert variant="success">{message}</Alert> : null}
      {error ? <Alert variant="error">{error}</Alert> : null}

      {loading ? (
        <Card>
          <Card.Body>
            <Loading label="Carregando usuario..." />
          </Card.Body>
        </Card>
      ) : (
        <form id="usuario-page-form" className="bp-usuario-page-form" onSubmit={handleSubmit}>
          <Card>
            <Card.Body>
              <section className="bp-usuario-form-section">
                <div className="bp-usuario-form-section__header">
                  <h2>Acesso</h2>
                </div>
                <div className="bp-usuario-form-grid">
                  <Input id="usuario-nome" label="Nome" value={form.nome} onChange={(event) => updateField('nome', event.target.value)} />
                  <Input
                    id="usuario-nome-exibicao"
                    label="Nome de exibicao"
                    value={form.nomeExibicao}
                    onChange={(event) => updateField('nomeExibicao', event.target.value)}
                  />
                  <Input
                    id="usuario-email"
                    label="Email"
                    type="email"
                    value={form.email}
                    onChange={(event) => updateField('email', event.target.value)}
                  />
                  {!isEdit ? (
                    <Input
                      id="usuario-senha"
                      label="Senha inicial"
                      type="password"
                      value={form.senha}
                      onChange={(event) => updateField('senha', event.target.value)}
                    />
                  ) : null}
                </div>
              </section>
            </Card.Body>
          </Card>

          <Card>
            <Card.Body>
              <section className="bp-usuario-form-section">
                <div className="bp-usuario-form-section__header bp-usuario-form-section__header--split">
                  <div>
                    <h2>Seguranca</h2>
                    {isEdit ? (
                      <p>
                        Status atual:{' '}
                        <Badge variant={user?.bloqueado ? 'danger' : 'success'}>
                          {user?.bloqueado ? 'Bloqueado' : 'Liberado'}
                        </Badge>
                      </p>
                    ) : null}
                  </div>
                  {isEdit && (canResetPassword || canEdit) ? (
                    <div className="bp-usuario-form-section__actions bp-action-group">
                      {canResetPassword ? (
                        <ActionIconButton
                          icon={KeyRound}
                          label="Resetar senha"
                          title="Resetar senha"
                          variant="primary"
                          onClick={() => setResetModalOpen(true)}
                        />
                      ) : null}
                      {canEdit ? (
                        <ActionIconButton
                          icon={user?.bloqueado ? Unlock : Lock}
                          label={user?.bloqueado ? 'Desbloquear' : 'Bloquear'}
                          title={user?.bloqueado ? 'Desbloquear' : 'Bloquear'}
                          variant={user?.bloqueado ? 'subtle' : 'danger'}
                          onClick={() => setBlockTarget(!user?.bloqueado)}
                        />
                      ) : null}
                    </div>
                  ) : null}
                </div>
                <div className="bp-usuario-form-flags">
                  <SwitchField checked={form.ativo} id="usuario-ativo" label="Ativo" onChange={(checked) => updateField('ativo', checked)} />
                  <SwitchField
                    checked={form.trocarSenhaPrimeiroAcesso}
                    id="usuario-trocar-senha"
                    label="Forcar troca de senha no primeiro acesso"
                    onChange={(checked) => updateField('trocarSenhaPrimeiroAcesso', checked)}
                  />
                </div>
              </section>
            </Card.Body>
          </Card>

          <Card>
            <Card.Body>
              <section className="bp-usuario-form-section">
                <div className="bp-usuario-form-section__header">
                  <h2>Dados corporativos</h2>
                </div>
                <div className="bp-usuario-form-grid">
                  <Input id="usuario-cargo" label="Cargo" value={form.cargo} onChange={(event) => updateField('cargo', event.target.value)} />
                  <Input
                    id="usuario-departamento"
                    label="Departamento"
                    value={form.departamento}
                    onChange={(event) => updateField('departamento', event.target.value)}
                  />
                  <Input id="usuario-telefone" label="Telefone" value={form.telefone} onChange={(event) => updateField('telefone', event.target.value)} />
                  <Input id="usuario-celular" label="Celular" value={form.celular} onChange={(event) => updateField('celular', event.target.value)} />
                  <Input id="usuario-matricula" label="Matricula" value={form.matricula} onChange={(event) => updateField('matricula', event.target.value)} />
                  <Input
                    id="usuario-observacoes-internas"
                    label="Observacoes internas"
                    value={form.observacoesInternas}
                    onChange={(event) => updateField('observacoesInternas', event.target.value)}
                  />
                </div>
              </section>
            </Card.Body>
          </Card>

          <Card>
            <Card.Body>
              <section className="bp-usuario-form-section">
                <div className="bp-usuario-form-section__header bp-usuario-form-section__header--split">
                  <div>
                    <h2>Perfis</h2>
                    <p>Vincule perfis administrativos ao usuario sem sair da edicao.</p>
                  </div>
                  {selectedRoles.length ? <Badge variant="primary">{selectedRoles.length} vinculados</Badge> : null}
                </div>
                {!isEdit ? (
                  <p className="bp-usuario-empty">Perfis podem ser vinculados depois que o usuario for criado.</p>
                ) : (
                  <div className="bp-usuario-role-manager">
                    <div className="bp-usuario-role-manager__search">
                      <Input
                        id="usuario-role-search"
                        label="Buscar perfis"
                        placeholder="Nome ou descricao"
                        value={roleSearch}
                        onChange={(event) => setRoleSearch(event.target.value)}
                      />
                      <Search aria-hidden="true" size={18} strokeWidth={2} />
                    </div>

                    {rolesError ? <Alert variant="error">{rolesError}</Alert> : null}

                    <div className="bp-usuario-role-manager__grid">
                      <div className="bp-usuario-role-panel">
                        <div className="bp-usuario-role-panel__header">
                          <h3>Disponiveis</h3>
                          {rolesLoading ? <span>Carregando...</span> : null}
                        </div>
                        {rolesLoading ? (
                          <Loading label="Carregando perfis..." />
                        ) : filteredAvailableRoles.length ? (
                          <div className="bp-usuario-role-list">
                            {filteredAvailableRoles.map((role) => (
                              <div className="bp-usuario-role-row" data-system={role.sistema ? 'true' : undefined} key={role.id}>
                                <div className="bp-usuario-role-row__content">
                                  <strong>{role.name}</strong>
                                  <span>{role.description || 'Sem descricao cadastrada.'}</span>
                                  <div className="bp-usuario-role-row__badges">
                                    <RoleTypeBadge role={role} />
                                    {!role.ativo ? <Badge variant="warning">Inativo</Badge> : null}
                                  </div>
                                </div>
                                <ActionIconButton icon={Plus} label={`Adicionar ${role.name}`} title="Adicionar perfil" onClick={() => addRole(role)} />
                              </div>
                            ))}
                          </div>
                        ) : (
                          <p className="bp-usuario-empty">Nenhum perfil disponivel para esta busca.</p>
                        )}
                      </div>

                      <div className="bp-usuario-role-panel">
                        <div className="bp-usuario-role-panel__header">
                          <h3>Vinculados</h3>
                        </div>
                        {selectedRoles.length ? (
                          <div className="bp-usuario-role-list">
                            {selectedRoles.map((role) => (
                              <div className="bp-usuario-role-row" data-system={role.sistema ? 'true' : undefined} key={role.id ?? role.name}>
                                <div className="bp-usuario-role-row__content">
                                  <strong>{role.name}</strong>
                                  <span>{role.description || 'Sem descricao cadastrada.'}</span>
                                  <div className="bp-usuario-role-row__badges">
                                    <RoleTypeBadge role={role} />
                                    {!role.ativo ? <Badge variant="warning">Inativo</Badge> : null}
                                  </div>
                                </div>
                                <ActionIconButton
                                  icon={X}
                                  label={`Remover ${role.name}`}
                                  title="Remover perfil"
                                  variant="danger"
                                  onClick={() => removeRole(role.id)}
                                />
                              </div>
                            ))}
                          </div>
                        ) : (
                          <p className="bp-usuario-empty">Nenhum perfil vinculado. Use a busca para adicionar perfis a este usuario.</p>
                        )}
                      </div>
                    </div>
                  </div>
                )}
              </section>
            </Card.Body>
          </Card>

          <div className="bp-usuario-form-footer">
            <Button type="button" variant="secondary" onClick={() => navigate('/app/usuarios')}>
              Cancelar
            </Button>
            <Button disabled={saving} type="submit">
              {saving ? 'Salvando...' : 'Salvar usuario'}
            </Button>
          </div>
        </form>
      )}

      <ResetSenhaUsuarioModal
        loading={resetLoading}
        open={resetModalOpen}
        onClose={() => setResetModalOpen(false)}
        onSubmit={handleResetPassword}
      />

      <ConfirmDialog
        cancelLabel="Cancelar"
        confirmLabel={blockTarget ? 'Bloquear' : 'Desbloquear'}
        message={blockTarget ? 'Esta acao vai bloquear o acesso do usuario.' : 'Esta acao vai liberar o acesso do usuario.'}
        onCancel={() => setBlockTarget(null)}
        onConfirm={confirmBlockToggle}
        open={blockTarget !== null}
        title={blockTarget ? 'Bloquear usuario' : 'Desbloquear usuario'}
      />
    </div>
  );
}
