# Processo Oficial de Release da Base+

## Objetivo

Este documento define como a Base+ evolui entre versoes.

O processo de release existe para garantir estabilidade, previsibilidade e rastreabilidade da plataforma.

Cada release deve representar um estado conhecido, validado e documentado, capaz de servir como referencia para novas aplicacoes e para evolucoes futuras.

## Ciclo de Vida da Plataforma

Fluxo oficial:

```text
Desenvolvimento
↓
Validacao Local (DEV)
↓
Homologacao (HOM)
↓
Correcoes
↓
Consolidacao
↓
Release
↓
Producao
```

Cada etapa deve reduzir incerteza e aumentar confianca sobre a versao candidata.

## Tipos de Versao

A Base+ utiliza Semantic Versioning.

Formato:

```text
MAJOR.MINOR.PATCH
```

### MAJOR

Usado para grandes mudancas, que podem alterar fundamentos da plataforma ou exigir adaptacao relevante de aplicacoes derivadas.

Exemplo:

```text
1.2.0 -> 2.0.0
```

### MINOR

Usado para evolucoes compativeis, novas capacidades estruturais, novos recursos de plataforma ou melhorias relevantes sem quebra planejada.

Exemplo:

```text
1.1.0 -> 1.2.0
```

### PATCH

Usado para correcoes, ajustes pequenos, melhorias localizadas e reparos que nao alteram a direcao da versao.

Exemplo:

```text
1.1.0 -> 1.1.1
```

## Consolidacao de uma Versao

Durante a consolidacao, uma versao candidata ainda pode receber pequenos ajustes.

Esses ajustes devem estar diretamente relacionados ao fechamento da versao e podem incluir:

- correcoes de falhas encontradas na validacao;
- ajustes de documentacao;
- pequenos refinamentos de operacao;
- correcoes de compatibilidade;
- reparos em fluxos ja previstos no escopo.

A consolidacao ocorre apenas enquanto a versao ainda nao foi oficialmente congelada.

## Congelamento (Freeze)

Freeze e o momento em que a versao se torna imutavel.

Apos o congelamento:

- a versao torna-se imutavel;
- nao deve receber alteracoes;
- novas correcoes deverao gerar uma nova versao.

O freeze protege rastreabilidade, confianca e reproducibilidade.

Uma tag publicada deve representar exatamente o estado congelado da plataforma.

## Correcoes

Correcoes devem usar versao PATCH.

Exemplo:

```text
1.1.0
↓
1.1.1
↓
1.1.2
```

Use PATCH para:

- corrigir regressao;
- ajustar documentacao publicada;
- corrigir comportamento localizado;
- reparar problema operacional sem alterar a arquitetura;
- aplicar melhoria pequena e compativel.

## Evolucao

Evolucoes devem usar versao MINOR.

Exemplo:

```text
1.1.0
↓
1.2.0
```

Use MINOR para:

- adicionar capacidade de plataforma;
- criar novo padrao oficial;
- melhorar arquitetura sem quebra;
- adicionar suporte operacional;
- consolidar melhorias compatíveis.

## Grandes Mudancas

Grandes mudancas devem usar versao MAJOR.

Exemplo:

```text
1.x.x
↓
2.0.0
```

Use MAJOR para:

- mudancas incompatíveis;
- revisao estrutural relevante;
- alteracao profunda de contratos;
- migracao arquitetural significativa;
- mudanca que exige adaptacao planejada de aplicacoes derivadas.

## Fluxo Oficial

Fluxo oficial de release:

```text
Implementacao
↓
Testes
↓
Documentacao
↓
Homologacao
↓
Validacao
↓
Tag
↓
Producao
```

Cada etapa deve gerar evidencias suficientes para justificar a publicacao.

## Criterios para uma Release

Requisitos minimos:

- testes executados;
- documentacao atualizada;
- Docker validado;
- Bootstrap validado;
- Branding validado;
- Uploads validados;
- Atualizacao validada;
- Rollback documentado;
- sem erros conhecidos criticos.

Quando algum criterio nao puder ser validado, a release deve registrar a ressalva e o risco associado.

## Processo para Aplicacoes Derivadas

Aplicacoes construidas sobre a Base+ podem adotar este mesmo fluxo.

O processo deve ser ajustado ao contexto operacional da aplicacao, mas deve preservar:

- versionamento claro;
- changelog;
- tag Git;
- homologacao;
- validacao;
- estrategia de rollback;
- documentacao atualizada.

Aplicacoes derivadas devem registrar qual baseline da Base+ foi usada como origem.

## Historico

Todas as releases devem possuir:

- changelog;
- documentacao;
- tag Git;
- versao identificavel.

O historico de releases deve permitir entender:

- o que mudou;
- por que mudou;
- como validar;
- quais riscos existem;
- como atualizar ambientes existentes.

Sem historico claro, a plataforma perde rastreabilidade e capacidade de evolucao segura.
