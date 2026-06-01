# Setup - Base+

Guia rapido para rodar e validar o projeto localmente.

## Requisitos

- Java 17+
- Maven
- Node.js
- npm
- Docker, opcional para PostgreSQL

## Backend

```powershell
cd C:\dev\baseplus\baseplus-backend
mvn spring-boot:run
```

URL:

```text
http://localhost:8080
```

Perfil dev:

- H2 em memoria.
- Flyway desativado no `application-dev.yml`.
- Segredo JWT local padrao e usuario seed conhecidos; use somente em desenvolvimento local.

## Segredo JWT fora de desenvolvimento

Em qualquer perfil diferente de `dev`, defina `JWT_SECRET` antes de iniciar o backend. O valor deve ser longo, aleatorio e mantido fora do repositorio.

```powershell
$env:JWT_SECRET = '<segredo-aleatorio-seguro>'
$env:SPRING_PROFILES_ACTIVE = 'prod'
mvn spring-boot:run
```

## Frontend

```powershell
cd C:\dev\baseplus\baseplus-frontend
npm install
npm run dev
```

URL:

```text
http://localhost:5173
```

## Usuario dev

```text
admin@baseplus.com
Baseplus@123
```

## PostgreSQL local

```powershell
cd C:\dev\baseplus\baseplus-backend
docker compose up -d
```

O `docker-compose.yml` sobe PostgreSQL 16.

## Validacao

Preferencialmente use:

```powershell
cd C:\dev\baseplus
.\scripts\check-project.ps1
```

Ou rode manualmente:

```powershell
cd C:\dev\baseplus\baseplus-backend
mvn test

cd C:\dev\baseplus\baseplus-frontend
npm run build
```
