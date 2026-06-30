# Como Trabalhar com Playbooks

Este documento define o uso oficial dos Playbooks da Base+.

## Passo a passo

## 1. Abrir uma nova conversa

Abra uma nova conversa para o tema que sera tratado.

Evite misturar assuntos diferentes na mesma conversa.

## 2. Anexar o Playbook correspondente

Anexe ou referencie o playbook mais adequado ao tema.

Se nao existir playbook especifico, use `99-playbook-template.md` para criar um novo.

## 3. Explicar o objetivo

Explique claramente:

- qual problema precisa ser resolvido;
- qual resultado e esperado;
- qual contexto ja existe;
- quais restricoes devem ser respeitadas.

## 4. Conduzir todas as etapas

Siga o fluxo do playbook.

Nao pule perguntas obrigatorias sem justificativa.

## 5. Nao solicitar codigo nas primeiras etapas

As primeiras etapas devem focar entendimento, contexto, alternativas, riscos e criterios.

Codigo antes da decisao aumenta risco de retrabalho e divergencia arquitetural.

## 6. Consolidar a arquitetura

Antes de pedir implementacao, consolide:

- decisao tecnica;
- limites de escopo;
- arquivos ou camadas afetadas;
- validacoes necessarias;
- riscos conhecidos.

## 7. Solicitar geracao do Prompt

Depois da decisao, solicite um prompt objetivo para implementacao.

O prompt deve conter:

- projeto;
- contexto;
- tarefa;
- escopo;
- regras;
- validacao;
- restricoes.

## 8. Validar

Revise o prompt antes de executar.

Confirme se ele respeita:

- arquitetura Base+;
- principios arquiteturais;
- ADRs;
- padroes de modulo;
- operacao e seguranca.

## 9. Implementar

Use o prompt validado para implementacao.

A implementacao deve listar arquivos alterados, comandos executados e pendencias.

## 10. Homologar

Valide em ambiente adequado.

Quando aplicavel, execute:

- testes backend;
- build frontend;
- Docker;
- health;
- readiness;
- login;
- uploads;
- auditoria;
- fluxo principal da funcionalidade.

## Encerramento

Uma conversa conduzida por playbook deve terminar com decisao clara, prompt pronto ou justificativa para nao implementar.
