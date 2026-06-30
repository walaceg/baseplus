# Runbook de Restore - Base+

## Objetivo

Orientar restauracao de banco e uploads.

Comandos completos para Docker estao em `docs/docker.md`.

## Restaurar banco

1. Parar operacoes que escrevem dados, quando necessario.
2. Garantir que o dump correto foi selecionado.
3. Restaurar o dump PostgreSQL.
4. Reiniciar backend quando aplicavel.
5. Validar readiness.

## Restaurar uploads

1. Identificar pacote de uploads correspondente ao backup do banco.
2. Restaurar arquivos no volume ou diretorio configurado em `UPLOAD_DIR`.
3. Garantir permissoes de leitura pelo backend.
4. Reiniciar backend se necessario.

## Validar sistema

Validar:

- `/health`;
- `/health/ready`;
- login;
- usuarios;
- branding;
- uploads;
- auditoria;
- menus e permissoes.

## Testes apos restore

- [ ] Login com usuario administrativo.
- [ ] Abrir imagem de branding.
- [ ] Abrir avatar existente.
- [ ] Fazer novo upload.
- [ ] Consultar auditoria.
- [ ] Validar fluxo principal da aplicacao.
