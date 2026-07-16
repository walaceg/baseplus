# Heuristicas de Nielsen na Base+

## Objetivo

Aplicar as heuristicas classicas de usabilidade ao contexto da Base+.

## Visibilidade do Status

O sistema deve informar o que esta acontecendo.

Use carregamento, sucesso, erro, vazio e indisponibilidade de forma explicita.

## Correspondencia com o Mundo Real

Use termos do dominio do usuario.

Evite nomes tecnicos quando a tela for operacional.

## Controle e Liberdade

Permita cancelar, voltar, desfazer ou abandonar fluxos quando fizer sentido.

Acoes destrutivas devem exigir confirmacao.

## Consistencia e Padroes

Modulos diferentes devem reutilizar os mesmos padroes de formulario, tabela, botoes, filtros e feedback.

## Prevencao de Erros

Evite erro antes que aconteca.

Use validacao, mascara, limites, mensagens preventivas e estados desabilitados quando necessario.

## Reconhecimento em vez de Memorizacao

Mostre informacoes, opcoes e contexto suficientes para o usuario decidir sem depender de memoria.

## Flexibilidade e Eficiencia

Fluxos frequentes devem ser rapidos.

Usuarios experientes devem conseguir executar tarefas com poucos passos.

## Design Minimalista

Cada elemento deve ter funcao.

Remova explicacoes visuais desnecessarias, duplicidade e decoracao sem objetivo.

## Ajuda para Reconhecer e Corrigir Erros

Mensagens devem dizer o que ocorreu e como resolver.

Evite erros genericos quando houver acao clara.

## Ajuda e Documentacao

Quando a tarefa exigir conhecimento previo, ofereca suporte contextual sem poluir a interface.

## Estrutura oficial para avaliacao

Toda revisao baseada em Nielsen deve considerar, para cada heuristica:

- conceito original;
- interpretacao Base+;
- exemplos corretos;
- exemplos incorretos;
- checklist;
- recomendacoes para IA.

## Aplicacao em sistemas corporativos

### 1. Visibilidade do status

Conceito original: manter o usuario informado sobre o que esta acontecendo.

Interpretacao Base+: toda operacao relevante deve possuir estado visivel.

Exemplos corretos: loading ao salvar, progresso no upload, empty state em listagem vazia.

Exemplos incorretos: botao sem estado apos clique, tela em branco, upload sem retorno.

Checklist:

- [ ] Existe loading?
- [ ] Existe sucesso?
- [ ] Existe erro?
- [ ] Operacoes longas indicam andamento?

Recomendacao para IA: sempre solicitar estados de loading, sucesso, erro e vazio.

### 2. Correspondencia com o mundo real

Conceito original: usar linguagem familiar ao usuario.

Interpretacao Base+: usar termos do dominio corporativo, nao nomes tecnicos.

Exemplos corretos: "Cliente", "Pedido", "Filial", "Salvar alteracoes".

Exemplos incorretos: nomes de classes, mensagens SQL, codigos internos.

Checklist:

- [ ] Os termos pertencem ao dominio?
- [ ] As mensagens evitam linguagem tecnica?
- [ ] Rotulos sao compreensiveis?

Recomendacao para IA: gerar textos com linguagem humana e objetiva.

### 3. Controle e liberdade

Conceito original: permitir saidas claras, cancelar e desfazer quando aplicavel.

Interpretacao Base+: fluxos corporativos devem permitir cancelar, voltar e confirmar acoes de risco.

Exemplos corretos: cancelar formulario, confirmar exclusao, voltar para listagem.

Exemplos incorretos: exclusao imediata, modal sem saida, perda de dados apos erro.

Checklist:

- [ ] Existe cancelar ou voltar?
- [ ] Acoes destrutivas pedem confirmacao?
- [ ] Dados sao preservados em falha?

Recomendacao para IA: solicitar confirmacao e preservacao de dados.

### 4. Consistencia e padroes

Conceito original: evitar que o usuario precise adivinhar significados diferentes.

Interpretacao Base+: CRUDs, filtros, botoes e mensagens devem seguir o mesmo padrao.

Exemplos corretos: mesma posicao para salvar/cancelar, mesmos tokens, mesmas acoes.

Exemplos incorretos: cada modulo com visual proprio, nomes diferentes para a mesma acao.

Checklist:

- [ ] Segue componentes existentes?
- [ ] Usa design tokens?
- [ ] Mantem nomenclatura consistente?

Recomendacao para IA: reutilizar componentes e padroes existentes.

### 5. Prevencao de erros

Conceito original: prevenir erros antes de exibir mensagens.

Interpretacao Base+: reduzir entradas invalidas, cliques duplicados e acoes acidentais.

Exemplos corretos: validar obrigatorios, desabilitar botao enquanto salva, alertar exclusao.

Exemplos incorretos: salvar vazio, duplicar por duplo clique, excluir sem aviso.

Checklist:

- [ ] Campos obrigatorios sao tratados?
- [ ] Acoes repetidas sao bloqueadas?
- [ ] Erros previsiveis sao evitados?

Recomendacao para IA: incluir validacao e estados desabilitados.

### 6. Reconhecimento em vez de memorizacao

Conceito original: mostrar opcoes e contexto visiveis.

Interpretacao Base+: menus, breadcrumbs, filtros e labels devem reduzir memoria exigida.

Exemplos corretos: titulo claro, labels visiveis, filtros ativos.

Exemplos incorretos: campo apenas com placeholder, tela sem titulo, acao escondida.

Checklist:

- [ ] A tela possui titulo claro?
- [ ] Campos possuem labels?
- [ ] Contexto esta visivel?

Recomendacao para IA: solicitar labels persistentes e contexto de pagina.

### 7. Flexibilidade e eficiencia

Conceito original: atender usuarios iniciantes e experientes.

Interpretacao Base+: fluxos frequentes devem ser rapidos.

Exemplos corretos: busca, filtros, acoes por linha.

Exemplos incorretos: muitos passos para tarefa simples, filtros escondidos.

Checklist:

- [ ] Tarefas frequentes sao rapidas?
- [ ] Existem filtros adequados?
- [ ] Acoes comuns estao acessiveis?

Recomendacao para IA: reduzir passos em fluxos recorrentes.

### 8. Design estetico e minimalista

Conceito original: remover informacoes irrelevantes.

Interpretacao Base+: elegancia corporativa nasce de clareza, hierarquia e foco.

Exemplos corretos: paineis objetivos, texto curto, foco na tarefa.

Exemplos incorretos: hero em tela administrativa, cards decorativos, sombras pesadas.

Checklist:

- [ ] Existe excesso de informacao?
- [ ] Existe excesso de cor?
- [ ] A tela respira?

Recomendacao para IA: solicitar interface discreta, corporativa e sem ornamentacao inutil.

### 9. Ajuda para reconhecer e corrigir erros

Conceito original: erros devem ser claros e indicar solucao.

Interpretacao Base+: mensagens devem dizer o que aconteceu e o que fazer agora.

Exemplos corretos: "Verifique os campos obrigatorios", "Sessao expirada".

Exemplos incorretos: "Erro 500", stack trace, falha sem orientacao.

Checklist:

- [ ] Mensagem explica o problema?
- [ ] Mensagem orienta proxima acao?
- [ ] Dados sensiveis nao sao expostos?

Recomendacao para IA: ocultar detalhes tecnicos e gerar mensagens acionaveis.

### 10. Ajuda e documentacao

Conceito original: oferecer suporte quando necessario.

Interpretacao Base+: ajuda contextual deve orientar sem poluir.

Exemplos corretos: tooltip para icone ambiguo, texto auxiliar em campo complexo.

Exemplos incorretos: manual inteiro dentro da tela, ajuda ausente em processo complexo.

Checklist:

- [ ] Campos complexos possuem ajuda?
- [ ] Icones ambiguos possuem tooltip?
- [ ] Documentacao e referenciada quando necessario?

Recomendacao para IA: usar ajuda contextual discreta.
