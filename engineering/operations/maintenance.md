# Runbook de Manutencao - Base+

## Objetivo

Definir atividades periodicas de manutencao da Base+.

## Atividades periodicas

### Atualizar plataforma

- Revisar release disponivel.
- Ler changelog.
- Executar backup.
- Atualizar HOM.
- Validar.
- Atualizar PRD.

### Validar backups

- Confirmar existencia de backups.
- Confirmar retencao.
- Conferir armazenamento seguro.
- Registrar data da ultima validacao.

### Testar restore

- Restaurar banco em ambiente isolado.
- Restaurar uploads correspondentes.
- Validar login, branding e uploads.

### Limpeza de imagens Docker

- Listar imagens antigas.
- Confirmar que nao estao em uso.
- Remover com criterio operacional.

### Atualizacao de dependencias

- Revisar dependencias backend e frontend.
- Validar compatibilidade.
- Executar testes.
- Registrar riscos.

### Revisao de documentacao

- Atualizar runbooks a cada release.
- Revisar README.
- Revisar `docs/docker.md`.
- Revisar `docs/update.md`.
- Revisar troubleshooting.

## Checklist

- [ ] Backups validados.
- [ ] Restore testado.
- [ ] Docker revisado.
- [ ] Dependencias revisadas.
- [ ] Documentacao revisada.
- [ ] Pendencias operacionais registradas.
