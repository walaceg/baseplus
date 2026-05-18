alter table roles add column if not exists ativo boolean not null default true;
alter table roles add column if not exists sistema boolean not null default false;
alter table roles add column if not exists criado_em timestamp with time zone not null default current_timestamp;
alter table roles add column if not exists atualizado_em timestamp with time zone not null default current_timestamp;

update roles set sistema = true where upper(name) = 'ADMIN';
