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
} from '../../shared/components/index.js';
import { Pencil, Trash2 } from 'lucide-react';
import { useAuthorization } from '../../core/auth/useAuthorization.js';
import { PERMISSIONS } from '../../shared/auth/permissions.js';
import { useDebouncedValue } from '../../shared/hooks/useDebouncedValue.js';
import * as usuarioService from './usuarioService.js';
import './usuario.css';

const PROTECTED_USER_EMAIL = 'admin@baseplus.com';
const DEFAULT_PAGE_SIZE = 10;

function toBooleanParam(value) {
  return value === 'true' ? true : undefined;
}

function formatDateTime(value) {
  if (!value) {
    return 'Nunca';
  }

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value));
}

export function UsuariosPage() {
  const { hasPermission } = useAuthorization();
  const canCreate = hasPermission(PERMISSIONS.USERS_CREATE);
  const canEdit = hasPermission(PERMISSIONS.USERS_EDIT);
  const canDelete = hasPermission(PERMISSIONS.USERS_DELETE);
  const location = useLocation();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const searchValue = searchParams.get('search') ?? '';
  const ativoValue = searchParams.get('ativo') ?? '';
  const bloqueadoValue = searchParams.get('bloqueado') ?? '';
  const primeiroAcessoValue = searchParams.get('primeiroAcesso') ?? '';
  const pageValue = Number(searchParams.get('page') ?? '0');
  const sizeValue = Number(searchParams.get('size') ?? String(DEFAULT_PAGE_SIZE));
  const [searchInput, setSearchInput] = useState(searchValue);
  const debouncedSearch = useDebouncedValue(searchInput, 300);
  const [reloadToken, setReloadToken] = useState(0);
  const [pageData, setPageData] = useState({
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

    async function loadUsuarios() {
      try {
        setLoading(true);
        const data = await usuarioService.getUsuarios({
          search: searchValue || undefined,
          ativo: toBooleanParam(ativoValue),
          bloqueado: toBooleanParam(bloqueadoValue),
          primeiroAcesso: toBooleanParam(primeiroAcessoValue),
          page: Number.isFinite(pageValue) ? pageValue : 0,
          size: Number.isFinite(sizeValue) ? sizeValue : DEFAULT_PAGE_SIZE,
        });

        if (active) {
          setPageData({
            content: data.content ?? [],
            page: data.page ?? 0,
            size: data.size ?? DEFAULT_PAGE_SIZE,
            totalElements: data.totalElements ?? 0,
            totalPages: data.totalPages ?? 0,
          });
        }
      } catch (requestError) {
        if (active) {
          setError(requestError.response?.data?.message ?? 'Não foi possível carregar os usuários.');
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadUsuarios();

    return () => {
      active = false;
    };
  }, [ativoValue, bloqueadoValue, pageValue, primeiroAcessoValue, reloadToken, searchValue, sizeValue]);

  function refreshUsuarios() {
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
      next.delete('bloqueado');
      next.delete('primeiroAcesso');
      next.set('page', '0');
      next.set('size', String(sizeValue || DEFAULT_PAGE_SIZE));
    });
  }

  function toggleBooleanFilter(name, checked) {
    updateSearchParams((next) => {
      if (checked) {
        next.set(name, 'true');
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

  function isProtectedUser(row) {
    return row?.email?.toLowerCase() === PROTECTED_USER_EMAIL;
  }

  function handleDelete(userId) {
    setDeleteTarget(userId);
  }

  async function confirmDelete() {
    if (!deleteTarget) {
      return;
    }

    setError('');
    setMessage('');

    try {
      await usuarioService.deleteUsuario(deleteTarget);
      setMessage('Usuário removido com sucesso.');
      refreshUsuarios();
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Não foi possível remover o usuário.');
    } finally {
      setDeleteTarget(null);
    }
  }

  const columns = [
    {
      key: 'usuario',
      header: 'Usuário',
      render: (row) => (
        <div className="bp-usuario-cell">
          <Avatar alt={row.nome} name={row.nome} size="sm" src={row.avatarUrl} version={row.avatarVersion} />
          <div>
            <strong>{row.nomeExibicao || row.nome}</strong>
            <span>{row.email}</span>
          </div>
        </div>
      ),
    },
    {
      key: 'corporativo',
      header: 'Dados corporativos',
      render: (row) =>
        row.cargo || row.departamento ? (
          <div className="bp-usuario-corporate">
            {row.cargo ? <strong>{row.cargo}</strong> : null}
            {row.departamento ? <span>{row.departamento}</span> : null}
          </div>
        ) : (
          <span className="bp-usuario-empty">Não informado</span>
        ),
    },
    {
      key: 'roles',
      header: 'Perfis',
      render: (row) =>
        row.roles?.length ? (
          <div className="bp-usuario-roles">
            {row.roles.map((role) => (
              <Badge key={role}>{role}</Badge>
            ))}
          </div>
        ) : (
          <span className="bp-usuario-empty">Sem perfis</span>
        ),
    },
    {
      key: 'ativo',
      header: 'Status',
      render: (row) => (
        <div className="bp-usuario-badges">
          <Badge variant={row.ativo ? 'success' : 'warning'}>{row.ativo ? 'Ativo' : 'Inativo'}</Badge>
          {row.bloqueado ? <Badge variant="danger">Bloqueado</Badge> : null}
          {row.trocarSenhaPrimeiroAcesso ? <Badge variant="primary">Primeiro acesso</Badge> : null}
        </div>
      ),
    },
    {
      key: 'ultimoLoginEm',
      header: 'Último login',
      render: (row) => <span className="bp-usuario-meta">{formatDateTime(row.ultimoLoginEm)}</span>,
    },
    {
      key: 'tentativasLoginInvalidas',
      header: 'Tentativas',
      render: (row) => <Badge variant={row.tentativasLoginInvalidas > 0 ? 'warning' : 'secondary'}>{row.tentativasLoginInvalidas ?? 0}</Badge>,
    },
    {
      key: 'acoes',
      header: 'Ações',
      render: (row) => (
        <div className="bp-usuario-actions bp-action-group">
          {canEdit ? (
            <ActionIconButton
              icon={Pencil}
              label="Editar"
              title="Editar"
              onClick={() => navigate(`/app/usuarios/${row.id}/editar`)}
            />
          ) : null}
          {canDelete ? (
            <ActionIconButton
              disabled={isProtectedUser(row)}
              icon={Trash2}
              label="Remover"
              variant="danger"
              title={isProtectedUser(row) ? 'Registro protegido pelo sistema.' : 'Remover'}
              onClick={() => handleDelete(row.id)}
            />
          ) : null}
        </div>
      ),
    },
  ];

  const hasFilters = Boolean(searchValue || ativoValue || bloqueadoValue || primeiroAcessoValue);

  return (
    <div className="bp-usuarios-page bp-list-page">
      <section className="bp-list-page__header">
        <div>
          <h1>Usuários</h1>
          <p>Gestão inicial de contas administrativas da Base+.</p>
        </div>
        {canCreate ? <Button onClick={() => navigate('/app/usuarios/novo')}>Novo usuário</Button> : null}
      </section>

      <Card>
        <Card.Body>
          <div className="bp-list-page__toolbar">
            <div className="bp-list-page__toolbar-row">
              <div className="bp-list-page__search">
                <Input
                  id="usuarios-search"
                  label="Buscar usuários"
                  placeholder="Buscar usuários..."
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
            <div className="bp-list-page__filters bp-usuario-filters">
              <label className="bp-usuario-filter-option">
                <input
                  checked={ativoValue === 'true'}
                  type="checkbox"
                  onChange={(event) => toggleBooleanFilter('ativo', event.target.checked)}
                />
                <span>Ativos</span>
              </label>
              <label className="bp-usuario-filter-option">
                <input
                  checked={bloqueadoValue === 'true'}
                  type="checkbox"
                  onChange={(event) => toggleBooleanFilter('bloqueado', event.target.checked)}
                />
                <span>Bloqueados</span>
              </label>
              <label className="bp-usuario-filter-option">
                <input
                  checked={primeiroAcessoValue === 'true'}
                  type="checkbox"
                  onChange={(event) => toggleBooleanFilter('primeiroAcesso', event.target.checked)}
                />
                <span>Primeiro acesso</span>
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
            <Loading label="Carregando usuários..." />
          ) : pageData.content.length ? (
            <>
              <Table columns={columns} rows={pageData.content} />
              <Pagination
                page={pageData.page}
                size={pageData.size}
                totalElements={pageData.totalElements}
                totalPages={pageData.totalPages}
                onChangePage={changePage}
              />
            </>
          ) : (
            <EmptyState
              description={
                hasFilters
                  ? 'Nenhum usuário corresponde aos filtros atuais. Limpe a busca para ampliar os resultados.'
                  : 'Ainda não existem usuários cadastrados.'
              }
              title="Nenhum usuário encontrado"
            />
          )}
        </Card.Body>
      </Card>

      <ConfirmDialog
        cancelLabel="Cancelar"
        confirmLabel="Remover"
        message="Esta ação vai remover o usuário selecionado."
        onCancel={() => setDeleteTarget(null)}
        onConfirm={confirmDelete}
        open={Boolean(deleteTarget)}
        title="Confirmar remoção"
      />
    </div>
  );
}
