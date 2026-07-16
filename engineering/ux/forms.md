# Formularios

## Objetivo

Definir diretrizes para formularios em aplicacoes Base+.

## Organizacao

Formularios devem ser agrupados por significado.

Campos relacionados devem aparecer proximos.

## Rotulos

Todo campo deve possuir rotulo claro.

Evite depender apenas de placeholder.

## Validacao

Valide entradas de forma objetiva.

Mensagens devem indicar o problema e, quando possivel, como corrigir.

## Campos Obrigatorios

Campos obrigatorios devem ser identificaveis sem poluir a tela.

## Acoes

Use acoes previsiveis:

- salvar;
- cancelar;
- criar;
- atualizar;
- excluir;
- voltar.

## Formularios Longos

Para formularios longos, considere secoes, abas ou paginas dedicadas.

Nao force CRUD compacto quando o dominio exigir fluxo completo.

## Preservacao de Dados

Em erro, preserve os dados preenchidos sempre que possivel.

## Diretrizes oficiais

### Labels

Todo campo deve possuir label visivel e objetiva.

### Placeholder

Placeholder deve ajudar com exemplo, nao substituir rotulo.

### Ajuda contextual

Use para campos complexos, regras especificas ou impacto operacional.

### Campos obrigatorios

Campos obrigatorios devem ser identificaveis de forma consistente.

### Validacao

Validacao deve indicar o problema e como corrigir.

### Ordem logica

Campos devem seguir a ordem mental do usuario, nao a ordem tecnica do banco.

### Agrupamento

Agrupe por significado e use secoes quando o formulario crescer.

### Autofocus

Use com cuidado para nao deslocar o usuario inesperadamente.

### Salvar e cancelar

Salvar deve indicar processamento e evitar duplo envio. Cancelar deve ser previsivel.

### Desfazer

Quando aplicavel, oferecer desfazer para acoes de baixo risco.

### Reducao de carga cognitiva

Remova campos desnecessarios, divida por secoes e use progressive disclosure.

### Divisao em etapas

Use etapas para processos longos ou dependentes. Nao transforme formulario simples em wizard.
