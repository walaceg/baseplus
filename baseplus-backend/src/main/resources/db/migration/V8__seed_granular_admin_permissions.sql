insert into permissions (name, description)
select 'ADMIN_ACCESS', 'Acesso administrativo inicial.'
where not exists (select 1 from permissions where name = 'ADMIN_ACCESS');

insert into permissions (name, description)
select 'DASHBOARD_VIEW', 'Visualizar dashboard.'
where not exists (select 1 from permissions where name = 'DASHBOARD_VIEW');

insert into permissions (name, description)
select 'USERS_VIEW', 'Visualizar usuarios.'
where not exists (select 1 from permissions where name = 'USERS_VIEW');

insert into permissions (name, description)
select 'USERS_CREATE', 'Criar usuarios.'
where not exists (select 1 from permissions where name = 'USERS_CREATE');

insert into permissions (name, description)
select 'USERS_EDIT', 'Editar usuarios.'
where not exists (select 1 from permissions where name = 'USERS_EDIT');

insert into permissions (name, description)
select 'USERS_DELETE', 'Remover usuarios.'
where not exists (select 1 from permissions where name = 'USERS_DELETE');

insert into permissions (name, description)
select 'USERS_RESET_PASSWORD', 'Redefinir senha de usuarios.'
where not exists (select 1 from permissions where name = 'USERS_RESET_PASSWORD');

insert into permissions (name, description)
select 'ROLES_VIEW', 'Visualizar perfis.'
where not exists (select 1 from permissions where name = 'ROLES_VIEW');

insert into permissions (name, description)
select 'ROLES_CREATE', 'Criar perfis.'
where not exists (select 1 from permissions where name = 'ROLES_CREATE');

insert into permissions (name, description)
select 'ROLES_EDIT', 'Editar perfis.'
where not exists (select 1 from permissions where name = 'ROLES_EDIT');

insert into permissions (name, description)
select 'ROLES_DELETE', 'Remover perfis.'
where not exists (select 1 from permissions where name = 'ROLES_DELETE');

insert into permissions (name, description)
select 'ROLES_MANAGE_PERMISSIONS', 'Gerenciar permissoes de perfis.'
where not exists (select 1 from permissions where name = 'ROLES_MANAGE_PERMISSIONS');

insert into permissions (name, description)
select 'PERMISSIONS_VIEW', 'Visualizar permissoes.'
where not exists (select 1 from permissions where name = 'PERMISSIONS_VIEW');

insert into permissions (name, description)
select 'PERMISSIONS_CREATE', 'Criar permissoes.'
where not exists (select 1 from permissions where name = 'PERMISSIONS_CREATE');

insert into permissions (name, description)
select 'PERMISSIONS_EDIT', 'Editar permissoes.'
where not exists (select 1 from permissions where name = 'PERMISSIONS_EDIT');

insert into permissions (name, description)
select 'PERMISSIONS_DELETE', 'Remover permissoes.'
where not exists (select 1 from permissions where name = 'PERMISSIONS_DELETE');

insert into permissions (name, description)
select 'BRANDING_VIEW', 'Visualizar branding.'
where not exists (select 1 from permissions where name = 'BRANDING_VIEW');

insert into permissions (name, description)
select 'BRANDING_EDIT', 'Editar branding.'
where not exists (select 1 from permissions where name = 'BRANDING_EDIT');

insert into permissions (name, description)
select 'BRANDING_UPLOAD_ASSETS', 'Enviar assets de branding.'
where not exists (select 1 from permissions where name = 'BRANDING_UPLOAD_ASSETS');

insert into permissions (name, description)
select 'AUDIT_VIEW', 'Visualizar auditoria.'
where not exists (select 1 from permissions where name = 'AUDIT_VIEW');

insert into permissions (name, description)
select 'AUDIT_EXPORT', 'Exportar auditoria.'
where not exists (select 1 from permissions where name = 'AUDIT_EXPORT');

update roles set ativo = true, sistema = true where name = 'ADMIN';

insert into role_permissions (role_id, permission_id)
select r.id, p.id
from roles r
cross join permissions p
where r.name = 'ADMIN'
  and p.name in (
      'ADMIN_ACCESS',
      'DASHBOARD_VIEW',
      'USERS_VIEW',
      'USERS_CREATE',
      'USERS_EDIT',
      'USERS_DELETE',
      'USERS_RESET_PASSWORD',
      'ROLES_VIEW',
      'ROLES_CREATE',
      'ROLES_EDIT',
      'ROLES_DELETE',
      'ROLES_MANAGE_PERMISSIONS',
      'PERMISSIONS_VIEW',
      'PERMISSIONS_CREATE',
      'PERMISSIONS_EDIT',
      'PERMISSIONS_DELETE',
      'BRANDING_VIEW',
      'BRANDING_EDIT',
      'BRANDING_UPLOAD_ASSETS',
      'AUDIT_VIEW',
      'AUDIT_EXPORT'
  )
  and not exists (
      select 1
      from role_permissions rp
      where rp.role_id = r.id
        and rp.permission_id = p.id
  );
