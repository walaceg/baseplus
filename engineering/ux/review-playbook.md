# Playbook de Revisao de UX

## Objetivo

Conduzir uma revisao estruturada de UX antes de implementar, homologar ou publicar uma mudanca.

## Quando utilizar

Use este playbook para:

- novas telas;
- novos modulos;
- alteracoes em fluxos existentes;
- ajustes de navegacao;
- revisoes antes de release;
- diagnostico de problemas de usabilidade.

## Etapas

### 1. Contexto

Entender quem usa, para que usa e em qual momento do trabalho.

Nao deve ser feito: revisar apenas a aparencia.

### 2. Objetivo da Tela

Confirmar se a tela possui um objetivo principal claro.

Nao deve ser feito: misturar muitas responsabilidades sem necessidade.

### 3. Fluxo

Validar entrada, acao, resultado, erro e retorno.

Nao deve ser feito: analisar apenas o estado feliz.

### 4. Componentes

Verificar se os componentes seguem padroes da Base+.

Nao deve ser feito: criar variacoes visuais sem justificativa.

### 5. Feedback

Conferir carregamento, sucesso, erro, vazio e restricao.

Nao deve ser feito: deixar operacoes silenciosas.

### 6. Acessibilidade

Revisar contraste, foco, teclado, labels e textos alternativos.

Nao deve ser feito: tratar acessibilidade como etapa opcional.

### 7. Responsividade

Validar se a interface permanece utilizavel em larguras menores.

Nao deve ser feito: aceitar sobreposicao ou perda de acao essencial.

### 8. Encerramento

Registrar ajustes obrigatorios, recomendados e opcionais.

Nao deve ser feito: encerrar sem criterio de validacao.

## Perguntas Obrigatorias

- Qual tarefa o usuario precisa concluir?
- O fluxo possui uma acao principal evidente?
- Existem estados de carregamento, erro e vazio?
- A interface segue os tokens e componentes da Base+?
- Existe algum texto ambiguo?
- A tela funciona sem depender apenas de cor?
- O comportamento e consistente com outros modulos?

## Resultado Esperado

Ao final da revisao, deve existir uma lista objetiva de ajustes, riscos e validacoes necessarias.

## Estrutura oficial

### Objetivo

Conduzir uma revisao de UX antes de gerar prompt, implementar, homologar ou publicar uma funcionalidade.

### Quando utilizar

Use para novas telas, novos modulos, novas funcionalidades, alteracoes em formularios, tabelas, navegacao e releases.

### Pre-requisitos

- `engineering/ux/ux-manifesto.md`
- `engineering/ux/nielsen.md`
- `engineering/ux/quality-checklist.md`
- Fluxo ou tela a ser avaliado
- Objetivo da funcionalidade

### Fluxo da conversa

1. Contexto.
2. Compreensao imediata.
3. Carga cognitiva.
4. Consistencia.
5. Feedback.
6. Acessibilidade.
7. Decisao.

### Perguntas obrigatorias

- O usuario entende imediatamente esta tela?
- Existe excesso de informacao?
- Existe excesso de botoes?
- Existe excesso de cores?
- Existe excesso de texto?
- Existe carga cognitiva desnecessaria?
- Existe consistencia?
- Segue Nielsen?
- Segue Manifesto Base+?
- Transmite elegancia?
- Transmite confianca?
- Os feedbacks sao suficientes?
- A interface respira?
- O usuario sabe sempre onde esta?
- Os erros ajudam o usuario?
- As mensagens utilizam linguagem humana?
- Existe oportunidade para simplificar?

### Criterios de decisao

- Clareza.
- Simplicidade.
- Elegancia.
- Consistencia.
- Acessibilidade.
- Feedback suficiente.
- Baixa carga cognitiva.
- Aderencia aos tokens.

### Criterios de encerramento

A conversa termina quando riscos de UX foram identificados, ajustes obrigatorios foram definidos e a decisao pode virar prompt.

### Checklist

- [ ] Manifesto Base+ considerado.
- [ ] Nielsen considerado.
- [ ] Consistencia validada.
- [ ] Feedback validado.
- [ ] Acessibilidade validada.
- [ ] Simplificacao avaliada.
- [ ] Decisao consolidada.
- [ ] Agora solicite ao ChatGPT a geracao do Prompt para o Codex.
