# Task Prompt - Base+

Use este modelo para novas interacoes com IA/Codex.

```text
Projeto: C:\dev\baseplus

Antes de alterar qualquer coisa, leia:
- C:\dev\baseplus\AI_CONTEXT.md
- C:\dev\baseplus\state.txt
- C:\dev\baseplus\README.md

Se a tarefa envolver novo modulo, leia tambem:
- C:\dev\baseplus\MODULE_TEMPLATE.md
- C:\dev\baseplus\docs\module-development.md

Se a tarefa envolver marca, logo, tema, white label ou branding, leia tambem:
- C:\dev\baseplus\BRAND_GUIDE.md
- C:\dev\baseplus\docs\branding.md

Tarefa:
<descreva aqui>

Regras:
- Siga os padroes atuais do projeto.
- Nao remova nem limite a personalizacao existente do modulo Branding.
- Nao altere arquitetura global sem necessidade.
- Nao apague nem reverta alteracoes existentes sem pedido explicito.
- Para novos modulos, declare CRUD Compacto ou CRUD Completo.
- Rode testes/build relevantes.
- Atualize state.txt se o estado do projeto mudou.
- Atualize CHANGELOG.md se houve mudanca funcional ou operacional relevante.
```

## Exemplos

CRUD Compacto:

```text
Crie o modulo Categorias usando CRUD Compacto.

Campos:
- nome
- descricao
- ativo
```

CRUD Completo:

```text
Crie o modulo Empresas usando CRUD Completo.

Campos:
- razaoSocial
- nomeFantasia
- documento
- email
- telefone
- ativo
```

