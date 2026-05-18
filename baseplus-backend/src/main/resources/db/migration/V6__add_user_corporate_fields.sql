alter table usuarios add column if not exists nome_exibicao varchar(120);
alter table usuarios add column if not exists cargo varchar(120);
alter table usuarios add column if not exists departamento varchar(120);
alter table usuarios add column if not exists telefone varchar(40);
alter table usuarios add column if not exists celular varchar(40);
alter table usuarios add column if not exists matricula varchar(60);
alter table usuarios add column if not exists observacoes_internas varchar(1000);
