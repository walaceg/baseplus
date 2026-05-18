insert into permissions (name, description)
select 'BRANDING_VIEW', 'Visualizar branding.'
where not exists (select 1 from permissions where name = 'BRANDING_VIEW');

insert into permissions (name, description)
select 'BRANDING_EDIT', 'Editar branding.'
where not exists (select 1 from permissions where name = 'BRANDING_EDIT');

insert into permissions (name, description)
select 'BRANDING_UPLOAD_ASSETS', 'Enviar assets de branding.'
where not exists (select 1 from permissions where name = 'BRANDING_UPLOAD_ASSETS');

insert into role_permissions (role_id, permission_id)
select r.id, p.id
from roles r
cross join permissions p
where r.name = 'ADMIN'
  and p.name in ('BRANDING_VIEW', 'BRANDING_EDIT', 'BRANDING_UPLOAD_ASSETS')
  and not exists (
      select 1
      from role_permissions rp
      where rp.role_id = r.id
        and rp.permission_id = p.id
  );
