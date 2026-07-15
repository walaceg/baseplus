# Quality Gates da Base+

## Objetivo

Definir gates de qualidade para validar evolucoes da Base+ antes de commit, homologacao e release.

Quality Gates existem para garantir que novas funcionalidades sejam avaliadas de forma consistente.

## Gate UX

UX e gate oficial da Base+.

Antes de implementar ou publicar funcionalidades com impacto visual ou de fluxo, validar:

- Manifesto Base+;
- Principios de Elegancia;
- Nielsen;
- consistencia;
- elegancia;
- feedback;
- performance percebida;
- navegacao;
- acessibilidade;
- estados vazios;
- loading;
- formularios;
- tabelas;
- componentes.

## Gate Elegancia

Elegancia e parte do Gate UX.

Toda alteracao visual deve validar:

- simplicidade;
- baixa carga cognitiva;
- hierarquia visual;
- ritmo visual;
- consistencia;
- poucas cores;
- poucas bordas;
- espacamento;
- tipografia;
- feedback;
- estados vazios;
- independencia do branding.

## Criterio

Uma entrega nao deve seguir para release quando houver problema de UX que comprometa compreensao, acessibilidade, confianca, seguranca operacional ou execucao da tarefa principal.

## Documentos relacionados

- `engineering/ux/README.md`
- `engineering/ux/ux-manifesto.md`
- `engineering/ux/elegance.md`
- `engineering/ux/nielsen.md`
- `engineering/ux/quality-checklist.md`
- `engineering/ux/review-playbook.md`
