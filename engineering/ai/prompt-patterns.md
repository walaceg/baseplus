# Padroes Oficiais de Prompts - Base+

Este documento define categorias de prompts para evolucao assistida por IA.

## Backend

Objetivo: implementar ou ajustar regras de API, services, repositories, entidades, DTOs e testes.

Quando utilizar: criacao de modulos, alteracao de contratos, regras de permissao, persistencia e validacoes.

Responsabilidade: manter arquitetura `Controller -> Service -> Repository -> Domain`.

Resultado esperado:

- arquivos Java alterados;
- migrations quando necessarias;
- testes backend;
- compatibilidade PostgreSQL;
- resumo dos endpoints afetados.

## Frontend

Objetivo: implementar telas, componentes, services, rotas, menus e experiencia de usuario.

Quando utilizar: novas paginas, ajustes visuais, integracao com API, loading, erro, filtros e formularios.

Responsabilidade: respeitar `core/shared/modules`, design tokens e CSS Variables.

Resultado esperado:

- arquivos React/CSS alterados;
- componentes reutilizados;
- build validado;
- estados de loading, erro e vazio considerados.

## Infraestrutura

Objetivo: ajustar execucao, Docker, Nginx, profiles, variaveis de ambiente e operacao tecnica.

Quando utilizar: Docker Compose, profiles Spring, proxy, volumes, healthcheck, deploy e execucao local.

Responsabilidade: preservar seguranca, volumes, segredos fora do repositorio e compatibilidade com HOM/PRD.

Resultado esperado:

- arquivos de infraestrutura alterados;
- comandos de validacao;
- riscos operacionais;
- estrategia de rollback quando aplicavel.

## Documentacao

Objetivo: registrar conhecimento tecnico, guias, processos, padroes e decisoes.

Quando utilizar: nova release, novo modulo, processo operacional, onboarding e arquitetura.

Responsabilidade: manter linguagem tecnica, objetiva e reutilizavel.

Resultado esperado:

- arquivos Markdown alterados;
- links adicionados quando necessario;
- consistencia com README e docs existentes;
- `git diff --check` validado.

## Operacao

Objetivo: orientar execucao, backup, restore, atualizacao, bootstrap e validacoes de ambiente.

Quando utilizar: preparacao de HOM/PRD, atualizacao de ambiente, incidentes operacionais e instalacao.

Responsabilidade: preservar dados, usuarios, uploads, auditoria e branding.

Resultado esperado:

- procedimento seguro;
- comandos claros;
- pre-requisitos;
- validacao pos-operacao.

## Diagnostico

Objetivo: investigar problema sem alterar arquivos.

Quando utilizar: comportamento inesperado, regressao, erro de build, falha de login, upload, Docker ou banco.

Responsabilidade: identificar causa provavel com evidencias.

Resultado esperado:

- arquivos analisados;
- testes ou comandos executados;
- evidencias;
- causa provavel;
- correcao recomendada.

## Homologacao

Objetivo: validar que uma mudanca funciona em ambiente proximo de producao.

Quando utilizar: antes de release, apos alteracoes em Docker, banco, uploads, autenticacao ou integracoes.

Responsabilidade: validar fluxo completo com PostgreSQL, Docker, frontend e backend.

Resultado esperado:

- checklist executado;
- pendencias;
- riscos;
- parecer de liberacao.

## Atualizacao

Objetivo: atualizar ambiente existente sem reinstalar e sem perder dados.

Quando utilizar: evolucao de HOM/PRD para nova tag ou imagem.

Responsabilidade: preservar banco, uploads, usuarios, auditoria, branding e configuracoes.

Resultado esperado:

- backup antes da atualizacao;
- comandos de atualizacao;
- validacao depois da atualizacao;
- rollback documentado.
