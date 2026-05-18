alter table usuarios add column if not exists bloqueado boolean not null default false;
alter table usuarios add column if not exists trocar_senha_primeiro_acesso boolean not null default false;
alter table usuarios add column if not exists ultimo_login_em timestamp;
alter table usuarios add column if not exists tentativas_login_invalidas integer not null default 0;
