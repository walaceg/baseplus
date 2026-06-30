# Objetivo

Conduzir preparacao de release da Base+ ou de aplicacao derivada.

O playbook organiza mudancas, documentacao, testes, homologacao, quality gates, tag e publicacao.

# Quando utilizar

Use quando uma versao candidata precisa ser consolidada, validada e publicada.

Nao utilize para desenvolver funcionalidades ainda abertas.

# Pre-requisitos

- `engineering/release/release-process.md`
- `CHANGELOG.md`
- Checklist da release, quando existir
- Lista de mudancas
- Status de testes
- Status de homologacao

# Fluxo da Conversa

## 1. Mudancas

Objetivo: entender escopo da versao.

Resultado esperado: lista consolidada de mudancas.

Nao deve ser feito: adicionar novo escopo sem justificativa.

## 2. Documentacao

Objetivo: verificar docs afetadas.

Resultado esperado: documentacao necessaria.

Nao deve ser feito: publicar sem historico.

## 3. Testes

Objetivo: definir quality gates.

Resultado esperado: lista de validacoes.

Nao deve ser feito: aceitar release sem teste minimo.

## 4. Homologacao

Objetivo: validar em ambiente proximo de PRD.

Resultado esperado: evidencias de HOM.

Nao deve ser feito: confundir DEV com HOM.

## 5. Tag e publicacao

Objetivo: definir versao e congelamento.

Resultado esperado: decisao de tag.

Nao deve ser feito: alterar versao congelada.

# Perguntas Obrigatorias

- Qual versao candidata?
- Quais mudancas entraram?
- Ha breaking changes?
- Documentacao esta atualizada?
- Docker foi validado?
- Banco e migrations foram validados?
- Bootstrap foi validado quando aplicavel?
- Uploads e branding foram validados?
- Existe erro critico conhecido?
- Rollback esta documentado?

# Criterios de Decisao

- Estabilidade.
- Rastreabilidade.
- Testes executados.
- Documentacao atualizada.
- Homologacao aprovada.
- Sem criticos conhecidos.
- Rollback possivel.

# Criterios de Encerramento

A conversa termina quando:

- release foi aprovada ou rejeitada;
- pendencias foram listadas;
- tag foi definida quando aplicavel;
- proximos passos foram claros.

# Resultado Esperado

Parecer de release e prompt para ajustes finais, commit, tag ou publicacao.

# Proximo Playbook

`90-prompt-generation.md` ou `80-postmortem.md` se houver incidente.

# Checklist Final

- [ ] Mudancas consolidadas.
- [ ] Documentacao revisada.
- [ ] Testes definidos.
- [ ] Homologacao considerada.
- [ ] Quality gates definidos.
- [ ] Tag definida.
- [ ] Rollback considerado.
- [ ] Parecer emitido.
- [ ] Agora solicitar ao ChatGPT a geracao do Prompt para o Codex.
