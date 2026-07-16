# Tabelas e Listagens

## Objetivo

Definir diretrizes para tabelas, listagens e grids administrativos.

## Quando usar

Use tabelas quando o usuario precisar comparar registros, filtrar informacoes ou executar acoes repetidas.

## Colunas

Colunas devem representar informacoes realmente uteis para decisao.

Evite excesso de colunas.

## Filtros

Filtros devem ser proximos da listagem e preservar o contexto.

## Ordenacao

Quando houver ordenacao, ela deve ser visualmente clara.

## Estados Vazios

Listas vazias devem explicar o estado e indicar a proxima acao quando aplicavel.

## Acoes por Linha

Acoes por linha devem ser consistentes entre modulos.

Acoes destrutivas devem ser protegidas por confirmacao.

## Paginacao

Use paginacao quando a quantidade de dados puder crescer.

## Responsividade

Em telas menores, priorize dados essenciais e preserve acesso as acoes.

## Boas praticas para CRUDs corporativos

### Listagens

Listagens devem permitir leitura rapida e comparacao.

### Busca

Busca deve ser simples, previsivel e tolerante quando possivel.

### Filtros

Filtros devem ser claros, reversiveis e indicar estado ativo.

### Ordenacao

Ordenacao deve ser visualmente identificavel.

### Estado vazio

Estado vazio deve explicar a ausencia de dados e indicar proxima acao quando aplicavel.

### Exportacao

Exportacao deve respeitar permissao, filtros aplicados e privacidade.

### Importacao

Importacao deve informar formato esperado, erros por linha e resultado final.

### Selecao

Selecao em massa deve deixar claro quantos itens estao selecionados.

### CRUD Compacto

Use quando a entidade for simples e puder ser criada/editada sem fluxo dedicado.

### CRUD Completo

Use quando a entidade exigir muitas informacoes, etapas, validacoes, anexos ou relacoes.
