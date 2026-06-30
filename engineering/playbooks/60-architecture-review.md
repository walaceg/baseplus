# Objetivo

Conduzir revisao arquitetural de uma funcionalidade, modulo, decisao ou versao da Base+.

O playbook verifica simplicidade, modularidade, reutilizacao, acoplamento, aderencia aos principios e riscos de overengineering.

# Quando utilizar

Use antes de consolidar uma decisao arquitetural, promover algo para `core`, fechar release ou revisar modulo relevante.

Nao utilize para revisar estilo de codigo isolado.

# Pre-requisitos

- `engineering/architecture/principles.md`
- `engineering/architecture/decisions.md`
- Escopo da revisao
- Contexto da mudanca
- Evidencias ou artefatos existentes

# Fluxo da Conversa

## 1. Escopo

Objetivo: delimitar o que sera revisado.

Resultado esperado: fronteira clara.

Nao deve ser feito: revisar a plataforma inteira sem necessidade.

## 2. Simplicidade

Objetivo: avaliar se a solucao esta simples.

Resultado esperado: pontos de complexidade.

Nao deve ser feito: confundir simplicidade com ausencia de estrutura.

## 3. Modularidade

Objetivo: avaliar limites de responsabilidade.

Resultado esperado: conformidade com `modules`, `shared` e `core`.

Nao deve ser feito: promover para `core` por conveniencia.

## 4. Reutilizacao

Objetivo: verificar reutilizacao real.

Resultado esperado: decisao sobre manter local ou extrair.

Nao deve ser feito: abstrair sem duas aplicacoes ou usos comprovados.

## 5. Riscos

Objetivo: identificar riscos arquiteturais.

Resultado esperado: lista priorizada.

Nao deve ser feito: ignorar operacao, seguranca ou banco.

## 6. Parecer

Objetivo: consolidar avaliacao.

Resultado esperado: aprovado, aprovado com ressalvas ou nao aprovado.

Nao deve ser feito: emitir parecer sem evidencias.

# Perguntas Obrigatorias

- Esta simples?
- Esta modular?
- Esta reutilizavel?
- Deve permanecer em `modules`?
- Deve evoluir para `core`?
- Existe overengineering?
- Ha acoplamento indevido?
- Esta alinhado aos principios da Base+?
- Ha impacto em seguranca, banco ou operacao?

# Criterios de Decisao

- Simplicidade.
- Modularidade.
- Baixo acoplamento.
- Responsabilidade unica.
- Reutilizacao comprovada.
- Estabilidade.
- Aderencia aos ADRs.

# Criterios de Encerramento

A conversa termina quando:

- parecer foi emitido;
- ressalvas foram classificadas;
- recomendacoes foram definidas;
- proximos passos foram indicados.

# Resultado Esperado

Relatorio de revisao arquitetural com conformidades, riscos, ressalvas e recomendacoes.

# Proximo Playbook

`90-prompt-generation.md` quando houver acao de implementacao.

# Checklist Final

- [ ] Escopo definido.
- [ ] Simplicidade avaliada.
- [ ] Modularidade avaliada.
- [ ] Reutilizacao avaliada.
- [ ] Regra do core aplicada.
- [ ] Riscos identificados.
- [ ] Parecer emitido.
- [ ] Proximos passos definidos.
- [ ] Agora solicitar ao ChatGPT a geracao do Prompt para o Codex.
