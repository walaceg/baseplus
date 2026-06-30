# Runbook de Atualizacao - Base+

## Objetivo

Orientar atualizacoes de ambientes existentes sem reinstalar a plataforma e sem perder dados.

O processo oficial detalhado esta em `docs/update.md`.

## Principios

- Atualizacao nao e instalacao inicial.
- Banco PostgreSQL deve ser preservado.
- Uploads devem ser preservados.
- Usuarios, auditoria e branding devem ser preservados.
- Bootstrap administrativo nao deve ser executado novamente.

## Flyway

Em profiles persistentes, Flyway aplica migrations novas automaticamente na subida do backend.

Migrations publicadas nao devem ser alteradas.

## Checklist resumido

- [ ] Tag de destino definida.
- [ ] Backup PostgreSQL executado.
- [ ] Backup uploads executado.
- [ ] `.env` preservado.
- [ ] Volumes preservados.
- [ ] `docker compose config` validado.
- [ ] Health e readiness validados apos atualizacao.
- [ ] Login, branding e uploads validados.

## Referencia

Consulte `docs/update.md` para comandos completos de atualizacao, backup, validacao e rollback.
