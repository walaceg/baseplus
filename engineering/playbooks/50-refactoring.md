# Objetivo

Conduzir discussao de refatoracao antes de alterar codigo.

O playbook avalia problema atual, riscos, beneficios, impacto, compatibilidade, estrategia e validacao.

# Quando utilizar

Use quando uma parte do sistema precisa melhorar estrutura interna sem alterar comportamento externo.

Nao utilize para adicionar funcionalidade nova.

# Pre-requisitos

- Codigo ou fluxo afetado
- Problema observado
- Evidencias de complexidade ou duplicacao
- Testes existentes
- Principios arquiteturais da Base+

# Fluxo da Conversa

## 1. Problema atual

Objetivo: entender por que refatorar.

Resultado esperado: problema claro.

Nao deve ser feito: refatorar por preferencia estetica.

## 2. Riscos

Objetivo: mapear riscos da mudanca.

Resultado esperado: riscos e areas sensiveis.

Nao deve ser feito: ignorar compatibilidade.

## 3. Beneficios

Objetivo: validar ganho real.

Resultado esperado: beneficios mensuraveis ou justificaveis.

Nao deve ser feito: criar abstracao sem uso.

## 4. Impacto

Objetivo: identificar camadas afetadas.

Resultado esperado: escopo controlado.

Nao deve ser feito: ampliar para refatoracao geral.

## 5. Estrategia

Objetivo: escolher caminho incremental.

Resultado esperado: plano seguro.

Nao deve ser feito: fazer big bang sem necessidade.

## 6. Validacao

Objetivo: garantir comportamento preservado.

Resultado esperado: testes e checks.

Nao deve ser feito: depender apenas de build.

# Perguntas Obrigatorias

- Qual problema real a refatoracao resolve?
- O comportamento externo deve mudar?
- Quais riscos existem?
- Quais testes protegem a mudanca?
- A refatoracao reduz complexidade?
- Existe impacto em API, banco ou UX?
- A mudanca pode ser feita incrementalmente?
- Como validar ausencia de regressao?

# Criterios de Decisao

- Reducao de complexidade.
- Preservacao de comportamento.
- Baixo risco.
- Melhor manutencao.
- Testabilidade.
- Aderencia aos principios Base+.

# Criterios de Encerramento

A conversa termina quando:

- motivacao esta clara;
- escopo esta limitado;
- estrategia esta definida;
- validacao esta planejada.

# Resultado Esperado

Plano de refatoracao e prompt objetivo para execucao.

# Proximo Playbook

`90-prompt-generation.md`

# Checklist Final

- [ ] Problema atual descrito.
- [ ] Riscos identificados.
- [ ] Beneficios confirmados.
- [ ] Impacto mapeado.
- [ ] Compatibilidade avaliada.
- [ ] Estrategia definida.
- [ ] Validacao definida.
- [ ] Agora solicitar ao ChatGPT a geracao do Prompt para o Codex.
