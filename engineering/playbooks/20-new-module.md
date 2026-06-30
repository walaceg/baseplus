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

# Fluxo da Conversa

## 1. Dominio

Objetivo: entender o conceito de negocio do modulo.

Resultado esperado: descricao do modulo em linguagem de dominio.

Nao deve ser feito: definir classes ou tabelas.

## 2. Entidades e responsabilidades

Objetivo: identificar os principais objetos e responsabilidades.

Resultado esperado: lista conceitual de entidades e relacoes.

Nao deve ser feito: escrever migration.

## 3. Impacto arquitetural

Objetivo: verificar se o modulo cabe em `modules` e se ha dependencias.

Resultado esperado: limites do modulo e dependencias conhecidas.

Nao deve ser feito: mover logica para `core`.

## 4. Seguranca e permissoes

Objetivo: definir operacoes protegidas e perfis envolvidos.

Resultado esperado: permissoes candidatas.

Nao deve ser feito: flexibilizar seguranca por conveniencia.

## 5. Banco e API

Objetivo: definir contratos conceituais e persistencia esperada.

Resultado esperado: visao inicial de dados e APIs.

Nao deve ser feito: criar DTOs finais antes de validar escopo.

## 6. Frontend e UX

Objetivo: decidir CRUD Compacto ou Completo e fluxo de telas.

Resultado esperado: tipo de CRUD e experiencia esperada.

Nao deve ser feito: desenhar CSS ou componentes detalhados.

## 7. Testes e documentacao

Objetivo: definir validacoes e documentacao necessaria.

Resultado esperado: checklist de testes e docs.

Nao deve ser feito: dispensar teste por ser modulo simples.

# Perguntas Obrigatorias

- Qual responsabilidade unica do modulo?
- O modulo e CRUD Compacto ou CRUD Completo?
- Quais entidades fazem parte do dominio?
- Quais permissoes sao necessarias?
- Existe escopo organizacional?
- Ha integracao externa?
- Existe reutilizacao real com outro modulo?
- Quais fluxos precisam de testes?
- Qual documentacao deve ser atualizada?

# Criterios de Decisao

- Simplicidade.
- Separacao de responsabilidades.
- Baixo acoplamento.
- Compatibilidade PostgreSQL.
- Aderencia ao padrao Base+.
- Clareza do fluxo de usuario.
- Segurança e auditoria.

# Criterios de Encerramento

A conversa esta concluida quando:

- tipo de CRUD foi escolhido;
- responsabilidades estao claras;
- permissoes foram definidas;
- impacto no banco e API foi compreendido;
- validacoes foram definidas.

# Resultado Esperado

Decisao consolidada para criacao do modulo e prompt de implementacao pronto para ser gerado.

# Proximo Playbook

`90-prompt-generation.md`

# Checklist Final

- [ ] Dominio definido.
- [ ] Tipo de CRUD escolhido.
- [ ] Entidades conceituais mapeadas.
- [ ] Permissoes definidas.
- [ ] Impacto em banco entendido.
- [ ] API conceitual definida.
- [ ] Frontend conceitual definido.
- [ ] Testes planejados.
- [ ] Documentacao planejada.
- [ ] Agora solicitar ao ChatGPT a geracao do Prompt para o Codex.
