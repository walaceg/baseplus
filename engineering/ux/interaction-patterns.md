# Padroes de Interacao

## Objetivo

Definir comportamentos esperados para interacoes da Base+.

## Acoes

Acoes principais devem ser claras e previsiveis.

Use uma acao primaria por contexto quando possivel.

## Estados

Componentes interativos devem considerar:

- padrao;
- hover;
- foco;
- ativo;
- desabilitado;
- carregando;
- erro;
- sucesso.

## Confirmacoes

Acoes destrutivas, irreversiveis ou de alto impacto devem exigir confirmacao.

## Navegacao entre Fluxos

O usuario deve entender onde esta, de onde veio e como voltar.

## Carregamento

Evite telas silenciosas durante operacoes.

Use indicadores proporcionais ao tempo esperado.

## Erros

Erros devem preservar os dados ja informados sempre que possivel.

## Permissoes

Quando uma acao nao estiver disponivel por permissao, a interface deve evitar friccao desnecessaria e nunca prometer algo que o usuario nao pode executar.

## Padroes oficiais

### Loading

Use quando uma operacao aguarda resposta. Evite bloquear a tela inteira se apenas uma area estiver carregando.

### Skeleton

Use quando a estrutura do conteudo e conhecida e o carregamento pode demorar.

### Spinner

Use para acoes curtas ou carregamento pontual. Evite spinner sem texto em operacoes longas.

### Upload

Uploads devem informar arquivo selecionado, progresso quando possivel, sucesso, erro e restricoes.

### Progress

Use em processos com etapas ou duracao perceptivel.

### Toast

Use para feedback rapido e nao bloqueante. Evite toast para erros que exigem decisao.

### Modal

Use para confirmacao, formulario curto ou decisao focada. Evite modal para fluxos longos.

### Drawer

Use para detalhes laterais, filtros ou edicao auxiliar sem perder contexto.

### Wizard

Use para processos longos com etapas claras. Evite wizard para formularios simples.

### Confirmacoes e exclusao

Acoes destrutivas, irreversiveis ou de alto impacto devem exigir confirmacao.

### Pesquisa e filtros

Pesquisa deve ter escopo claro. Filtros devem ser visiveis, compreensiveis e reversiveis.

### Paginacao

Use quando o volume de dados puder crescer.

### Autocomplete

Use para listas grandes ou termos conhecidos.

### Tabs

Use para separar secoes equivalentes dentro do mesmo contexto.

### Accordion

Use para detalhes secundarios. Evite esconder informacao essencial.

### Tooltip

Use para explicar icones ou termos curtos. Evite instrucoes longas.

### Popover

Use para opcoes contextuais leves. Evite conteudo critico.
