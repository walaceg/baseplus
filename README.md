# Base+

Versao atual: `1.0.1`

Base+ e uma plataforma corporativa modular para construcao de aplicacoes administrativas e sistemas de negocio.

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
- PostgreSQL
- H2 para desenvolvimento

Frontend:

- React 19
- Vite
- React Router
- Axios
- lucide-react
- CSS variables

## Estrutura

```text
baseplus/
├── README.md
├── AI_CONTEXT.md
├── BRAND_GUIDE.md
├── MODULE_TEMPLATE.md
├── CHANGELOG.md
├── ROADMAP.md
├── TASK_PROMPT.md
├── state.txt
├── brand/
├── docs/
├── scripts/
├── baseplus-backend/
└── baseplus-frontend/
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
- Health checks.

## Ambiente local

Backend:

```powershell
cd C:\dev\baseplus\baseplus-backend
mvn spring-boot:run
```

Frontend:

```powershell
cd C:\dev\baseplus\baseplus-frontend
npm install
npm run dev
```

URLs padrao:

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:5173`

Usuario dev:

- Email: `admin@baseplus.com`
- Senha: `Baseplus@123`

## Banco de dados

No perfil `dev`, o backend usa H2 em memoria e `ddl-auto: update`.

Existe um `docker-compose.yml` no backend para subir PostgreSQL 16 quando o projeto for configurado para ambiente persistente.

## Documentacao do projeto

- `AI_CONTEXT.md`: contexto e regras para desenvolvimento assistido por IA.
- `BRAND_GUIDE.md`: kit visual padrao da marca Base+ e regras de uso sem limitar personalizacao.
- `MODULE_TEMPLATE.md`: padrao oficial para novos modulos, com CRUD Compacto e CRUD Completo.
- `TASK_PROMPT.md`: prompt padrao para novas interacoes com IA/Codex.
- `docs/architecture.md`: visao arquitetural da plataforma.
- `docs/module-development.md`: guia pratico para criar novos modulos.
- `docs/branding.md`: regras de marca, assets e precedencia de personalizacao.
- `docs/permissions.md`: padrao de permissoes por modulo.
- `docs/release-1.0.md`: escopo, validacao e proximos passos da versao 1.0.
- `docs/setup.md`: setup local e validacao.
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
6. Rode `scripts/check-project.ps1` ou os comandos equivalentes.
7. Atualize `state.txt` e `CHANGELOG.md` quando a mudanca for relevante.

## Validacao

```powershell
cd C:\dev\baseplus
.\scripts\check-project.ps1
```

Para validar apenas uma parte:

```powershell
.\scripts\check-project.ps1 -SkipBackend
.\scripts\check-project.ps1 -SkipFrontend
```

## Release 1.0

A versao 1.0 fecha a Base+ como fundacao administrativa reutilizavel. O escopo detalhado esta em `docs/release-1.0.md`.

Antes de iniciar novos modulos de negocio, use `MODULE_TEMPLATE.md`, `TASK_PROMPT.md` e `docs/module-development.md`.
