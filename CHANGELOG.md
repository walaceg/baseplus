# Changelog

Todas as mudancas relevantes do projeto devem ser registradas neste arquivo.

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
