# Objetivo

Conduzir a criacao de um novo modulo na Base+ ou em aplicacao derivada.

O playbook orienta a conversa sobre dominio, entidades, responsabilidades, arquitetura, reutilizacao, seguranca, banco, APIs, frontend, testes e documentacao.

# Quando utilizar

Use antes de criar qualquer modulo novo.

Nao utilize para corrigir bug ou refatorar modulo existente.

# Pre-requisitos

- `MODULE_TEMPLATE.md`
- `docs/module-development.md`
- `engineering/architecture/principles.md`
- `engineering/architecture/decisions.md`
- Regras de negocio iniciais
- Permissoes esperadas, quando conhecidas


## Classificacao arquitetural obrigatoria

Antes de discutir entidades, telas, rotas, menus ou permissoes, aplicar o principio:

```text
A plataforma administra a si mesma.
A aplicacao administra o negocio.
```

Checklist obrigatorio:

```text
1. Este modulo administra a plataforma Base+?

Se SIM -> Administracao.
Se NAO -> agrupamento funcional da aplicacao.

2. O modulo pertence ao dominio do cliente?

Se SIM -> nunca podera ser colocado em Administracao.
```

A Base+ nao define o nome do agrupamento funcional da aplicacao. A aplicacao pode usar Negocio, Comercial, Operacao, ERP, Producao, Gestao, Atendimento, Academico, Assistencial ou outro nome coerente.

E proibido criar modulos de dominio do cliente dentro de Administracao.

# Fluxo da Conversa

## 1. Classificacao do modulo

Objetivo: decidir se o modulo pertence a Plataforma ou a Aplicacao.

Resultado esperado: classificacao oficial do modulo e agrupamento funcional definido quando pertencer a Aplicacao.

Nao deve ser feito: criar modulo de dominio dentro de Administracao.

## 2. Dominio

Objetivo: entender o conceito de negocio ou administracao do modulo.

Resultado esperado: descricao do modulo em linguagem de dominio ou administracao.

Nao deve ser feito: definir classes ou tabelas.

## 3. Entidades e responsabilidades

Objetivo: identificar os principais objetos e responsabilidades.

Resultado esperado: lista conceitual de entidades e relacoes.

Nao deve ser feito: escrever migration.

## 4. Impacto arquitetural

Objetivo: verificar se o modulo cabe em `modules`, qual contexto funcional ocupa e se ha dependencias.

Resultado esperado: limites do modulo, agrupamento de navegacao e dependencias conhecidas.

Nao deve ser feito: mover logica para `core` ou Administracao sem justificativa arquitetural.

## 5. Seguranca e permissoes

Objetivo: definir operacoes protegidas e perfis envolvidos.

Resultado esperado: permissoes candidatas.

Nao deve ser feito: flexibilizar seguranca por conveniencia.

## 6. Banco e API

Objetivo: definir contratos conceituais e persistencia esperada.

Resultado esperado: visao inicial de dados e APIs.

Nao deve ser feito: criar DTOs finais antes de validar escopo.

## 7. Frontend e UX

Objetivo: decidir CRUD Compacto ou Completo, fluxo de telas e posicionamento na navegacao.

Resultado esperado: tipo de CRUD, experiencia esperada e agrupamento funcional.

Nao deve ser feito: desenhar CSS ou componentes detalhados.

## 8. Testes e documentacao

Objetivo: definir validacoes e documentacao necessaria.

Resultado esperado: checklist de testes e docs.

Nao deve ser feito: dispensar teste por ser modulo simples.


## 9. Revisao de UX

Objetivo: validar Manifesto Base+, Nielsen, consistencia, feedback, acessibilidade, formularios, tabelas e navegacao antes da geracao do prompt.

Resultado esperado: decisao de UX consolidada para o modulo.

Nao deve ser feito: gerar prompt de implementacao sem revisar a experiencia esperada.

# Perguntas Obrigatorias

- Este modulo administra a plataforma Base+?
- O modulo pertence ao dominio do cliente?
- Se pertence a Aplicacao, qual agrupamento funcional recebera o modulo?
- Qual responsabilidade unica do modulo?
- O modulo e CRUD Compacto ou CRUD Completo?
- Quais entidades fazem parte do dominio?
- Quais permissoes sao necessarias?
- Existe escopo organizacional?
- Ha integracao externa?
- Existe reutilizacao real com outro modulo?
- Quais fluxos precisam de testes?
- Qual documentacao deve ser atualizada?
- O modulo segue o Manifesto UX da Base+?
- A experiencia foi revisada antes do prompt?

# Criterios de Decisao

- Simplicidade.
- Separacao de responsabilidades.
- Baixo acoplamento.
- Compatibilidade PostgreSQL.
- Aderencia ao padrao Base+.
- Clareza do fluxo de usuario.
- Seguranca e auditoria.

# Criterios de Encerramento

A conversa esta concluida quando:

- classificacao Plataforma ou Aplicacao foi definida;
- agrupamento funcional foi definido quando o modulo pertencer a Aplicacao;
- tipo de CRUD foi escolhido;
- responsabilidades estao claras;
- permissoes foram definidas;
- impacto no banco e API foi compreendido;
- validacoes foram definidas.
- revisao de UX foi concluida.

# Resultado Esperado

Decisao consolidada para criacao do modulo e prompt de implementacao pronto para ser gerado.

# Proximo Playbook

`90-prompt-generation.md`

# Checklist Final

- [ ] Classificacao do modulo definida.
- [ ] Agrupamento funcional definido quando aplicavel.
- [ ] Dominio definido.
- [ ] Tipo de CRUD escolhido.
- [ ] Entidades conceituais mapeadas.
- [ ] Permissoes definidas.
- [ ] Impacto em banco entendido.
- [ ] API conceitual definida.
- [ ] Frontend conceitual definido.
- [ ] Testes planejados.
- [ ] Documentacao planejada.
- [ ] Revisao de UX concluida.
- [ ] Agora solicitar ao ChatGPT a geracao do Prompt para o Codex.
