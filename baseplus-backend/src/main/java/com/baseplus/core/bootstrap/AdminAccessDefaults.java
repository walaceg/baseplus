package com.baseplus.core.bootstrap;

public final class AdminAccessDefaults {

    public static final String ADMIN_ROLE = "ADMIN";
    public static final String ADMIN_ROLE_DESCRIPTION = "Administrador do sistema.";

    public static final String[][] PERMISSIONS = {
            {"ADMIN_ACCESS", "Acesso administrativo inicial."},
            {"DASHBOARD_VIEW", "Visualizar dashboard."},
            {"USERS_VIEW", "Visualizar usuários."},
            {"USERS_CREATE", "Criar usuários."},
            {"USERS_EDIT", "Editar usuários."},
            {"USERS_DELETE", "Remover usuários."},
            {"USERS_RESET_PASSWORD", "Redefinir senha de usuários."},
            {"ROLES_VIEW", "Visualizar perfis."},
            {"ROLES_CREATE", "Criar perfis."},
            {"ROLES_EDIT", "Editar perfis."},
            {"ROLES_DELETE", "Remover perfis."},
            {"ROLES_MANAGE_PERMISSIONS", "Gerenciar permissões de perfis."},
            {"ROLES_MANAGE_USERS", "Gerenciar usuários vinculados a perfis."},
            {"ROLES_MANAGE_ORGANIZATION_SCOPES", "Gerenciar escopos organizacionais de perfis."},
            {"PERMISSIONS_VIEW", "Visualizar permissões."},
            {"PERMISSIONS_CREATE", "Criar permissões."},
            {"PERMISSIONS_EDIT", "Editar permissões."},
            {"PERMISSIONS_DELETE", "Remover permissões."},
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
