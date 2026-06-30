# Checklist de Revisao - Base+

Use este checklist para revisar mudancas implementadas na Base+ ou em aplicacoes derivadas.

## Arquitetura

- [ ] A arquitetura `core/shared/modules` foi respeitada.
- [ ] A funcionalidade nasceu em `modules`.
- [ ] Nao houve promocao indevida para `core`.
- [ ] Responsabilidades estao separadas.
- [ ] Nao ha overengineering.
- [ ] A mudanca e incremental e compreensivel.

## Backend

- [ ] Fluxo `Controller -> Service -> Repository -> Domain` preservado.
- [ ] Controllers nao contem regra de negocio.
- [ ] Services concentram regras de aplicacao.
- [ ] Repositories nao possuem regra sensivel.
- [ ] DTOs de entrada e saida foram usados corretamente.
- [ ] Exceptions seguem padrao da plataforma.
- [ ] Testes backend cobrem o comportamento principal.

## Frontend

- [ ] Estrutura modular foi preservada.
- [ ] Componentes compartilhados foram reutilizados quando possivel.
- [ ] Services frontend centralizam chamadas de API.
- [ ] Loading foi considerado.
- [ ] Erro foi considerado.
- [ ] Estado vazio foi considerado.
- [ ] Feedback visual foi considerado.
- [ ] Nao foi introduzido Redux.

## Infraestrutura

- [ ] Variaveis de ambiente foram mantidas fora do codigo.
- [ ] Nenhum segredo foi versionado.
- [ ] Profiles continuam consistentes.
- [ ] Health e readiness continuam validos.
- [ ] Configuracoes temporarias nao foram fixadas no codigo.

## Docker

- [ ] `docker compose config` foi considerado.
- [ ] Volumes persistentes foram preservados.
- [ ] PostgreSQL nao foi exposto publicamente sem justificativa.
- [ ] Nginx continua roteando `/api` corretamente.
- [ ] Nginx continua roteando `/uploads` corretamente.
- [ ] Docker nao foi alterado sem justificativa.

## Banco

- [ ] PostgreSQL continua compativel.
- [ ] H2 permanece apenas para desenvolvimento.
- [ ] Migrations novas seguem Flyway.
- [ ] Migrations publicadas nao foram editadas.
- [ ] Hibernate validate continua coerente em profiles persistentes.
- [ ] Queries customizadas foram revisadas.

## Seguranca

- [ ] Spring Security nao foi alterado sem justificativa.
- [ ] JWT continua preservado.
- [ ] Permissoes granulares foram aplicadas.
- [ ] Endpoints sensiveis exigem autenticacao.
- [ ] Dados sensiveis nao sao expostos em DTOs.
- [ ] Uploads continuam validados.
- [ ] CORS continua configuravel por ambiente.

## UX

- [ ] Interface respeita design tokens.
- [ ] Nao ha cores fixas.
- [ ] Textos cabem nos componentes.
- [ ] Estados interativos possuem feedback.
- [ ] Fluxos principais sao claros.
- [ ] Comportamento responsivo foi considerado.

## Documentacao

- [ ] README foi atualizado somente quando necessario.
- [ ] Documentacao especifica foi criada em local adequado.
- [ ] `state.txt` ou documento de estado foi atualizado quando aplicavel.
- [ ] CHANGELOG foi atualizado quando a mudanca for relevante.
- [ ] Links internos continuam validos.

## Operacao

- [ ] Validacoes foram executadas ou justificadas.
- [ ] Comandos de execucao foram informados.
- [ ] Impactos em HOM/PRD foram considerados.
- [ ] Rollback foi considerado quando aplicavel.
- [ ] Backup foi considerado para alteracoes persistentes.
