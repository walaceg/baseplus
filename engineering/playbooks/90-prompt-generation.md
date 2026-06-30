# Objetivo

Transformar uma decisao consolidada em um prompt objetivo para implementacao.

Este playbook define o momento correto para gerar prompts, escopo, responsabilidade unica, separacao backend/frontend, validacao, risco, tempo e consumo estimado de tokens.

# Quando utilizar

Use somente depois que a conversa de engenharia consolidou decisao, escopo e validacoes.

Nao utilize para pensar a arquitetura. Use para converter decisao em tarefa executavel.

# Pre-requisitos

- Decisao consolidada
- Escopo definido
- Arquivos ou camadas afetadas
- Restricoes conhecidas
- Validacoes esperadas
- Riscos identificados

# Fluxo da Conversa

## 1. Confirmar decisao

Objetivo: garantir que nao ha duvida arquitetural aberta.

Resultado esperado: decisao final resumida.

Nao deve ser feito: rediscutir tudo.

## 2. Definir responsabilidade unica

Objetivo: limitar o prompt a uma tarefa.

Resultado esperado: objetivo claro e isolado.

Nao deve ser feito: misturar diagnostico, backend, frontend e release sem necessidade.

## 3. Separar backend e frontend

Objetivo: decidir se prompts devem ser separados.

Resultado esperado: um ou mais prompts coerentes.

Nao deve ser feito: criar prompt grande demais por conveniencia.

## 4. Definir escopo

Objetivo: listar o que entra e o que nao entra.

Resultado esperado: escopo implementavel.

Nao deve ser feito: deixar margem para alteracoes amplas.

## 5. Definir validacao

Objetivo: estabelecer como provar que funcionou.

Resultado esperado: comandos e checks.

Nao deve ser feito: pedir implementacao sem validacao.

## 6. Estimar risco, tempo e tokens

Objetivo: entender custo e impacto da tarefa.

Resultado esperado: classificacao simples.

Nao deve ser feito: tratar tarefa grande como pequena.

## 7. Gerar prompt

Objetivo: produzir prompt final.

Resultado esperado: texto pronto para execucao.

Nao deve ser feito: incluir ambiguidade desnecessaria.

# Perguntas Obrigatorias

- Qual decisao sera implementada?
- O prompt possui uma unica responsabilidade?
- Backend e frontend devem ser separados?
- Quais arquivos ou camadas podem ser alterados?
- Quais arquivos nao devem ser alterados?
- Qual validacao e obrigatoria?
- Existe risco em banco, seguranca, Docker ou UX?
- Qual estimativa de tempo?
- Qual estimativa de risco?
- Qual estimativa de consumo de tokens?

# Criterios de Decisao

- Clareza.
- Escopo controlado.
- Responsabilidade unica.
- Validacao objetiva.
- Baixo risco.
- Aderencia a Base+.

# Criterios de Encerramento

A conversa termina quando:

- prompt esta pronto;
- restricoes estao claras;
- validacoes estao definidas;
- riscos foram comunicados.

# Resultado Esperado

Prompt final para execucao pelo Codex ou ferramenta equivalente.

# Proximo Playbook

`70-release.md` quando a implementacao fizer parte de uma release, ou `40-bug-analysis.md` se a validacao revelar falha.

# Checklist Final

- [ ] Decisao confirmada.
- [ ] Responsabilidade unica definida.
- [ ] Backend/frontend separados quando necessario.
- [ ] Escopo definido.
- [ ] Restricoes definidas.
- [ ] Validacao definida.
- [ ] Risco estimado.
- [ ] Tempo estimado.
- [ ] Tokens estimados.
- [ ] Agora solicitar ao ChatGPT a geracao do Prompt para o Codex.
