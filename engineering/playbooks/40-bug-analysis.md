# Objetivo

Conduzir diagnostico estruturado de bug antes da correcao.

O playbook organiza sintoma, reproducao, ambiente, hipoteses, arquivos suspeitos, diagnostico, correcao proposta, validacao e prompt.

# Quando utilizar

Use quando houver comportamento incorreto, regressao, falha de build, erro operacional ou divergencia entre esperado e observado.

Nao utilize para implementar melhoria sem bug confirmado.

# Pre-requisitos

- Descricao do sintoma
- Ambiente afetado
- Passos para reproduzir
- Logs, prints ou mensagens de erro
- Versao/tag afetada

# Fluxo da Conversa

## 1. Sintoma

Objetivo: descrever o problema observado.

Resultado esperado: sintoma claro e verificavel.

Nao deve ser feito: propor correcao antes de entender.

## 2. Como reproduzir

Objetivo: definir passos objetivos.

Resultado esperado: fluxo reproduzivel.

Nao deve ser feito: aceitar descricao vaga.

## 3. Ambiente

Objetivo: identificar onde ocorre.

Resultado esperado: dev, HOM, PRD, Docker, navegador, banco e versao.

Nao deve ser feito: assumir que todos ambientes falham.

## 4. Hipoteses

Objetivo: levantar causas provaveis.

Resultado esperado: lista priorizada de hipoteses.

Nao deve ser feito: corrigir sem evidencias.

## 5. Arquivos suspeitos

Objetivo: mapear areas a inspecionar.

Resultado esperado: arquivos e fluxos candidatos.

Nao deve ser feito: alterar arquivos.

## 6. Diagnostico

Objetivo: confirmar causa raiz provavel.

Resultado esperado: evidencia tecnica.

Nao deve ser feito: confundir sintoma com causa.

## 7. Correcao proposta

Objetivo: definir abordagem segura.

Resultado esperado: plano de correcao.

Nao deve ser feito: ampliar escopo.

## 8. Validacao

Objetivo: definir testes e checks.

Resultado esperado: validacao objetiva.

Nao deve ser feito: depender apenas de percepcao visual.

## 9. Prompt

Objetivo: transformar diagnostico em tarefa.

Resultado esperado: prompt objetivo para implementacao.

Nao deve ser feito: incluir hipoteses nao confirmadas como fatos.

# Perguntas Obrigatorias

- Qual sintoma exato?
- Quando comecou?
- Qual ambiente afetado?
- Como reproduzir?
- Existe erro em log ou console?
- O problema e backend, frontend, infraestrutura ou dado?
- Quais arquivos participam do fluxo?
- Qual evidencia confirma a causa?
- Como validar a correcao?

# Criterios de Decisao

- Evidencia concreta.
- Reproducibilidade.
- Menor mudanca segura.
- Ausencia de regressao.
- Compatibilidade com arquitetura Base+.

# Criterios de Encerramento

A conversa termina quando:

- causa provavel esta sustentada por evidencias;
- correcao recomendada foi definida;
- validacao foi planejada;
- prompt pode ser gerado.

# Resultado Esperado

Relatorio de diagnostico e prompt de correcao.

# Proximo Playbook

`90-prompt-generation.md`

# Checklist Final

- [ ] Sintoma descrito.
- [ ] Reproducao definida.
- [ ] Ambiente identificado.
- [ ] Hipoteses listadas.
- [ ] Arquivos suspeitos mapeados.
- [ ] Diagnostico consolidado.
- [ ] Correcao proposta.
- [ ] Validacao definida.
- [ ] Agora solicitar ao ChatGPT a geracao do Prompt para o Codex.
