# Base+ 1.0

Base+ 1.0 e a primeira versao fechada da fundacao administrativa reutilizavel.

## Objetivo

Entregar uma base pronta para iniciar novos modulos administrativos e sistemas de negocio, com seguranca, identidade visual, auditoria, perfis e padroes de desenvolvimento assistido por IA.

## Escopo incluido

- Autenticacao JWT, refresh token e logout.
- Troca de senha inicial.
- Conta do usuario, preferencias, avatar e sessoes.
- CRUD de usuarios.
- CRUD de perfis funcionais e organizacionais.
- CRUD de permissions.
- Autorizacao granular obrigatoria por permission.
- Estrutura organizacional parametrizavel:
  - tipos organizacionais;
  - unidades organizacionais;
  - escopos em perfis organizacionais.
- Branding com configuracoes, upload de assets e white label.
- Auditoria com consulta de logs.
- Dashboard administrativo inicial.
- Health checks.
- Kit visual padrao da marca Base+.
- Documentacao operacional para IA/Codex.
- Templates de novos modulos com CRUD Compacto e CRUD Completo.
- Script de validacao local.

## Fora do escopo da 1.0

- Primeiro modulo real de negocio.
- Validacao de escopo organizacional aplicada a registros de negocio.
- Pipeline CI/CD.
- Testes E2E do frontend.
- Contrato formal completo da API.
- Estrategia final de banco persistente para producao.
- Observabilidade avancada.

## Ambiente local

Backend:

```powershell
cd C:\dev\baseplus\baseplus-backend
mvn spring-boot:run
```

Frontend:

```powershell
cd C:\dev\baseplus\baseplus-frontend
npm run dev
```

URLs:

```text
Backend: http://localhost:8080
Frontend: http://localhost:5173
```

Usuario dev:

```text
admin@baseplus.com
Baseplus@123
```

## Validacao de release

Execute:

```powershell
cd C:\dev\baseplus
.\scripts\check-project.ps1
```

Resultado esperado:

```text
Backend tests OK
Frontend build OK
Base+ validation finished.
```

## Checklist manual

Validar no navegador:

- `/login`
- `/app/dashboard`
- `/app/usuarios`
- `/app/roles`
- `/app/permissions`
- `/app/organizacao`
- `/app/branding`
- `/app/auditoria`
- `/app/conta`

Fluxos essenciais:

- Login com admin dev.
- Criar tipo organizacional.
- Criar unidade organizacional.
- Criar perfil organizacional e vincular escopo.
- Criar perfil funcional e vincular permissions.
- Verificar menus e botoes conforme permissoes.

## Proxima versao sugerida

Base+ 1.1 deve priorizar:

- modulo modelo real, preferencialmente Empresas;
- validacao de escopo organizacional por registro de negocio;
- `.env.example`;
- estrategia oficial PostgreSQL/Flyway para ambiente persistente;
- testes frontend/E2E para fluxos criticos.
