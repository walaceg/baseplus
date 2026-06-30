# Guia de Uso de Assistentes de Codigo - Base+

Este documento define diretrizes para desenvolvimento assistido por IA na Base+.

## Principios

- Um prompt deve possuir apenas uma responsabilidade.
- Backend e frontend devem ser tratados separadamente quando a mudanca permitir.
- Nunca alterar arquitetura sem necessidade comprovada.
- Nao misturar responsabilidades.
- Priorizar simplicidade.
- Sempre respeitar a estrutura Base+.
- Sempre listar arquivos alterados.
- Sempre sugerir validacao.
- Sempre manter compatibilidade com PostgreSQL.

## Regras de implementacao

- Nao introduzir Redux.
- Nao utilizar cores fixas.
- Nao alterar Docker sem justificativa.
- Nao modificar autenticacao sem justificativa.
- Nao colocar regra sensivel apenas no frontend.
- Nao mover funcionalidades para `core` sem reutilizacao comprovada.
- Nao editar migrations ja publicadas.
- Nao criar abstracoes sem necessidade real.

## Prompts

Um prompt adequado deve informar:

- projeto;
- area afetada;
- objetivo;
- escopo;
- restricoes;
- validacao esperada;
- se deve ou nao alterar arquivos.

Exemplo:

```text
Projeto: Base+
Area: Backend
Tarefa:
Criar o modulo Produtos usando CRUD Compacto.

Regras:
- seguir Controller -> Service -> Repository -> Domain
- criar DTOs
- criar migration Flyway
- manter compatibilidade com PostgreSQL
- nao alterar autenticacao

Validacao:
- mvn test
- listar arquivos alterados
```

## Separacao por responsabilidade

Quando uma tarefa envolver varias camadas, prefira dividir:

1. Diagnostico.
2. Backend.
3. Frontend.
4. Infraestrutura.
5. Documentacao.
6. Validacao.

Essa separacao reduz regressao e facilita revisao.

## Saida esperada

Ao final de uma tarefa, a resposta deve informar:

- arquivos alterados;
- resumo tecnico;
- validacoes executadas;
- validacoes nao executadas, se houver;
- riscos remanescentes;
- proximos passos recomendados.

## Cuidados especiais

### Backend

- Controllers devem ser finos.
- Services concentram regras de aplicacao.
- Repositories nao devem conter regra de negocio.
- DTOs protegem contratos externos.
- Exceptions devem seguir padrao da plataforma.

### Frontend

- Usar componentes compartilhados quando existirem.
- Respeitar design tokens.
- Evitar logica duplicada.
- Garantir loading, erro, vazio e feedback visual.
- Nao criar estado global novo sem necessidade.

### Infraestrutura

- Preservar volumes.
- Nao versionar segredos.
- Nao expor PostgreSQL publicamente no Compose integrado.
- Validar `docker compose config` antes de alteracoes operacionais.

### Documentacao

- Usar linguagem objetiva.
- Registrar decisoes relevantes.
- Manter README curto e guias especificos em `docs/` ou `engineering/`.
