# Branding - Base+

Este guia complementa `BRAND_GUIDE.md`.

## Objetivo

Manter uma identidade visual padrao para a Base+ sem bloquear personalizacao por cliente, produto ou ambiente.

## Assets padrao

Arquivos fonte oficiais:

```text
brand/logo-principal.png
brand/logo-horizontal.png
brand/simbolo.png
brand/app-icons-source.png
```

Arquivos publicos para frontend:

```text
baseplus-frontend/public/brand/logo-principal.png
baseplus-frontend/public/brand/logo-horizontal.png
baseplus-frontend/public/brand/simbolo.png
baseplus-frontend/public/brand/app-icons-source.png
```

URLs publicas:

```text
/brand/logo-principal.png
/brand/logo-horizontal.png
/brand/simbolo.png
/brand/app-icons-source.png
```

## Regra de precedencia

Sempre preservar:

```text
branding configurado > asset padrao Base+ > fallback textual
```

Exemplos:

- `branding.logoUrl` prevalece sobre `/brand/logo-principal.png`.
- `branding.compactLogoUrl` prevalece sobre `/brand/simbolo.png`.
- `branding.faviconUrl` prevalece sobre qualquer favicon padrao.
- White label prevalece sobre textos padrao.

## Formatos de upload

- Logos, marcas reduzidas, logos de login e backgrounds aceitam somente imagens raster validas: `PNG`, `JPG` ou `JPEG`.
- Favicons aceitam tambem `ICO` valido.
- SVG continua permitido apenas como asset empacotado e revisado no frontend; arquivos SVG nao sao aceitos nem servidos em `/uploads/**`.

## O que nao fazer

- Nao remover upload de assets.
- Nao substituir white label por assets fixos.
- Nao hardcodar logo em componentes que ja recebem `logoUrl`.
- Nao apagar `BRAND_GUIDE.md` ou a pasta `brand/`.
- Nao usar `app-icons-source.png` como favicon final sem separar uma variacao individual.

## Quando alterar este guia

Atualize este documento quando:

- novos assets oficiais forem adicionados;
- o fluxo de branding mudar;
- houver novos tamanhos/formatos obrigatorios;
- a estrategia de fallback visual for implementada no codigo.
