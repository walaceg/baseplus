# Objetivo

Conduzir a criacao de uma nova funcionalidade em modulo existente.

O playbook ajuda a avaliar necessidade, impacto, arquitetura, UX, integracao e testes antes da implementacao.

# Quando utilizar

Use quando o modulo ja existe e precisa receber uma capacidade nova.

Nao utilize para criar modulo novo ou tratar incidente.

# Pre-requisitos

- Contexto do modulo afetado
- Regra de negocio desejada
- Fluxo atual da funcionalidade
- `engineering/architecture/principles.md`
- Documentacao do modulo, se existir

# Fluxo da Conversa

## 1. Necessidade

Objetivo: entender por que a funcionalidade e necessaria.

Resultado esperado: problema e valor esperado.

Nao deve ser feito: partir direto para tela ou endpoint.

## 2. Impacto

Objetivo: identificar camadas e usuarios afetados.

Resultado esperado: mapa de impacto.

Nao deve ser feito: assumir que e mudanca local sem verificar.

## 3. Arquitetura

Objetivo: definir onde a funcionalidade deve viver.

Resultado esperado: decisao de escopo e limites.

Nao deve ser feito: mover responsabilidade para `core` sem evidencia.

## 4. UX

Objetivo: entender experiencia esperada.

Resultado esperado: fluxo de usuario e estados necessarios.

Nao deve ser feito: definir visual final prematuramente.

## 5. Integracao

Objetivo: identificar dependencias externas ou internas.

Resultado esperado: dependencias e riscos.

Nao deve ser feito: criar contrato sem validar necessidade.

## 6. Testes

Objetivo: definir cobertura minima.

Resultado esperado: lista de testes e validacoes.

Nao deve ser feito: deixar validacao apenas manual.

# Perguntas Obrigatorias

- Qual problema a funcionalidade resolve?
- Quem usa essa funcionalidade?
- Ela altera regra existente?
- Ela afeta seguranca, auditoria ou permissoes?
- Ela exige banco ou migration?
- Ela afeta frontend e backend?
- Quais estados de UX devem existir?
- Como validar sucesso?

# Criterios de Decisao

- Valor de negocio.
- Baixo risco.
- Compatibilidade.
- Clareza de responsabilidade.
- Testabilidade.
- Experiencia do usuario.
- Aderencia a Base+.

# Criterios de Encerramento

A conversa termina quando:

- necessidade foi validada;
- impacto foi entendido;
- escopo foi definido;
- riscos foram mapeados;
- validacoes foram planejadas.

# Resultado Esperado

Decisao sobre a funcionalidade e prompt de implementacao pronto para ser gerado.

# Proximo Playbook

`90-prompt-generation.md`

# Checklist Final

- [ ] Necessidade confirmada.
- [ ] Impacto mapeado.
- [ ] Escopo definido.
- [ ] UX discutida.
- [ ] Integracoes avaliadas.
- [ ] Testes definidos.
- [ ] Riscos registrados.
- [ ] Agora solicitar ao ChatGPT a geracao do Prompt para o Codex.
