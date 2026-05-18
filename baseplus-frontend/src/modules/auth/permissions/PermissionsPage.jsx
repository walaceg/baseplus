import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
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
import { Pencil, Trash2 } from 'lucide-react';
import { useAuthorization } from '../../../core/auth/useAuthorization.js';
import { PERMISSIONS } from '../../../shared/auth/permissions.js';
import { useDebouncedValue } from '../../../shared/hooks/useDebouncedValue.js';
import * as permissionService from './permissionService.js';
import { PermissionFormModal } from './PermissionFormModal.jsx';
import './permissions.css';

const DEFAULT_PAGE_SIZE = 10;

export function PermissionsPage() {
  const { hasPermission } = useAuthorization();
  const canCreate = hasPermission(PERMISSIONS.PERMISSIONS_CREATE);
  const canEdit = hasPermission(PERMISSIONS.PERMISSIONS_EDIT);
  const canDelete = hasPermission(PERMISSIONS.PERMISSIONS_DELETE);
  const [searchParams, setSearchParams] = useSearchParams();
  const searchValue = searchParams.get('search') ?? '';
  const pageValue = Number(searchParams.get('page') ?? '0');
  const sizeValue = Number(searchParams.get('size') ?? String(DEFAULT_PAGE_SIZE));
  const [searchInput, setSearchInput] = useState(searchValue);
  const debouncedSearch = useDebouncedValue(searchInput, 300);
  const [reloadToken, setReloadToken] = useState(0);
  const [permissionsPage, setPermissionsPage] = useState({
    content: [],
    page: 0,
    size: DEFAULT_PAGE_SIZE,
    totalElements: 0,
    totalPages: 0,
  });
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState('create');
  const [selectedPermission, setSelectedPermission] = useState(null);
  const [saving, setSaving] = useState(false);
  const [modalLoading, setModalLoading] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

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

    async function loadPermissions() {
      try {
        setLoading(true);
        const data = await permissionService.getPermissions({
          search: searchValue || undefined,
          page: Number.isFinite(pageValue) ? pageValue : 0,
          size: Number.isFinite(sizeValue) ? sizeValue : DEFAULT_PAGE_SIZE,
        });

        if (active) {
          setPermissionsPage({
            content: data.content ?? [],
            page: data.page ?? 0,
            size: data.size ?? DEFAULT_PAGE_SIZE,
            totalElements: data.totalElements ?? 0,
            totalPages: data.totalPages ?? 0,
          });
        }
      } catch (requestError) {
        if (active) {
          setError(requestError.response?.data?.message ?? 'Nao foi possivel carregar as permissoes.');
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadPermissions();

    return () => {
      active = false;
    };
  }, [pageValue, reloadToken, searchValue, sizeValue]);

  function refreshPermissions() {
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

  function openCreate() {
    setError('');
    setMessage('');
    setSelectedPermission(null);
    setModalMode('create');
    setModalOpen(true);
  }

  async function openEdit(id) {
    setError('');
    setMessage('');
    setModalLoading(true);
    setModalMode('edit');
    setModalOpen(true);

    try {
      const data = await permissionService.getPermission(id);
      setSelectedPermission(data);
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Nao foi possivel carregar a permissao.');
      setModalOpen(false);
    } finally {
      setModalLoading(false);
    }
  }

  async function handleSubmit(form) {
    setSaving(true);
    setError('');
    setMessage('');

    try {
      const payload = {
        name: form.name,
        description: form.description || null,
      };

      if (modalMode === 'edit' && selectedPermission) {
        await permissionService.updatePermission(selectedPermission.id, payload);
        setMessage('Permissao atualizada com sucesso.');
      } else {
        await permissionService.createPermission(payload);
        setMessage('Permissao criada com sucesso.');
      }

      setModalOpen(false);
      setSelectedPermission(null);
      refreshPermissions();
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Nao foi possivel salvar a permissao.');
    } finally {
      setSaving(false);
    }
  }

  function isProtectedPermission(row) {
    return row?.name?.toUpperCase() === 'ADMIN_ACCESS';
  }

  function handleDelete(permissionId) {
    setDeleteTarget(permissionId);
  }

  async function confirmDelete() {
    if (!deleteTarget) {
      return;
    }

    setError('');
    setMessage('');

    try {
      await permissionService.deletePermission(deleteTarget);
      setMessage('Permissao removida com sucesso.');
      refreshPermissions();
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Nao foi possivel remover a permissao.');
    } finally {
      setDeleteTarget(null);
    }
  }

  const columns = [
    {
      key: 'name',
      header: 'Permissao',
      render: (row) => (
        <div className="bp-permission-cell">
          <Avatar alt={row.name} name={row.name} size="sm" />
          <div>
            <strong>{row.name}</strong>
            <span>{row.description || 'Sem descricao'}</span>
          </div>
        </div>
      ),
    },
    {
      key: 'actions',
      header: 'Acoes',
      render: (row) => (
        <div className="bp-permission-actions bp-action-group">
          {canEdit ? <ActionIconButton icon={Pencil} label="Editar" title="Editar" onClick={() => openEdit(row.id)} /> : null}
          {canDelete ? (
            <ActionIconButton
              disabled={isProtectedPermission(row)}
              icon={Trash2}
              label="Remover"
              variant="danger"
              title={isProtectedPermission(row) ? 'Permissao protegida pelo sistema.' : 'Remover'}
              onClick={() => handleDelete(row.id)}
            />
          ) : null}
        </div>
      ),
    },
  ];

  const hasFilters = Boolean(searchValue);

  return (
    <div className="bp-permissions-page bp-list-page">
      <section className="bp-list-page__header">
        <div>
          <h1>Permissoes</h1>
          <p>Gestao de permissoes da Base+.</p>
        </div>
        {canCreate ? <Button onClick={openCreate}>Nova permissao</Button> : null}
      </section>

      <Card>
        <Card.Body>
          <div className="bp-list-page__toolbar">
            <div className="bp-list-page__toolbar-row">
              <div className="bp-list-page__search">
                <Input
                  id="permissions-search"
                  label="Buscar permissoes"
                  placeholder="Buscar permissoes..."
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
          </div>
        </Card.Body>
      </Card>

      {message ? <Alert variant="success">{message}</Alert> : null}
      {error ? <Alert variant="error">{error}</Alert> : null}

      <Card>
        <Card.Body>
          {loading ? (
            <Loading label="Carregando permissoes..." />
          ) : permissionsPage.content.length ? (
            <>
              <Table columns={columns} rows={permissionsPage.content} />
              <Pagination
                page={permissionsPage.page}
                size={permissionsPage.size}
                totalElements={permissionsPage.totalElements}
                totalPages={permissionsPage.totalPages}
                onChangePage={changePage}
              />
            </>
          ) : (
            <EmptyState
              description={
                hasFilters
                  ? 'Nenhuma permissao corresponde aos filtros atuais. Limpe a busca para ampliar os resultados.'
                  : 'Ainda nao existem permissoes cadastradas.'
              }
              title="Nenhuma permissao encontrada"
            />
          )}
        </Card.Body>
      </Card>

      <PermissionFormModal
        isOpen={modalOpen}
        loading={saving || modalLoading}
        mode={modalMode}
        onClose={() => setModalOpen(false)}
        onSubmit={handleSubmit}
        permission={selectedPermission}
      />

      <ConfirmDialog
        cancelLabel="Cancelar"
        confirmLabel="Remover"
        message="Esta acao vai remover a permissao selecionada."
        onCancel={() => setDeleteTarget(null)}
        onConfirm={confirmDelete}
        open={Boolean(deleteTarget)}
        title="Confirmar remocao"
      />
    </div>
  );
}
