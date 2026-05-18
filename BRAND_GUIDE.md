# Brand Guide - Base+

Este documento define o kit visual padrao da plataforma Base+.

As imagens oficiais foram copiadas para:

```text
brand/
+-- logo-principal.png
+-- logo-horizontal.png
+-- simbolo.png
+-- app-icons-source.png

baseplus-frontend/public/brand/
+-- logo-principal.png
+-- logo-horizontal.png
+-- simbolo.png
+-- app-icons-source.png
```

## Regra importante

Estes arquivos sao os assets oficiais padrao da plataforma Base+.

Eles nao substituem nem limitam as funcionalidades de personalizacao existentes no modulo Branding.

O sistema deve continuar permitindo:

- upload de logo principal;
- upload de logo compacta;
- upload de favicon;
- upload de logo da tela de login;
- upload de background da tela de login;
- white label;
- alteracao de nome, subtitulo, tema, cores e densidade visual.

Quando houver configuracao personalizada salva no Branding, ela deve prevalecer sobre os assets padrao.

## Assets

### `logo-principal.png`

Uso recomendado:

- tela de login;
- areas institucionais;
- apresentacoes internas;
- pontos onde ha espaco horizontal suficiente.

Origem:

```text
imagens uteis/LogoPrincipal.png
```

### `logo-horizontal.png`

Uso recomendado:

- topbar;
- sidebar;
- cabecalhos compactos;
- lugares onde o logo principal fica grande demais.

Origem:

```text
imagens uteis/HorizontalSimbolo.png
```

### `simbolo.png`

Uso recomendado:

- sidebar recolhida;
- marca compacta;
- fallback institucional;
- favicon quando ainda nao houver favicon especifico.

Origem:

```text
imagens uteis/Simbolo Icone.png
```

### `app-icons-source.png`

Prancha fonte com variacoes de icone.

Uso recomendado:

- referencia visual;
- geracao futura de icones individuais;
- material de design.

Nao usar diretamente como favicon ou icone final da aplicacao sem antes separar a variacao escolhida.

Origem:

```text
imagens uteis/appicones.png
```

## Caminhos publicos no frontend

Os arquivos em `baseplus-frontend/public/brand` podem ser referenciados pelo frontend como:

```text
/brand/logo-principal.png
/brand/logo-horizontal.png
/brand/simbolo.png
/brand/app-icons-source.png
```

Use esses caminhos apenas como assets padrao ou fallback visual.

Nao remova o fluxo atual de personalizacao por API/upload.

## Recomendacao para implementacao futura

Se a aplicacao passar a usar estes assets como fallback visual automatico, implemente a regra assim:

```text
branding customizado salvo > asset padrao Base+ > fallback textual
```

Exemplo:

```text
logoUrl configurado > /brand/logo-principal.png > iniciais "B+"
compactLogoUrl configurado > /brand/simbolo.png > iniciais "B+"
faviconUrl configurado > /brand/simbolo.png > favicon SVG gerado
```

Essa mudanca deve ser feita com cuidado para preservar white label e configuracoes existentes.

