import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Avatar, Badge, Button, Card, EmptyState, Loading, Table } from '../../shared/components/index.js';
import { useAuth } from '../../core/auth/useAuth.js';
import { useAuthorization } from '../../core/auth/useAuthorization.js';
import { PERMISSIONS } from '../../shared/auth/permissions.js';
import { getConta, getSessoes } from '../conta/contaService.js';
import { getRoles } from '../auth/roles/roleService.js';
import { getPermissions } from '../auth/permissions/permissionService.js';
import { getUsuarios } from '../usuario/usuarioService.js';
import './dashboard.css';

export function DashboardPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { roles: currentRoles, hasPermission } = useAuthorization();
  const canViewUsers = hasPermission(PERMISSIONS.USERS_VIEW);
  const canViewRoles = hasPermission(PERMISSIONS.ROLES_VIEW);
  const canViewPermissions = hasPermission(PERMISSIONS.PERMISSIONS_VIEW);
  const [state, setState] = useState({
    conta: null,
    usuariosCount: null,
    rolesCount: null,
    permissionsCount: null,
    sessoes: [],
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;

    async function load() {
      try {
        const contaPromise = getConta().catch(() => null);
        const sessoesPromise = getSessoes().catch(() => []);
        const usuariosPromise = canViewUsers ? getUsuarios({ size: 1 }).catch(() => null) : Promise.resolve(null);
        const rolesPromise = canViewRoles ? getRoles({ size: 1 }).catch(() => null) : Promise.resolve(null);
        const permissionsPromise = canViewPermissions ? getPermissions({ size: 1 }).catch(() => null) : Promise.resolve(null);

        const [conta, sessoes, usuarios, roles, permissions] = await Promise.all([
          contaPromise,
          sessoesPromise,
          usuariosPromise,
          rolesPromise,
          permissionsPromise,
        ]);

        if (!active) {
          return;
        }

        setState({
          conta,
          usuariosCount: usuarios?.totalElements ?? usuarios?.content?.length ?? usuarios,
          rolesCount: roles?.totalElements ?? roles?.content?.length ?? roles,
          permissionsCount: permissions?.totalElements ?? permissions?.content?.length ?? permissions,
          sessoes: Array.isArray(sessoes) ? sessoes : [],
        });
      } catch (requestError) {
        if (active) {
          setError(requestError.response?.data?.message ?? 'Não foi possível carregar o dashboard.');
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    load();

    return () => {
      active = false;
    };
  }, [canViewPermissions, canViewRoles, canViewUsers]);

  const metrics = [
    {
      label: 'Usuários',
      value: state.usuariosCount ?? 'Restrito',
      hint: canViewUsers ? 'Total cadastrado no sistema' : 'Sem permissão para visualizar',
    },
    {
      label: 'Perfis',
      value: state.rolesCount ?? 'Restrito',
      hint: canViewRoles ? 'Perfis configurados' : 'Sem permissão para visualizar',
    },
    {
      label: 'Permissões',
      value: state.permissionsCount ?? 'Restrito',
      hint: canViewPermissions ? 'Permissões disponíveis' : 'Sem permissão para visualizar',
    },
    {
      label: 'Sessões ativas',
      value: state.sessoes.length,
      hint: 'Sessões abertas na conta autenticada',
    },
  ];

  const sessionColumns = [
    { key: 'id', header: 'Sessão', render: (row) => `#${row.id}` },
    {
      key: 'criadaEm',
      header: 'Criada em',
      render: (row) => new Date(row.criadaEm).toLocaleString('pt-BR'),
    },
  ];

  if (loading) {
    return (
      <Card>
        <Card.Body>
          <Loading label="Carregando dashboard..." />
        </Card.Body>
      </Card>
    );
  }

  return (
    <div className="bp-dashboard">
      <section className="bp-dashboard__hero">
        <div className="bp-dashboard__hero-main">
          <Badge variant="primary">Base+</Badge>
          <h1>Dashboard</h1>
          <p>Visão inicial da operação administrativa com dados reais quando disponíveis.</p>
        </div>
        <div className="bp-dashboard__hero-user">
          <Avatar alt={user?.nome ?? user?.email ?? 'Usuário'} name={user?.nome ?? user?.email ?? 'Usuário'} size="lg" />
          <div>
            <strong>{user?.nome ?? user?.email ?? 'Usuário'}</strong>
            <span>{user?.email ?? ''}</span>
            <div className="bp-dashboard__chips">
              {currentRoles.length ? currentRoles.map((role) => <Badge key={role}>{role}</Badge>) : <Badge variant="warning">Sem perfil</Badge>}
            </div>
          </div>
        </div>
      </section>

      {error ? <Alert variant="error">{error}</Alert> : null}

      <section className="bp-dashboard__metrics">
        {metrics.map((metric) => (
          <Card key={metric.label} className="bp-dashboard__metric-card">
            <Card.Body>
              <span className="bp-dashboard__metric-label">{metric.label}</span>
              <strong className="bp-dashboard__metric-value">{metric.value}</strong>
              <p className="bp-dashboard__muted">{metric.hint}</p>
            </Card.Body>
          </Card>
        ))}
      </section>

      <section className="bp-dashboard__grid">
        <Card>
          <Card.Body>
            <div className="bp-dashboard__section-head">
              <h2>Usuário autenticado</h2>
              <Badge variant="success">{state.conta ? 'Sincronizado' : 'Local'}</Badge>
            </div>
            <div className="bp-dashboard__profile">
              <strong>{state.conta?.nome ?? user?.nome ?? 'Usuário'}</strong>
              <span>{state.conta?.email ?? user?.email ?? ''}</span>
            </div>
            <div className="bp-dashboard__quick-actions">
              <Button variant="secondary" onClick={() => navigate('/app/conta')}>
                Conta
              </Button>
              {canViewUsers ? (
                <Button variant="secondary" onClick={() => navigate('/app/usuarios')}>
                  Usuários
                </Button>
              ) : null}
              {canViewRoles ? (
                <Button variant="secondary" onClick={() => navigate('/app/roles')}>
                  Perfis
                </Button>
              ) : null}
              {canViewPermissions ? (
                <Button variant="secondary" onClick={() => navigate('/app/permissions')}>
                  Permissões
                </Button>
              ) : null}
            </div>
          </Card.Body>
        </Card>

        <Card>
          <Card.Body>
            <div className="bp-dashboard__section-head">
              <h2>Sessões ativas</h2>
              <Badge variant="primary">{state.sessoes.length}</Badge>
            </div>
            {state.sessoes.length ? (
              <Table columns={sessionColumns} rows={state.sessoes.slice(0, 3)} emptyMessage="Nenhuma sessão encontrada." />
            ) : (
              <EmptyState description="Não há sessões ativas no momento." title="Nenhuma sessão ativa" />
            )}
          </Card.Body>
        </Card>
      </section>
    </div>
  );
}
