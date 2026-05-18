# Roadmap Base+

## Curto prazo

- Fazer o primeiro commit/tag da Base+ 1.0.0 quando a revisao estiver aprovada.
- Criar arquivos `.env.example` para backend e frontend, se forem necessarios.
- Definir estrategia oficial para H2, PostgreSQL e Flyway em dev/prod.
- Criar o primeiro modulo real de negocio usando `MODULE_TEMPLATE.md`.
- Aplicar validacao de escopo organizacional no primeiro modulo que possuir dados restritos por contexto de negocio.

## Medio prazo

- Evoluir dashboard com dados reais.
- Implementar exportacao de auditoria.
- Criar testes automatizados para fluxos principais do frontend.
- Documentar contrato da API.
- Implementar um modulo modelo real usando o padrao de CRUD Completo, como Empresas.
- Evoluir `scripts/check-project.ps1` com lint e testes frontend quando existirem.
- Revisar autorizacao granular por permission em todos os endpoints sensiveis.

## Longo prazo

- Suporte a multiempresa/tenant, se fizer sentido para o produto.
- Pipeline CI/CD.
- Observabilidade: logs estruturados, metricas e tracing.
- Marketplace ou registro de modulos reutilizaveis.
- Guia de extensao da plataforma Base+.
