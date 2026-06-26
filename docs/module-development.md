# Module Development - Base+

Este guia descreve como iniciar novas funcionalidades e modulos na Base+.

## Antes de comecar

Leia:

- `AI_CONTEXT.md`
- `state.txt`
- `MODULE_TEMPLATE.md`
- `docs/integrations.md`, se o modulo receber ou consumir chamadas externas
- `BRAND_GUIDE.md`, se a tarefa envolver marca ou branding

Declare o tipo de CRUD:

- CRUD Compacto
- CRUD Completo

## CRUD Compacto

Use para cadastros pequenos, auxiliares e rapidos.

Exemplo de pedido:

```text
Projeto: C:\dev\baseplus

Leia AI_CONTEXT.md, state.txt e MODULE_TEMPLATE.md.

Crie o modulo Categorias usando CRUD Compacto.

Campos:
- nome
- descricao
- ativo

Permissoes:
- CATEGORIAS_VIEW
- CATEGORIAS_CREATE
- CATEGORIAS_EDIT
- CATEGORIAS_DELETE

Ao finalizar:
- registrar rotas e menu;
- criar testes backend relevantes;
- rodar build/testes relevantes;
- atualizar state.txt e CHANGELOG.md.
```

Estrutura esperada:

```text
src/modules/categoria/
+-- categoria.css
+-- categoriaService.js
+-- CategoriasPage.jsx
+-- CategoriaFormModal.jsx
```

## CRUD Completo

Use para cadastros principais, com muitos campos, secoes, vinculos ou URL propria.

Exemplo de pedido:

```text
Projeto: C:\dev\baseplus

Leia AI_CONTEXT.md, state.txt e MODULE_TEMPLATE.md.

Crie o modulo Empresas usando CRUD Completo.

Campos:
- razaoSocial
- nomeFantasia
- documento
- email
- telefone
- ativo

Permissoes:
- EMPRESAS_VIEW
- EMPRESAS_CREATE
- EMPRESAS_EDIT
- EMPRESAS_DELETE

Ao finalizar:
- registrar rotas e menu;
- criar testes backend relevantes;
- rodar build/testes relevantes;
- atualizar state.txt e CHANGELOG.md.
```

Estrutura esperada:

```text
src/modules/empresa/
+-- empresa.css
+-- empresaService.js
+-- EmpresasPage.jsx
+-- EmpresaFormPage.jsx
```

Rotas esperadas:

```text
/app/empresas
/app/empresas/novo
/app/empresas/:id/editar
```

## Checklist de implementacao

1. Confirmar o tipo de CRUD.
2. Criar entidade backend.
3. Criar DTOs.
4. Criar repository.
5. Criar service.
6. Criar controller.
7. Definir adapters, controller externo, autenticacao e idempotencia quando houver integracao.
8. Criar/registrar permissoes.
9. Criar service frontend.
10. Criar telas.
11. Registrar rotas.
12. Registrar menu.
13. Criar testes backend relevantes.
14. Rodar `scripts/check-project.ps1` ou comandos equivalentes.
15. Atualizar `state.txt` e `CHANGELOG.md`.

## Padrao de permissoes

```text
<MODULO>_VIEW
<MODULO>_CREATE
<MODULO>_EDIT
<MODULO>_DELETE
```

Permissoes extras devem ser especificas:

```text
<MODULO>_EXPORT
<MODULO>_IMPORT
<MODULO>_APPROVE
<MODULO>_MANAGE_USERS
```

## Cuidados

- Nao duplicar componentes compartilhados.
- Nao colocar regra sensivel apenas no frontend.
- Nao quebrar a personalizacao do Branding.
- Nao alterar arquitetura global para resolver problema local.
- Nao reverter alteracoes existentes sem pedido explicito.
