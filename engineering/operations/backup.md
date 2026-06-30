# Runbook de Backup - Base+

## Objetivo

Definir o backup operacional minimo da Base+.

Detalhes de comandos Docker estao em `docs/docker.md`.

## Quando executar backup

- Antes de toda atualizacao.
- Antes de migrations relevantes.
- Antes de manutencoes de infraestrutura.
- Em rotina periodica definida pela operacao.
- Antes de testes de rollback.

## Banco PostgreSQL

O backup do PostgreSQL deve preservar dados de negocio, usuarios, perfis, permissoes, auditoria, branding e configuracoes.

Use `pg_dump` conforme documentado em `docs/docker.md`.

## Uploads

O backup de uploads deve preservar arquivos enviados pela aplicacao, incluindo avatares, branding e outros arquivos persistidos em `/uploads`.

Em Docker, compacte o volume de uploads conforme `docs/docker.md`.

## Branding

Branding depende de registros no banco e arquivos em uploads.

Backup confiavel deve guardar banco e uploads juntos.

## Arquivos importantes

Preservar:

- `.env` operacional fora do repositorio;
- dump PostgreSQL;
- pacote de uploads;
- versao/tag em execucao;
- documentacao de restore.

## Checklist pre-release

- [ ] Backup PostgreSQL criado.
- [ ] Backup uploads criado.
- [ ] Arquivos armazenados em local seguro.
- [ ] Versao atual registrada.
- [ ] Restore conhecido.
- [ ] Responsavel pela validacao definido.
