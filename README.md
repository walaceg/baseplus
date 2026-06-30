# Base+

Versao publicada: `1.1.0`

Estado atual: Baseline Oficial `v1.1.0`.

Base+ e uma plataforma corporativa modular para construcao de aplicacoes administrativas e sistemas de negocio. A proposta e servir como fundacao reutilizavel: autenticacao, autorizacao, perfis, auditoria, branding, estrutura organizacional, padroes de modulo, Docker e documentacao operacional ficam prontos antes da criacao dos modulos especificos de negocio.

## Visao da Plataforma

A visao institucional da Base+ esta documentada em `VISION.md`.

O projeto esta organizado em duas aplicacoes principais:

- `baseplus-backend`: API Java/Spring Boot.
- `baseplus-frontend`: aplicacao React/Vite.

## Stack

Backend:

- Java 17
- Spring Boot 3.3.5
- Spring Security com JWT
- JPA/Hibernate
- Flyway
- PostgreSQL como banco persistente padrao
- H2 apenas para desenvolvimento local
- Testcontainers para validacao de compatibilidade com PostgreSQL

Frontend:

- React 19
- Vite
- React Router
- Axios
- lucide-react
- CSS variables/design tokens

Infraestrutura:

- Dockerfiles multi-stage para backend e frontend.
- Docker Compose integrado com PostgreSQL, backend e frontend.
- Nginx servindo o frontend e fazendo proxy de `/api` e `/uploads`.
- Volumes persistentes para PostgreSQL e uploads.

## Estrutura

```text
baseplus/
|-- README.md
|-- AI_CONTEXT.md
|-- BRAND_GUIDE.md
|-- MODULE_TEMPLATE.md
|-- CHANGELOG.md
|-- ROADMAP.md
|-- TASK_PROMPT.md
|-- state.txt
|-- brand/
|-- docs/
|-- scripts/
|-- baseplus-backend/
`-- baseplus-frontend/
```

## Modulos atuais

- Autenticacao JWT, refresh token e logout.
- Troca de senha inicial.
- Conta do usuario, preferencias, avatar e sessoes.
- Usuarios.
- Perfis/roles.
- Permissoes.
- Estrutura organizacional parametrizavel.
- Perfis funcionais e perfis organizacionais.
- Branding.
- Auditoria.
- Health e readiness.

## Profiles

### `dev`

Uso local simples com H2 em memoria.

- H2 em memoria.
- Flyway desativado.
- `ddl-auto: update`.
- Credenciais dev conhecidas apenas para ambiente local.

```powershell
cd C:\dev\baseplus\baseplus-backend
mvn spring-boot:run
```

### `docker`

Uso local integrado com PostgreSQL 16, backend e frontend em containers.

- PostgreSQL via Docker Compose.
- Flyway ativo.
- Hibernate `ddl-auto=validate`.
- `JWT_SECRET` e `DB_PASSWORD` exigidos pelo Compose.
- Uploads persistidos em volume Docker.

```powershell
cd C:\dev\baseplus\baseplus-backend
Copy-Item .env.example .env
# Edite .env e substitua JWT_SECRET e DB_PASSWORD.
docker compose up --detach --build --wait
```

### `prod`

Perfil produtivo sem fallback para H2 ou segredos locais.

- PostgreSQL por variaveis de ambiente.
- Flyway ativo.
- Hibernate `ddl-auto=validate`.
- `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` e `UPLOAD_DIR` fornecidos externamente.

## URLs padrao

Desenvolvimento local:

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:5173`

Docker integrado:

- Frontend/Nginx: `http://127.0.0.1:5173`
- Backend direto: `http://127.0.0.1:8080`
- PostgreSQL: sem porta publica no host; acesso apenas pela rede Docker.

Usuario dev:

- Email: `admin@baseplus.com`
- Senha: `Baseplus@123`

Em `docker` e `prod`, a Base+ nao cria administrador automaticamente. Use o bootstrap administrativo manual na primeira inicializacao do ambiente.

## Docker integrado

O `docker-compose.yml` oficial fica em `baseplus-backend/` e sobe:

- `postgres`: PostgreSQL 16 com volume persistente.
- `backend`: Spring Boot com profile `docker`.
- `frontend`: Nginx servindo o build Vite.

Comandos principais:

```powershell
cd C:\dev\baseplus\baseplus-backend
docker compose config
docker compose up --detach --build --wait
docker compose ps
```

Health:

```powershell
curl.exe http://127.0.0.1:5173/api/health
curl.exe http://127.0.0.1:5173/api/health/ready
```

Operacao completa, portas, volumes, backup e restore estao em `docs/docker.md`.
Atualizacao de ambientes existentes em HOM e PRD esta documentada em `docs/update.md`.

## Criando uma nova aplicacao

A Base+ pode ser usada como fundacao para iniciar novos sistemas corporativos. O fluxo oficial de clone, troca de repositorio, configuracao, validacao e criacao do primeiro modulo esta em `docs/new-application.md`.

## Engenharia Assistida por IA

A Base+ possui uma camada oficial de engenharia para orientar desenvolvimento assistido por IA e preservar conhecimento tecnico reutilizavel da plataforma. A referencia principal esta em `engineering/ai/README.md`.
O metodo oficial de colaboracao entre humanos, IA e Codex esta em `engineering/playbooks/100-ai-collaboration-method.md`.
Os principios arquiteturais permanentes da plataforma estao em `engineering/architecture/principles.md`.
As decisoes arquiteturais permanentes sao registradas em `engineering/architecture/decisions.md`.
O processo oficial de release da plataforma esta em `engineering/release/release-process.md`.

## Operacao

Os runbooks operacionais oficiais da Base+ estao em `engineering/operations/README.md`.

## Playbooks

A Base+ possui uma biblioteca oficial de Playbooks para conduzir conversas de engenharia entre desenvolvedores e IA antes da implementacao. A referencia principal esta em `engineering/playbooks/README.md`.

## Bootstrap administrativo

Use o bootstrap administrativo apenas na primeira inicializacao de ambientes `docker`, homologacao ou producao, quando ainda nao existir usuario com perfil `ADMIN`.

O comando recebe nome, email e senha por variaveis de ambiente ou argumentos da aplicacao, cria o perfil `ADMIN` com as permissions oficiais, cria o usuario administrador com senha criptografada e registra auditoria de sistema. Nova execucao e recusada quando ja houver administrador cadastrado.

Docker:

```powershell
cd C:\dev\baseplus\baseplus-backend
$env:BASEPLUS_BOOTSTRAP_ADMIN_ENABLED = 'true'
$env:BASEPLUS_BOOTSTRAP_ADMIN_NAME = 'Administrador'
$env:BASEPLUS_BOOTSTRAP_ADMIN_EMAIL = 'admin@empresa.com'
$env:BASEPLUS_BOOTSTRAP_ADMIN_PASSWORD = '<senha-forte>'
docker compose run --rm backend java -jar /app/app.jar --spring.main.web-application-type=none
```

Producao sem Compose:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'prod'
$env:BASEPLUS_BOOTSTRAP_ADMIN_ENABLED = 'true'
$env:BASEPLUS_BOOTSTRAP_ADMIN_NAME = 'Administrador'
$env:BASEPLUS_BOOTSTRAP_ADMIN_EMAIL = 'admin@empresa.com'
$env:BASEPLUS_BOOTSTRAP_ADMIN_PASSWORD = '<senha-forte>'
java -jar baseplus-backend.jar --spring.main.web-application-type=none
```

## Banco de dados

- `dev`: H2 em memoria para desenvolvimento rapido.
- `docker`: PostgreSQL 16 em container, com Flyway e Hibernate validate.
- `prod`: PostgreSQL externo por variaveis de ambiente, sem H2.

A compatibilidade com PostgreSQL e validada por teste automatizado com Testcontainers.

## Seguranca

- JWT com refresh token e logout.
- `JWT_SECRET` obrigatorio fora do profile `dev`.
- Permissoes granulares obrigatorias.
- Roles agrupam permissoes, mas nao substituem permissions exigidas por endpoints.
- CORS configuravel por `BASEPLUS_CORS_ALLOWED_ORIGINS`.
- Hosts temporarios de tunel devem ficar apenas em `.env` local ou variaveis de ambiente.
- Uploads aceitam apenas imagens validas (`PNG`, `JPG`/`JPEG`, `ICO`) e usam `UPLOAD_DIR`.

## Documentacao do projeto

- `AI_CONTEXT.md`: contexto e regras para desenvolvimento assistido por IA.
- `BRAND_GUIDE.md`: kit visual padrao da marca Base+ e regras de uso sem limitar personalizacao.
- `MODULE_TEMPLATE.md`: padrao oficial para novos modulos, com CRUD Compacto e CRUD Completo.
- `TASK_PROMPT.md`: prompt padrao para novas interacoes com IA/Codex.
- `docs/architecture.md`: visao arquitetural da plataforma.
- `docs/module-development.md`: guia pratico para criar novos modulos.
- `docs/new-application.md`: guia oficial para criar uma nova aplicacao a partir da Base+.
- `docs/docker.md`: operacao Docker integrada, volumes, backup e restore.
- `docs/update.md`: processo oficial de atualizacao de HOM e PRD sem reinstalar nem perder dados.
- `docs/branding.md`: regras de marca, assets e precedencia de personalizacao.
- `docs/permissions.md`: padrao de permissoes por modulo.
- `docs/release-1.0.md`: escopo, validacao e proximos passos da versao 1.0.
- `docs/release-1.1-checklist.md`: checklist oficial para fechamento da v1.1.0.
- `docs/setup.md`: setup local e validacao.
- `docs/integrations.md`: padrao arquitetural para integracoes externas.
- `engineering/ai/README.md`: camada oficial de engenharia assistida por IA.
- `scripts/check-project.ps1`: checagem local de backend e frontend.
- `state.txt`: checkpoint curto do estado atual.
- `ROADMAP.md`: proximas etapas planejadas.
- `CHANGELOG.md`: historico de mudancas relevantes.
- `Promptbaseplus.txt`: documento legado de prompt/contexto original.

## Marca

Os assets oficiais padrao da marca ficam em:

- `brand/`
- `baseplus-frontend/public/brand/`

Esses arquivos servem como referencia e fallback visual da plataforma Base+.
O modulo Branding continua sendo responsavel por personalizacao, uploads, white label, cores, tema e densidade visual.

## Fluxo para novas funcionalidades

1. Comece pelo modelo em `TASK_PROMPT.md`.
2. Leia `AI_CONTEXT.md` e `state.txt`.
3. Para novos modulos, leia `MODULE_TEMPLATE.md` e `docs/module-development.md`.
4. Escolha CRUD Compacto ou CRUD Completo.
5. Implemente backend, frontend, permissoes, rotas e menu.
6. Rode as validacoes relevantes.
7. Atualize `state.txt` e `CHANGELOG.md` quando a mudanca for relevante.

## Validacao

Validacao padrao:

```powershell
cd C:\dev\baseplus
.\scripts\check-project.ps1
```

Checklist completo da v1.1.0:

```text
docs/release-1.1-checklist.md
```

Para validar partes especificas:

```powershell
cd C:\dev\baseplus\baseplus-backend
mvn test
mvn -Dtest=PostgreSqlCompatibilityTest test

cd C:\dev\baseplus\baseplus-frontend
npm run build

cd C:\dev\baseplus\baseplus-backend
docker compose config
docker compose up --detach --build --wait
```

## Release

A versao publicada atual e `1.1.0`.

A `v1.1.0` consolida PostgreSQL, Docker integrado, readiness, CORS configuravel, uploads configuraveis, documentacao operacional, padrao de integracoes e checklist oficial de release.

Antes de iniciar novos modulos de negocio, use `MODULE_TEMPLATE.md`, `TASK_PROMPT.md` e `docs/module-development.md`.
