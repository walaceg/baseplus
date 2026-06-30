# Runbook de Rollback - Base+

## Quando utilizar

Use rollback quando uma atualizacao causar falha critica, indisponibilidade, perda de compatibilidade ou comportamento incorreto que nao possa ser corrigido rapidamente.

## Fluxo sugerido

```text
Identificar problema
↓
Interromper atualizacao
↓
Restaurar banco
↓
Restaurar uploads
↓
Executar versao anterior
↓
Validar
```

## Etapas

1. Registrar a falha observada.
2. Interromper novas alteracoes.
3. Confirmar a ultima versao estavel.
4. Restaurar banco quando a atualizacao alterou dados ou schema.
5. Restaurar uploads quando houver risco de divergencia.
6. Executar a versao anterior.
7. Validar health, readiness, login, branding e uploads.

## Observacoes

Rollback simples de aplicacao pode ser suficiente quando nao houve migration nem alteracao de dados.

Quando houve migration irreversivel ou alteracao persistente, rollback seguro exige restore do backup pre-atualizacao.

Consulte `docs/update.md` para orientacao operacional complementar.
