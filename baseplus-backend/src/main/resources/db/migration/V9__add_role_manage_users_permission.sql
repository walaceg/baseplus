insert into permissions (name, description)
select 'ROLES_MANAGE_USERS', 'Gerenciar usuarios vinculados a perfis.'
where not exists (select 1 from permissions where name = 'ROLES_MANAGE_USERS');

insert into role_permissions (role_id, permission_id)
select r.id, p.id
from roles r
join permissions p on p.name = 'ROLES_MANAGE_USERS'
where r.name = 'ADMIN'
  and not exists (
      select 1
      from role_permissions rp
      where rp.role_id = r.id
        and rp.permission_id = p.id
  );
