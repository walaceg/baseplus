# Objetivo

Conduzir analise pos-incidente.

O playbook busca entender o que aconteceu, causa raiz, aprendizados e acoes para evitar recorrencia.

# Quando utilizar

Use apos incidente, regressao relevante, falha de deploy, perda de dados, indisponibilidade ou erro operacional significativo.

Nao utilize para atribuir culpa.

# Pre-requisitos

- Linha do tempo do incidente
- Logs e evidencias
- Ambiente afetado
- Versao afetada
- Impacto conhecido

# Fluxo da Conversa

## 1. O que aconteceu

Objetivo: registrar fatos.

Resultado esperado: descricao objetiva.

Nao deve ser feito: apontar culpados.

## 2. Impacto

Objetivo: entender usuarios, dados e operacao afetados.

Resultado esperado: impacto classificado.

Nao deve ser feito: minimizar sem evidencia.

## 3. Causa raiz

Objetivo: identificar causa principal.

Resultado esperado: causa sustentada por evidencias.

Nao deve ser feito: parar no primeiro sintoma.

## 4. Aprendizados

Objetivo: extrair conhecimento institucional.

Resultado esperado: aprendizados claros.

Nao deve ser feito: focar apenas no reparo tecnico.

## 5. Prevencao

Objetivo: definir acoes preventivas.

Resultado esperado: melhorias de processo, teste ou documentacao.

Nao deve ser feito: criar acoes vagas.

## 6. Atualizacoes institucionais

Objetivo: decidir se docs, ADRs ou playbooks mudam.

Resultado esperado: artefatos a atualizar.

Nao deve ser feito: perder aprendizado.

# Perguntas Obrigatorias

- O que aconteceu?
- Quando aconteceu?
- Qual foi o impacto?
- Qual causa raiz?
- O que aprendemos?
- Como evitar recorrencia?
- Devemos atualizar documentacao?
- Devemos criar um ADR?
- Devemos atualizar um Playbook?
- Quais validacoes faltaram?

# Criterios de Decisao

- Evidencias.
- Causa raiz real.
- Acoes preventivas verificaveis.
- Melhoria institucional.
- Clareza de responsabilidade.

# Criterios de Encerramento

A conversa termina quando:

- causa raiz foi definida;
- acoes corretivas foram listadas;
- acoes preventivas foram definidas;
- documentacao a atualizar foi identificada.

# Resultado Esperado

Relatorio pos-incidente com aprendizados e plano de acao.

# Proximo Playbook

`90-prompt-generation.md` quando houver acao de correcao.

# Checklist Final

- [ ] Fatos registrados.
- [ ] Impacto classificado.
- [ ] Causa raiz identificada.
- [ ] Aprendizados registrados.
- [ ] Acoes preventivas definidas.
- [ ] Necessidade de ADR avaliada.
- [ ] Necessidade de atualizar playbook avaliada.
- [ ] Agora solicitar ao ChatGPT a geracao do Prompt para o Codex.
