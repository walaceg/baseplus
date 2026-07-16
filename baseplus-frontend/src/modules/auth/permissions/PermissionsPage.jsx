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
          setError(requestError.response?.data?.message ?? 'Não foi possível carregar as permissões.');
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
      setError(requestError.response?.data?.message ?? 'Não foi possível carregar a permissão.');
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
        setMessage('Permissão atualizada com sucesso.');
      } else {
        await permissionService.createPermission(payload);
        setMessage('Permissão criada com sucesso.');
      }

      setModalOpen(false);
      setSelectedPermission(null);
      refreshPermissions();
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Não foi possível salvar a permissão.');
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
      setMessage('Permissão removida com sucesso.');
      refreshPermissions();
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Não foi possível remover a permissão.');
    } finally {
      setDeleteTarget(null);
    }
  }

  const columns = [
    {
      key: 'name',
      header: 'Permissão',
      render: (row) => (
        <div className="bp-permission-cell">
          <Avatar alt={row.name} name={row.name} size="sm" />
          <div>
            <strong>{row.name}</strong>
            <span>{row.description || 'Sem descrição'}</span>
          </div>
        </div>
      ),
    },
    {
      key: 'actions',
      header: 'Ações',
      render: (row) => (
        <div className="bp-permission-actions bp-action-group">
          {canEdit ? <ActionIconButton icon={Pencil} label="Editar" title="Editar" onClick={() => openEdit(row.id)} /> : null}
          {canDelete ? (
            <ActionIconButton
              disabled={isProtectedPermission(row)}
              icon={Trash2}
              label="Remover"
              variant="danger"
              title={isProtectedPermission(row) ? 'Permissão protegida pelo sistema.' : 'Remover'}
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
          <h1>Permissões</h1>
          <p>Gestão de permissões da Base+.</p>
        </div>
        {canCreate ? <Button onClick={openCreate}>Nova permissão</Button> : null}
      </section>

      <Card>
        <Card.Body>
          <div className="bp-list-page__toolbar">
            <div className="bp-list-page__toolbar-row">
              <div className="bp-list-page__search">
                <Input
                  id="permissions-search"
                  label="Buscar permissões"
                  placeholder="Buscar permissões..."
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
            <Loading label="Carregando permissões..." />
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
                  ? 'Nenhuma permissão corresponde aos filtros atuais. Limpe a busca para ampliar os resultados.'
                  : 'Ainda não existem permissões cadastradas.'
              }
              title="Nenhuma permissão encontrada"
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
        message="Esta ação vai remover a permissão selecionada."
        onCancel={() => setDeleteTarget(null)}
        onConfirm={confirmDelete}
        open={Boolean(deleteTarget)}
        title="Confirmar remoção"
      />
    </div>
  );
}
