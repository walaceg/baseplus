package com.baseplus.core.bootstrap;

public final class AdminAccessDefaults {

    public static final String ADMIN_ROLE = "ADMIN";
    public static final String ADMIN_ROLE_DESCRIPTION = "Administrador do sistema.";

    public static final String[][] PERMISSIONS = {
            {"ADMIN_ACCESS", "Acesso administrativo inicial."},
            {"DASHBOARD_VIEW", "Visualizar dashboard."},
            {"USERS_VIEW", "Visualizar usuarios."},
            {"USERS_CREATE", "Criar usuarios."},
            {"USERS_EDIT", "Editar usuarios."},
            {"USERS_DELETE", "Remover usuarios."},
            {"USERS_RESET_PASSWORD", "Redefinir senha de usuarios."},
            {"ROLES_VIEW", "Visualizar perfis."},
            {"ROLES_CREATE", "Criar perfis."},
            {"ROLES_EDIT", "Editar perfis."},
            {"ROLES_DELETE", "Remover perfis."},
            {"ROLES_MANAGE_PERMISSIONS", "Gerenciar permissoes de perfis."},
            {"ROLES_MANAGE_USERS", "Gerenciar usuarios vinculados a perfis."},
            {"ROLES_MANAGE_ORGANIZATION_SCOPES", "Gerenciar escopos organizacionais de perfis."},
            {"PERMISSIONS_VIEW", "Visualizar permissoes."},
            {"PERMISSIONS_CREATE", "Criar permissoes."},
            {"PERMISSIONS_EDIT", "Editar permissoes."},
            {"PERMISSIONS_DELETE", "Remover permissoes."},
            {"ORGANIZATION_UNITS_VIEW", "Visualizar estrutura organizacional."},
            {"ORGANIZATION_UNITS_CREATE", "Criar estrutura organizacional."},
            {"ORGANIZATION_UNITS_EDIT", "Editar estrutura organizacional."},
            {"ORGANIZATION_UNITS_DELETE", "Excluir estrutura organizacional."},
            {"BRANDING_VIEW", "Visualizar branding."},
            {"BRANDING_EDIT", "Editar branding."},
            {"BRANDING_UPLOAD_ASSETS", "Enviar assets de branding."},
            {"AUDIT_VIEW", "Visualizar auditoria."},
            {"AUDIT_EXPORT", "Exportar auditoria."}
    };

    private AdminAccessDefaults() {
    }
}
