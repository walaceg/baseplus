# Contexto Arquitetural - Base+

Este documento resume a arquitetura da Base+ para orientar evolucoes assistidas por IA e revisoes tecnicas.

## Objetivo da Base+

A Base+ e uma fundacao reutilizavel para construir aplicacoes corporativas administrativas e sistemas de negocio.

Ela entrega recursos comuns antes da criacao dos modulos especificos:

- autenticacao;
- autorizacao;
- perfis;
- permissoes;
- estrutura organizacional;
- auditoria;
- branding;
- uploads;
- Docker;
- documentacao operacional.

## Arquitetura modular

A Base+ organiza responsabilidades em:

- `core`: infraestrutura e recursos transversais da plataforma.
- `shared`: contratos, componentes e utilitarios reutilizaveis.
- `modules`: funcionalidades da plataforma e do negocio.
- `infra`: configuracoes de execucao, Docker, banco e operacao.

Regra obrigatoria:

```text
Toda funcionalidade nasce em modules.
Somente evolui para core quando houver reutilizacao comprovada por duas ou mais aplicacoes.
```

## Backend

O backend usa Java e Spring Boot.

Fluxo padrao:

```text
Controller -> Service -> Repository -> Domain
```

Estrutura esperada de modulo:

```text
com.baseplus.modules.<modulo>
|-- controller
|-- service
|-- repository
|-- domain
`-- dto
```

Responsabilidades:

- `controller`: contrato HTTP, entrada e autorizacao declarativa.
- `service`: regras de aplicacao, transacoes e orquestracao.
- `repository`: persistencia.
- `domain`: entidades e comportamento estrutural.
- `dto`: contratos de entrada e saida.

## Frontend

O frontend usa React e Vite.

Organizacao principal:

- `core`: layout, autenticacao, tema e estrutura da aplicacao.
- `shared`: componentes, hooks, API clients, storage, branding e utilitarios.
- `modules`: telas e services especificos de cada modulo.

Estrutura esperada de modulo:

```text
src/modules/<modulo>/
|-- pages/
|-- components/
`-- services/
```

Modulos simples podem usar estrutura compacta quando isso reduz complexidade sem quebrar padroes.

## Seguranca

A Base+ utiliza:

- Spring Security;
- JWT;
- refresh token;
- permissoes granulares;
- CORS configuravel por ambiente;
- protecao de endpoints por permissao.

Autenticacao e autorizacao nao devem ser alteradas sem justificativa tecnica clara e validacao adequada.

## Banco de dados

A Base+ usa PostgreSQL como banco persistente padrao.

H2 e usado apenas para desenvolvimento local.

Flyway e responsavel por evolucao de schema em ambientes persistentes. Migrations publicadas nao devem ser editadas; novas alteracoes devem ser entregues por novas migrations.

## Docker

A stack Docker integrada inclui:

- PostgreSQL;
- backend Spring Boot;
- frontend servido por Nginx;
- proxy Nginx para `/api` e `/uploads`;
- volume persistente para banco;
- volume persistente para uploads.

## Design system

O frontend usa CSS Variables e design tokens.

Evite cores fixas. Componentes devem respeitar tokens da plataforma, estados visuais existentes e acessibilidade.

## Resposta padrao da API

Endpoints seguem resposta padrao com sucesso, dados, mensagem e erros quando aplicavel.

Controllers nao devem concentrar regra de negocio. A regra deve ficar em services.

## Bootstrap administrativo

O bootstrap administrativo cria o primeiro usuario administrador em ambientes persistentes com banco vazio.

Ele nao substitui usuarios reais, nao deve rodar em atualizacoes e nao deve ser usado como seed permanente.

## Branding

O modulo Branding controla identidade visual, white label, logos, favicon, background de login, cores, tema e densidade.

Personalizacoes devem preservar fallback seguro e compatibilidade com tela de login sem token.

## Auditoria

Operacoes administrativas e eventos sensiveis devem registrar auditoria quando relevantes.

Auditoria deve evitar exposicao de segredos ou dados sensiveis desnecessarios.

## Uploads

Uploads usam diretorio configuravel por ambiente.

URLs persistidas devem ser relativas, iniciando com `/uploads/`, para manter portabilidade entre dev, Docker e producao.

## Principios Arquiteturais

- Simplicidade.
- Evolucao incremental.
- Reutilizacao.
- Separacao de responsabilidades.
- Evitar overengineering.

O objetivo e permitir evolucao continua sem transformar a fundacao em um conjunto rigido ou excessivamente abstrato.
