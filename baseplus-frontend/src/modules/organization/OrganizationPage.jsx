import { useEffect, useMemo, useState } from 'react';
import { ActionIconButton, Alert, Badge, Button, Card, ConfirmDialog, EmptyState, Input, Loading, Select, Table } from '../../shared/components/index.js';
import { GitBranch, Pencil, Tags, Trash2 } from 'lucide-react';
import { useAuthorization } from '../../core/auth/useAuthorization.js';
import { PERMISSIONS } from '../../shared/auth/permissions.js';
import { useDebouncedValue } from '../../shared/hooks/useDebouncedValue.js';
import * as organizationService from './organizationService.js';
import { OrganizationTypeModal } from './OrganizationTypeModal.jsx';
import { OrganizationUnitModal } from './OrganizationUnitModal.jsx';
import './organization.css';

function getApiError(requestError, fallback) {
  const response = requestError.response?.data;
  return response?.errors?.[0] ?? response?.message ?? fallback;
}

export function OrganizationPage() {
  const { hasPermission } = useAuthorization();
  const canCreate = hasPermission(PERMISSIONS.ORGANIZATION_UNITS_CREATE) || hasPermission(PERMISSIONS.ROLES_EDIT);
  const canEdit = hasPermission(PERMISSIONS.ORGANIZATION_UNITS_EDIT) || hasPermission(PERMISSIONS.ROLES_EDIT);
  const canDelete = hasPermission(PERMISSIONS.ORGANIZATION_UNITS_DELETE) || hasPermission(PERMISSIONS.ROLES_DELETE);
  const [types, setTypes] = useState([]);
  const [units, setUnits] = useState([]);
  const [typeSearch, setTypeSearch] = useState('');
  const [unitSearch, setUnitSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const debouncedTypeSearch = useDebouncedValue(typeSearch, 250);
  const debouncedUnitSearch = useDebouncedValue(unitSearch, 250);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [reloadToken, setReloadToken] = useState(0);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [typeModal, setTypeModal] = useState({ open: false, mode: 'create', item: null });
  const [unitModal, setUnitModal] = useState({ open: false, mode: 'create', item: null });
  const [deleteTarget, setDeleteTarget] = useState(null);

  useEffect(() => {
    let active = true;

    async function loadData() {
      try {
        setLoading(true);
        setError('');
        const [typesData, unitsData] = await Promise.all([
          organizationService.getOrganizationUnitTypes({ size: 1000 }),
          organizationService.getOrganizationUnits({ size: 1000 }),
        ]);

        if (active) {
          setTypes(typesData.content ?? []);
          setUnits(unitsData.content ?? []);
        }
      } catch (requestError) {
        if (active) {
          setError(getApiError(requestError, 'Nao foi possivel carregar a estrutura organizacional.'));
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
  }, [reloadToken]);

  const filteredTypes = useMemo(() => {
    const term = debouncedTypeSearch.trim().toLowerCase();
    if (!term) {
      return types;
    }
    return types.filter((type) => type.code?.toLowerCase().includes(term) || type.name?.toLowerCase().includes(term));
  }, [debouncedTypeSearch, types]);

  const filteredUnits = useMemo(() => {
    const term = debouncedUnitSearch.trim().toLowerCase();
    return units.filter((unit) => {
      const matchesType = typeFilter ? String(unit.type?.id) === typeFilter : true;
      const matchesSearch = term
        ? unit.code?.toLowerCase().includes(term) || unit.name?.toLowerCase().includes(term) || unit.type?.name?.toLowerCase().includes(term)
        : true;
      return matchesType && matchesSearch;
    });
  }, [debouncedUnitSearch, typeFilter, units]);

  function refreshData() {
    setReloadToken((current) => current + 1);
  }

  function openTypeCreate() {
    setTypeModal({ open: true, mode: 'create', item: null });
  }

  function openTypeEdit(type) {
    setTypeModal({ open: true, mode: 'edit', item: type });
  }

  function openUnitCreate() {
    setUnitModal({ open: true, mode: 'create', item: null });
  }

  function openUnitEdit(unit) {
    setUnitModal({ open: true, mode: 'edit', item: unit });
  }

  async function handleTypeSubmit(form) {
    if (!form.code || !form.name) {
      setError('Informe codigo e nome do tipo organizacional.');
      return;
    }

    try {
      setSaving(true);
      setError('');
      setMessage('');
      if (typeModal.mode === 'edit' && typeModal.item) {
        await organizationService.updateOrganizationUnitType(typeModal.item.id, form);
        setMessage('Tipo organizacional atualizado com sucesso.');
      } else {
        await organizationService.createOrganizationUnitType(form);
        setMessage('Tipo organizacional criado com sucesso.');
      }
      setTypeModal({ open: false, mode: 'create', item: null });
      refreshData();
    } catch (requestError) {
      setError(getApiError(requestError, 'Nao foi possivel salvar o tipo organizacional.'));
    } finally {
      setSaving(false);
    }
  }

  async function handleUnitSubmit(form) {
    if (!form.typeId || !form.code || !form.name) {
      setError('Informe tipo, codigo e nome da unidade organizacional.');
      return;
    }

    try {
      setSaving(true);
      setError('');
      setMessage('');
      if (unitModal.mode === 'edit' && unitModal.item) {
        await organizationService.updateOrganizationUnit(unitModal.item.id, form);
        setMessage('Unidade organizacional atualizada com sucesso.');
      } else {
        await organizationService.createOrganizationUnit(form);
        setMessage('Unidade organizacional criada com sucesso.');
      }
      setUnitModal({ open: false, mode: 'create', item: null });
      refreshData();
    } catch (requestError) {
      setError(getApiError(requestError, 'Nao foi possivel salvar a unidade organizacional.'));
    } finally {
      setSaving(false);
    }
  }

  async function confirmDelete() {
    if (!deleteTarget) {
      return;
    }

    try {
      setSaving(true);
      setError('');
      setMessage('');
      if (deleteTarget.kind === 'type') {
        await organizationService.deleteOrganizationUnitType(deleteTarget.item.id);
        setMessage('Tipo organizacional removido com sucesso.');
      } else {
        await organizationService.deleteOrganizationUnit(deleteTarget.item.id);
        setMessage('Unidade organizacional removida com sucesso.');
      }
      setDeleteTarget(null);
      refreshData();
    } catch (requestError) {
      setError(getApiError(requestError, 'Nao foi possivel remover o registro.'));
    } finally {
      setSaving(false);
    }
  }

  const typeColumns = [
    {
      key: 'type',
      header: 'Tipo',
      render: (row) => (
        <div className="bp-organization-cell">
          <span className="bp-organization-cell__icon" aria-hidden="true">
            <Tags size={16} />
          </span>
          <div>
            <strong>{row.code}</strong>
            <span>{row.name}</span>
          </div>
        </div>
      ),
    },
    {
      key: 'status',
      header: 'Status',
      render: (row) => <Badge variant={row.active ? 'success' : 'warning'}>{row.active ? 'Ativo' : 'Inativo'}</Badge>,
    },
    {
      key: 'actions',
      header: 'Acoes',
      render: (row) => (
        <div className="bp-action-group">
          {canEdit ? <ActionIconButton icon={Pencil} label="Editar tipo" title="Editar tipo" onClick={() => openTypeEdit(row)} /> : null}
          {canDelete ? <ActionIconButton icon={Trash2} label="Remover tipo" title="Remover tipo" variant="danger" onClick={() => setDeleteTarget({ kind: 'type', item: row })} /> : null}
        </div>
      ),
    },
  ];

  const unitColumns = [
    {
      key: 'unit',
      header: 'Unidade',
      render: (row) => (
        <div className="bp-organization-cell">
          <span className="bp-organization-cell__icon" aria-hidden="true">
            <GitBranch size={16} />
          </span>
          <div>
            <strong>{row.code}</strong>
            <span>{row.name}</span>
          </div>
        </div>
      ),
    },
    {
      key: 'type',
      header: 'Tipo',
      render: (row) => <Badge variant="secondary">{row.type?.code ?? 'Sem tipo'}</Badge>,
    },
    {
      key: 'parent',
      header: 'Superior',
      render: (row) => row.parent ? `${row.parent.code} - ${row.parent.name}` : 'Sem superior',
    },
    {
      key: 'status',
      header: 'Status',
      render: (row) => <Badge variant={row.active ? 'success' : 'warning'}>{row.active ? 'Ativo' : 'Inativo'}</Badge>,
    },
    {
      key: 'actions',
      header: 'Acoes',
      render: (row) => (
        <div className="bp-action-group">
          {canEdit ? <ActionIconButton icon={Pencil} label="Editar unidade" title="Editar unidade" onClick={() => openUnitEdit(row)} /> : null}
          {canDelete ? <ActionIconButton icon={Trash2} label="Remover unidade" title="Remover unidade" variant="danger" onClick={() => setDeleteTarget({ kind: 'unit', item: row })} /> : null}
        </div>
      ),
    },
  ];

  return (
    <div className="bp-organization-page bp-list-page">
      <section className="bp-list-page__header">
        <div>
          <h1>Estrutura organizacional</h1>
          <p>Cadastre tipos e unidades usados pelos perfis organizacionais.</p>
        </div>
      </section>

      {message ? <Alert variant="success">{message}</Alert> : null}
      {error ? <Alert variant="error">{error}</Alert> : null}

      <div className="bp-organization-grid">
        <Card>
          <Card.Body>
            <section className="bp-organization-section">
              <header className="bp-organization-section__header">
                <div>
                  <h2>Tipos</h2>
                  <p>Exemplos: empresa, filial, equipe ou centro de custo.</p>
                </div>
                {canCreate ? <Button onClick={openTypeCreate}>Novo tipo</Button> : null}
              </header>
              <Input id="organization-type-search" label="Buscar tipos" placeholder="Buscar codigo ou nome..." value={typeSearch} onChange={(event) => setTypeSearch(event.target.value)} />
              {loading ? (
                <Loading label="Carregando tipos..." />
              ) : filteredTypes.length ? (
                <Table columns={typeColumns} rows={filteredTypes} />
              ) : (
                <EmptyState title="Nenhum tipo encontrado" description="Cadastre o primeiro tipo organizacional para liberar unidades." />
              )}
            </section>
          </Card.Body>
        </Card>

        <Card>
          <Card.Body>
            <section className="bp-organization-section">
              <header className="bp-organization-section__header">
                <div>
                  <h2>Unidades</h2>
                  <p>Registros reais que poderao ser vinculados a perfis organizacionais.</p>
                </div>
                {canCreate ? <Button disabled={!types.length} onClick={openUnitCreate}>Nova unidade</Button> : null}
              </header>
              <div className="bp-organization-toolbar">
                <Input id="organization-unit-search" label="Buscar unidades" placeholder="Buscar codigo, nome ou tipo..." value={unitSearch} onChange={(event) => setUnitSearch(event.target.value)} />
                <Select
                  id="organization-unit-type-filter"
                  label="Filtrar por tipo"
                  options={types.map((type) => ({ value: String(type.id), label: `${type.code} - ${type.name}` }))}
                  placeholder="Todos"
                  value={typeFilter}
                  onChange={(event) => setTypeFilter(event.target.value)}
                />
              </div>
              {loading ? (
                <Loading label="Carregando unidades..." />
              ) : filteredUnits.length ? (
                <Table columns={unitColumns} rows={filteredUnits} />
              ) : (
                <EmptyState title="Nenhuma unidade encontrada" description="Cadastre unidades para que os perfis organizacionais possam usar escopos." />
              )}
            </section>
          </Card.Body>
        </Card>
      </div>

      <OrganizationTypeModal
        isOpen={typeModal.open}
        loading={saving}
        mode={typeModal.mode}
        type={typeModal.item}
        onClose={() => setTypeModal({ open: false, mode: 'create', item: null })}
        onSubmit={handleTypeSubmit}
      />

      <OrganizationUnitModal
        isOpen={unitModal.open}
        loading={saving}
        mode={unitModal.mode}
        types={types}
        unit={unitModal.item}
        units={units}
        onClose={() => setUnitModal({ open: false, mode: 'create', item: null })}
        onSubmit={handleUnitSubmit}
      />

      <ConfirmDialog
        cancelLabel="Cancelar"
        confirmLabel="Remover"
        message="Esta acao vai remover o registro selecionado quando ele nao possuir dependencias."
        onCancel={() => setDeleteTarget(null)}
        onConfirm={confirmDelete}
        open={Boolean(deleteTarget)}
        title="Confirmar remocao"
      />
    </div>
  );
}
