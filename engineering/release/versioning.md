# Padrao Oficial de Versionamento da Base+

## Objetivo

Este documento define como a Base+ identifica versoes de desenvolvimento, release candidates, releases oficiais e correcoes.

O objetivo e manter consistencia entre backend Maven, frontend, documentacao, Git e processo de release.

## Formatos Oficiais

A Base+ utiliza Semantic Versioning no formato:

```text
MAJOR.MINOR.PATCH
```

### Desenvolvimento

Versoes em desenvolvimento devem usar o sufixo `SNAPSHOT`:

```text
x.y.z-SNAPSHOT
```

Use este formato enquanto a versao ainda estiver recebendo implementacoes sem compromisso de estabilidade.

### Release Candidate

Versoes candidatas devem usar o sufixo `RC` com numeracao sequencial:

```text
x.y.z-RC1
x.y.z-RC2
```

Use este formato quando a versao estiver em validacao final, antes da publicacao oficial.

### Release Oficial

Releases oficiais nao utilizam sufixo:

```text
x.y.z
```

Exemplo:

```text
1.2.0
```

A release oficial deve estar sincronizada entre:

- `baseplus-backend/pom.xml`;
- `baseplus-frontend/package.json`;
- `baseplus-frontend/package-lock.json`;
- documentacao oficial;
- tag Git.

## Incrementos

### PATCH

Incrementa o terceiro numero.

Use para correcoes compativeis:

```text
1.2.0 -> 1.2.1
```

### MINOR

Incrementa o segundo numero.

Use para evolucoes compativeis da plataforma:

```text
1.2.0 -> 1.3.0
```

### MAJOR

Incrementa o primeiro numero.

Use para mudancas incompativeis ou revisoes estruturais profundas:

```text
1.2.0 -> 2.0.0
```

## Release Oficial v1.2.0

A Base+ `v1.2.0` e registrada como release oficial atual da plataforma.

A `v1.1.0` permanece como baseline anterior e referencia historica.

Apos a criacao da tag Git `v1.2.0`, essa versao deve ser tratada como imutavel. Correcoes posteriores devem gerar nova versao PATCH, como `v1.2.1`.

## Regras de Governanca

- Nao publicar release oficial com sufixo `SNAPSHOT` ou `RC`.
- Nao mover tag Git apos publicacao.
- Nao reutilizar numero de versao para conteudo diferente.
- Nao alterar release congelada; criar patch quando necessario.
- Manter changelog, documentacao e metadados de build sincronizados.