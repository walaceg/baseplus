# Troubleshooting - Base+

Base inicial de diagnostico para problemas recorrentes na Base+ e em aplicacoes derivadas.

## Bootstrap

Sintoma: primeiro administrador nao e criado.

Causa provavel:

- banco nao esta vazio;
- `BASEPLUS_BOOTSTRAP_ADMIN_ENABLED` nao esta ativo;
- variaveis obrigatorias do bootstrap ausentes;
- ja existe usuario administrador.

Como diagnosticar:

- verificar logs do backend;
- consultar usuarios e roles;
- revisar variaveis de ambiente.

Como corrigir:

- executar bootstrap apenas em banco vazio;
- informar nome, email e senha;
- remover variaveis de bootstrap apos execucao.

## CORS

Sintoma: frontend recebe erro de CORS ao chamar backend.

Causa provavel:

- origem nao esta em `BASEPLUS_CORS_ALLOWED_ORIGINS`;
- uso de tunel sem configuracao;
- frontend e backend em hosts diferentes.

Como diagnosticar:

- verificar console do navegador;
- confirmar origem exata da pagina;
- revisar variaveis de ambiente do backend.

Como corrigir:

- adicionar origem exata em `BASEPLUS_CORS_ALLOWED_ORIGINS`;
- reiniciar backend;
- nao usar `*` em CORS.

## ngrok

Sintoma: aplicacao abre pelo tunel, mas chamadas API falham.

Causa provavel:

- host nao permitido no Vite;
- CORS sem dominio do tunel;
- dominio real fixado em arquivo versionado.

Como diagnosticar:

- verificar console do navegador;
- revisar `VITE_ALLOWED_HOSTS`;
- revisar `BASEPLUS_CORS_ALLOWED_ORIGINS`.

Como corrigir:

- configurar tunel apenas em `.env.local` ou variaveis locais;
- reiniciar frontend e backend;
- nao versionar dominio real do tunel.

## Uploads

Sintoma: upload retorna sucesso, mas arquivo nao abre depois.

Causa provavel:

- `UPLOAD_DIR` incorreto;
- volume nao persistente;
- Nginx nao roteia `/uploads`;
- arquivo removido por troca ou exclusao.

Como diagnosticar:

- verificar URL salva no banco;
- verificar arquivo no diretorio de uploads;
- testar `GET /uploads/...`;
- verificar volume Docker.

Como corrigir:

- usar URL relativa `/uploads/...`;
- configurar volume persistente;
- garantir proxy `/uploads/` no Nginx.

## Avatar

Sintoma: avatar aparece apos upload e some depois de logout/login.

Causa provavel:

- `/auth/me` nao retorna `avatarUrl`;
- frontend nao preserva `avatarUrl`;
- URL relativa `/uploads/...` e resolvida no host errado;
- container frontend usa build antigo.

Como diagnosticar:

- verificar resposta de `/auth/me`;
- verificar resposta de `/conta`;
- testar `GET /uploads/avatars/<arquivo>`;
- inspecionar `src` final da imagem no navegador.

Como corrigir:

- retornar `avatarUrl` em `/auth/me` e `/conta`;
- resolver `/uploads/...` com helper compartilhado;
- rebuildar frontend e backend.

## Branding

Sintoma: login nao mostra branding personalizado.

Causa provavel:

- tela de login nao usa endpoint publico;
- assets sem cache-bust;
- URL de upload invalida;
- Nginx nao roteia `/uploads`.

Como diagnosticar:

- testar `/branding/public`;
- verificar URLs retornadas;
- testar assets em `/uploads/...`;
- limpar cache ou conferir `assetVersion`.

Como corrigir:

- usar endpoint publico sanitizado;
- aplicar versionamento de assets;
- preservar URLs relativas.

## Flyway

Sintoma: backend nao sobe em profile docker/prod.

Causa provavel:

- migration com erro;
- migration duplicada;
- schema alterado manualmente;
- migration publicada editada.

Como diagnosticar:

- revisar logs do Flyway;
- consultar tabela `flyway_schema_history`;
- comparar migrations versionadas.

Como corrigir:

- criar nova migration corretiva;
- nao editar migrations publicadas;
- validar em PostgreSQL antes de HOM/PRD.

## Docker

Sintoma: stack nao sobe.

Causa provavel:

- `.env` ausente;
- `DB_PASSWORD` ou `JWT_SECRET` ausentes;
- porta em uso;
- imagem antiga.

Como diagnosticar:

- executar `docker compose config`;
- executar `docker compose ps`;
- verificar logs dos servicos.

Como corrigir:

- configurar `.env`;
- liberar portas;
- rebuildar imagens;
- subir com `docker compose up --detach --build --wait`.

## Volumes

Sintoma: dados somem apos recriar containers.

Causa provavel:

- uso de `docker compose down --volumes`;
- volume com nome diferente;
- `UPLOAD_DIR` sem volume;
- banco recriado.

Como diagnosticar:

- listar volumes Docker;
- verificar `docker-compose.yml`;
- conferir backup recente.

Como corrigir:

- nao usar `--volumes` em atualizacao;
- manter volumes persistentes;
- restaurar backup quando necessario.

## Healthcheck

Sintoma: container backend fica unhealthy.

Causa provavel:

- banco indisponivel;
- migrations falhando;
- endpoint readiness retornando erro;
- variaveis de ambiente invalidas.

Como diagnosticar:

- testar `/health`;
- testar `/health/ready`;
- verificar logs do backend e PostgreSQL.

Como corrigir:

- corrigir conexao com banco;
- corrigir migration;
- ajustar variaveis obrigatorias.

## Atualizacao

Sintoma: ambiente perde dados apos atualizar.

Causa provavel:

- reinstalacao em vez de atualizacao;
- volumes removidos;
- banco recriado;
- bootstrap executado indevidamente.

Como diagnosticar:

- verificar historico de comandos;
- conferir volumes;
- conferir backups;
- revisar `docs/update.md`.

Como corrigir:

- atualizar sem remover volumes;
- restaurar backup quando necessario;
- usar Flyway para evolucao de schema.

## PostgreSQL

Sintoma: erro apenas em docker/prod, mas nao em dev.

Causa provavel:

- comportamento especifico de H2 mascarando problema;
- tipo SQL incompativel;
- query nao portavel;
- schema divergente.

Como diagnosticar:

- rodar teste de compatibilidade PostgreSQL;
- verificar Flyway;
- revisar queries customizadas;
- conferir `ddl-auto=validate`.

Como corrigir:

- ajustar query ou migration para PostgreSQL;
- manter H2 apenas para desenvolvimento;
- validar com Testcontainers.
