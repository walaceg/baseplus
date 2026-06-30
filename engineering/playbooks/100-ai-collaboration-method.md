# Metodo Oficial de Colaboracao entre Humanos, IA e Codex

## Objetivo

A Base+ adota oficialmente um processo de Engenharia Conversacional para orientar a colaboracao entre pessoas, Inteligencia Artificial e agentes de implementacao.

O foco deste metodo nao e gerar codigo rapidamente.

O foco e tomar boas decisoes arquiteturais antes da implementacao.

Codigo e consequencia da decisao.

Este documento nao depende de uma ferramenta especifica. Ele define um processo reproduzivel que pode ser utilizado com qualquer IA atual ou futura, desde que respeite os principios de engenharia da Base+.

## Principios

- Conversar antes de implementar.
- Compreender antes de decidir.
- Decidir antes de gerar prompts.
- Validar antes de realizar commit.
- Documentar antes da release.
- Arquitetura possui prioridade sobre velocidade.
- IA acelera desenvolvimento, mas nao substitui pensamento arquitetural.

## Papeis

### Humano

O humano e responsavel por:

- visao do produto;
- conhecimento do negocio;
- definicao de prioridades;
- escolhas finais;
- validacao funcional;
- validacao de aderencia ao contexto real.

O humano decide o que deve existir, por que deve existir e quando uma solucao atende ao objetivo.

### IA

A IA e responsavel por:

- apoiar analises;
- levantar alternativas;
- identificar riscos;
- organizar perguntas;
- estruturar decisoes;
- revisar arquitetura;
- gerar prompts para implementacao.

A IA amplia a capacidade de raciocinio e organizacao, mas nao substitui a responsabilidade arquitetural da plataforma.

### Codex

Codex e responsavel por:

- implementar tarefas especificas;
- alterar codigo quando o escopo estiver definido;
- executar atividades delimitadas;
- rodar validacoes;
- informar arquivos alterados;
- manter responsabilidade unica por tarefa.

Codex deve receber prompts claros, com escopo objetivo, criterios de validacao e restricoes explicitas.

## Fluxo Oficial

```text
Ideia
  |
  v
Escolha do Playbook
  |
  v
Conversa
  |
  v
Refinamento
  |
  v
Arquitetura
  |
  v
Decisao
  |
  v
Prompt
  |
  v
Codex
  |
  v
Validacao
  |
  v
Homologacao
  |
  v
Commit
  |
  v
Release
```

### Ideia

Toda evolucao comeca por uma necessidade, oportunidade, problema ou risco identificado.

Nesta etapa, nao se deve pedir implementacao. O objetivo e declarar a intencao e o contexto.

### Escolha do Playbook

Seleciona-se o playbook mais adequado ao tipo de trabalho.

O playbook orienta a conversa para evitar improviso, perguntas soltas ou prompts prematuros.

### Conversa

A conversa busca compreender o problema, o dominio, os limites, os riscos e as alternativas.

Nesta etapa, a IA deve ajudar a organizar o pensamento, nao gerar codigo.

### Refinamento

As ideias iniciais sao reduzidas a uma proposta mais objetiva.

Decisoes vagas devem ser transformadas em criterios verificaveis.

### Arquitetura

A solucao e avaliada contra os principios da Base+:

- simplicidade;
- modularidade;
- baixo acoplamento;
- responsabilidade unica;
- seguranca;
- persistencia;
- operacao;
- documentacao.

### Decisao

A decisao deve deixar claro:

- o que sera feito;
- o que nao sera feito;
- onde a solucao deve ficar;
- quais arquivos ou camadas podem ser afetados;
- quais validacoes serao exigidas.

### Prompt

O prompt transforma a decisao consolidada em uma tarefa objetiva para o Codex.

Prompts nao substituem analise. Eles executam uma decisao ja tomada.

### Codex

Codex implementa a tarefa delimitada, respeitando escopo, restricoes, padroes do projeto e validacoes definidas.

### Validacao

Antes de commit, a alteracao deve ser validada com testes, builds, checagens de formatacao, revisao de diff ou verificacoes manuais quando aplicavel.

### Homologacao

Quando a mudanca afeta comportamento, ambiente, operacao ou experiencia de usuario, ela deve ser homologada no fluxo adequado.

### Commit

O commit deve ocorrer somente depois da validacao.

O historico deve refletir uma unidade coerente de mudanca.

### Release

Mudancas relevantes devem ser documentadas, consolidadas e publicadas conforme o processo oficial de release da Base+.

## Playbooks

Cada tipo de trabalho possui um playbook especifico.

Exemplos:

- nova aplicacao;
- novo modulo;
- nova funcionalidade;
- diagnostico;
- refatoracao;
- revisao arquitetural;
- release;
- postmortem;
- geracao de prompt.

Playbooks conduzem decisoes.

Prompts implementam decisoes.

## Prompts

Prompts representam somente a implementacao de uma decisao.

Eles nao devem ser usados para descobrir arquitetura durante a execucao.

Todo prompt deve possuir:

- responsabilidade unica;
- escopo limitado;
- contexto suficiente;
- restricoes claras;
- criterios de validacao;
- indicacao do que nao deve ser alterado;
- resultado esperado.

Problemas grandes devem ser divididos em prompts menores.

## Boas Praticas

- Discutir arquitetura primeiro.
- Evitar implementar durante a analise.
- Dividir problemas grandes.
- Validar decisoes antes da execucao.
- Registrar decisoes importantes em ADR quando necessario.
- Atualizar documentacao quando a plataforma mudar.
- Separar diagnostico de correcao.
- Separar backend, frontend, infraestrutura e documentacao quando o risco justificar.
- Evitar prompts amplos demais.
- Encerrar a conversa com criterios objetivos.

## Integracao com Engenharia

O metodo de colaboracao da Base+ integra os principais artefatos de engenharia da plataforma:

- `VISION.md`: define proposito e direcao.
- `engineering/architecture/principles.md`: define os principios permanentes.
- `engineering/architecture/decisions.md`: registra decisoes arquiteturais.
- Quality Gates: validam qualidade antes de commit e release.
- Runbooks: orientam operacao.
- Playbooks: conduzem conversas e decisoes.
- Processo de Release: consolida, valida e publica versoes.

Esses artefatos fazem parte de um unico processo de engenharia.

## Beneficios

O metodo oficial de colaboracao gera os seguintes beneficios:

- maior consistencia entre decisoes e implementacoes;
- menor retrabalho;
- decisoes documentadas;
- prompts menores;
- implementacoes mais previsiveis;
- onboarding facilitado;
- melhor rastreabilidade;
- maior independencia da ferramenta de IA utilizada;
- preservacao do conhecimento arquitetural.

## Evolucao

Este documento deve evoluir continuamente conforme a plataforma amadurecer.

Novos playbooks poderao ser adicionados.

Novas ferramentas poderao ser utilizadas.

O metodo deve permanecer valido independentemente da IA utilizada, desde que preserve a filosofia da Base+:

arquitetura antes da implementacao, decisao antes do prompt e validacao antes do commit.
