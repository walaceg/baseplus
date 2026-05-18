import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ActionIconButton, Alert, Badge, Button, Card, ConfirmDialog, EmptyState, Input, Loading, Select } from '../../../shared/components/index.js';
import { Trash2, UserPlus } from 'lucide-react';
import { useAuthorization } from '../../../core/auth/useAuthorization.js';
import { PERMISSIONS } from '../../../shared/auth/permissions.js';
import { useDebouncedValue } from '../../../shared/hooks/useDebouncedValue.js';
import * as roleService from './roleService.js';
import './roles.css';

const EMPTY_FORM = {
  name: '',
  description: '',
  ativo: true,
  type: 'FUNCTIONAL',
  permissionIds: [],
  organizationScopes: [],
};

const ROLE_TYPE_OPTIONS = [
  { value: 'FUNCTIONAL', label: 'Funcional - agrupa permissoes' },
  { value: 'ORGANIZATIONAL', label: 'Organizacional - agrupa escopos' },
  { value: 'SYSTEM', label: 'Sistema' },
];

const SCOPE_LEVEL_OPTIONS = [
  { value: 'VIEW', label: 'Visualizar' },
  { value: 'EDIT', label: 'Editar' },
  { value: 'ADMIN', label: 'Administrar' },
];

function getApiError(requestError, fallback) {
  const response = requestError.response?.data;
  return response?.errors?.[0] ?? response?.message ?? fallback;
}

function toForm(role) {
  return {
    name: role?.name ?? '',
    description: role?.description ?? '',
    ativo: role?.ativo ?? true,
    type: role?.type ?? 'FUNCTIONAL',
    permissionIds: role?.permissions?.map((permission) => permission.id) ?? [],
    organizationScopes: role?.organizationScopes?.map((scope) => ({
      id: scope.id,
      organizationUnitId: scope.organizationUnitId,
      scopeLevel: scope.scopeLevel,
      label: `${scope.organizationUnitTypeCode} ${scope.organizationUnitCode} - ${scope.organizationUnitName}`,
    })) ?? [],
  };
}

function getPermissionModule(permissionName) {
  if (!permissionName) {
    return 'GERAL';
  }

  return permissionName.split('_')[0] || 'GERAL';
}

export function RoleFormPage({ mode = 'create' }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const { hasPermission } = useAuthorization();
  const canEditRole = hasPermission(PERMISSIONS.ROLES_EDIT);
  const canManagePermissions = hasPermission(PERMISSIONS.ROLES_MANAGE_PERMISSIONS);
  const canManageOrganizationScopes = hasPermission(PERMISSIONS.ROLES_MANAGE_ORGANIZATION_SCOPES);
  const canManageUsers = hasPermission(PERMISSIONS.ROLES_MANAGE_USERS);
  const isEdit = mode === 'edit';
  const [form, setForm] = useState(EMPTY_FORM);
  const [role, setRole] = useState(null);
  const [permissions, setPermissions] = useState([]);
  const [organizationUnits, setOrganizationUnits] = useState([]);
  const [scopeDraft, setScopeDraft] = useState({ organizationUnitId: '', scopeLevel: 'VIEW' });
  const [permissionSearch, setPermissionSearch] = useState('');
  const debouncedPermissionSearch = useDebouncedValue(permissionSearch, 200);
  const [roleUsers, setRoleUsers] = useState([]);
  const [availableUsers, setAvailableUsers] = useState([]);
  const [userSearch, setUserSearch] = useState('');
  const debouncedUserSearch = useDebouncedValue(userSearch, 300);
  const [loadingUsers, setLoadingUsers] = useState(false);
  const [searchingUsers, setSearchingUsers] = useState(false);
  const [addingUserId, setAddingUserId] = useState(null);
  const [removeUserTarget, setRemoveUserTarget] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const title = isEdit ? 'Editar perfil' : 'Novo perfil';

  useEffect(() => {
    let active = true;

    async function loadData() {
      try {
        setLoading(true);
        setError('');
        const permissionsPromise = canManagePermissions ? roleService.getPermissions({ size: 1000 }) : Promise.resolve({ content: [] });
        const organizationUnitsPromise = canManageOrganizationScopes ? roleService.getOrganizationUnits({ active: true, size: 1000 }) : Promise.resolve({ content: [] });
        const rolePromise = isEdit ? roleService.getRole(id) : Promise.resolve(null);
        const [permissionsData, organizationUnitsData, roleData] = await Promise.all([permissionsPromise, organizationUnitsPromise, rolePromise]);

        if (active) {
          setPermissions(canManagePermissions ? permissionsData.content ?? [] : roleData?.permissions ?? []);
          setOrganizationUnits(organizationUnitsData.content ?? []);
          setRole(roleData);
          setForm(isEdit ? toForm(roleData) : EMPTY_FORM);
        }
      } catch (requestError) {
        if (active) {
          setError(getApiError(requestError, 'Nao foi possivel carregar o formulario de perfil.'));
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadData();

    return () => {
      active = false;
    };
  }, [canManageOrganizationScopes, canManagePermissions, id, isEdit]);

  useEffect(() => {
    if (!isEdit || !canManageUsers) {
      return;
    }

    let active = true;

    async function loadRoleUsers() {
      try {
        setLoadingUsers(true);
        const data = await roleService.getRoleUsuarios(id, { vinculado: true, size: 100 });

        if (active) {
          setRoleUsers(data.content ?? []);
        }
      } catch (requestError) {
        if (active) {
          setError(getApiError(requestError, 'Nao foi possivel carregar os usuarios do perfil.'));
        }
      } finally {
        if (active) {
          setLoadingUsers(false);
        }
      }
    }

    loadRoleUsers();

    return () => {
      active = false;
    };
  }, [canManageUsers, id, isEdit]);

  useEffect(() => {
    if (!isEdit || !canManageUsers) {
      return;
    }

    const term = debouncedUserSearch.trim();
    if (term.length < 2) {
      setAvailableUsers([]);
      return;
    }

    let active = true;

    async function searchAvailableUsers() {
      try {
        setSearchingUsers(true);
        const data = await roleService.getRoleUsuarios(id, { search: term, vinculado: false, size: 8 });

        if (active) {
          const linkedIds = new Set(roleUsers.map((usuario) => usuario.id));
          setAvailableUsers((data.content ?? []).filter((usuario) => !linkedIds.has(usuario.id)));
        }
      } catch (requestError) {
        if (active) {
          setError(getApiError(requestError, 'Nao foi possivel buscar usuarios para vincular.'));
        }
      } finally {
        if (active) {
          setSearchingUsers(false);
        }
      }
    }

    searchAvailableUsers();

    return () => {
      active = false;
    };
  }, [canManageUsers, debouncedUserSearch, id, isEdit, roleUsers]);

  const filteredPermissions = useMemo(() => {
    const term = debouncedPermissionSearch.trim().toLowerCase();
    const visiblePermissions = term
      ? permissions.filter((permission) => {
          const name = permission.name?.toLowerCase() ?? '';
          const description = permission.description?.toLowerCase() ?? '';
          return name.includes(term) || description.includes(term);
        })
      : permissions;

    return visiblePermissions.reduce((groups, permission) => {
      const moduleName = getPermissionModule(permission.name);
      if (!groups[moduleName]) {
        groups[moduleName] = [];
      }
      groups[moduleName].push(permission);
      return groups;
    }, {});
  }, [debouncedPermissionSearch, permissions]);

  function updateField(field, value) {
    setForm((current) => {
      if (field !== 'type') {
        return { ...current, [field]: value };
      }

      return {
        ...current,
        type: value,
        permissionIds: value === 'ORGANIZATIONAL' ? [] : current.permissionIds,
        organizationScopes: value === 'ORGANIZATIONAL' ? current.organizationScopes : [],
      };
    });
  }

  function togglePermission(permissionId, checked) {
    setForm((current) => {
      const nextIds = checked
        ? Array.from(new Set([...current.permissionIds, permissionId]))
        : current.permissionIds.filter((currentId) => currentId !== permissionId);

      return { ...current, permissionIds: nextIds };
    });
  }

  function addOrganizationScope() {
    const unitId = Number(scopeDraft.organizationUnitId);
    if (!unitId) {
      setError('Selecione a unidade organizacional.');
      return;
    }

    const unit = organizationUnits.find((item) => item.id === unitId);
    if (!unit) {
      setError('Unidade organizacional nao encontrada.');
      return;
    }

    setForm((current) => {
      if (current.organizationScopes.some((scope) => Number(scope.organizationUnitId) === unitId)) {
        setError('Esta unidade organizacional ja esta vinculada ao perfil.');
        return current;
      }

      return {
        ...current,
        organizationScopes: [
          ...current.organizationScopes,
          {
            organizationUnitId: unit.id,
            scopeLevel: scopeDraft.scopeLevel,
            label: `${unit.type?.code} ${unit.code} - ${unit.name}`,
          },
        ],
      };
    });
    setScopeDraft({ organizationUnitId: '', scopeLevel: 'VIEW' });
  }

  function removeOrganizationScope(scope) {
    setForm((current) => ({
      ...current,
      organizationScopes: current.organizationScopes.filter((currentScope) => {
        if (scope.id && currentScope.id) {
          return currentScope.id !== scope.id;
        }
        return Number(currentScope.organizationUnitId) !== Number(scope.organizationUnitId);
      }),
    }));
  }

  async function refreshRoleUsers() {
    const data = await roleService.getRoleUsuarios(id, { vinculado: true, size: 100 });
    setRoleUsers(data.content ?? []);
  }

  async function handleAddUser(usuario) {
    if (!usuario) {
      return;
    }

    try {
      setAddingUserId(usuario.id);
      setError('');
      setMessage('');
      const response = await roleService.addRoleUsuario(id, usuario.id);
      setMessage(response.message ?? 'Usuario vinculado ao perfil com sucesso.');
      await refreshRoleUsers();
      setAvailableUsers((current) => current.filter((item) => item.id !== usuario.id));
    } catch (requestError) {
      setError(getApiError(requestError, 'Nao foi possivel vincular o usuario ao perfil.'));
    } finally {
      setAddingUserId(null);
    }
  }

  async function confirmRemoveUser() {
    if (!removeUserTarget) {
      return;
    }

    try {
      setSaving(true);
      setError('');
      setMessage('');
      const response = await roleService.removeRoleUsuario(id, removeUserTarget.id);
      setMessage(response.message ?? 'Usuario removido do perfil com sucesso.');
      await refreshRoleUsers();
      setRemoveUserTarget(null);
    } catch (requestError) {
      setError(getApiError(requestError, 'Nao foi possivel remover o usuario do perfil.'));
    } finally {
      setSaving(false);
    }
  }

  async function syncOrganizationScopes() {
    const currentScopes = role?.organizationScopes ?? [];
    const currentByUnitId = new Map(currentScopes.map((scope) => [Number(scope.organizationUnitId), scope]));
    const nextByUnitId = new Map(form.organizationScopes.map((scope) => [Number(scope.organizationUnitId), scope]));
    let updated = role;

    for (const currentScope of currentScopes) {
      const nextScope = nextByUnitId.get(Number(currentScope.organizationUnitId));
      if (!nextScope || nextScope.scopeLevel !== currentScope.scopeLevel) {
        updated = await roleService.removeRoleOrganizationScope(id, currentScope.id);
      }
    }

    for (const nextScope of form.organizationScopes) {
      const currentScope = currentByUnitId.get(Number(nextScope.organizationUnitId));
      if (!currentScope || currentScope.scopeLevel !== nextScope.scopeLevel) {
        updated = await roleService.addRoleOrganizationScope(id, {
          organizationUnitId: Number(nextScope.organizationUnitId),
          scopeLevel: nextScope.scopeLevel,
        });
      }
    }

    return updated;
  }

  function validate() {
    if (!form.name.trim()) {
      return 'Informe o nome do perfil.';
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

    const payload = {
      name: form.name,
      description: form.description || null,
      ativo: form.ativo,
      type: role?.sistema ? 'SYSTEM' : form.type,
      permissionIds: form.type === 'ORGANIZATIONAL' ? [] : (canManagePermissions ? form.permissionIds : role?.permissions?.map((permission) => permission.id) ?? []),
      organizationScopes: form.type === 'ORGANIZATIONAL'
        ? form.organizationScopes.map((scope) => ({
            organizationUnitId: Number(scope.organizationUnitId),
            scopeLevel: scope.scopeLevel,
          }))
        : [],
    };

    try {
      setSaving(true);
      setError('');
      setMessage('');

      if (isEdit && canEditRole) {
        const updated = await roleService.updateRole(id, payload);
        setRole(updated);
        setForm(toForm(updated));
        setMessage('Perfil atualizado com sucesso.');
      } else if (isEdit && canManageOrganizationScopes && form.type === 'ORGANIZATIONAL') {
        const updated = await syncOrganizationScopes();
        setRole(updated);
        setForm(toForm(updated));
        setMessage('Escopos organizacionais do perfil atualizados com sucesso.');
      } else if (isEdit && canManagePermissions && form.type !== 'ORGANIZATIONAL') {
        const currentPermissionIds = new Set(role?.permissions?.map((permission) => permission.id) ?? []);
        const nextPermissionIds = new Set(form.permissionIds);
        const additions = [...nextPermissionIds].filter((permissionId) => !currentPermissionIds.has(permissionId));
        const removals = [...currentPermissionIds].filter((permissionId) => !nextPermissionIds.has(permissionId));
        let updated = role;

        for (const permissionId of additions) {
          updated = await roleService.addRolePermission(id, permissionId);
        }

        for (const permissionId of removals) {
          updated = await roleService.removeRolePermission(id, permissionId);
        }

        setRole(updated);
        setForm(toForm(updated));
        setMessage('Permissoes do perfil atualizadas com sucesso.');
      } else {
        await roleService.createRole(payload);
        navigate('/app/roles', { replace: true, state: { message: 'Perfil criado com sucesso.' } });
      }
    } catch (requestError) {
      setError(getApiError(requestError, 'Nao foi possivel salvar o perfil.'));
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="bp-role-form-page">
      <section className="bp-list-page__header">
        <div>
          <h1>{title}</h1>
          <p>{isEdit ? 'Atualize dados administrativos e permissoes do perfil.' : 'Cadastre uma unidade de autorizacao da plataforma.'}</p>
        </div>
      </section>

      {message ? <Alert variant="success">{message}</Alert> : null}
      {error ? <Alert variant="error">{error}</Alert> : null}

      {loading ? (
        <Card>
          <Card.Body>
            <Loading label="Carregando perfil..." />
          </Card.Body>
        </Card>
      ) : (
        <form className="bp-role-page-form" onSubmit={handleSubmit}>
          <Card>
            <Card.Body>
              <section className="bp-role-form-section">
                <div className="bp-role-form-section__header">
                  <h2>Dados do perfil</h2>
                </div>
                <div className="bp-role-form-grid">
                  <Input id="role-name" disabled={isEdit && !canEditRole} label="Nome" value={form.name} onChange={(event) => updateField('name', event.target.value)} />
                  <Input
                    id="role-description"
                    disabled={isEdit && !canEditRole}
                    label="Descricao"
                    value={form.description}
                    onChange={(event) => updateField('description', event.target.value)}
                  />
                  <Select
                    disabled={role?.sistema || (isEdit && !canEditRole)}
                    id="role-type"
                    label="Tipo de perfil"
                    options={ROLE_TYPE_OPTIONS}
                    value={form.type}
                    onChange={(event) => updateField('type', event.target.value)}
                  />
                </div>
              </section>
            </Card.Body>
          </Card>

          {form.type !== 'ORGANIZATIONAL' ? (
          <Card>
            <Card.Body>
              <section className="bp-role-form-section">
                <div className="bp-role-form-section__header">
                  <h2>Permissoes</h2>
                  <p>{canManagePermissions ? 'Selecione as permissoes vinculadas a este perfil.' : 'Seu perfil permite editar dados, mas nao gerenciar permissoes.'}</p>
                </div>
                <Input
                  id="role-permission-search"
                  label="Buscar permissoes"
                  placeholder="Buscar por permissao ou descricao..."
                  value={permissionSearch}
                  onChange={(event) => setPermissionSearch(event.target.value)}
                />
                <div className="bp-role-permission-groups">
                  {Object.entries(filteredPermissions).length ? (
                    Object.entries(filteredPermissions)
                      .sort(([first], [second]) => first.localeCompare(second))
                      .map(([moduleName, modulePermissions]) => (
                        <section className="bp-role-permission-group" key={moduleName}>
                          <header>
                            <strong>{moduleName}</strong>
                            <Badge variant="secondary">{modulePermissions.length}</Badge>
                          </header>
                          <div className="bp-role-permission-options">
                            {modulePermissions.map((permission) => (
                              <label className="bp-role-permission-option" htmlFor={`role-permission-${permission.id}`} key={permission.id}>
                                <input
                                  checked={form.permissionIds.includes(permission.id)}
                                  disabled={!canManagePermissions}
                                  id={`role-permission-${permission.id}`}
                                  type="checkbox"
                                  onChange={(event) => togglePermission(permission.id, event.target.checked)}
                                />
                                <span>
                                  <strong>{permission.name}</strong>
                                  <small>{permission.description || 'Sem descricao'}</small>
                                </span>
                              </label>
                            ))}
                          </div>
                        </section>
                      ))
                  ) : (
                    <p className="bp-role-empty">Nenhuma permissao encontrada.</p>
                  )}
                </div>
              </section>
            </Card.Body>
          </Card>
          ) : null}

          {form.type === 'ORGANIZATIONAL' ? (
            <Card>
              <Card.Body>
                <section className="bp-role-form-section">
                  <div className="bp-role-form-section__header">
                    <h2>Escopos organizacionais</h2>
                    <p>Selecione onde este perfil organizacional concede acesso.</p>
                  </div>

                  <div className="bp-role-scope-editor">
                    <Select
                      disabled={!canManageOrganizationScopes}
                      id="role-organization-unit"
                      label="Unidade organizacional"
                      options={organizationUnits.map((unit) => ({
                        value: String(unit.id),
                        label: `${unit.type?.code} ${unit.code} - ${unit.name}`,
                      }))}
                      placeholder="Selecione uma unidade"
                      value={scopeDraft.organizationUnitId}
                      onChange={(event) => setScopeDraft((current) => ({ ...current, organizationUnitId: event.target.value }))}
                    />
                    <Select
                      disabled={!canManageOrganizationScopes}
                      id="role-organization-scope-level"
                      label="Nivel"
                      options={SCOPE_LEVEL_OPTIONS}
                      value={scopeDraft.scopeLevel}
                      onChange={(event) => setScopeDraft((current) => ({ ...current, scopeLevel: event.target.value }))}
                    />
                    <Button disabled={!canManageOrganizationScopes} type="button" onClick={addOrganizationScope}>
                      Adicionar escopo
                    </Button>
                  </div>

                  <div className="bp-role-scope-list">
                    {form.organizationScopes.length ? (
                      form.organizationScopes.map((scope) => (
                        <div className="bp-role-scope-row" key={`${scope.id ?? 'new'}-${scope.organizationUnitId}`}>
                          <div>
                            <strong>{scope.label}</strong>
                            <span>{SCOPE_LEVEL_OPTIONS.find((option) => option.value === scope.scopeLevel)?.label ?? scope.scopeLevel}</span>
                          </div>
                          <ActionIconButton
                            disabled={!canManageOrganizationScopes}
                            icon={Trash2}
                            label="Remover escopo"
                            title="Remover escopo"
                            variant="danger"
                            onClick={() => removeOrganizationScope(scope)}
                          />
                        </div>
                      ))
                    ) : (
                      <EmptyState description="Nenhuma unidade organizacional vinculada ao perfil" title="Sem escopos" />
                    )}
                  </div>
                </section>
              </Card.Body>
            </Card>
          ) : null}

          <Card>
            <Card.Body>
              <section className="bp-role-form-section">
                <div className="bp-role-form-section__header">
                  <h2>Seguranca administrativa</h2>
                  {role?.sistema ? <p>Perfil interno da plataforma. A exclusao e desativacao ficam bloqueadas.</p> : null}
                </div>
                <div className="bp-role-admin-flags">
                  <label className="bp-role-switch" htmlFor="role-active">
                    <input
                      checked={form.ativo}
                      className="bp-role-switch__input"
                      disabled={role?.sistema || (isEdit && !canEditRole)}
                      id="role-active"
                      type="checkbox"
                      onChange={(event) => updateField('ativo', event.target.checked)}
                    />
                    <span className="bp-role-switch__control" aria-hidden="true" />
                    <span className="bp-role-switch__label">Ativo</span>
                  </label>
                  <Badge variant={role?.sistema ? 'primary' : 'secondary'}>{role?.sistema ? 'Sistema' : 'Customizado'}</Badge>
                </div>
              </section>
            </Card.Body>
          </Card>

          {isEdit && canManageUsers ? (
            <Card>
              <Card.Body>
                <section className="bp-role-form-section">
                  <div className="bp-role-form-section__header">
                    <h2>Usuarios do perfil</h2>
                    <p>Gerencie quais usuarios pertencem a este perfil de acesso.</p>
                  </div>

                  <div className="bp-role-user-manager">
                    <div className="bp-role-user-manager__search">
                      <Input
                        id="role-user-search"
                        label="Buscar usuario para adicionar"
                        placeholder="Digite nome ou email..."
                        value={userSearch}
                        onChange={(event) => setUserSearch(event.target.value)}
                      />
                      {searchingUsers ? <Loading label="Buscando usuarios..." /> : null}
                      {debouncedUserSearch.trim().length >= 2 && availableUsers.length ? (
                        <div className="bp-role-user-results">
                          {availableUsers.map((usuario) => (
                            <div className="bp-role-user-row" key={usuario.id}>
                              <UserSummary usuario={usuario} />
                              <ActionIconButton
                                disabled={addingUserId === usuario.id}
                                icon={UserPlus}
                                label="Adicionar usuario"
                                title="Adicionar usuario"
                                variant="primary"
                                onClick={() => handleAddUser(usuario)}
                              />
                            </div>
                          ))}
                        </div>
                      ) : null}
                    </div>

                    <div className="bp-role-user-manager__list">
                      {loadingUsers ? (
                        <Loading label="Carregando usuarios do perfil..." />
                      ) : roleUsers.length ? (
                        <div className="bp-role-user-list">
                          {roleUsers.map((usuario) => (
                            <div className="bp-role-user-row" key={usuario.id}>
                              <UserSummary usuario={usuario} />
                              <ActionIconButton
                                icon={Trash2}
                                label="Remover usuario"
                                title="Remover usuario"
                                variant="danger"
                                onClick={() => setRemoveUserTarget(usuario)}
                              />
                            </div>
                          ))}
                        </div>
                      ) : (
                        <EmptyState description="Nenhum usuario vinculado a este perfil" title="Sem usuarios vinculados" />
                      )}
                    </div>
                  </div>
                </section>
              </Card.Body>
            </Card>
          ) : null}

          <div className="bp-role-form-footer">
            <Button type="button" variant="secondary" onClick={() => navigate('/app/roles')}>
              Cancelar
            </Button>
            {canEditRole || (canManagePermissions && form.type !== 'ORGANIZATIONAL') || (canManageOrganizationScopes && form.type === 'ORGANIZATIONAL') || !isEdit ? (
              <Button disabled={saving} type="submit">
                {saving ? 'Salvando...' : 'Salvar perfil'}
              </Button>
            ) : null}
          </div>
        </form>
      )}

      <ConfirmDialog
        cancelLabel="Cancelar"
        confirmLabel="Remover"
        message="Esta acao vai remover o usuario selecionado deste perfil."
        onCancel={() => setRemoveUserTarget(null)}
        onConfirm={confirmRemoveUser}
        open={Boolean(removeUserTarget)}
        title="Remover usuario do perfil"
      />
    </div>
  );
}

function UserSummary({ usuario }) {
  return (
    <div className="bp-role-user-summary">
      <div>
        <strong>{usuario.nomeExibicao || usuario.nome}</strong>
        <span>{usuario.email}</span>
      </div>
      <div className="bp-role-user-badges">
        <Badge variant={usuario.ativo ? 'success' : 'warning'}>{usuario.ativo ? 'Ativo' : 'Inativo'}</Badge>
        {usuario.bloqueado ? <Badge variant="danger">Bloqueado</Badge> : null}
        {usuario.trocarSenhaPrimeiroAcesso ? <Badge variant="primary">Primeiro acesso</Badge> : null}
      </div>
    </div>
  );
}
