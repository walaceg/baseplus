# Runbook de Instalacao - Base+

## Objetivo

Orientar a instalacao inicial da Base+ em DEV, HOM e PRD.

Para detalhes completos de setup, consulte tambem `docs/setup.md` e `docs/docker.md`.

## DEV

### Pre-requisitos

- Java 17 ou superior.
- Maven.
- Node.js.
- npm.
- Git.

### Clone

```powershell
git clone https://github.com/walaceg/baseplus.git
cd baseplus
```

### Configuracao

O profile `dev` usa H2 em memoria e usuario seed de desenvolvimento.

### Backend

```powershell
cd C:\dev\baseplus\baseplus-backend
mvn spring-boot:run
```

### Frontend

```powershell
cd C:\dev\baseplus\baseplus-frontend
npm install
npm run dev
```

### Login inicial

Use o usuario dev documentado no README.

### Validacao

```powershell
cd C:\dev\baseplus
.\scripts\check-project.ps1
```

## HOM

### Pre-requisitos

- Docker.
- Docker Compose.
- Git.
- Valores seguros para `JWT_SECRET` e `DB_PASSWORD`.

### Clone

```powershell
git clone https://github.com/walaceg/baseplus.git
cd baseplus
git checkout v1.1.0
```

### Configuracao

```powershell
cd C:\dev\baseplus\baseplus-backend
Copy-Item .env.example .env
```

Edite `.env` com os valores do ambiente.

### Bootstrap

Execute bootstrap apenas em banco vazio e sem usuario `ADMIN`.

Consulte `docs/docker.md` para o comando completo.

### Login inicial

Use o usuario criado pelo bootstrap administrativo.

### Validacao

```powershell
docker compose config
docker compose up --detach --build --wait
curl.exe http://127.0.0.1:5173/api/health
curl.exe http://127.0.0.1:5173/api/health/ready
```

## PRD

### Pre-requisitos

- PostgreSQL persistente.
- Segredos gerenciados fora do repositorio.
- Diretorio ou volume persistente para uploads.
- Estrategia de backup e restore validada.

### Configuracao

Configure variaveis obrigatorias do profile `prod`:

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET`
- `UPLOAD_DIR`

### Bootstrap

Execute somente na primeira inicializacao, com banco vazio.

Depois de executado, remova variaveis `BASEPLUS_BOOTSTRAP_ADMIN_*` do ambiente.

### Validacao

Validar:

- health;
- readiness;
- login;
- branding;
- uploads;
- auditoria;
- backup inicial.
