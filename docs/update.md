# Atualizacao de ambientes - Base+

Guia oficial para atualizar instalacoes existentes da Base+ em homologacao e producao sem reinstalar a plataforma e sem perder banco, uploads, usuarios, auditoria ou branding.

## Instalacao inicial x atualizacao

A instalacao inicial cria a infraestrutura pela primeira vez:

- banco PostgreSQL vazio;
- volume de uploads vazio;
- variaveis de ambiente configuradas;
- migrations Flyway aplicadas pela primeira execucao;
- bootstrap administrativo executado uma unica vez para criar o primeiro administrador.

A atualizacao parte de um ambiente ja existente:

- banco PostgreSQL ja contem dados;
- volume de uploads ja contem arquivos reais;
- usuarios, perfis, auditoria, branding e configuracoes precisam ser preservados;
- Flyway aplica apenas migrations novas;
- bootstrap administrativo nao deve ser executado.

Nao use `docker compose down --volumes`, recriacao manual de banco, limpeza de uploads ou novos seeds para atualizar ambientes existentes.

## Bootstrap administrativo

O bootstrap administrativo e exclusivo para banco vazio, antes da operacao normal do ambiente.

Em HOM ou PRD ja operacionais:

- nao execute o bootstrap durante atualizacoes;
- nao altere variaveis `BASEPLUS_BOOTSTRAP_ADMIN_*`;
- nao crie novo administrador por seed, migration ou insert SQL;
- mantenha usuarios, perfis e auditoria existentes.

Se a execucao do bootstrap for tentada em ambiente que ja possui administrador, a aplicacao deve recusar a operacao.

## Fluxo DEV -> HOM -> PRD

O fluxo recomendado para evolucoes da Base+ e:

1. Desenvolver e validar em `dev`, com H2 local.
2. Rodar testes automatizados e build do frontend.
3. Validar compatibilidade com PostgreSQL.
4. Publicar tag da versao candidata.
5. Atualizar HOM pela tag publicada.
6. Validar smoke tests, login, uploads, branding, auditoria, health e readiness.
7. Fazer backup de PRD.
8. Atualizar PRD pela mesma tag validada em HOM.
9. Monitorar logs e endpoints de saude apos a atualizacao.

HOM deve usar o mesmo modelo operacional de PRD sempre que possivel: PostgreSQL persistente, uploads persistentes, Flyway ativo e Hibernate `validate`.

## Checklist antes da atualizacao

Antes de atualizar HOM ou PRD, confirme:

- tag de destino definida e validada;
- `CHANGELOG.md` revisado para a versao;
- backup recente do PostgreSQL;
- backup recente do volume/diretorio de uploads;
- restore testado ou procedimento de restore conhecido;
- variaveis obrigatorias conferidas;
- `JWT_SECRET` preservado;
- `DB_URL`, `DB_USER`, `DB_PASSWORD` preservados;
- `UPLOAD_DIR` ou volume de uploads preservado;
- nenhuma alteracao pendente nao versionada em producao;
- janela de manutencao aprovada quando necessario;
- plano de rollback conhecido.

## Backup antes da atualizacao

Em ambientes Docker Compose, execute os backups a partir de `baseplus-backend/`.

Backup logico do PostgreSQL:

```powershell
docker compose exec -T postgres pg_dump -U baseplus -d baseplus -Fc > C:\backups\baseplus\baseplus-before-update.dump
```

Backup do volume de uploads:

```powershell
docker run --rm -v baseplus_baseplus_uploads:/data -v C:/backups/baseplus:/backup alpine sh -c "cd /data && tar czf /backup/uploads-before-update.tgz ."
```

Se o ambiente usa nomes diferentes em `DB_USER`, `DB_NAME`, volume ou diretorio de uploads, ajuste os comandos mantendo os mesmos dados reais do ambiente.

Em producao sem Compose, use o `pg_dump` do PostgreSQL e compacte o diretorio configurado em `UPLOAD_DIR` pelo mecanismo operacional do servidor.

## Atualizacao por Git/tag

Use este modelo quando o ambiente executa a aplicacao a partir do codigo fonte ou artefatos gerados localmente.

1. Entrar no diretorio do projeto:

```powershell
cd C:\dev\baseplus
```

2. Conferir o estado atual:

```powershell
git status --short
```

3. Buscar tags remotas:

```powershell
git fetch --tags
```

4. Trocar para a tag de destino:

```powershell
git checkout v1.1.0
```

5. Recompilar backend e frontend conforme o modelo de deploy do ambiente:

```powershell
cd C:\dev\baseplus\baseplus-backend
mvn clean package

cd C:\dev\baseplus\baseplus-frontend
npm install
npm run build
```

6. Reiniciar os servicos da aplicacao.

Na subida do backend, o Flyway executa automaticamente migrations novas contra o banco existente. O Hibernate deve validar o schema em profiles persistentes, sem recriar tabelas.

## Atualizacao por Docker Compose

Use este modelo para ambientes que executam a stack integrada da Base+ com Docker Compose.

1. Entrar no diretorio do Compose oficial:

```powershell
cd C:\dev\baseplus\baseplus-backend
```

2. Conferir o `.env` local e preservar segredos e variaveis existentes.

3. Atualizar o codigo ou tag do repositorio para a versao desejada.

4. Validar a configuracao resolvida:

```powershell
docker compose config
```

5. Construir imagens e subir a stack sem remover volumes:

```powershell
docker compose up --detach --build --wait
```

6. Conferir os servicos:

```powershell
docker compose ps
```

O PostgreSQL nao deve publicar porta no host no Compose integrado oficial. Os dados permanecem no volume persistente do PostgreSQL e os arquivos permanecem no volume persistente de uploads.

Nao use:

```powershell
docker compose down --volumes
```

durante atualizacoes, pois isso remove os volumes locais e pode apagar banco e uploads.

## Preservacao de dados

Durante a atualizacao, devem ser preservados:

- banco PostgreSQL;
- volume ou diretorio de uploads;
- usuarios;
- roles/perfis;
- permissoes;
- perfis organizacionais;
- estrutura organizacional;
- auditoria;
- branding;
- sessoes e refresh tokens, salvo decisao operacional contraria.

A Base+ nao deve depender de reinstalacao para aplicar novas versoes. Evolucoes estruturais do banco devem ser entregues por migrations Flyway versionadas.

## Migrations Flyway

Nos profiles persistentes, Flyway e executado automaticamente na inicializacao do backend.

Comportamento esperado:

- migrations ja aplicadas nao sao reaplicadas;
- migrations novas sao aplicadas em ordem;
- falha de migration deve impedir a subida normal do backend;
- Hibernate `validate` deve confirmar compatibilidade entre entidades e schema.

Nunca edite migrations ja publicadas para corrigir ambiente existente. Crie uma nova migration versionada.

## Validacao apos atualizacao

Apos atualizar HOM ou PRD, valide:

```powershell
curl.exe http://127.0.0.1:5173/api/health
curl.exe http://127.0.0.1:5173/api/health/ready
```

Tambem confira manualmente:

- tela de login carrega;
- login funciona;
- refresh/logout funcionam;
- usuario administrador permanece existente;
- uploads antigos continuam abrindo;
- novo upload funciona;
- branding permanece configurado;
- auditoria registra eventos;
- menus e permissoes continuam corretos.

## Rollback basico

Rollback deve ser planejado antes da atualizacao.

Para rollback simples de aplicacao, volte a tag/imagem anterior e reinicie os servicos:

```powershell
git checkout v1.0.2
```

ou ajuste a tag de imagem usada pelo ambiente e suba novamente:

```powershell
docker compose up --detach --build --wait
```

Se a atualizacao aplicou migrations irreversiveis ou alterou dados, o rollback seguro exige restore do backup feito antes da atualizacao:

- restaurar dump do PostgreSQL;
- restaurar backup de uploads;
- reiniciar backend e frontend;
- validar health, readiness, login, uploads e branding.

Nao tente desfazer migrations manualmente em PRD sem plano validado.

## Pendencias operacionais futuras

Para evolucoes futuras da Base+, recomenda-se:

- documentar estrategia de blue/green ou rolling update;
- criar runbook de rollback por provedor de infraestrutura;
- automatizar backup antes de deploy;
- validar restore periodicamente em ambiente isolado;
- adicionar pipeline CI/CD para build, testes, imagens e publicacao de tags.
