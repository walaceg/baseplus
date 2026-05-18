import { useEffect, useState } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import {
  ActionIconButton,
  Alert,
  Avatar,
  Badge,
  Button,
  Card,
  ConfirmDialog,
  EmptyState,
  Input,
  Loading,
  Pagination,
  Table,
} from '../../../shared/components/index.js';
import { Pencil, Power, PowerOff, Trash2 } from 'lucide-react';
import { useAuthorization } from '../../../core/auth/useAuthorization.js';
import { PERMISSIONS } from '../../../shared/auth/permissions.js';
import { useDebouncedValue } from '../../../shared/hooks/useDebouncedValue.js';
import * as roleService from './roleService.js';
import './roles.css';

const DEFAULT_PAGE_SIZE = 10;
const ROLE_TYPE_LABELS = {
  FUNCTIONAL: 'Funcional',
  ORGANIZATIONAL: 'Organizacional',
  SYSTEM: 'Sistema',
};

function toBooleanParam(value) {
  return value === 'true' ? true : undefined;
}

export function RolesPage() {
  const { hasPermission } = useAuthorization();
  const canCreate = hasPermission(PERMISSIONS.ROLES_CREATE);
  const canEdit = hasPermission(PERMISSIONS.ROLES_EDIT);
  const canDelete = hasPermission(PERMISSIONS.ROLES_DELETE);
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();
  const searchValue = searchParams.get('search') ?? '';
  const ativoValue = searchParams.get('ativo') ?? '';
  const sistemaValue = searchParams.get('sistema') ?? '';
  const pageValue = Number(searchParams.get('page') ?? '0');
  const sizeValue = Number(searchParams.get('size') ?? String(DEFAULT_PAGE_SIZE));
  const [searchInput, setSearchInput] = useState(searchValue);
  const debouncedSearch = useDebouncedValue(searchInput, 300);
  const [reloadToken, setReloadToken] = useState(0);
  const [rolesPage, setRolesPage] = useState({
    content: [],
    page: 0,
    size: DEFAULT_PAGE_SIZE,
    totalElements: 0,
    totalPages: 0,
  });
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(location.state?.message ?? '');
  const [error, setError] = useState('');
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [statusTarget, setStatusTarget] = useState(null);

  useEffect(() => {
    if (location.state?.message) {
      navigate(location.pathname + location.search, { replace: true, state: null });
    }
  }, [location.pathname, location.search, location.state, navigate]);

  useEffect(() => {
    setSearchInput(searchValue);
  }, [searchValue]);

  useEffect(() => {
    if (debouncedSearch === searchValue) {
      return;
    }

    const next = new URLSearchParams(searchParams);
    const trimmed = debouncedSearch.trim();

    if (trimmed) {
      next.set('search', trimmed);
    } else {
      next.delete('search');
    }

    next.set('page', '0');
    next.set('size', String(sizeValue || DEFAULT_PAGE_SIZE));
    setSearchParams(next, { replace: true });
  }, [debouncedSearch, searchParams, searchValue, setSearchParams, sizeValue]);

  useEffect(() => {
    let active = true;

    async function loadRoles() {
      try {
        setLoading(true);
        const rolesData = await roleService.getRoles({
          search: searchValue || undefined,
          ativo: toBooleanParam(ativoValue),
          sistema: toBooleanParam(sistemaValue),
          page: Number.isFinite(pageValue) ? pageValue : 0,
          size: Number.isFinite(sizeValue) ? sizeValue : DEFAULT_PAGE_SIZE,
        });

        if (active) {
          setRolesPage({
            content: rolesData.content ?? [],
            page: rolesData.page ?? 0,
            size: rolesData.size ?? DEFAULT_PAGE_SIZE,
            totalElements: rolesData.totalElements ?? 0,
            totalPages: rolesData.totalPages ?? 0,
          });
        }
      } catch (requestError) {
        if (active) {
          setError(requestError.response?.data?.errors?.[0] ?? requestError.response?.data?.message ?? 'Nao foi possivel carregar os perfis.');
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadRoles();

    return () => {
      active = false;
    };
  }, [ativoValue, pageValue, reloadToken, searchValue, sistemaValue, sizeValue]);

  function refreshRoles() {
    setReloadToken((current) => current + 1);
  }

  function updateSearchParams(updater) {
    const next = new URLSearchParams(searchParams);
    updater(next);
    setSearchParams(next, { replace: true });
  }

  function clearFilters() {
    setSearchInput('');
    updateSearchParams((next) => {
      next.delete('search');
      next.delete('ativo');
      next.delete('sistema');
      next.set('page', '0');
      next.set('size', String(sizeValue || DEFAULT_PAGE_SIZE));
    });
  }

  function toggleExclusiveBooleanFilter(name, value, checked) {
    updateSearchParams((next) => {
      if (checked) {
        next.set(name, String(value));
      } else {
        next.delete(name);
      }

      next.set('page', '0');
      next.set('size', String(sizeValue || DEFAULT_PAGE_SIZE));
    });
  }

  function changePage(nextPage) {
    updateSearchParams((next) => {
      next.set('page', String(nextPage));
      next.set('size', String(sizeValue || DEFAULT_PAGE_SIZE));
    });
  }

  async function confirmDelete() {
    if (!deleteTarget) {
      return;
    }

    setError('');
    setMessage('');

    try {
      await roleService.deleteRole(deleteTarget.id);
      setMessage('Perfil removido com sucesso.');
      refreshRoles();
    } catch (requestError) {
      setError(requestError.response?.data?.errors?.[0] ?? requestError.response?.data?.message ?? 'Nao foi possivel remover o perfil.');
    } finally {
      setDeleteTarget(null);
    }
  }

  async function confirmStatusToggle() {
    if (!statusTarget) {
      return;
    }

    setError('');
    setMessage('');

    try {
      const nextStatus = !statusTarget.ativo;
      await roleService.updateRoleStatus(statusTarget.id, nextStatus);
      setMessage(nextStatus ? 'Perfil ativado com sucesso.' : 'Perfil desativado com sucesso.');
      refreshRoles();
    } catch (requestError) {
      setError(requestError.response?.data?.errors?.[0] ?? requestError.response?.data?.message ?? 'Nao foi possivel atualizar o status do perfil.');
    } finally {
      setStatusTarget(null);
    }
  }

  const columns = [
    {
      key: 'name',
      header: 'Perfil',
      render: (row) => (
        <div className="bp-role-cell">
          <Avatar alt={row.name} name={row.name} size="sm" />
          <div>
            <strong>{row.name}</strong>
            <span>{row.description || 'Sem descricao'}</span>
          </div>
        </div>
      ),
    },
    {
      key: 'permissionCount',
      header: 'Alcance',
      render: (row) => (
        <div className="bp-role-badges">
          <Badge variant={row.type === 'ORGANIZATIONAL' ? 'secondary' : 'primary'}>{ROLE_TYPE_LABELS[row.type] ?? 'Funcional'}</Badge>
          {row.type === 'ORGANIZATIONAL' ? (
            <Badge variant="primary">{row.organizationScopes?.length ?? 0} escopos</Badge>
          ) : (
            <Badge variant="primary">{row.permissions?.length ?? 0} permissoes</Badge>
          )}
        </div>
      ),
    },
    {
      key: 'status',
      header: 'Status',
      render: (row) => (
        <div className="bp-role-badges">
          <Badge variant={row.ativo ? 'success' : 'warning'}>{row.ativo ? 'Ativo' : 'Inativo'}</Badge>
          <Badge variant={row.sistema ? 'primary' : 'secondary'}>{row.sistema ? 'Sistema' : 'Customizado'}</Badge>
        </div>
      ),
    },
    {
      key: 'actions',
      header: 'Acoes',
      render: (row) => (
        <div className="bp-role-actions bp-action-group">
          {canEdit ? (
            <ActionIconButton icon={Pencil} label="Editar" title="Editar" onClick={() => navigate(`/app/roles/${row.id}/editar`)} />
          ) : null}
          {canEdit ? (
            <ActionIconButton
              disabled={row.sistema}
              icon={row.ativo ? PowerOff : Power}
              label={row.ativo ? 'Desativar' : 'Ativar'}
              title={row.sistema ? 'Perfil de sistema nao pode ser desativado.' : row.ativo ? 'Desativar' : 'Ativar'}
              variant={row.ativo ? 'danger' : 'primary'}
              onClick={() => setStatusTarget(row)}
            />
          ) : null}
          {canDelete ? (
            <ActionIconButton
              disabled={row.sistema}
              icon={Trash2}
              label="Remover"
              variant="danger"
              title={row.sistema ? 'Perfil de sistema nao pode ser removido.' : 'Remover'}
              onClick={() => setDeleteTarget(row)}
            />
          ) : null}
        </div>
      ),
    },
  ];

  const hasFilters = Boolean(searchValue || ativoValue || sistemaValue);

  return (
    <div className="bp-roles-page bp-list-page">
      <section className="bp-list-page__header">
        <div>
          <h1>Perfis</h1>
          <p>Gestao de perfis, status e permissoes da Base+.</p>
        </div>
        {canCreate ? <Button onClick={() => navigate('/app/roles/novo')}>Novo perfil</Button> : null}
      </section>

      <Card>
        <Card.Body>
          <div className="bp-list-page__toolbar">
            <div className="bp-list-page__toolbar-row">
              <div className="bp-list-page__search">
                <Input
                  id="roles-search"
                  label="Buscar perfis"
                  placeholder="Buscar nome ou descricao..."
                  value={searchInput}
                  onChange={(event) => setSearchInput(event.target.value)}
                />
              </div>
              <div className="bp-list-page__actions">
                <Button disabled={!hasFilters} size="sm" variant="secondary" onClick={clearFilters}>
                  Limpar filtros
                </Button>
              </div>
            </div>
            <div className="bp-list-page__filters bp-role-filters">
              <label className="bp-role-filter-option">
                <input checked={ativoValue === 'true'} type="checkbox" onChange={(event) => toggleExclusiveBooleanFilter('ativo', true, event.target.checked)} />
                <span>Ativos</span>
              </label>
              <label className="bp-role-filter-option">
                <input checked={ativoValue === 'false'} type="checkbox" onChange={(event) => toggleExclusiveBooleanFilter('ativo', false, event.target.checked)} />
                <span>Inativos</span>
              </label>
              <label className="bp-role-filter-option">
                <input checked={sistemaValue === 'true'} type="checkbox" onChange={(event) => toggleExclusiveBooleanFilter('sistema', true, event.target.checked)} />
                <span>Sistema</span>
              </label>
              <label className="bp-role-filter-option">
                <input checked={sistemaValue === 'false'} type="checkbox" onChange={(event) => toggleExclusiveBooleanFilter('sistema', false, event.target.checked)} />
                <span>Customizados</span>
              </label>
            </div>
          </div>
        </Card.Body>
      </Card>

      {message ? <Alert variant="success">{message}</Alert> : null}
      {error ? <Alert variant="error">{error}</Alert> : null}

      <Card>
        <Card.Body>
          {loading ? (
            <Loading label="Carregando perfis..." />
          ) : rolesPage.content.length ? (
            <>
              <Table columns={columns} rows={rolesPage.content} />
              <Pagination
                page={rolesPage.page}
                size={rolesPage.size}
                totalElements={rolesPage.totalElements}
                totalPages={rolesPage.totalPages}
                onChangePage={changePage}
              />
            </>
          ) : (
            <EmptyState
              description={
                hasFilters
                  ? 'Nenhum perfil corresponde aos filtros atuais. Limpe a busca para ampliar os resultados.'
                  : 'Ainda nao existem perfis cadastrados.'
              }
              title="Nenhum perfil encontrado"
            />
          )}
        </Card.Body>
      </Card>

      <ConfirmDialog
        cancelLabel="Cancelar"
        confirmLabel="Remover"
        message="Esta acao vai remover o perfil selecionado."
        onCancel={() => setDeleteTarget(null)}
        onConfirm={confirmDelete}
        open={Boolean(deleteTarget)}
        title="Confirmar remocao"
      />

      <ConfirmDialog
        cancelLabel="Cancelar"
        confirmLabel={statusTarget?.ativo ? 'Desativar' : 'Ativar'}
        message={statusTarget?.ativo ? 'Esta acao vai desativar o perfil selecionado.' : 'Esta acao vai ativar o perfil selecionado.'}
        onCancel={() => setStatusTarget(null)}
        onConfirm={confirmStatusToggle}
        open={Boolean(statusTarget)}
        title={statusTarget?.ativo ? 'Desativar perfil' : 'Ativar perfil'}
      />
    </div>
  );
}
