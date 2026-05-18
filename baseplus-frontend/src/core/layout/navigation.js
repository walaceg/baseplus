import {
  Building2,
  GitBranch,
  History,
  LayoutDashboard,
  Palette,
  Shield,
  ShieldCheck,
  Users,
  Settings2,
} from 'lucide-react';
import { PERMISSIONS } from '../../shared/auth/permissions.js';

export const moduleNavigation = {
  title: 'Administracao',
  subtitle: 'Admin',
  icon: Building2,
};

export const navigationGroups = [
  {
    title: 'Geral',
    icon: LayoutDashboard,
    items: [{ icon: LayoutDashboard, label: 'Dashboard', permission: PERMISSIONS.DASHBOARD_VIEW, to: '/app/dashboard' }],
  },
  {
    title: 'Acesso',
    icon: Shield,
    items: [
      { icon: Users, label: 'Usuarios', permission: PERMISSIONS.USERS_VIEW, to: '/app/usuarios' },
      { icon: Shield, label: 'Perfis', permission: PERMISSIONS.ROLES_VIEW, to: '/app/roles' },
      { icon: ShieldCheck, label: 'Permissoes', permission: PERMISSIONS.PERMISSIONS_VIEW, to: '/app/permissions' },
    ],
  },
  {
    title: 'Configuracoes',
    icon: Settings2,
    items: [
      { icon: Palette, label: 'Branding', permission: PERMISSIONS.BRANDING_VIEW, to: '/app/branding' },
      { icon: GitBranch, label: 'Estrutura org.', permissions: [PERMISSIONS.ORGANIZATION_UNITS_VIEW, PERMISSIONS.ROLES_VIEW], to: '/app/organizacao' },
      { icon: History, label: 'Auditoria', permission: PERMISSIONS.AUDIT_VIEW, to: '/app/auditoria' },
    ],
  },
];

export function getAuthorizedNavigationGroups(hasPermission) {
  return navigationGroups
    .map((group) => ({
      ...group,
      items: group.items.filter((item) => {
        if (item.permissions?.length) {
          return item.permissions.some((permission) => hasPermission(permission));
        }
        return !item.permission || hasPermission(item.permission);
      }),
    }))
    .filter((group) => group.items.length > 0);
}
