# Integracoes Externas - Base+

Este documento define o padrao arquitetural para modulos que recebem chamadas externas ou consomem servicos de terceiros. Ele nao cria endpoints, autenticacao ou clients nesta versao.

## Principios

- Regra de negocio permanece no service ou na camada de aplicacao do modulo.
- Controllers e clients de integracao devem ser adaptadores finos.
- DTOs externos nao devem ser entidades JPA nem atravessar diretamente o dominio.
- Credenciais, URLs, timeouts e flags devem vir de configuracao por ambiente.
- Segredos e payloads sensiveis nunca devem aparecer em logs ou auditoria.
- Contratos externos devem ser isolados para reduzir o impacto de mudancas do fornecedor.
- Persistencia futura deve ser compativel com PostgreSQL e controlada por Flyway.

## Organizacao backend

Infraestrutura transversal de integracao deve ficar em:

```text
com.baseplus.core.integration/
+-- auth/          # autenticacao externa compartilhada
+-- audit/         # contratos transversais de auditoria
+-- config/        # propriedades tipadas por ambiente
+-- exception/     # erros tecnicos normalizados
+-- idempotency/   # suporte comum de idempotencia
+-- model/         # modelos tecnicos sem regra de negocio
+-- rest/          # infraestrutura comum para clients REST
+-- soap/          # infraestrutura SOAP, quando necessaria
```

Os pacotes so devem ser criados quando existir implementacao concreta.

Integracoes pertencentes a um unico dominio ficam no modulo dono:

```text
com.baseplus.modules.<modulo>/
+-- controller/
|   +-- <Modulo>Controller.java
|   +-- <Modulo>ExternalController.java, quando houver entrada externa
+-- integration/
|   +-- client/
|   +-- dto/
|   +-- mapper/
|   +-- legacy/
+-- service/
+-- domain/
+-- repository/
+-- dto/
```

Nao colocar regra especifica de um modulo em `core.integration`. Um adapter compartilhado pode ser promovido ao core somente quando a reutilizacao for real e estavel.

## Integracoes de saida

### REST e APIs de terceiros

- Encapsular chamadas em client ou gateway, nunca em controller ou repository.
- Manter URL base, credencial, timeout e habilitacao em propriedades tipadas por ambiente.
- Mapear respostas externas para DTOs internos antes de chegar ao service.
- Normalizar status HTTP, timeout e indisponibilidade em erros tecnicos.
- Aplicar retry somente em operacoes seguras e idempotentes, com limite e espera definidos.
- Propagar `correlationId` quando o fornecedor aceitar correlacao.

### SOAP

- Adicionar dependencias SOAP somente quando existir integracao concreta aprovada.
- Isolar classes geradas por WSDL do dominio e das entidades JPA.
- Traduzir envelopes, faults e tipos gerados em DTOs internos no adapter.
- Configurar endpoint, credenciais e timeouts por ambiente.
- Versionar o WSDL ou registrar sua origem e versao de forma rastreavel.

### Sistemas legados

- Usar camada anticorrupcao em `integration/legacy`.
- Tornar explicitas conversoes de charset, datas, nulos, codigos e formatos proprietarios.
- Nao espalhar identificadores ou estruturas legadas pelo dominio interno.
- Registrar limites operacionais, timeout e comportamento de indisponibilidade.

## Integracoes de entrada

Separar controllers por publico e mecanismo de autenticacao:

```text
<Modulo>Controller
```

Atende a aplicacao Base+ e segue JWT, permissions e escopo organizacional.

```text
<Modulo>ExternalController
```

Recebe chamadas maquina-a-maquina. Quando implementado, usa prefixo `/external/<modulo>`, contrato externo proprio e autenticacao de integracao. Nao misturar autenticacao externa e endpoints administrativos no mesmo controller.

O controller externo delega para o mesmo service ou caso de uso do modulo, sem duplicar regra de negocio. Diferencas de payload ficam em DTOs e mappers.

## Autenticacao inicial

O mecanismo inicial reservado e:

```text
X-API-Key: <chave>
```

Diretrizes para a futura implementacao:

- `EXTERNAL_API_ENABLED=false` mantem entradas externas desabilitadas por padrao.
- `EXTERNAL_API_KEY` e obrigatoria quando a API externa estiver habilitada.
- A chave vem de secret manager ou variavel de ambiente, nunca do repositorio.
- A comparacao deve evitar vazamento por tempo e a chave nunca deve aparecer em logs.
- TLS e obrigatorio fora do ambiente local.
- Rotacao de chave deve ser planejada sem alterar contratos de negocio.
- Chaves por parceiro, escopos e assinatura podem evoluir sem mudar a separacao dos controllers.

As variaveis reservadas ainda nao sao consumidas pela aplicacao.

## Idempotencia

Toda entrada externa que cria ou altera estado deve definir uma estrategia antes da implementacao.

Use `externalId` quando a origem fornecer identificador estavel do registro ou evento. A unicidade considera ao menos `origem + externalId`.

Use `Idempotency-Key` quando a idempotencia pertencer a tentativa de requisicao. A unicidade considera `origem + operacao + chave`.

Regras:

- Persistir chave e resultado na mesma fronteira transacional da operacao.
- Proteger unicidade com constraint no PostgreSQL; nao usar apenas consulta seguida de insert.
- Repeticao com o mesmo conteudo retorna o resultado conhecido quando aplicavel.
- Mesma chave com payload semanticamente diferente gera conflito.
- Definir retencao e limpeza conforme o ciclo de vida do modulo.
- Propagar a chave em chamadas de saida quando o fornecedor oferecer idempotencia.

## Auditoria minima

Toda operacao externa registra, no minimo:

- `origem`: parceiro, sistema ou client identificado.
- `dataHora`: instante em UTC.
- `identificadorExterno`: `externalId` ou `Idempotency-Key`, quando existir.
- `resultado`: sucesso, rejeicao, duplicidade ou erro.
- `correlationId`: recebido ou gerado, quando aplicavel.

Tambem e recomendado registrar operacao, recurso interno afetado, duracao e codigo de erro sanitizado. Nunca registrar API keys, tokens, senhas ou payloads sensiveis.

Quando possivel, evoluir o modulo de Auditoria existente em vez de criar historicos paralelos. Tabelas novas usam Flyway, tipos adequados ao PostgreSQL, indices para origem/data/correlationId e politica de retencao.

## Correlacao

- Aceitar `X-Correlation-Id` quando valido ou gerar um identificador novo.
- Devolver o identificador na resposta externa.
- Propagar o valor para clients de saida, logs e auditoria.
- Nao usar correlationId como substituto de idempotencia.

## Rate limit

`EXTERNAL_API_RATE_LIMIT` fica reservado para o limite inicial de requisicoes por minuto. Semantica definitiva, armazenamento dos contadores e resposta HTTP serao definidos com a primeira API externa.

## Checklist para o primeiro modulo integrado

1. Classificar a integracao como entrada, saida ou ambas.
2. Definir contrato, ownership e versionamento.
3. Escolher `<Modulo>Controller` ou `<Modulo>ExternalController` conforme o publico.
4. Definir autenticacao, autorizacao e configuracao por ambiente.
5. Definir `externalId` ou `Idempotency-Key` para operacoes mutaveis.
6. Definir auditoria e correlationId.
7. Isolar DTOs, mappers e client do dominio.
8. Definir timeouts, retry e comportamento de indisponibilidade.
9. Criar migrations e constraints PostgreSQL quando houver persistencia.
10. Criar testes de contrato, autenticacao, idempotencia e falhas externas.
