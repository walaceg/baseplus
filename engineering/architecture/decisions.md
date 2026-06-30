# Architecture Decision Records (ADR)

Este documento registra decisoes arquiteturais permanentes da Base+.

ADRs existem para preservar contexto, problema, decisao, justificativa e consequencias de escolhas relevantes da plataforma.

Novas decisoes deverao ser adicionadas ao longo da evolucao da Base+, mantendo numeracao sequencial e formato padronizado.

## ADR-001 - Arquitetura Modular

### Contexto

A Base+ precisa servir como fundacao reutilizavel para diferentes aplicacoes corporativas.

### Problema

Sem uma arquitetura modular, funcionalidades de plataforma e regras de negocio tendem a se misturar, dificultando manutencao, evolucao e reutilizacao.

### Decisao

Adotar arquitetura modular como principio central da Base+.

### Justificativa

Modularidade permite organizar responsabilidades, evoluir partes da plataforma de forma incremental e reduzir acoplamento entre capacidades distintas.

### Consequencias

- Funcionalidades devem ser organizadas por modulo.
- A evolucao exige respeito aos limites de responsabilidade.
- Reutilizacao deve surgir de necessidades reais, nao de abstracoes prematuras.

## ADR-002 - Backend em Java + Spring Boot

### Contexto

A Base+ precisa de uma plataforma backend robusta, madura e adequada a sistemas corporativos.

### Problema

Aplicacoes corporativas exigem seguranca, transacoes, persistencia, validacoes, testes e ecossistema confiavel.

### Decisao

Utilizar Java com Spring Boot como base do backend.

### Justificativa

Java e Spring Boot oferecem maturidade, ampla adocao corporativa, bom suporte a seguranca, persistencia, observabilidade, testes e integracao com bancos relacionais.

### Consequencias

- O backend segue padroes do ecossistema Spring.
- A plataforma se beneficia de ferramentas consolidadas.
- Evolucoes devem preservar compatibilidade com o modelo Spring Boot.

## ADR-003 - Frontend React + Vite

### Contexto

A Base+ precisa de um frontend moderno, modular e produtivo para interfaces administrativas.

### Problema

Interfaces corporativas exigem boa organizacao, componentizacao, build rapido e manutencao previsivel.

### Decisao

Utilizar React com Vite como base do frontend.

### Justificativa

React oferece componentizacao madura e Vite fornece experiencia de desenvolvimento rapida, build simples e boa integracao com tooling moderno.

### Consequencias

- Interfaces devem ser organizadas em componentes e modulos.
- Build e desenvolvimento seguem padroes Vite.
- Estado global adicional deve ser evitado sem necessidade clara.

## ADR-004 - PostgreSQL como banco oficial

### Contexto

A Base+ precisa de um banco relacional confiavel para ambientes persistentes.

### Problema

Ambientes de homologacao e producao precisam de consistencia transacional, integridade, recursos SQL maduros e previsibilidade operacional.

### Decisao

Adotar PostgreSQL como banco oficial da Base+ para ambientes persistentes.

H2 deve ser usado exclusivamente para desenvolvimento local.

### Justificativa

PostgreSQL e uma base relacional madura, robusta e adequada a sistemas corporativos. H2 acelera desenvolvimento local, mas nao deve orientar decisoes produtivas.

### Consequencias

- Queries, migrations e validacoes devem ser compativeis com PostgreSQL.
- H2 nao deve mascarar decisoes de schema ou comportamento.
- Compatibilidade PostgreSQL deve ser validada continuamente.

## ADR-005 - Flyway para versionamento do banco

### Contexto

A Base+ precisa evoluir schema de banco de forma controlada entre versoes.

### Problema

Alteracoes manuais ou nao rastreadas dificultam atualizacoes, rollback, auditoria tecnica e reproducibilidade.

### Decisao

Utilizar Flyway para versionamento do banco.

### Justificativa

Flyway permite controlar historico de migrations, aplicar evolucoes automaticamente e manter previsibilidade em ambientes persistentes.

### Consequencias

- Alteracoes de schema devem ser entregues por migrations.
- Migrations publicadas nao devem ser editadas.
- Falhas de migration devem bloquear subida normal em ambientes persistentes.

## ADR-006 - JWT para autenticacao

### Contexto

A Base+ precisa de autenticacao adequada a frontend separado do backend e ambientes stateless.

### Problema

Sessao baseada em servidor aumenta acoplamento operacional e dificulta escalabilidade horizontal em alguns cenarios.

### Decisao

Utilizar JWT para autenticacao.

### Justificativa

JWT permite autenticacao stateless, integracao simples com APIs e transporte seguro de claims essenciais, quando combinado com boas praticas de expiracao e refresh token.

### Consequencias

- Tokens devem ter expiracao controlada.
- Refresh token deve ser tratado com seguranca.
- Mudancas em autenticacao exigem revisao cuidadosa.

## ADR-007 - Controle de acesso baseado em Perfis e Permissoes

### Contexto

A Base+ precisa controlar acesso a funcionalidades administrativas e futuras regras de negocio.

### Problema

Controle apenas por papel fixo tende a ser rigido e insuficiente para diferentes organizacoes e modulos.

### Decisao

Adotar controle de acesso baseado em perfis e permissoes granulares.

### Justificativa

Permissoes granulares permitem evolucao por modulo, agrupamentos funcionais e maior controle administrativo.

### Consequencias

- Endpoints devem exigir permissoes especificas.
- Perfis agrupam permissoes, mas nao substituem verificacoes granulares.
- Novos modulos devem declarar suas permissoes.

## ADR-008 - Docker como ambiente oficial de homologacao e producao

### Contexto

A Base+ precisa de execucao previsivel para homologacao e producao.

### Problema

Instalacoes manuais aumentam divergencia entre ambientes e dificultam atualizacao, suporte e diagnostico.

### Decisao

Utilizar Docker e Docker Compose como referencia oficial para homologacao e producao.

### Justificativa

Docker reduz divergencia ambiental, padroniza execucao e facilita integracao entre frontend, backend, banco e volumes.

### Consequencias

- Ambientes devem preservar volumes.
- Configuracoes devem ser externas.
- Atualizacoes devem evitar reinstalacao e perda de dados.

## ADR-009 - Design Tokens e CSS Variables

### Contexto

A Base+ precisa de consistencia visual e capacidade de personalizacao.

### Problema

Estilos isolados e cores fixas dificultam branding, manutencao e acessibilidade visual.

### Decisao

Utilizar Design Tokens e CSS Variables como base do design system.

### Justificativa

Tokens centralizam decisoes visuais, facilitam temas, branding e consistencia entre componentes.

### Consequencias

- Cores fixas devem ser evitadas.
- Componentes devem consumir tokens.
- Personalizacao visual deve preservar acessibilidade e consistencia.

## ADR-010 - Uploads utilizando Storage Service

### Contexto

A Base+ precisa tratar uploads de forma segura e configuravel.

### Problema

Uploads espalhados por services e caminhos fixos dificultam seguranca, portabilidade e operacao.

### Decisao

Centralizar uploads por meio de um Storage Service.

### Justificativa

Um servico dedicado padroniza validacao, armazenamento, URLs relativas e remocao de arquivos.

### Consequencias

- Uploads devem usar diretorio configuravel.
- URLs persistidas devem ser relativas.
- Validacoes de tipo e tamanho devem permanecer centralizadas.

## ADR-011 - Resposta padronizada das APIs

### Contexto

A Base+ precisa de contratos previsiveis entre backend, frontend e integracoes futuras.

### Problema

Respostas inconsistentes aumentam tratamento condicional no frontend e dificultam diagnostico.

### Decisao

Adotar resposta padronizada para APIs.

### Justificativa

Um formato comum melhora previsibilidade, tratamento de erros, integracao e leitura por desenvolvedores.

### Consequencias

- Endpoints devem retornar contratos consistentes.
- Erros devem seguir formato padrao.
- Alteracoes de contrato devem ser tratadas como mudancas relevantes.

## ADR-012 - Estrutura Core / Shared / Modules

### Contexto

A Base+ precisa separar fundamentos da plataforma, elementos compartilhados e funcionalidades.

### Problema

Sem separacao clara, regras especificas podem contaminar a base e reduzir reutilizacao.

### Decisao

Adotar estrutura `core`, `shared` e `modules`.

### Justificativa

Essa separacao facilita manutencao, evolucao incremental e clareza sobre onde cada responsabilidade deve viver.

### Consequencias

- `core` deve ser pequeno e estavel.
- `shared` deve conter elementos reutilizaveis.
- `modules` deve concentrar funcionalidades.

## ADR-013 - Evolucao incremental da plataforma

### Contexto

A Base+ deve evoluir por versoes sem perder estabilidade.

### Problema

Grandes mudancas estruturais aumentam risco de regressao, dificultam atualizacao e reduzem confianca na fundacao.

### Decisao

Adotar evolucao incremental como filosofia permanente.

Toda funcionalidade nasce em `modules`.

Somente evolui para `core` quando houver reutilizacao comprovada.

### Justificativa

Essa abordagem preserva simplicidade, evita overengineering e permite que padroes sejam extraidos apenas apos uso real.

### Consequencias

- Novas funcionalidades devem comecar isoladas.
- Promocoes para `core` exigem evidencia.
- Versoes devem documentar evolucoes relevantes.

## ADR-014 - Desenvolvimento Assistido por IA

### Contexto

A Base+ usa documentacao e conhecimento estruturado para apoiar desenvolvimento assistido por IA.

### Problema

Sem contexto arquitetural, ferramentas de IA podem acelerar tambem inconsistencias, duplicacoes e decisoes inadequadas.

### Decisao

Adotar desenvolvimento assistido por IA com governanca arquitetural documentada.

### Justificativa

A IA acelera implementacao, diagnostico, revisao e documentacao. A arquitetura continua sendo responsabilidade da plataforma.

Os documentos de engenharia representam conhecimento institucional e orientam tanto IA quanto desenvolvedores.

### Consequencias

- Prompts devem respeitar principios da Base+.
- Mudancas assistidas por IA devem ser revisadas.
- Documentacao de engenharia deve evoluir junto com a plataforma.

## Como registrar novos ADRs

Novos ADRs devem ser adicionados neste documento usando numeracao sequencial.

Convencao:

```text
ADR-015 - Titulo da decisao
ADR-016 - Titulo da decisao
ADR-017 - Titulo da decisao
```

Cada ADR deve conter:

- contexto;
- problema;
- decisao;
- justificativa;
- consequencias.

ADRs devem registrar decisoes relevantes e duradouras. Detalhes de implementacao devem permanecer em documentacao tecnica, codigo, testes ou guias operacionais.
