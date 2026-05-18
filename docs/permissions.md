# Permissions - Base+

Este guia resume o padrao de autorizacao para novos modulos.

## Modelo

A Base+ usa:

- autenticacao JWT;
- roles/perfis;
- permissions/permissoes.

O backend deve ser a fonte da verdade para autorizacao.

## Regra oficial

A Base+ usa permissoes granulares obrigatorias.

Roles nao liberam acoes por si so quando o endpoint exige uma permission.

```text
Role agrupa permissoes.
Permission autoriza a acao.
```

Na primeira versao da base existem dois tipos principais de perfil:

```text
Perfil funcional = agrupa permissions.
Perfil organizacional = agrupa escopos de acesso.
Usuario recebe ambos quando necessario.
```

O perfil organizacional nao substitui permissions. Ele limita ou amplia onde a acao pode ocorrer depois que a permission funcional ja autorizou a acao.

Exemplo:

```text
ROLE_ADMIN sem ADMIN_ACCESS nao acessa endpoint que exige ADMIN_ACCESS.
ROLE_EMPRESAS_ADMIN sem EMPRESAS_EDIT nao edita empresas.
```

Use verificacao por role somente quando a regra realmente for sobre o perfil em si.

## Padrao por modulo

Use:

```text
<MODULO>_VIEW
<MODULO>_CREATE
<MODULO>_EDIT
<MODULO>_DELETE
```

Exemplo:

```text
EMPRESAS_VIEW
EMPRESAS_CREATE
EMPRESAS_EDIT
EMPRESAS_DELETE
```

## Roles por modulo

Roles devem ser usadas como agrupamentos de permissoes por modulo ou responsabilidade.

Padrao recomendado:

```text
<MODULO>_ADMIN
<MODULO>_OPERADOR
<MODULO>_LEITOR
```

Exemplo para empresas:

```text
EMPRESAS_ADMIN
EMPRESAS_OPERADOR
EMPRESAS_LEITOR
```

Sugestao de agrupamento:

```text
EMPRESAS_ADMIN
- EMPRESAS_VIEW
- EMPRESAS_CREATE
- EMPRESAS_EDIT
- EMPRESAS_DELETE

EMPRESAS_OPERADOR
- EMPRESAS_VIEW
- EMPRESAS_CREATE
- EMPRESAS_EDIT

EMPRESAS_LEITOR
- EMPRESAS_VIEW
```

Roles transversais continuam permitidas quando fizerem sentido:

```text
ADMIN
AUDITOR
SUPORTE
GESTOR
```

Mesmo nesses casos, o acesso a endpoints por permission deve depender das permissions vinculadas ao role.

## Escopo organizacional

O escopo organizacional e parametrizavel. Tipos como empresa, filial, local de expedicao, equipe de vendas ou centro de custo sao apenas exemplos de `OrganizationUnitType`.

O modelo oficial e:

```text
OrganizationUnitType define o tipo de unidade.
OrganizationUnit define a unidade real.
Perfil organizacional vincula unidades com nivel VIEW, EDIT ou ADMIN.
```

Na interface administrativa, estes cadastros ficam em:

```text
Configuracoes > Estrutura org.
/app/organizacao
```

Exemplo:

```text
Tipo: EMPRESA
Unidade: 0001 - Matriz

Tipo: EQUIPE_VENDAS
Unidade: SP-B2B - Vendas B2B Sao Paulo
```

Um modulo que precise restringir dados por escopo deve validar duas coisas:

```text
1. Permission funcional para a acao.
2. Escopo organizacional compativel com o registro acessado.
```

Assim, um usuario pode ter `PEDIDOS_EDIT`, mas editar somente pedidos dentro das unidades organizacionais que seus perfis organizacionais permitem.

## Permissoes extras

Use somente quando houver comportamento real diferente:

```text
<MODULO>_EXPORT
<MODULO>_IMPORT
<MODULO>_APPROVE
<MODULO>_MANAGE_USERS
<MODULO>_UPLOAD_ASSETS
```

## Backend

Endpoints devem ser protegidos por permission quando aplicavel.

Exemplo:

```java
@PreAuthorize("@authorizationService.hasPermission('EMPRESAS_VIEW')")
```

Evite usar role como atalho para permissoes granulares.

## Frontend

O frontend deve:

- esconder ou desabilitar acoes sem permission;
- proteger rotas com `ProtectedRoute`;
- reutilizar `PERMISSIONS` em `shared/auth/permissions.js`.

O frontend nao substitui a autorizacao do backend.

## Checklist

1. Criar/registrar permissions.
2. Criar roles de agrupamento por modulo quando aplicavel.
3. Associar permissions aos roles.
4. Definir se o modulo exige escopo organizacional.
5. Criar tipos/unidades organizacionais quando o negocio exigir.
6. Proteger endpoints.
7. Proteger rotas.
8. Controlar acoes visuais.
9. Criar testes relevantes de autorizacao.
