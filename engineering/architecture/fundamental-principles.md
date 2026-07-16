# Principios Fundamentais da Base+

Este documento consolida os principios fundamentais que orientam toda evolucao da Base+.

Ele deve ser utilizado como referencia por arquitetos, desenvolvedores, equipes tecnicas e agentes de IA antes da criacao de novos recursos, modulos, revisoes arquiteturais ou decisoes permanentes da plataforma.

Toda nova funcionalidade, modulo, decisao tecnica ou documentacao oficial deve respeitar estes principios. Tecnologias podem evoluir, mas estes fundamentos representam a identidade arquitetural da Base+.

## 1. Evolucao Incremental

A Base+ evolui continuamente, por etapas pequenas, verificaveis e compativeis.

Mudancas devem priorizar estabilidade, rastreabilidade e capacidade de atualizacao entre versoes. Grandes reescritas devem ser evitadas, salvo quando houver justificativa tecnica clara, risco controlado e plano de migracao.

A solucao preferida e a mais simples que resolve corretamente o problema, preserva a arquitetura e evita overengineering.

## 2. Arquitetura Modular

A organizacao oficial da Base+ separa responsabilidades entre `core`, `shared`, `modules` e `infra`.

- `core`: capacidades centrais da plataforma, reutilizaveis e estaveis.
- `shared`: componentes, utilitarios e contratos compartilhados.
- `modules`: funcionalidades administrativas da plataforma e funcionalidades de dominio da aplicacao.
- `infra`: configuracoes e recursos de infraestrutura, execucao, persistencia e operacao.

Cada responsabilidade deve permanecer em seu lugar. Dependencias desnecessarias entre camadas ou modulos devem ser evitadas.

## 3. Separacao entre Plataforma e Aplicacao

A plataforma administra a si mesma.

A aplicacao administra o dominio do cliente.

Esses dois contextos nunca devem ser misturados.

### Plataforma

A Plataforma contem funcionalidades responsaveis por administrar a propria Base+.

Exemplos:

- Usuarios
- Perfis
- Permissoes
- Organizacao
- Branding
- Auditoria
- Configuracoes
- Integracoes tecnicas
- Monitoramento
- Recursos administrativos

Esses recursos pertencem ao contexto de Administracao da Plataforma.

### Aplicacao

A Aplicacao contem os modulos funcionais do cliente.

Exemplos:

- Clientes
- Fornecedores
- Produtos
- Estoque
- Compras
- Vendas
- Financeiro
- Contratos
- Atendimento
- RH
- Producao

Esses recursos pertencem ao agrupamento funcional definido por cada projeto. A Base+ nao fixa o nome desse agrupamento.

## 4. UX por Padrao

UX nao e uma etapa opcional da Base+.

Toda funcionalidade deve nascer considerando:

- Heuristicas de Nielsen
- Acessibilidade
- Feedback continuo
- Consistencia
- Previsibilidade
- Reducao de carga cognitiva
- Elegancia

Uma funcionalidade somente pode ser considerada completa quando for compreensivel, operavel, acessivel e coerente com a experiencia da plataforma.

## 5. Elegancia

Elegancia na Base+ significa simplicidade, clareza, leveza, consistencia e profissionalismo.

Interfaces devem evitar excesso de informacao, poluicao visual, elementos decorativos sem funcao e hierarquias confusas.

A prioridade visual deve estar na tarefa principal do usuario, com bom espacamento, leitura clara e baixa friccao operacional.

## 6. Branding Desacoplado

A identidade visual deve ser configuravel e independente do codigo da plataforma.

A Base+ deve utilizar Design Tokens e CSS Variables como fonte de verdade visual. Cores fixas devem ser evitadas em componentes, paginas e estilos.

A plataforma nunca deve depender de uma identidade visual especifica de cliente. O branding deve adaptar a aparencia sem comprometer contraste, legibilidade, acessibilidade ou consistencia.

## 7. Desenvolvimento

O desenvolvimento local oficial da Base+ prioriza simplicidade e velocidade.

O fluxo padrao utiliza:

- Windows
- Spring Boot
- Profile `dev`
- H2
- React
- Vite

Docker nao e obrigatorio para o desenvolvimento diario. Ele deve ser utilizado quando a validacao exigir ambiente mais proximo de homologacao ou producao.

## 8. Producao

O ambiente produtivo da Base+ deve ser controlado, rastreavel e recuperavel.

Principios obrigatorios:

- PostgreSQL e o banco oficial.
- Versoes publicadas devem ser identificadas por tag Git.
- Backup deve ser executado antes de atualizacoes.
- Atualizacoes devem ser controladas.
- Rollback deve ser planejado.
- Dados, uploads, auditoria, usuarios e branding devem ser preservados.

## 9. IA como Aceleradora

A IA faz parte do processo de engenharia da Base+, mas nao substitui a arquitetura.

A IA pode apoiar analises, identificar riscos, gerar alternativas, produzir prompts e acelerar implementacoes. Ainda assim, a fonte de verdade continua sendo a arquitetura, os principios, os ADRs, os playbooks, os quality gates e a documentacao oficial.

Toda sugestao gerada por IA deve respeitar estes principios antes de ser implementada.

## 10. Documentacao Viva

A documentacao e parte da plataforma.

Decisoes importantes devem ser registradas em documentos versionados. O conhecimento arquitetural nao deve ficar restrito a historico de conversas, memoria individual ou contexto temporario de IA.

A documentacao deve evoluir junto com o codigo, a operacao, a UX e os processos de release.

## 11. Compatibilidade

Toda evolucao deve preservar compatibilidade sempre que possivel.

Isso inclui compatibilidade arquitetural, APIs, dados, atualizacoes, operacao, Docker, frontend, backend e documentacao.

Mudancas disruptivas devem ser tratadas como excecao, justificadas em ADR, documentadas no changelog e acompanhadas de estrategia de migracao.

## 12. Qualidade

A Base+ nao esta completa apenas porque compila.

Qualidade inclui:

- Quality Gates
- Playbooks
- Architecture Reviews
- Principios de UX
- Testes
- Validacao operacional
- Documentacao atualizada
- Seguranca
- Manutenibilidade

Toda entrega deve ser revisada pelo impacto tecnico, operacional, visual e arquitetural.

## 13. Filosofia Base+

A Base+ e uma plataforma corporativa para criacao de aplicacoes de negocio.

Ela combina simplicidade, arquitetura solida, UX consistente, documentacao viva e desenvolvimento acelerado por IA, preservando evolucao incremental e manutencao de longo prazo.

A Base+ nao e apenas uma base de codigo reutilizavel. Ela representa uma forma consistente de construir, evoluir e operar software corporativo ao longo do tempo.

## Documentos Relacionados

Este documento deve ser lido em conjunto com:

- `engineering/architecture/principles.md`
- `engineering/architecture/decisions.md`
- `engineering/ux/`
- `engineering/quality/`
- `engineering/playbooks/`
- `engineering/ai/`
- `engineering/release/`
- `engineering/operations/`