# Module Template - Base+

Este documento define o padrao oficial para criar novos modulos na Base+.

Todo novo modulo deve escolher um dos dois modelos de CRUD:

- CRUD Compacto
- CRUD Completo

A escolha deve ser feita antes da implementacao e registrada na tarefa, PR ou changelog quando relevante.

## Decisao rapida

Use CRUD Compacto quando o usuario consegue criar ou editar o registro rapidamente, sem sair da listagem.

Use CRUD Completo quando a criacao ou edicao precisa de foco, URL propria, varias secoes, vinculos ou mais seguranca contra perda de contexto.

## CRUD Compacto

O CRUD Compacto concentra listagem, busca, filtros e formulario em uma unica pagina.

### Quando usar

- Formulario com poucos campos simples.
- Cadastro auxiliar ou parametrizacao.
- Criacao/edicao rapida.
- Sem abas.
- Sem relacoes complexas.
- Sem upload relevante.
- Sem necessidade de URL propria para editar.

Exemplos:

- Categorias.
- Tipos.
- Tags.
- Departamentos simples.
- Centros de custo basicos.
- Status.
- Parametros simples.
- Permissoes simples.

### Estrutura frontend

```text
src/modules/<modulo>/
+-- <modulo>.css
+-- <modulo>Service.js
+-- <ModuloPage>.jsx
+-- <ModuloFormModal>.jsx
```

### Fluxo de tela

```text
Listagem + filtros + tabela + modal/drawer de criar/editar
```

### Rotas

Normalmente existe apenas a rota de listagem:

```text
/app/<modulo>
```

### Comportamento esperado

- O botao "Novo" abre modal ou drawer.
- A acao de editar abre o mesmo modal/drawer preenchido.
- Excluir exige confirmacao.
- Salvar atualiza a listagem sem perder o contexto.
- A pagina deve ter estado vazio, carregamento, erro, busca/filtros e paginacao quando aplicavel.

## CRUD Completo

O CRUD Completo separa listagem e formulario em paginas diferentes.

### Quando usar

- Formulario com muitos campos.
- Cadastro principal do negocio.
- Necessidade de URL propria para criar ou editar.
- Edicao pode demorar.
- Existem abas, secoes ou etapas.
- Existem vinculos com outras entidades.
- Existem uploads ou anexos.
- Existem permissoes por bloco de edicao.
- O usuario precisa revisar dados antes de salvar.

Exemplos:

- Usuarios.
- Empresas.
- Clientes.
- Produtos complexos.
- Contratos.
- Projetos.
- Pedidos.
- Financeiro.
- Integracoes.

### Estrutura frontend

```text
src/modules/<modulo>/
+-- <modulo>.css
+-- <modulo>Service.js
+-- <ModuloPage>.jsx
+-- <ModuloFormPage>.jsx
```

### Fluxo de tela

```text
Listagem + filtros + tabela
Formulario de criacao em pagina propria
Formulario de edicao em pagina propria
```

### Rotas

```text
/app/<modulo>
/app/<modulo>/novo
/app/<modulo>/:id/editar
```

### Comportamento esperado

- A listagem apresenta busca, filtros, tabela, status, acoes e paginacao quando aplicavel.
- A criacao usa rota propria.
- A edicao usa rota propria.
- Cancelar volta para a listagem.
- Excluir exige confirmacao.
- Formularios longos devem ser divididos por secoes ou abas quando isso reduzir complexidade.
- Vinculos com outras entidades devem ficar em secoes claras, como ja ocorre em usuarios e roles.

## Padrao backend

O backend nao muda conforme o tipo visual de CRUD.

Todo modulo deve seguir:

```text
baseplus-backend/src/main/java/com/baseplus/modules/<modulo>/
+-- controller/
+-- domain/
+-- dto/
+-- repository/
+-- service/
```

Arquivos comuns:

```text
<Modulo>Controller.java
<Modulo>.java
<Modulo>Repository.java
<Modulo>Service.java
Create<Modulo>Request.java
Update<Modulo>Request.java
Update<Modulo>StatusRequest.java, quando houver status
<Modulo>Response.java
```

Endpoints comuns:

```text
GET    /<modulos>
GET    /<modulos>/{id}
POST   /<modulos>
PUT    /<modulos>/{id}
PATCH  /<modulos>/{id}/status, quando houver ativo/inativo
DELETE /<modulos>/{id}
```

Regras:

- Controllers devem retornar `ApiResponse`.
- Listagens devem usar `PageResponse` quando houver paginacao.
- Services concentram regra de negocio.
- Repositories nao devem ser acessados diretamente por controllers.
- Validacoes de duplicidade e regras de exclusao ficam no service.
- Endpoints devem ser protegidos por permission quando aplicavel.

## Integracoes externas

Quando o modulo receber ou consumir integracoes, siga `docs/integrations.md`.

- `<Modulo>Controller` atende a aplicacao Base+ com JWT, permissions e escopo organizacional.
- `<Modulo>ExternalController` e reservado para chamadas maquina-a-maquina e nao mistura mecanismos de autenticacao.
- Adapters especificos ficam em `modules/<modulo>/integration`; somente infraestrutura transversal pertence a `com.baseplus.core.integration`.
- REST, SOAP, APIs de terceiros e legados sao isolados do dominio por clients, DTOs e mappers.
- Entradas mutaveis definem `externalId` ou `Idempotency-Key`.
- Auditoria registra origem, data/hora, identificador externo, resultado e correlationId quando aplicavel.
- A autenticacao inicial reservada usa `X-API-Key` configurada por ambiente; ela ainda nao esta implementada.

Checklist adicional:

1. Classificar entrada, saida ou ambas.
2. Definir contrato e versionamento.
3. Definir autenticacao e configuracao por ambiente.
4. Definir idempotencia e constraints PostgreSQL.
5. Definir auditoria, correlationId, timeout e retry.
6. Criar testes de contrato e indisponibilidade.

## Permissoes

Use o padrao:

```text
<MODULO>_VIEW
<MODULO>_CREATE
<MODULO>_EDIT
<MODULO>_DELETE
```

Exemplo para empresas:

```text
EMPRESAS_VIEW
EMPRESAS_CREATE
EMPRESAS_EDIT
EMPRESAS_DELETE
```

Permissoes extras devem ser explicitas:

```text
<MODULO>_EXPORT
<MODULO>_IMPORT
<MODULO>_MANAGE_USERS
<MODULO>_APPROVE
```

## Roles por modulo

Roles devem agrupar permissoes do modulo.

Padrao recomendado:

```text
<MODULO>_ADMIN
<MODULO>_OPERADOR
<MODULO>_LEITOR
```

Exemplo para empresas:

```text
EMPRESAS_ADMIN
- EMPRESAS_VIEW
- EMPRESAS_CREATE
- EMPRESAS_EDIT
- EMPRESAS_DELETE

EMPRESAS_OPERADOR
- EMPRESAS_VIEW
- EMPRESAS_CREATE
- EMPRESAS_EDIT

EMPRESAS_LEITOR
- EMPRESAS_VIEW
```

Regra:

```text
Role agrupa permissions.
Permission autoriza endpoints e acoes.
```

Nao usar role como atalho para liberar todas as permissoes do modulo.

## Escopo organizacional

Quando o modulo precisar limitar acesso por contexto de negocio, use o modelo generico de escopo organizacional:

```text
Perfil funcional autoriza a acao.
Perfil organizacional define onde a acao pode ocorrer.
```

Tipos como empresa, filial, equipe de vendas, centro de custo ou local de expedicao devem ser tratados como dados parametrizaveis, nao como tipos fixos no codigo do modulo.

Checklist adicional para modulo com escopo:

1. Identificar qual unidade organizacional se relaciona com o registro.
2. Validar permission funcional antes da acao.
3. Validar escopo organizacional antes de listar, editar, aprovar ou excluir dados restritos.
4. Expor filtros de escopo no frontend somente quando fizer sentido para o usuario.

## Checklist de novo modulo

1. Escolher CRUD Compacto ou CRUD Completo.
2. Criar entidade de dominio no backend.
3. Criar DTOs de request/response.
4. Criar repository.
5. Criar service com regras e validacoes.
6. Criar controller com `ApiResponse`.
7. Criar migration Flyway quando a estrategia de banco estiver ativa.
8. Criar/registrar permissoes.
9. Criar roles por modulo quando aplicavel.
10. Associar permissoes aos roles.
11. Criar service frontend do modulo.
12. Criar pagina compacta ou paginas de listagem/formulario.
13. Registrar rotas em `App.jsx`.
14. Registrar item de menu em `navigation.js`.
15. Criar testes backend relevantes.
16. Rodar testes/build relevantes.
17. Atualizar `CHANGELOG.md` e `state.txt` quando houver mudanca funcional.

## Referencias atuais

- CRUD Compacto: permissions usa uma pagina com formulario/modal enxuto.
- CRUD Completo: usuarios e roles usam paginas individuais para criacao/edicao.
