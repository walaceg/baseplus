# Navegacao

## Objetivo

Definir diretrizes de navegacao para a Base+.

## Principio arquitetural de navegacao

A navegacao deve tornar visivel o principio:

```text
A plataforma administra a si mesma.
A aplicacao administra o negocio.
```

A navegacao deve separar claramente dois contextos:

1. Plataforma.
2. Aplicacao.

Essa separacao e obrigatoria em aplicacoes Base+ e aplicacoes derivadas.

### Plataforma

Plataforma contem funcionalidades responsaveis por administrar a propria Base+.

Exemplos:

- Usuarios;
- Perfis;
- Permissoes;
- Organizacao;
- Branding;
- Auditoria;
- Configuracoes;
- Monitoramento;
- integracoes tecnicas;
- recursos administrativos.

### Aplicacao

Aplicacao contem os modulos funcionais do cliente.

Exemplos:

- Clientes;
- Fornecedores;
- Produtos;
- Estoque;
- Compras;
- Vendas;
- Financeiro;
- Contratos;
- Atendimento;
- RH;
- Producao.

O agrupamento onde esses modulos aparecem nao possui nome fixo definido pela Base+.

Cada aplicacao pode definir o agrupamento conforme seu contexto, por exemplo:

- Negocio;
- Comercial;
- Operacao;
- ERP;
- Producao;
- Gestao;
- Atendimento;
- Academico;
- Assistencial.

A Base+ nao define esse nome. A Base+ define apenas a separacao entre Plataforma e Aplicacao.

## Regra obrigatoria para novos modulos

Antes de criar qualquer item de menu, rota ou breadcrumb de um novo modulo, responder:

```text
1. Este modulo administra a plataforma Base+?

Se SIM -> Administracao.
Se NAO -> agrupamento funcional da aplicacao.

2. O modulo pertence ao dominio do cliente?

Se SIM -> nunca podera ser colocado em Administracao.
```

E proibido criar modulos de dominio do cliente dentro de Administracao.

## Anti-pattern oficial

Errado:

```text
Administracao
- Usuarios
- Permissoes
- Clientes
- Produtos
- Financeiro
```

Correto:

```text
Administracao
- Usuarios
- Permissoes
- Branding
- Organizacao

<agrupamento funcional definido pela aplicacao>
- Clientes
- Produtos
- Financeiro
```
## Estrutura

A navegacao deve refletir a organizacao real da aplicacao.

Menus devem agrupar modulos por responsabilidade e respeitar a separacao entre Plataforma e Aplicacao.

## Hierarquia

O usuario deve entender:

- onde esta;
- qual modulo esta usando;
- se o modulo pertence a Plataforma ou a Aplicacao;
- qual acao esta executando;
- como retornar.

## Menus

Menus devem ser estaveis e previsiveis.

Evite criar caminhos diferentes para a mesma tarefa sem necessidade.

Nao misture funcionalidades administrativas da Base+ com modulos de dominio do cliente no mesmo agrupamento.

## Breadcrumbs

Use breadcrumbs quando a profundidade de navegacao justificar.

## Rotas

Rotas devem ser legiveis, estaveis e compativeis com permissao.

A estrutura de rotas deve preservar a separacao conceitual entre Plataforma e Aplicacao, mesmo quando a implementacao tecnica compartilha o mesmo shell ou layout.

## Acesso Negado

Quando o usuario nao possuir permissao, a experiencia deve explicar a restricao sem expor informacoes sensiveis.

## Padroes oficiais

### Sidebar

Sidebar deve agrupar modulos por responsabilidade.

Administracao deve conter somente recursos da Plataforma.

Modulos de dominio devem aparecer no agrupamento funcional da Aplicacao.

### Header

Header deve manter identidade, contexto e acesso a conta sem competir com o conteudo.

### Dashboard

Dashboard deve apresentar visao inicial util, nao apenas decorativa.

### Breadcrumb

Use quando houver profundidade de navegacao ou edicao em paginas internas.

### Pesquisa

Pesquisa global ou local deve deixar claro seu escopo.

### Favoritos

Favoritos podem reduzir esforco em sistemas com muitos modulos.

### Atalhos

Atalhos devem ser consistentes e opcionais.

### Menu

Menus devem respeitar permissao e manter estabilidade.

### Perfis

A navegacao deve refletir permissoes sem expor funcionalidades inacessiveis de forma confusa.

### Avatar

Avatar representa conta, identidade e menu pessoal. Deve ser discreto, acessivel e consistente.

### Fluxo

O usuario deve saber sempre onde esta, o que esta vendo, para onde pode ir e como retornar.