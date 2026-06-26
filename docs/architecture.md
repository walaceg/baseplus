# Architecture - Base+

Base+ e uma plataforma corporativa modular para aplicacoes administrativas e sistemas de negocio.

## Visao geral

O projeto e dividido em duas aplicacoes:

- `baseplus-backend`: API e regras de negocio.
- `baseplus-frontend`: interface administrativa.

Documentos de apoio:

- `AI_CONTEXT.md`: contexto principal para desenvolvimento assistido por IA.
- `MODULE_TEMPLATE.md`: padrao para novos modulos.
- `BRAND_GUIDE.md`: padrao visual da marca e regra de personalizacao.
- `TASK_PROMPT.md`: prompt operacional para novas tarefas.
- `docs/integrations.md`: padrao oficial para integracoes externas.

## Backend

Stack:

- Java 17
- Spring Boot
- Spring Security com JWT
- JPA/Hibernate
- Flyway
- PostgreSQL para ambiente persistente
- H2 para desenvolvimento/testes locais

Pacote raiz:

```text
com.baseplus
```

Estrutura principal:

```text
core/
shared/
modules/
application/
```

Responsabilidades:

- `core`: seguranca, configuracao, excecoes, storage e recursos transversais.
- `core.integration`: infraestrutura transversal de autenticacao, configuracao, auditoria, idempotencia e transports de integracao.
- `shared`: DTOs e utilitarios reutilizaveis.
- `modules`: dominios de negocio.
- `application`: orquestracao quando necessaria.

Cada modulo deve manter:

```text
controller/
service/
domain/
repository/
dto/
```

Regras:

- Controller nao deve conter regra de negocio.
- Service concentra validacoes e regras.
- Repository nao deve ser acessado diretamente por controller.
- Respostas devem usar `ApiResponse`.
- Listagens paginadas devem usar `PageResponse`.
- Endpoints sensiveis devem validar permission.

## Frontend

Stack:

- React
- Vite
- React Router
- Axios
- CSS variables
- Componentes compartilhados

Estrutura principal:

```text
src/core/
src/shared/
src/modules/
```

Responsabilidades:

- `core`: app, layout, auth, rotas, tema e navegacao.
- `shared`: componentes, hooks, storage, branding e utilitarios.
- `modules`: telas, services e estilos de cada dominio.

Regras:

- Chamadas HTTP ficam em services do modulo.
- Paginas usam componentes compartilhados quando possivel.
- Autorizacao visual usa permissoes de `shared/auth/permissions.js`.
- Regras sensiveis devem permanecer no backend.

## Branding

O modulo Branding permite personalizacao por API e upload.

Assets padrao ficam em:

```text
brand/
baseplus-frontend/public/brand/
```

Regra:

```text
configuracao personalizada > asset padrao Base+ > fallback textual
```

Nao remover upload, white label, temas, cores ou densidade visual ao evoluir a marca.

## Novos modulos

Todo novo modulo deve escolher:

- CRUD Compacto
- CRUD Completo

O padrao completo esta em `MODULE_TEMPLATE.md`.

## Integracoes externas

Infraestrutura compartilhada usa o namespace reservado:

```text
com.baseplus.core.integration
```

Adapters e contratos de um unico dominio permanecem em `com.baseplus.modules.<modulo>.integration`. Entradas maquina-a-maquina usam `<Modulo>ExternalController`, separadas dos controllers administrativos protegidos por JWT.

Autenticacao por `X-API-Key`, idempotencia, auditoria, correlationId e transports REST/SOAP estao definidos em `docs/integrations.md`. Nenhum endpoint externo ou dependencia SOAP faz parte da base atual.
