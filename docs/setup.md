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

## Variaveis de ambiente

O arquivo `baseplus-backend/.env.example` documenta as variaveis reconhecidas pelos profiles. Use-o como referencia e nunca coloque segredos reais no repositorio.

O Docker Compose carrega `baseplus-backend/.env` automaticamente. O Spring Boot iniciado pelo Maven nao carrega arquivos `.env`; nesse caso, exporte as variaveis no terminal, configure-as na IDE ou use o gerenciador de segredos do ambiente.

### Obrigatorias

| Profile | Variavel | Finalidade |
| --- | --- | --- |
| `docker` | `JWT_SECRET` | Assinatura dos tokens JWT. |
| `docker` | `DB_PASSWORD` | Senha do PostgreSQL exigida pelo Docker Compose. |
| `prod` | `DB_URL` | URL JDBC completa do PostgreSQL. |
| `prod` | `DB_USER` | Usuario do PostgreSQL. |
| `prod` | `DB_PASSWORD` | Senha do PostgreSQL. |
| `prod` | `JWT_SECRET` | Assinatura dos tokens JWT. |

### Opcionais

| Profile | Variavel | Padrao | Finalidade |
| --- | --- | --- | --- |
| todos | `SPRING_PROFILES_ACTIVE` | `dev` | Seleciona `dev`, `docker` ou `prod`. |
| todos | `BASEPLUS_CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | Origens exatas permitidas pelo CORS do backend. |
| `dev` | `JWT_SECRET` | segredo local conhecido | Permite substituir o segredo exclusivo de desenvolvimento. |
| `docker` | `DB_HOST` | `localhost` | Host do PostgreSQL local. |
| `docker` | `DB_PORT` | `5432` | Porta publicada pelo PostgreSQL. |
| `docker` | `DB_NAME` | `baseplus` | Nome do banco local. |
| `docker` | `DB_USER` | `baseplus` | Usuario do banco local. |

### Reservadas para integracoes futuras

Estas variaveis ainda nao sao consumidas pela aplicacao e nao habilitam endpoints:

| Variavel | Valor de referencia | Finalidade futura |
| --- | --- | --- |
| `EXTERNAL_API_ENABLED` | `false` | Habilitar explicitamente entradas externas. |
| `EXTERNAL_API_KEY` | vazio | Chave enviada no cabecalho `X-API-Key`. |
| `EXTERNAL_API_RATE_LIMIT` | `60` | Limite inicial de requisicoes por minuto. |

O comportamento definitivo sera implementado com o primeiro modulo externo conforme `docs/integrations.md`.

## Profile dev

O profile padrao usa H2 em memoria e nao exige variaveis:

```powershell
cd C:\dev\baseplus\baseplus-backend
mvn spring-boot:run
```

## Profile docker

O Compose integrado sobe PostgreSQL 16, backend e frontend:

```powershell
cd C:\dev\baseplus\baseplus-backend
Copy-Item .env.example .env
# Edite .env e substitua JWT_SECRET e DB_PASSWORD.
docker compose up --build
```

O frontend fica em `http://localhost:5173`, o backend em `http://localhost:8080` e o PostgreSQL permanece acessivel apenas pela rede Docker interna.

Para personalizar a conexao, defina `DB_NAME`, `DB_USER` e `DB_PASSWORD` no `.env` usado pelo Compose. O backend usa `postgres:5432` dentro da rede Docker.

Para operacao completa com Docker, portas, volumes, backup e restore, consulte `docs/docker.md`.

## Bootstrap administrativo

O bootstrap administrativo cria o primeiro usuario administrador em ambientes persistentes sem usar seed permanente, credenciais fixas, migrations ou inserts SQL.

Use somente quando ainda nao existir usuario com perfil `ADMIN`. Se ja existir administrador cadastrado, a execucao sera recusada.

### Desenvolvimento

No profile `dev`, o comportamento atual permanece igual: `UsuarioSeed` cria o administrador local conhecido apenas para desenvolvimento.

```text
admin@baseplus.com
Baseplus@123
```

Nao use o bootstrap para o fluxo normal de desenvolvimento.

### Homologacao com Docker

Suba a stack e execute o bootstrap uma unica vez:

```powershell
cd C:\dev\baseplus\baseplus-backend
docker compose up --detach --build --wait

$env:BASEPLUS_BOOTSTRAP_ADMIN_ENABLED = 'true'
$env:BASEPLUS_BOOTSTRAP_ADMIN_NAME = 'Administrador'
$env:BASEPLUS_BOOTSTRAP_ADMIN_EMAIL = 'admin@empresa.com'
$env:BASEPLUS_BOOTSTRAP_ADMIN_PASSWORD = '<senha-forte>'
docker compose run --rm backend java -jar /app/app.jar --spring.main.web-application-type=none
```

Depois, acesse `http://127.0.0.1:5173/login` com o email e senha informados.

### Producao

Em producao, forneca as variaveis obrigatorias do profile `prod` e as variaveis do bootstrap no gerenciador seguro do ambiente:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'prod'
$env:DB_URL = 'jdbc:postgresql://database-host:5432/baseplus'
$env:DB_USER = '<usuario>'
$env:DB_PASSWORD = '<senha>'
$env:JWT_SECRET = '<segredo-aleatorio-seguro>'
$env:UPLOAD_DIR = '/opt/baseplus/uploads'
$env:BASEPLUS_BOOTSTRAP_ADMIN_ENABLED = 'true'
$env:BASEPLUS_BOOTSTRAP_ADMIN_NAME = 'Administrador'
$env:BASEPLUS_BOOTSTRAP_ADMIN_EMAIL = 'admin@empresa.com'
$env:BASEPLUS_BOOTSTRAP_ADMIN_PASSWORD = '<senha-forte>'
java -jar baseplus-backend.jar --spring.main.web-application-type=none
```

Remova as variaveis `BASEPLUS_BOOTSTRAP_ADMIN_*` do ambiente apos a execucao. A senha nunca deve ser versionada, compartilhada em logs ou mantida em arquivos `.env` comitaveis.

## Tunel local opcional

Para expor a aplicacao temporariamente com um tunel local, mantenha o dominio real apenas em arquivos locais nao versionados ou variaveis de ambiente, nunca no codigo versionado.

No backend, configure `baseplus-backend/.env` quando usar Docker Compose, ou exporte a variavel no terminal/IDE quando usar Maven:

```powershell
BASEPLUS_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173,https://seu-tunel.ngrok-free.app
```

No frontend em modo Vite dev, configure `baseplus-frontend/.env.local` ou exporte a variavel no terminal:

```powershell
VITE_ALLOWED_HOSTS=seu-tunel.ngrok-free.app
```

Reinicie backend e frontend depois de alterar essas variaveis. Nao use `*` em CORS.

## Profile prod

O profile produtivo exige PostgreSQL e nao possui fallback para H2, URL local, usuario, senha ou segredo JWT:

```powershell
cd C:\dev\baseplus\baseplus-backend
$env:SPRING_PROFILES_ACTIVE = 'prod'
$env:DB_URL = 'jdbc:postgresql://database-host:5432/baseplus'
$env:DB_USER = '<usuario>'
$env:DB_PASSWORD = '<senha>'
$env:JWT_SECRET = '<segredo-aleatorio-seguro>'
mvn spring-boot:run
```

O diretorio de uploads e configurado por `UPLOAD_DIR`.

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
