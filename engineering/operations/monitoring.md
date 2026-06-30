# Runbook de Monitoramento - Base+

## Objetivo

Definir verificacoes operacionais minimas para acompanhar saude da Base+.

## Health

Validar disponibilidade basica:

```powershell
curl.exe http://127.0.0.1:5173/api/health
```

## Readiness

Validar prontidao do backend e conectividade com banco:

```powershell
curl.exe http://127.0.0.1:5173/api/health/ready
```

## Logs

Acompanhar:

- erros do backend;
- falhas de Flyway;
- falhas de autenticacao;
- falhas de upload;
- erros do Nginx.

## Containers

Verificar:

```powershell
docker compose ps
```

## Volumes

Monitorar:

- volume PostgreSQL;
- volume de uploads;
- crescimento inesperado;
- risco de remocao acidental.

## Banco

Monitorar:

- conectividade;
- uso de disco;
- tempo de resposta;
- backups;
- falhas de migration.

## Uploads

Monitorar:

- espaco utilizado;
- permissao de escrita;
- arquivos inacessiveis;
- consistencia entre banco e filesystem.

## Espaco em disco

Monitorar:

- disco do host;
- imagens Docker antigas;
- volumes Docker;
- backups acumulados;
- logs.
