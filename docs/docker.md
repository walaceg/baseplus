# Docker integrado - Base+

Guia operacional para executar a Base+ com Docker Compose, PostgreSQL, backend e frontend integrados.

Para atualizar uma instalacao existente sem reinstalar e sem perder banco, uploads, usuarios, auditoria ou branding, consulte `docs/update.md`.

## Arquivo principal

Execute os comandos a partir do backend, onde esta o `docker-compose.yml` oficial:

```powershell
cd C:\dev\baseplus\baseplus-backend
```

## Variaveis obrigatorias

Crie um `.env` local a partir do exemplo e substitua os valores sensiveis:

```powershell
Copy-Item .env.example .env
```

Obrigatorias para a stack Docker:

| Variavel | Finalidade |
| --- | --- |
| `JWT_SECRET` | Assinatura dos tokens JWT. Use valor longo e aleatorio. |
| `DB_PASSWORD` | Senha do usuario PostgreSQL da stack local. |

Principais opcionais:

| Variavel | Padrao | Finalidade |
| --- | --- | --- |
| `DB_NAME` | `baseplus` | Nome do banco. |
| `DB_USER` | `baseplus` | Usuario do banco. |
| `FRONTEND_PORT` | `5173` | Porta local do frontend. |
| `BACKEND_PORT` | `8080` | Porta local do backend. |
| `BASEPLUS_CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | Origens permitidas pelo backend. |
| `BASEPLUS_TAG` | `local` | Tag das imagens locais. |

Nunca versionar `.env` com segredos reais.

## Subir a stack

Valide a configuracao resolvida:

```powershell
docker compose config
```

Construa e suba os servicos:

```powershell
docker compose up --detach --build --wait
```

Consultar status:

```powershell
docker compose ps
```

## Portas expostas

| Servico | URL local | Observacao |
| --- | --- | --- |
| Frontend | `http://127.0.0.1:5173` | Servido por Nginx. |
| Backend | `http://127.0.0.1:8080` | Exposto apenas no loopback local. |
| PostgreSQL | nenhuma porta publica | Acesso somente pela rede Docker. |

O frontend faz proxy para:

```text
/api -> backend:8080
/uploads -> backend:8080/uploads
```

## Health checks

Validar pelo frontend/Nginx:

```powershell
curl.exe http://127.0.0.1:5173/api/health
curl.exe http://127.0.0.1:5173/api/health/ready
```

O healthcheck do backend no Compose usa `/health/ready`, que verifica conectividade real com o banco sem expor dados sensiveis.

## Bootstrap administrativo

O profile `docker` nao cria usuario administrador automaticamente. Em um banco novo, execute o bootstrap administrativo uma unica vez para criar o primeiro usuario com perfil `ADMIN`.

O bootstrap:

- exige ativacao explicita por `BASEPLUS_BOOTSTRAP_ADMIN_ENABLED=true`;
- recebe nome, email e senha;
- cria ou atualiza o perfil `ADMIN` de sistema com as permissions oficiais;
- cria o usuario via servico da plataforma, com senha criptografada;
- registra auditoria de sistema;
- recusa nova execucao quando ja existir usuario administrador.

Exemplo:

```powershell
cd C:\dev\baseplus\baseplus-backend
docker compose up --detach --build --wait

$env:BASEPLUS_BOOTSTRAP_ADMIN_ENABLED = 'true'
$env:BASEPLUS_BOOTSTRAP_ADMIN_NAME = 'Administrador'
$env:BASEPLUS_BOOTSTRAP_ADMIN_EMAIL = 'admin@empresa.com'
$env:BASEPLUS_BOOTSTRAP_ADMIN_PASSWORD = '<senha-forte>'
docker compose run --rm backend java -jar /app/app.jar --spring.main.web-application-type=none
```

Validar login:

```powershell
curl.exe -X POST http://127.0.0.1:5173/api/auth/login -H "Content-Type: application/json" -d "{\"email\":\"admin@empresa.com\",\"password\":\"<senha-forte>\"}"
```

Nao mantenha `BASEPLUS_BOOTSTRAP_ADMIN_PASSWORD` em arquivos versionados. Depois de executar o bootstrap, remova as variaveis do terminal ou do ambiente operacional.

## Volumes persistentes

| Volume | Conteudo |
| --- | --- |
| `baseplus_baseplus_postgres_data` | Dados fisicos do PostgreSQL. |
| `baseplus_baseplus_uploads` | Arquivos enviados pela aplicacao. |

Confirmar nomes locais:

```powershell
docker volume ls --filter name=baseplus
```

## Backup do PostgreSQL

Crie uma pasta local para os backups:

```powershell
New-Item -ItemType Directory -Force C:\backups\baseplus
```

Gerar backup logico em formato customizado do PostgreSQL:

```powershell
docker compose exec -T postgres pg_dump -U baseplus -d baseplus -Fc > C:\backups\baseplus\baseplus.dump
```

Se `DB_USER` ou `DB_NAME` forem alterados no `.env`, use os mesmos valores no comando.

## Backup dos uploads

Compactar o volume de uploads:

```powershell
docker run --rm -v baseplus_baseplus_uploads:/data -v C:/backups/baseplus:/backup alpine sh -c "cd /data && tar czf /backup/uploads.tgz ."
```

O backup operacional minimo deve sempre guardar juntos:

- `baseplus.dump`
- `uploads.tgz`

## Restore do PostgreSQL

Com a stack em execucao, copie o dump para o container:

```powershell
docker compose cp C:\backups\baseplus\baseplus.dump postgres:/tmp/baseplus.dump
```

Restaurar o banco:

```powershell
docker compose exec postgres pg_restore -U baseplus -d baseplus --clean --if-exists /tmp/baseplus.dump
```

Se necessario, reinicie backend e frontend apos o restore:

```powershell
docker compose restart backend frontend
```

## Restore dos uploads

Restaurar o volume de uploads:

```powershell
docker run --rm -v baseplus_baseplus_uploads:/data -v C:/backups/baseplus:/backup alpine sh -c "cd /data && tar xzf /backup/uploads.tgz"
```

Reinicie o backend se a aplicacao estiver em uso durante o restore:

```powershell
docker compose restart backend
```

## Parar a stack

Parar containers mantendo volumes:

```powershell
docker compose down
```

Parar e remover volumes locais:

```powershell
docker compose down --volumes
```

Use `--volumes` somente quando quiser apagar banco e uploads locais.

## Observacoes de producao

- Para producao real, use secrets e senhas gerenciadas fora do repositorio.
- Teste restore periodicamente; backup sem restore testado nao deve ser considerado confiavel.
- PostgreSQL nao deve ser exposto publicamente no Compose integrado.
