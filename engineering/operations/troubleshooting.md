# Runbook de Troubleshooting Operacional - Base+

## Objetivo

Organizar problemas conhecidos para diagnostico operacional.

Para detalhes tecnicos complementares, consulte `engineering/ai/troubleshooting.md`.

## Bootstrap

Verificar:

- banco vazio;
- variaveis `BASEPLUS_BOOTSTRAP_ADMIN_*`;
- existencia previa de administrador;
- logs do backend.

## Docker

Verificar:

- `.env`;
- `docker compose config`;
- portas ocupadas;
- status dos containers;
- logs.

## Flyway

Verificar:

- logs de migration;
- tabela `flyway_schema_history`;
- duplicidade de versao;
- migrations editadas indevidamente.

## PostgreSQL

Verificar:

- conectividade;
- credenciais;
- volume de dados;
- readiness do backend;
- compatibilidade de migrations.

## Avatar

Verificar:

- `avatarUrl` retornado por `/auth/me`;
- arquivo em `/uploads/avatars`;
- proxy `/uploads`;
- URL final usada pelo navegador.

## Branding

Verificar:

- `/branding/public`;
- URLs de assets;
- `assetVersion`;
- arquivos em `/uploads/branding`;
- cache do navegador.

## Uploads

Verificar:

- `UPLOAD_DIR`;
- volume persistente;
- permissao de escrita;
- GET em `/uploads/<arquivo>`.

## Healthcheck

Verificar:

- `/health`;
- `/health/ready`;
- conectividade com banco;
- logs do backend.

## Login

Verificar:

- credenciais;
- usuario ativo;
- usuario bloqueado;
- JWT secret;
- CORS.

## Permissoes

Verificar:

- permissoes vinculadas ao perfil;
- perfil ativo;
- endpoint protegido;
- resposta 403;
- auditoria quando aplicavel.
