# Playbooks - Base+

Playbooks sao guias para conduzir conversas de engenharia entre desenvolvedores e IA.

Eles nao sao prompts.

Um playbook ajuda a estruturar raciocinio, perguntas, decisoes, criterios e proximos passos antes da implementacao.

## Objetivo

A biblioteca de Playbooks da Base+ define um metodo oficial de Engenharia Conversacional.

Seu objetivo e melhorar a qualidade das decisoes tecnicas antes que qualquer codigo seja solicitado.

## Quando utilizar

Use playbooks quando a tarefa envolver:

- decisao arquitetural;
- criacao de modulo;
- diagnostico complexo;
- evolucao de plataforma;
- mudanca operacional;
- preparacao de release;
- definicao de padrao;
- revisao de processo.

## Playbook x Prompt

Playbook conduz decisoes.

Prompt implementa decisoes.

O playbook organiza a conversa para chegar a uma conclusao tecnica. O prompt transforma a decisao consolidada em uma tarefa objetiva para implementacao.

## Fluxo oficial

```text
Playbook
↓
Conversa
↓
Arquitetura
↓
Decisao
↓
Prompt
↓
Codex
↓
Validacao
↓
Commit
↓
Release
```

## Documentos

- `00-how-to-work.md`: guia oficial de utilizacao.
- `10-new-application.md`: conducao para criacao de nova aplicacao.
- `20-new-module.md`: conducao para criacao de novo modulo.
- `30-new-feature.md`: conducao para nova funcionalidade.
- `40-bug-analysis.md`: conducao para diagnostico de bug.
- `50-refactoring.md`: conducao para refatoracao.
- `60-architecture-review.md`: conducao para revisao arquitetural.
- `70-release.md`: conducao para preparacao de release.
- `80-postmortem.md`: conducao para analise pos-incidente.
- `90-prompt-generation.md`: transformacao de decisao em prompt.
- `100-ai-collaboration-method.md`: metodo oficial de colaboracao entre humanos, IA e Codex.
- `99-playbook-template.md`: modelo padrao para novos playbooks.

## Principio

Uma boa implementacao depende de uma boa decisao.

Playbooks existem para reduzir improviso, evitar prompts prematuros e preservar coerencia com a arquitetura Base+.
