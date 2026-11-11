# Transicao de JWT

Tokens novos usam HS256 e incluem issuer (`iss`) e audience (`aud`). Tokens legados sem esses claims sao aceitos somente se a assinatura HS256 for valida, houver `sub`, `iat`, `exp`, listas `roles` e `permissions`, o subject for numerico e a validade nao exceder uma hora.

## Prazo

O prazo de transicao e controlado por `JWT_LEGACY_ACCEPT_UNTIL`, como instante absoluto UTC no formato ISO-8601 terminado em `Z`. O valor de referencia no repositorio (`2026-11-10T00:00:00Z`) nao e renovado automaticamente e nao deve ser tratado como prazo relativo. Defina no ambiente o instante real da implantacao, com limite curto que permita apenas a expiracao dos access tokens emitidos pela versao anterior: no maximo uma hora, mais a tolerancia de relogio configurada. O limite e estrito: no instante indicado e depois dele, tokens legados sao rejeitados.

## Logout e tokens sem `sid`

Os access tokens emitidos pela versao atual incluem o claim assinado `sid`, que identifica a sessao criada no login. `POST /auth/logout` revoga a capacidade de renovar somente dessa sessao, invalidando seus refresh tokens. Access JWTs ja emitidos, inclusive os atuais com `sid`, podem continuar validos ate a expiracao; a validade maxima configurada atualmente e de aproximadamente uma hora. O logout comum nao invalida imediatamente esses access tokens no backend.

JWTs validos sem `sid` continuam permitindo o encerramento local no cliente, mas nao permitem revogacao remota segura pelo logout comum; o backend nao tenta inferir a sessao pelo usuario nem revoga todas as sessoes como fallback.

Durante a transicao, um refresh token pertencente a uma sessao associada a JWT sem `sid` pode continuar valido no servidor ate sua expiracao original. A familia de refresh tokens nao tem extensao por rotacao e expira no maximo sete dias apos o login original. Um access token sem `sid` tambem continua limitado a sua expiracao propria, atualmente uma hora apos emissao. A limpeza local remove os tokens do navegador, mas nao encurta essas expiracoes no servidor.

Se a politica operacional exigir revogacao imediata das sessoes antigas, planeje uma revogacao global controlada para os usuarios afetados durante a implantacao. Essa acao encerra outras sessoes desses usuarios tambem; nao use o logout comum para esse fim. Caso contrario, mantenha a compatibilidade e aceite a janela residual ate a expiracao natural das familias antigas.

Nao mantenha instancias antigas atendendo logout durante um rollout misto: elas ainda podem revogar refresh tokens globalmente por usuario, inclusive de uma sessao criada depois. Drene as instancias antigas antes de considerar ativa a semantica de logout por sessao.

## Remocao da compatibilidade

Depois do prazo:

1. Remova `JWT_LEGACY_ACCEPT_UNTIL` dos ambientes e do `.env.example`.
2. Remova o ramo de validacao legado em `JwtConfiguration` e exija issuer e audience em todos os tokens.
3. Remova os testes de aceitacao/rejeicao legada e mantenha testes para assinatura, algoritmo, expiracao, issuer e audience.
4. Implante a versao atualizada. Tokens antigos sem os claims padronizados deixarao de funcionar; usuarios precisarao autenticar novamente.

Falhas de assinatura nunca sao ignoradas, inclusive durante a transicao.
