# Principios Arquiteturais da Base+

## Objetivo

Este documento define os principios que orientam todas as decisoes tecnicas da Base+.

Tecnologias, bibliotecas, ferramentas e formas de execucao podem evoluir ao longo do tempo. Os principios arquiteturais devem permanecer como referencia para preservar consistencia, estabilidade e capacidade de evolucao.

Estes principios se aplicam tanto a Base+ quanto a aplicacoes derivadas da plataforma.

## Simplicidade

Solucoes simples devem ser priorizadas.

A Base+ deve resolver problemas reais com clareza, evitando complexidade desnecessaria, abstracoes prematuras e camadas que nao gerem valor concreto.

Uma implementacao simples, testavel e compreensivel e preferivel a uma solucao generica demais, dificil de manter ou criada antes da necessidade existir.

## Evolucao Incremental

A plataforma evolui continuamente.

Cada mudanca deve preservar estabilidade, compatibilidade e previsibilidade sempre que possivel.

Evoluir incrementalmente significa:

- reduzir risco;
- validar cada etapa;
- evitar grandes rupturas sem necessidade;
- documentar decisoes relevantes;
- manter caminho claro de atualizacao.

## Arquitetura Modular

A Base+ organiza responsabilidades em camadas.

### core

Concentra recursos transversais e fundamentos tecnicos da plataforma.

Deve conter apenas aquilo que e realmente comum, estavel e reutilizavel por varias partes da Base+ ou por aplicacoes derivadas.

### shared

Reune contratos, componentes, utilitarios e padroes reutilizaveis.

Deve apoiar a consistencia sem concentrar regras especificas de negocio.

### modules

E onde funcionalidades nascem.

Cada modulo representa uma capacidade funcional da plataforma ou da aplicacao de negocio.

Modulos devem ser claros, isolados quando possivel e organizados por responsabilidade.

### infra

Representa configuracoes, execucao, operacao, ambientes, Docker, banco, healthcheck e demais aspectos de infraestrutura.

Infraestrutura faz parte da arquitetura e deve ser tratada como parte do produto.

## Reutilizacao

Reutilizacao e consequencia de uma boa arquitetura.

Ela nao deve ser criada artificialmente antes da necessidade real.

Componentes e servicos devem ser extraidos para camadas compartilhadas somente quando houver repeticao concreta, estabilidade do contrato e beneficio claro para manutencao.

## Regra do Core

Regra oficial:

```text
Toda funcionalidade nasce em modules.

Somente podera evoluir para core quando houver reutilizacao comprovada por duas ou mais aplicacoes.
```

Essa decisao evita que o `core` se torne grande, instavel ou carregado de regras especificas.

O `core` deve permanecer pequeno, confiavel e orientado a fundamentos da plataforma.

## Baixo Acoplamento

Modulos devem depender o minimo possivel entre si.

Dependencias devem ser explicitas, justificadas e preferencialmente mediadas por contratos claros.

Baixo acoplamento permite:

- evolucao independente;
- testes mais simples;
- menor risco de regressao;
- maior clareza de responsabilidades.

## Responsabilidade Unica

Componentes, servicos e modulos devem possuir responsabilidades bem definidas.

Uma unidade de codigo deve ter um motivo claro para existir e uma razao principal para mudar.

Misturar responsabilidades torna a manutencao mais dificil, aumenta risco de regressao e reduz a capacidade de evoluir incrementalmente.

## Seguranca

Seguranca faz parte da arquitetura.

Na Base+, os seguintes elementos sao pilares da plataforma:

- JWT;
- Spring Security;
- perfis;
- permissoes;
- auditoria.

Autenticacao, autorizacao, rastreabilidade e controle de acesso nao devem ser tratados como detalhes secundarios.

Mudancas em seguranca exigem justificativa, revisao e validacao cuidadosa.

## Persistencia

PostgreSQL e o banco oficial da Base+ para ambientes persistentes.

H2 existe exclusivamente para desenvolvimento local e nao deve guiar decisoes de schema, queries ou comportamento produtivo.

A evolucao de banco deve preservar compatibilidade, rastreabilidade e previsibilidade.

## API

APIs devem seguir contratos consistentes.

Principios obrigatorios:

- resposta padrao;
- uso de DTOs;
- validacoes claras;
- exceptions padronizadas;
- versionamento quando houver quebra de contrato.

Controllers nao devem concentrar regra de negocio.

Contratos de API devem proteger a plataforma contra vazamento de detalhes internos e preservar compatibilidade com o frontend e integracoes futuras.

## Frontend

O frontend da Base+ usa React e Vite.

A interface deve respeitar:

- CSS Variables;
- Design Tokens;
- componentes compartilhados;
- acessibilidade;
- estados de loading, erro, vazio e sucesso.

Nunca utilizar cores fixas.

Estilos devem ser orientados pelos tokens da plataforma para preservar consistencia visual e personalizacao por branding.

## Infraestrutura

Infraestrutura faz parte da arquitetura da Base+.

Devem ser tratados como elementos estruturais:

- Docker;
- profiles;
- volumes;
- Flyway;
- healthcheck;
- bootstrap.

Ambientes devem ser reprodutiveis, configuraveis e previsiveis.

Dados persistentes, uploads, usuarios, auditoria e branding devem ser preservados em atualizacoes.

## Desenvolvimento Assistido por IA

A IA deve seguir estes principios.

Ela pode acelerar implementacao, diagnostico, revisao e documentacao, mas nao deve substituir decisoes arquiteturais.

Toda sugestao ou alteracao assistida por IA deve respeitar:

- simplicidade;
- modularidade;
- responsabilidade unica;
- seguranca;
- compatibilidade;
- padroes existentes da Base+.

A arquitetura continua sendo a referencia principal.

## Encerramento

Toda evolucao futura da Base+ devera respeitar estes principios.

Eles existem para manter a plataforma simples, reutilizavel, segura, evolutiva e sustentavel ao longo do tempo.
