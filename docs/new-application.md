# Criando uma nova aplicacao com Base+

Guia oficial para usar a Base+ como fundacao de um novo sistema corporativo.

## Objetivo da Base+

A Base+ e uma plataforma reutilizavel para iniciar aplicacoes administrativas e sistemas de negocio com uma fundacao pronta.

Ela entrega como base:

- autenticacao JWT;
- autorizacao por permissoes granulares;
- perfis funcionais e organizacionais;
- usuarios;
- auditoria;
- branding;
- uploads;
- Docker integrado;
- PostgreSQL;
- health e readiness;
- padroes de modulo backend e frontend.

Novas aplicacoes devem evoluir adicionando modulos de negocio, telas, menus, regras e personalizacao, sem alterar a arquitetura principal da plataforma sem necessidade clara.

## Fluxo oficial para criar uma nova aplicacao

1. Clonar o repositorio oficial da Base+.
2. Fazer checkout da versao estavel publicada.
3. Criar um novo repositorio Git para a aplicacao de negocio.
4. Alterar o remote `origin` para o novo repositorio.
5. Configurar ambiente local.
6. Validar a instalacao.
7. Configurar Docker/HOM quando necessario.
8. Executar bootstrap administrativo em banco vazio.
9. Validar login, branding e uploads.
10. Criar o primeiro modulo de negocio.

## Exemplo completo com Git

Clone a Base+ usando o nome da nova aplicacao:

```powershell
git clone https://github.com/walaceg/baseplus.git minha-aplicacao
cd minha-aplicacao
```

Use uma tag estavel:

```powershell
git checkout v1.1.0
```

Remova o remote original:

```powershell
git remote remove origin
```

Crie o novo repositorio no GitHub, GitLab ou servidor interno e adicione o novo remote:

```powershell
git remote add origin https://github.com/<usuario>/minha-aplicacao.git
```

Envie a base para o novo repositorio:

```powershell
git push -u origin master
```

Se a organizacao usar branch principal `main`, ajuste antes do push:

```powershell
git branch -M main
git push -u origin main
```

## Configuracao inicial do ambiente

Backend em desenvolvimento:

```powershell
cd C:\dev\minha-aplicacao\baseplus-backend
mvn spring-boot:run
```

Frontend em desenvolvimento:

```powershell
cd C:\dev\minha-aplicacao\baseplus-frontend
npm install
npm run dev
```

Docker integrado:

```powershell
cd C:\dev\minha-aplicacao\baseplus-backend
Copy-Item .env.example .env
docker compose config
docker compose up --detach --build --wait
```

Edite o `.env` local antes de subir Docker e substitua, no minimo:

- `JWT_SECRET`
- `DB_PASSWORD`

Nunca versionar `.env` com segredos reais.

## Validacao da instalacao

Validacao padrao:

```powershell
cd C:\dev\minha-aplicacao
.\scripts\check-project.ps1
```

Validacao manual:

```powershell
cd C:\dev\minha-aplicacao\baseplus-backend
mvn test

cd C:\dev\minha-aplicacao\baseplus-frontend
npm run build
```

Validacao Docker:

```powershell
cd C:\dev\minha-aplicacao\baseplus-backend
docker compose config
docker compose up --detach --build --wait
curl.exe http://127.0.0.1:5173/api/health
curl.exe http://127.0.0.1:5173/api/health/ready
```

## Primeiro acesso

No profile `dev`, a Base+ usa usuario seed de desenvolvimento.

Em Docker, homologacao ou producao, execute o bootstrap administrativo somente quando o banco estiver vazio e ainda nao existir usuario com perfil `ADMIN`.

Depois de criar o primeiro administrador:

- valide login;
- valide logout;
- valide refresh de sessao;
- valide troca de senha quando aplicavel;
- valide acesso aos menus administrativos.

## O que pode ser alterado

Em uma nova aplicacao, e esperado personalizar:

- branding;
- nome da aplicacao;
- README;
- modulos;
- menus;
- paginas;
- regras de negocio;
- textos de dominio;
- permissoes especificas de novos modulos;
- documentacao operacional da aplicacao.

Essas alteracoes devem ser feitas preservando a fundacao tecnica da Base+.

## O que nao deve ser alterado inicialmente

Evite alterar no inicio:

- arquitetura `core/shared/modules`;
- Spring Security;
- JWT;
- estrutura Docker;
- padrao de API;
- padrao DTO;
- uploads;
- auditoria;
- permissoes;
- design tokens;
- CSS Variables.

Alteracoes nesses pontos devem ocorrer apenas quando houver necessidade arquitetural comprovada, testes suficientes e registro claro em documentacao.

## Organizacao dos modulos

### Backend

Estrutura esperada para novos modulos:

```text
baseplus-backend/src/main/java/com/baseplus/modules/<modulo>/
|-- controller/
|-- service/
|-- repository/
|-- domain/
`-- dto/
```

Responsabilidades:

- `controller`: endpoints HTTP, validacao de entrada e autorizacao declarativa.
- `service`: regras de aplicacao, transacoes e orquestracao.
- `repository`: acesso a dados.
- `domain`: entidades e regras estruturais do dominio.
- `dto`: contratos de entrada e saida da API.

### Frontend

Estrutura esperada:

```text
baseplus-frontend/src/modules/<modulo>/
|-- pages/
|-- components/
`-- services/
```

Quando o modulo for simples, a Base+ permite uma estrutura mais compacta, como:

```text
baseplus-frontend/src/modules/<modulo>/
|-- <modulo>Service.js
|-- <Modulo>Page.jsx
`-- <modulo>.css
```

Use `MODULE_TEMPLATE.md` e `docs/module-development.md` para escolher entre CRUD Compacto e CRUD Completo.

## Fluxo recomendado de desenvolvimento

Para cada modulo de negocio, siga a ordem:

1. Entidade.
2. Migration Flyway.
3. Repository.
4. Service.
5. Controller.
6. Testes backend.
7. Frontend service.
8. Paginas.
9. Componentes.
10. Integracao frontend/backend.
11. Permissoes e menu.
12. Homologacao Docker.

Para modulos com integracoes externas, consulte tambem `docs/integrations.md`.

## Checklist inicial

- [ ] Clone realizado.
- [ ] Checkout da tag estavel realizado.
- [ ] Novo repositorio criado.
- [ ] Remote `origin` atualizado.
- [ ] `.env` configurado.
- [ ] Validacao local executada.
- [ ] Docker validado.
- [ ] Bootstrap administrativo realizado quando aplicavel.
- [ ] Login validado.
- [ ] Branding validado.
- [ ] Upload validado.
- [ ] Primeiro modulo iniciado.
- [ ] `state.txt` ou documento equivalente atualizado.

## Estado do projeto

Ao iniciar uma nova aplicacao, crie um documento de acompanhamento do estado atual.

Use o template:

```text
docs/templates/BASEPLUS_STATE.template.md
```

Esse arquivo deve registrar:

- baseline Base+ utilizada;
- objetivo da aplicacao;
- modulos criados;
- decisoes arquiteturais;
- pendencias;
- validacoes executadas;
- proximos passos.

O acompanhamento de estado ajuda novas interacoes com IA, revisoes tecnicas e passagem de contexto entre desenvolvedores.
