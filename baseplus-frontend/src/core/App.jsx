import { Navigate, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './routing/ProtectedRoute.jsx';
import { AppLayout } from './layout/AppLayout.jsx';
import { LoginPage } from '../modules/auth/pages/LoginPage.jsx';
import { ChangeInitialPasswordPage } from '../modules/auth/pages/ChangeInitialPasswordPage.jsx';
import { DashboardPage } from '../modules/dashboard/DashboardPage.jsx';
import { BrandingPage } from '../modules/branding/BrandingPage.jsx';
import { AuditPage } from '../modules/audit/AuditPage.jsx';
import { ContaPage } from '../modules/conta/ContaPage.jsx';
import { OrganizationPage } from '../modules/organization/OrganizationPage.jsx';
import { PermissionsPage } from '../modules/auth/permissions/PermissionsPage.jsx';
import { RoleFormPage } from '../modules/auth/roles/RoleFormPage.jsx';
import { RolesPage } from '../modules/auth/roles/RolesPage.jsx';
import { UsuarioFormPage } from '../modules/usuario/UsuarioFormPage.jsx';
import { UsuariosPage } from '../modules/usuario/UsuariosPage.jsx';
import { PERMISSIONS } from '../shared/auth/permissions.js';

export function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/change-initial-password"
        element={
          <ProtectedRoute>
            <ChangeInitialPasswordPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/app"
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/app/dashboard" replace />} />
        <Route path="dashboard" element={<ProtectedRoute permissions={[PERMISSIONS.DASHBOARD_VIEW]}><DashboardPage /></ProtectedRoute>} />
        <Route path="conta" element={<ContaPage />} />
        <Route path="branding" element={<ProtectedRoute permissions={[PERMISSIONS.BRANDING_VIEW]}><BrandingPage /></ProtectedRoute>} />
        <Route path="auditoria" element={<ProtectedRoute permissions={[PERMISSIONS.AUDIT_VIEW]}><AuditPage /></ProtectedRoute>} />
        <Route path="usuarios" element={<ProtectedRoute permissions={[PERMISSIONS.USERS_VIEW]}><UsuariosPage /></ProtectedRoute>} />
        <Route path="usuarios/novo" element={<ProtectedRoute permissions={[PERMISSIONS.USERS_CREATE]}><UsuarioFormPage mode="create" /></ProtectedRoute>} />
        <Route path="usuarios/:id/editar" element={<ProtectedRoute permissions={[PERMISSIONS.USERS_EDIT]}><UsuarioFormPage mode="edit" /></ProtectedRoute>} />
        <Route path="roles" element={<ProtectedRoute permissions={[PERMISSIONS.ROLES_VIEW]}><RolesPage /></ProtectedRoute>} />
        <Route path="roles/novo" element={<ProtectedRoute permissions={[PERMISSIONS.ROLES_CREATE]}><RoleFormPage mode="create" /></ProtectedRoute>} />
        <Route path="roles/:id/editar" element={<ProtectedRoute permissions={[PERMISSIONS.ROLES_EDIT, PERMISSIONS.ROLES_MANAGE_PERMISSIONS, PERMISSIONS.ROLES_MANAGE_USERS, PERMISSIONS.ROLES_MANAGE_ORGANIZATION_SCOPES]}><RoleFormPage mode="edit" /></ProtectedRoute>} />
        <Route path="permissions" element={<ProtectedRoute permissions={[PERMISSIONS.PERMISSIONS_VIEW]}><PermissionsPage /></ProtectedRoute>} />
        <Route path="organizacao" element={<ProtectedRoute permissions={[PERMISSIONS.ORGANIZATION_UNITS_VIEW, PERMISSIONS.ROLES_VIEW]}><OrganizationPage /></ProtectedRoute>} />
      </Route>
    </Routes>
  );
}
