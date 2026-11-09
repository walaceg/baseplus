# Transicao de JWT

Tokens novos usam HS256 e incluem issuer (`iss`) e audience (`aud`). Tokens legados sem esses claims sao aceitos somente se a assinatura HS256 for valida, houver `sub`, `iat`, `exp`, listas `roles` e `permissions`, o subject for numerico e a validade nao exceder uma hora.

## Prazo

O prazo de transicao e controlado por `JWT_LEGACY_ACCEPT_UNTIL`, como instante absoluto UTC no formato ISO-8601 terminado em `Z`. O valor de referencia no repositorio (`2026-11-10T00:00:00Z`) nao e renovado automaticamente e nao deve ser tratado como prazo relativo. Defina no ambiente o instante real da implantacao, com limite curto que permita apenas a expiracao dos access tokens emitidos pela versao anterior: no maximo uma hora, mais a tolerancia de relogio configurada. O limite e estrito: no instante indicado e depois dele, tokens legados sao rejeitados.

## Remocao da compatibilidade

Depois do prazo:

1. Remova `JWT_LEGACY_ACCEPT_UNTIL` dos ambientes e do `.env.example`.
2. Remova o ramo de validacao legado em `JwtConfiguration` e exija issuer e audience em todos os tokens.
3. Remova os testes de aceitacao/rejeicao legada e mantenha testes para assinatura, algoritmo, expiracao, issuer e audience.
4. Implante a versao atualizada. Tokens antigos sem os claims padronizados deixarao de funcionar; usuarios precisarao autenticar novamente.

Falhas de assinatura nunca sao ignoradas, inclusive durante a transicao.
