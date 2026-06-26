# Checklist de validacao - Base+ v1.1.0

Checklist oficial para fechar a Base+ v1.1.0 antes de commit, tag ou publicacao.

## Premissas

- Executar a partir de `C:\dev\baseplus`.
- Nao usar segredos reais nos comandos documentados.
- Garantir que alteracoes locais esperadas estejam revisadas antes do commit.
- Usar `baseplus-backend/.env` local para Docker, sem versionar esse arquivo.

## 1. Estado inicial

```powershell
cd C:\dev\baseplus
git status --short
```

Validar:

- [ ] Nao ha arquivos temporarios indevidos.
- [ ] Nao ha `.env` real versionado.
- [ ] Alteracoes locais correspondem ao escopo da v1.1.0.
- [ ] `SecurityConfig.java` e `vite.config.js` nao possuem dominio de tunel fixo.

## 2. Backend tests

```powershell
cd C:\dev\baseplus\baseplus-backend
mvn test
```

Validar:

- [ ] Build Maven finaliza com `BUILD SUCCESS`.
- [ ] Nao ha falhas, erros ou testes inesperadamente ignorados.

## 3. PostgreSQL compatibility test

Com Docker disponivel:

```powershell
cd C:\dev\baseplus\baseplus-backend
mvn -Dtest=PostgreSqlCompatibilityTest test
```

Validar:

- [ ] PostgreSQL 16 sobe via Testcontainers quando Docker esta disponivel.
- [ ] Flyway executa em banco vazio.
- [ ] Hibernate `ddl-auto=validate` passa.
- [ ] Se Docker nao estiver disponivel, o teste e ignorado sem quebrar a build.

## 4. Frontend build

```powershell
cd C:\dev\baseplus\baseplus-frontend
npm run build
```

Validar:

- [ ] Build Vite finaliza com sucesso.
- [ ] Diretorio `dist` e gerado.
- [ ] Nao ha erro de TypeScript, bundling ou import.

## 5. Docker Compose

```powershell
cd C:\dev\baseplus\baseplus-backend
docker compose config
docker compose build
docker compose up --detach --wait
docker compose ps
```

Validar:

- [ ] `docker compose config` resolve sem erro.
- [ ] Backend e frontend constroem com sucesso.
- [ ] `postgres` fica `healthy`.
- [ ] `backend` fica `healthy`.
- [ ] `frontend` fica `healthy`.
- [ ] PostgreSQL nao publica porta no host.
- [ ] Volume `baseplus_baseplus_postgres_data` existe.
- [ ] Volume `baseplus_baseplus_uploads` existe.

## 6. Health e readiness

```powershell
curl.exe http://127.0.0.1:5173/api/health
curl.exe http://127.0.0.1:5173/api/health/ready
```

Validar:

- [ ] `/api/health` retorna HTTP 200.
- [ ] `/api/health/ready` retorna HTTP 200.
- [ ] Readiness retorna `database: "UP"`.
- [ ] Readiness nao expoe host, usuario, senha, JDBC URL ou detalhes internos.

## 7. Login, logout e refresh

Antes do primeiro login em ambiente Docker limpo, executar o bootstrap administrativo:

```powershell
cd C:\dev\baseplus\baseplus-backend
$env:BASEPLUS_BOOTSTRAP_ADMIN_ENABLED = 'true'
$env:BASEPLUS_BOOTSTRAP_ADMIN_NAME = 'Administrador'
$env:BASEPLUS_BOOTSTRAP_ADMIN_EMAIL = 'admin@empresa.com'
$env:BASEPLUS_BOOTSTRAP_ADMIN_PASSWORD = '<senha-forte>'
docker compose run --rm backend java -jar /app/app.jar --spring.main.web-application-type=none
```

Validar:

- [ ] Bootstrap cria o primeiro usuario administrador em banco PostgreSQL vazio.
- [ ] Senha nao e gravada em texto.
- [ ] Usuario criado recebe perfil `ADMIN`.
- [ ] Nova execucao do bootstrap e recusada quando ja existe administrador.
- [ ] O profile `dev` continua usando `UsuarioSeed` apenas localmente.

Validar no navegador:

- [ ] Abrir `http://127.0.0.1:5173/login`.
- [ ] Login com usuario administrador criado pelo bootstrap em Docker/HOM/PRD, ou usuario dev no profile `dev`.
- [ ] Acessar dashboard autenticado.
- [ ] Refresh de token mantem sessao valida.
- [ ] Logout encerra sessao.
- [ ] Acesso a rota autenticada apos logout redireciona/bloqueia corretamente.

## 8. Uploads

Validar:

- [ ] Upload de avatar da conta.
- [ ] Arquivo fica acessivel por `/uploads/...`.
- [ ] Upload invalido e recusado.
- [ ] Volume `baseplus_baseplus_uploads` persiste apos restart da stack.
- [ ] Caminho de uploads usa configuracao `UPLOAD_DIR`.

## 9. Branding

Validar:

- [ ] Alterar nome/subtitulo visual da plataforma.
- [ ] Upload de logo principal.
- [ ] Upload de logo compacta.
- [ ] Upload de favicon.
- [ ] Upload de logo/tela de login, quando aplicavel.
- [ ] Restaurar ou ajustar branding sem quebrar login e dashboard.

## 10. Auditoria

Validar:

- [ ] Acessar tela de auditoria.
- [ ] Eventos recentes aparecem apos operacoes administrativas.
- [ ] Filtros basicos funcionam.
- [ ] Registros nao exibem senha, token ou segredo.

## 11. Profiles dev, docker e prod

Dev:

- [ ] `mvn spring-boot:run` usa H2 em memoria.
- [ ] Flyway permanece desativado no dev.
- [ ] Usuario seed dev funciona apenas para ambiente local.

Docker:

- [ ] `SPRING_PROFILES_ACTIVE=docker`.
- [ ] Backend conecta em `postgres:5432` dentro da rede Docker.
- [ ] Flyway ativo.
- [ ] Hibernate validate ativo.
- [ ] `JWT_SECRET` e `DB_PASSWORD` exigidos pelo Compose.

Prod:

- [ ] `application-prod.yml` nao usa H2.
- [ ] `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` e `UPLOAD_DIR` vem de ambiente.
- [ ] Flyway ativo.
- [ ] Hibernate validate ativo.
- [ ] Logs sem debug excessivo.

## 12. Ausencia de ngrok fixo

```powershell
cd C:\dev\baseplus
rg -n "ngrok-free\.app|exemplo-de-tunel-real|allowedOriginPatterns|setAllowedOriginPatterns" baseplus-backend baseplus-frontend docs
```

Validar:

- [ ] Nao ha dominio real de tunel fixo no codigo.
- [ ] Exemplos de tunel, se existirem, usam dominio generico.
- [ ] CORS usa `BASEPLUS_CORS_ALLOWED_ORIGINS`.
- [ ] Vite usa `VITE_ALLOWED_HOSTS` apenas quando configurado localmente.

## 13. Backup e restore basico

Validar comandos documentados em `docs/docker.md`:

- [ ] Backup PostgreSQL com `pg_dump`.
- [ ] Backup de uploads com `tar`.
- [ ] Restore PostgreSQL em ambiente descartavel.
- [ ] Restore de uploads em ambiente descartavel.
- [ ] Resultado do restore abre login, dashboard e assets de branding/uploads.

## 14. Git antes de commit/tag

```powershell
cd C:\dev\baseplus
git status --short
git diff --check
```

Validar:

- [ ] `git diff --check` sem erros.
- [ ] Arquivos alterados correspondem a v1.1.0.
- [ ] Nao ha secrets, `.env`, dumps, backups, `dist`, logs ou temporarios indevidos.
- [ ] `README.md`, `CHANGELOG.md`, `state.txt` e docs estao coerentes com a versao, se forem atualizados.
- [ ] Definir commit e tag somente apos checklist completo.

## Encerramento

Antes de publicar:

- [ ] Stack Docker parada ou mantida conscientemente para validacao.
- [ ] Resultado dos testes registrado na conversa ou nota de release.
- [ ] Pendencias conhecidas documentadas para a proxima versao.
