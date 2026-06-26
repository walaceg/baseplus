# AI Context - Base+

Este arquivo e o contexto principal para desenvolvimento assistido por IA no projeto Base+.

## Documentos operacionais

- `README.md`: entrada geral do projeto.
- `state.txt`: checkpoint curto do estado atual.
- `TASK_PROMPT.md`: modelo para novas tarefas com IA/Codex.
- `MODULE_TEMPLATE.md`: padrao para novos modulos.
- `BRAND_GUIDE.md`: guia da marca Base+.
- `docs/architecture.md`: arquitetura.
- `docs/module-development.md`: criacao de modulos.
- `docs/branding.md`: detalhes de branding e personalizacao.
- `docs/permissions.md`: padrao de permissoes.
- `docs/docker.md`: operacao Docker integrada, volumes, backup e restore.
- `docs/release-1.0.md`: escopo fechado, validacao e proximos passos da versao 1.0.
- `docs/release-1.1-checklist.md`: checklist oficial para fechamento da v1.1.0.
- `docs/setup.md`: ambiente local e validacao.
- `docs/integrations.md`: padrao para integracoes REST, SOAP, terceiros e legados.

## Identidade

Base+ e uma plataforma corporativa modular para construcao de aplicacoes administrativas e sistemas de negocio.

Nome tecnico: `baseplus`
Namespace backend: `com.baseplus`
Versao publicada: `1.1.0`
Estado atual: Baseline Oficial `v1.1.0`, pronta para novos projetos de negocio.

## Stack oficial

Backend:

- Java 17
- Spring Boot
- Spring Security com JWT
- JPA/Hibernate
- Flyway
- PostgreSQL para ambiente persistente
- H2 apenas para desenvolvimento/testes locais
- Testcontainers para validacao de compatibilidade com PostgreSQL

Frontend:

- React
- Vite
- React Router
- Axios
- CSS variables
- Componentes compartilhados em `src/shared/components`

Infraestrutura:

- Dockerfiles multi-stage para backend e frontend.
- Docker Compose integrado com PostgreSQL, backend e frontend.
- Nginx com fallback SPA e proxy para `/api` e `/uploads`.
- Volumes persistentes para PostgreSQL e uploads.

## Arquitetura backend

Pacote raiz: `com.baseplus`

- `core`: infraestrutura, seguranca, excecoes, storage, configuracao, integracoes e recursos transversais.
- `shared`: DTOs e utilitarios reutilizaveis.
- `modules`: dominios de negocio.
- `application`: camada reservada para orquestracao quando necessario.

Cada modulo em `modules/<modulo>` deve seguir:

- `controller`
- `service`
- `domain`
- `repository`
- `dto`

## Regras backend

- O backend e a fonte da verdade.
- Regra de negocio fica no service do modulo correspondente.
- Controllers devem ser finos e delegar para services.
- Repositories nao devem ser acessados diretamente por controllers.
- APIs devem usar o padrao `ApiResponse`.
- Endpoints protegidos devem validar autenticacao e permissoes quando aplicavel.
- Nao criar dependencia direta desnecessaria entre modulos.
- Preferir nomes e vocabulario ja existentes no projeto.
- Evitar novas bibliotecas sem necessidade real.
- Integracoes externas seguem `docs/integrations.md`; infraestrutura comum usa `com.baseplus.core.integration` e regra especifica permanece no modulo dono.

## Persistencia e profiles

- `dev`: H2 em memoria, Flyway desativado e segredo local conhecido apenas para desenvolvimento.
- `docker`: PostgreSQL 16, Flyway ativo e Hibernate `ddl-auto=validate`.
- `prod`: PostgreSQL externo por variaveis de ambiente, Flyway ativo e Hibernate `ddl-auto=validate`.
- `JWT_SECRET` e obrigatorio fora do profile `dev`.
- Uploads usam `baseplus.upload.directory`, configuravel por `UPLOAD_DIR`.
- Ambientes `docker` e `prod` nao criam administrador automaticamente; o primeiro admin deve ser criado pelo bootstrap administrativo manual.

## Seguranca

- Autenticacao via JWT.
- Refresh token para renovacao de sessao.
- Controle por role e permission.
- Permissoes devem ser declaradas e reutilizadas de forma consistente entre backend e frontend.
- Senhas devem ser armazenadas apenas com encoder seguro.
- A autorizacao por permission e granular e obrigatoria: roles agrupam permissoes, mas nao substituem permissions exigidas por endpoints.
- Roles por modulo devem ser usadas como pacotes de permissoes, por exemplo `<MODULO>_ADMIN`, `<MODULO>_OPERADOR` e `<MODULO>_LEITOR`.
- A base contempla dois perfis: perfil funcional para permissions e perfil organizacional para escopos parametrizaveis.
- Escopos organizacionais devem usar tipos/unidades configuraveis, sem fixar empresa, filial ou equipe no codigo de novos modulos.
- CORS e configurado por `BASEPLUS_CORS_ALLOWED_ORIGINS`; nao fixar dominios temporarios no codigo.
- Hosts de tuneis locais para Vite devem usar `VITE_ALLOWED_HOSTS` em ambiente local nao versionado.
- Bootstrap administrativo deve ser acionado apenas por configuracao explicita `BASEPLUS_BOOTSTRAP_ADMIN_ENABLED=true`, receber nome, email e senha por ambiente/argumentos e recusar execucao se ja houver usuario `ADMIN`.

## Arquitetura frontend

Estrutura esperada:

- `src/core`: layout, rotas, auth, tema e configuracoes centrais.
- `src/shared`: componentes, hooks, storage, utils e recursos reutilizaveis.
- `src/modules`: telas e services por dominio.

## Regras frontend

- Chamadas HTTP devem ficar em services do modulo.
- Paginas nao devem duplicar logica de API.
- Usar `apiClient` compartilhado.
- Usar componentes de `src/shared/components` antes de criar novos.
- Usar permissoes declaradas em `shared/auth/permissions.js`.
- Nao colocar regra de negocio sensivel apenas no frontend.
- Manter telas administrativas objetivas, densas e consistentes.

## Marca e personalizacao

O kit visual padrao da marca Base+ esta documentado em `BRAND_GUIDE.md`.

Assets oficiais:

- `brand/logo-principal.png`
- `brand/logo-horizontal.png`
- `brand/simbolo.png`
- `brand/app-icons-source.png`
- `baseplus-frontend/public/brand/logo-principal.png`
- `baseplus-frontend/public/brand/logo-horizontal.png`
- `baseplus-frontend/public/brand/simbolo.png`
- `baseplus-frontend/public/brand/app-icons-source.png`

Esses assets podem ser usados como referencia visual ou fallback padrao.

Nao remover nem limitar as funcionalidades existentes de personalizacao do modulo Branding: upload de logos, favicon, login logo, login background, white label, nome, subtitulo, tema, cores e densidade visual.

Quando houver configuracao personalizada salva, ela deve prevalecer sobre os assets padrao.

## Padroes de CRUD

Todo novo modulo deve escolher entre dois modelos documentados em `MODULE_TEMPLATE.md`:

- CRUD Compacto: listagem, filtros e criar/editar em uma unica pagina, normalmente com modal ou drawer. Use para cadastros simples e rapidos.
- CRUD Completo: listagem separada de paginas proprias de criacao/edicao. Use para cadastros ricos, com muitos campos, secoes, vinculos, uploads ou necessidade de URL propria.

Antes de implementar um modulo, leia `MODULE_TEMPLATE.md` e declare qual modelo sera usado.

## Padrao de resposta da API

```json
{
  "success": true,
  "data": {},
  "message": "",
  "errors": []
}
```

## Antes de finalizar uma tarefa

- Rodar testes backend relevantes quando a alteracao afetar API, regra de negocio, seguranca ou persistencia.
- Rodar `npm run build` quando a alteracao afetar frontend.
- Rodar validacoes Docker/PostgreSQL quando a alteracao afetar profiles, persistencia, Docker, health/readiness ou uploads.
- Atualizar `state.txt` quando o estado atual ou proxima etapa mudar.
- Atualizar `CHANGELOG.md` quando houver mudanca funcional relevante.
- Atualizar `README.md` quando comandos, setup ou arquitetura mudarem.

## Cuidados para IA

- Ler arquivos existentes antes de alterar.
- Fazer mudancas pequenas e coesas.
- Nao reverter alteracoes do usuario sem pedido explicito.
- Nao transformar `state.txt` em historico longo.
- Preferir documentacao curta, atual e verificavel.
