# Objetivo

Conduzir a criacao de uma nova aplicacao baseada na Base+.

Este playbook ajuda a definir dominio, objetivos, publico, modulos iniciais, branding, seguranca, integracoes e roadmap inicial antes de qualquer implementacao.

# Quando utilizar

Use quando uma equipe deseja iniciar um novo sistema corporativo usando a Base+ como fundacao.

Nao utilize para implementar modulos diretamente. A conversa deve consolidar direcao, escopo e limites da nova aplicacao.

# Pre-requisitos

- `VISION.md`
- `docs/new-application.md`
- `engineering/architecture/principles.md`
- `engineering/architecture/decisions.md`
- `engineering/release/release-process.md`
- Contexto do negocio
- Publico-alvo
- Restricoes operacionais conhecidas

# Fluxo da Conversa

## 1. Contexto do negocio

Objetivo: entender o dominio e o problema principal.

Resultado esperado: descricao clara do sistema e do valor esperado.

Nao deve ser feito: discutir telas, entidades ou codigo.

## 2. Objetivos da aplicacao

Objetivo: definir resultados esperados e prioridades.

Resultado esperado: lista de objetivos de curto e medio prazo.

Nao deve ser feito: transformar objetivos em backlog tecnico prematuro.

## 3. Publico e operacao

Objetivo: identificar usuarios, perfis, ambientes e responsabilidades operacionais.

Resultado esperado: visao inicial de uso e operacao.

Nao deve ser feito: detalhar permissoes tecnicas ainda.

## 4. Modulos iniciais

Objetivo: identificar os primeiros modulos de negocio.

Resultado esperado: lista priorizada de modulos candidatos.

Nao deve ser feito: implementar ou definir schema.

## 5. Branding e identidade

Objetivo: definir nivel de personalizacao visual esperado.

Resultado esperado: direcao inicial de nome, marca, cores e white label.

Nao deve ser feito: alterar assets ou CSS.

## 6. Seguranca e integracoes

Objetivo: identificar requisitos de acesso, escopo organizacional e integracoes.

Resultado esperado: riscos e dependencias iniciais.

Nao deve ser feito: definir endpoints ou protocolo final sem diagnostico.

## 7. Roadmap inicial

Objetivo: organizar fases de entrega.

Resultado esperado: roadmap inicial com ordem de modulos e validacoes.

Nao deve ser feito: prometer datas sem base operacional.

# Perguntas Obrigatorias

- Qual problema de negocio a nova aplicacao resolve?
- Quem usara a aplicacao?
- Quais modulos sao essenciais para a primeira entrega?
- Quais dados ou processos devem ser preservados desde o inicio?
- Havera integracoes externas?
- Ha requisitos de segregacao por empresa, filial, equipe ou outro escopo?
- A aplicacao tera identidade propria ou manterá marca Base+?
- Qual ambiente sera usado para homologacao?
- Qual criterio define sucesso da primeira versao?

# Criterios de Decisao

- Aderencia ao proposito da Base+.
- Simplicidade.
- Baixo acoplamento.
- Viabilidade operacional.
- Clareza do dominio.
- Priorizacao por valor de negocio.
- Capacidade de evolucao incremental.

# Criterios de Encerramento

A conversa esta concluida quando:

- objetivo da aplicacao esta claro;
- modulos iniciais foram priorizados;
- riscos principais foram identificados;
- decisoes de branding, seguranca e integracao foram registradas;
- proximo passo esta definido.

# Resultado Esperado

Documento de decisao inicial contendo visao da aplicacao, modulos, riscos, roadmap e criterios para iniciar implementacao.

# Proximo Playbook

`20-new-module.md`

# Checklist Final

- [ ] Dominio entendido.
- [ ] Objetivos definidos.
- [ ] Publico identificado.
- [ ] Modulos iniciais listados.
- [ ] Branding discutido.
- [ ] Seguranca discutida.
- [ ] Integracoes mapeadas.
- [ ] Roadmap inicial consolidado.
- [ ] Agora solicitar ao ChatGPT a geracao do Prompt para o Codex.
