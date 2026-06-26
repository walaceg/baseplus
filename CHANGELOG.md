# Changelog

Todas as mudancas relevantes do projeto devem ser registradas neste arquivo.

## 2026-06-26

### Release v1.1.0

- Base+ publicada como versao `1.1.0` e definida como Baseline Oficial da plataforma.
- PostgreSQL consolidado como banco padrao para ambientes persistentes.
- H2 mantido apenas para desenvolvimento local no profile `dev`.
- Profiles `docker` e `prod` revisados com Flyway ativo e Hibernate `ddl-auto=validate`.
- Adicionada validacao automatizada de compatibilidade com PostgreSQL 16 usando Testcontainers, Flyway e JPA validate.
- Teste de compatibilidade PostgreSQL passa a ser ignorado quando Docker nao esta disponivel, sem quebrar a build.
- Dependencias de Testcontainers atualizadas para compatibilidade com Docker Engine 29.x.
- Criado profile `docker` para execucao local com PostgreSQL.
- Criado profile `prod` para execucao produtiva com PostgreSQL por variaveis de ambiente.
- Criado `.env.example` do backend com variaveis de dev, docker, prod e reservas futuras de integracao externa.
- Uploads passam a usar diretorio configuravel por `UPLOAD_DIR`, preservando `uploads` como padrao de desenvolvimento.
- Dockerfile multi-stage criado para o backend com Java 17, runtime enxuto e usuario nao-root.
- Dockerfile multi-stage criado para o frontend com build Vite e Nginx.
- Nginx configurado com fallback SPA e proxy para `/api` e `/uploads`.
- Docker Compose integrado passa a subir PostgreSQL, backend e frontend.
- PostgreSQL permanece sem porta publica no host no Compose integrado.
- Volumes persistentes definidos para PostgreSQL e uploads.
- Healthcheck do backend no Compose passa a usar readiness.
- Endpoint `/health/ready` criado para verificar conectividade real com o banco sem expor dados sensiveis.
- Endpoint `/health` mantido como liveness simples.
- CORS deixa de depender de dominio temporario fixo e passa a usar `BASEPLUS_CORS_ALLOWED_ORIGINS`.
- Vite deixa de manter host temporario fixo e passa a aceitar `VITE_ALLOWED_HOSTS` quando necessario localmente.
- Uso de tuneis locais documentado como configuracao opcional nao versionada.
- Padrao oficial de integracoes externas documentado em `docs/integrations.md`, incluindo REST, SOAP, terceiros, legados, `X-API-Key`, idempotencia, auditoria e correlationId.
- Documentacao Docker operacional criada em `docs/docker.md`, incluindo execucao, portas, volumes, health, backup, restore e parada da stack.
- Checklist oficial de validacao da v1.1.0 criado em `docs/release-1.1-checklist.md`.
- Documentacao oficial alinhada para refletir a Base+ como fundacao reutilizavel e Baseline Oficial v1.1.0.
- Bootstrap administrativo oficial criado para primeira inicializacao de ambientes Docker/HOM/PRD, sem seed permanente, credenciais fixas, migrations ou inserts SQL.

## 2026-06-01

### Release

- Base+ publicada como versao `1.0.2`.
- Backend e frontend versionados como `1.0.2`.
- Release focada em endurecimento de seguranca para JWT, access token, uploads e tentativas repetidas de login.
- Validacao backend aprovada com 114 testes automatizados.

## 2026-05-26

### Seguranca

- Removido o segredo JWT de desenvolvimento da configuracao base da aplicacao.
- `JWT_SECRET` passa a ser obrigatorio fora do perfil `dev`, impedindo que ambientes produtivos herdem o segredo conhecido usado localmente.
- Adicionado teste automatizado para preservar a separacao entre segredo externo e fallback exclusivo de desenvolvimento.
- Documentada a obrigatoriedade de fornecer segredo externo forte em perfis nao locais.
- Uploads passam a aceitar apenas imagens raster/icone com assinatura valida (`PNG`, `JPG`/`JPEG` ou `ICO`), recusando SVG e arquivos disfarcados.
- Arquivos SVG legados sob `/uploads/**` deixam de ser servidos publicamente; recursos ausentes passam a retornar `404`.
- Exclusao fisica de avatar passa a ser verificada por teste automatizado.
- Access tokens passam a ser revalidados contra o usuario atual a cada requisicao autenticada.
- Usuarios inativos/bloqueados deixam de autenticar mesmo com token ainda nao expirado.
- Roles e permissions efetivas passam a ser recalculadas a partir do banco, refletindo remocao de permissao em tokens ja emitidos.
- Login passa a bloquear automaticamente o usuario apos 5 tentativas consecutivas com senha invalida.
- Reset administrativo de senha permanece como fluxo de desbloqueio e zera tentativas invalidas.

## 2026-05-25

### Corrigido

- Base+ publicada como versao `1.0.1`.
- Corrigida a duplicidade de versao das migrations Flyway, renomeando `V9__normalize_user_preferences_defaults.sql` para `V13__normalize_user_preferences_defaults.sql`.
- Backend e frontend versionados como `1.0.1`.

## 2026-05-18

### Release

- Base+ preparada como versao `1.0.0`.
- Backend `baseplus-backend` versionado como `1.0.0`.
- Frontend `baseplus-frontend` versionado como `1.0.0`.
- Criado `docs/release-1.0.md` com escopo, validacao, checklist manual e proximos passos.
- `README.md` atualizado com a versao atual, modulos da 1.0 e referencia ao documento de release.
- `state.txt` atualizado como checkpoint da Base+ 1.0.

## 2026-05-16

### Alterado

- Autorizacao por permission passa a ser granular obrigatoria: `ROLE_ADMIN` nao concede mais bypass automatico em `hasPermission`.
- Documentado o padrao de roles por modulo como agrupamentos de permissions.
- `scripts/check-project.ps1` agora localiza Maven/Java no ambiente local e falha corretamente quando comandos externos retornam erro.
- Melhorado o avatar do menu de conta no topbar com variante premium, anel de branding, halo sutil e indicador online.

### Adicionado

- CRUD compacto de Estrutura Organizacional no frontend em `/app/organizacao`.
- Item de menu `Estrutura org.` em Configuracoes.
- Item `Estrutura org.` tambem fica visivel para quem possui `ROLES_VIEW`, pois serve como apoio aos perfis organizacionais.
- CRUD de Estrutura Organizacional permite criacao/edicao por `ROLES_EDIT` e exclusao por `ROLES_DELETE`, alem das permissions especificas `ORGANIZATION_UNITS_*`.
- Exclusao controlada de tipos e unidades organizacionais no backend com permissao `ORGANIZATION_UNITS_DELETE`.
- Modelo inicial de dois perfis:
  - Perfil funcional para agrupamento de permissions.
  - Perfil organizacional para escopos parametrizaveis por tipo/unidade organizacional.
- Backend de unidades organizacionais genericas com tipos configuraveis, hierarquia opcional e niveis `VIEW`, `EDIT` e `ADMIN`.
- Tela de perfis preparada para criar/editar perfil funcional ou organizacional e vincular escopos organizacionais.
- Pasta `docs/` com guias de arquitetura, criacao de modulos, branding, permissoes e setup.
- Pasta `scripts/` com `check-project.ps1` para validar backend e frontend.
- `TASK_PROMPT.md` como modelo padrao para novas interacoes com IA/Codex.
- Repositorio Git inicializado na raiz do projeto.
- `BRAND_GUIDE.md` com o kit visual padrao da marca Base+ e regra de preservacao da personalizacao.
- Pasta `brand/` na raiz e `baseplus-frontend/public/brand/` com logos padronizados da plataforma.
- `MODULE_TEMPLATE.md` com os padroes oficiais de CRUD Compacto e CRUD Completo para novos modulos.
- Organizacao documental da raiz do projeto.
- `README.md` com visao geral, stack, estrutura e setup local.
- `AI_CONTEXT.md` com regras para desenvolvimento assistido por IA.
- `ROADMAP.md` com proximas etapas.
- `CHANGELOG.md` para historico de mudancas.
- `.gitignore` na raiz para artefatos comuns.

### Atualizado

- `state.txt` passa a ser um checkpoint curto do estado atual do projeto.
