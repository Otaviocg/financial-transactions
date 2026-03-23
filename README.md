# Financial Transactions

Sistema de processamento de transações financeiras em tempo real. Mensagens de transação são publicadas em uma fila SQS, consumidas e persistidas de forma paralela e idempotente, e os saldos ficam disponíveis via API REST.

## Arquitetura

```
┌─────────────────────┐     SQS (LocalStack)            ┌──────────────────────────┐
│  message-generator  │ ──── transacoes-financeiras ──► │  transaction-consumer    │
│  (Go)               │        -processadas             │  (Kotlin / Spring Boot)  │
└─────────────────────┘                                 └────────────┬─────────────┘
                                                                     │
                                                            PostgreSQL (financial_transactions)
                                                                     │
                                                        ┌────────────▼─────────────┐
                                                        │       balance-api        │
                                                        │  (Kotlin / Spring Boot)  │
                                                        │   GET /accounts/{id}     │
                                                        └──────────────────────────┘
```

### Serviços

| Serviço | Tecnologia | Função |
|---|---|---|
| `message-generator` | Go | Gera transações sintéticas e publica na fila SQS em batches |
| `transaction-consumer` | Kotlin / Spring Boot | Consome a fila SQS em paralelo e persiste transações e saldos no banco |
| `balance-api` | Kotlin / Spring Boot | API REST para consulta do saldo atual de uma conta |
| `postgres` | PostgreSQL 15 | Armazena contas e transações |
| `localstack` | LocalStack 3.7 | Emula o serviço AWS SQS localmente |

## Stack

- **Kotlin** + **Spring Boot 4**
- **Spring Cloud AWS 3.2** — integração com SQS via `@SqsListener`
- **Spring Data JPA** + **PostgreSQL**
- **Docker** + **Docker Compose**

## Pré-requisitos

- [Docker](https://docs.docker.com/get-docker/) com Docker Compose

## Execução

Clone o repositório e suba todos os serviços com um único comando:

```bash
git clone https://github.com/Otaviocg/financial-transactions.git
cd financial-transactions

docker compose up --build
```

O `--build` compila os JARs de `balance-api` e `transaction-consumer` dentro do próprio Docker (multi-stage build) — não é necessário nenhum build manual.

### Ordem de inicialização

O Docker Compose gerencia as dependências automaticamente:

```
localstack ──► message-generator
postgres   ──► balance-api
postgres
localstack ──► transaction-consumer
```

### Parando o ambiente

```bash
# Mantém os dados do banco
docker compose down

# Remove também os volumes (reseta o banco)
docker compose down -v
```

## API

### `GET /accounts/{id}`

Retorna o saldo atual de uma conta.

**Parâmetros**

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador da conta |

**Resposta `200 OK`**

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "owner": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "balance": {
    "amount": 1250.75,
    "currency": "BRL"
  },
  "updated_at": "2024-11-14T18:32:00.000-03:00"
}
```

**Resposta `404 Not Found`**

```json
{
  "status": 404,
  "error": "Account not found",
  "message": "Account 3fa85f64-5717-4562-b3fc-2c963f66afa6 not found"
}
```

**Exemplo**

```bash
curl http://localhost:8080/accounts/3fa85f64-5717-4562-b3fc-2c963f66afa6
```

## Banco de Dados

Banco: `financial_transactions`

PostgreSQL foi escolhido por sua robustez, suporte a transações ACID, garantindo Forte Consistência de dados para o domínio. O esquema é simples, com duas tabelas principais: `accounts` para armazenar o saldo atual de cada conta, e `transactions` para registrar todas as transações processadas.

### `accounts`

| Coluna | Tipo | Descrição |
|---|---|---|
| `account_id` | UUID PK | Identificador da conta |
| `owner_id` | UUID | Identificador do titular |
| `status` | VARCHAR | Status da conta (`ENABLED`) |
| `balance_amount` | NUMERIC(19,2) | Saldo atual |
| `balance_currency` | VARCHAR | Moeda (`BRL`) |
| `updated_at` | BIGINT | Timestamp do último evento em microssegundos |
| `created_at` | TIMESTAMP | Data de criação |

### `transactions`

| Coluna | Tipo | Descrição |
|---|---|---|
| `transaction_id` | UUID PK | Identificador da transação |
| `account_id` | UUID FK | Referência à conta |
| `type` | VARCHAR | Tipo: `CREDIT` ou `DEBIT` |
| `amount` | NUMERIC(19,2) | Valor da transação |
| `currency` | VARCHAR | Moeda (`BRL`) |
| `status` | VARCHAR | Status: `APPROVED` ou `REJECTED` |
| `event_timestamp` | BIGINT | Timestamp do evento em microssegundos |
| `created_at` | TIMESTAMP | Data de inserção no banco |

## Configuração

As propriedades relevantes de cada serviço estão em `src/main/resources/application.yml`.

### transaction-consumer

| Propriedade | Padrão | Descrição |
|---|--------|---|
| `sqs.consumer-threads` | `4`    | Threads de polling |
| `sqs.max-messages` | `10`   | Mensagens por chamada ao SQS (máx. 10 por limitação AWS) |
| `sqs.wait-time-seconds` | `20`   | Tempo de long-polling |
| `sqs.enabled` | `true` | Habilita/desabilita o consumer |
| `spring.datasource.hikari.maximum-pool-size` | `20`   | Pool de conexões com o banco |

O paralelismo efetivo é `consumer-threads × max-messages` mensagens processadas simultaneamente.

## Testes

```bash
# balance-api
cd balance-api
./gradlew test

# transaction-consumer
cd transaction-consumer
./gradlew test
```

### Cobertura

| Módulo | Testes |
|---|---|
| `balance-api` | Controller, Service, GlobalExceptionHandler |
| `transaction-consumer` | `TransactionProcessorService`, `SqsConsumerService`, context load |

## Complementos

Eu não tive muito tempo disponível para implementar o restante (Mil perdões!!), mas o também adicionaria:

### Observabilidade

Utilizando Spring Boot Actuator e Micrometer, implementaria:
  - Logs estruturados com MDC (correlationId)
  - Métricas customizadas (transações processadas, tempo de processamento, etc)
  - Tracing distribuído (OpenTelemetry)
  - Alertas baseadas em métricas (ex: filas SQS com mensagens não processadas).

### Variaveis de Ambiente

O ideal seria externalizar as configurações sensíveis (credenciais, URLs) para variáveis de ambiente ou um serviço de configuração centralizado, evitando hardcoding e facilitando a portabilidade:
  - Criando arquivos personalizados `application-{profile}.yml` para cada ambiente (dev, prod)
  - Armazenar os valores das variáveis de ambiente no Secret Manager do LocalStack ou usar um `.env` para desenvolvimento local

### Load Balancing e Escalabilidade

Configurar os serviços para rodar em múltiplas instâncias:
  - Usar um NLB (Network Load Balancer) para distribuir as requisições entre múltiplas instâncias do `balance-api`.
  - Configurar o `transaction-consumer` para scalar horizontalmente, garantindo que múltiplas instâncias possam consumir da mesma fila SQS sem conflitos (graças à idempotência e controle de concorrência no banco).

### WAF

Como faria a escolha para utilizar um NLB, ele acaba não possuindo um WAF (Web Application Firewall) integrado, então para proteger a API REST do `balance-api` contra ataques comuns (SQL injection, XSS, etc), eu implementaria um WAF utilizando:
  - API Gateway do AWS, que pode ser configurado para rotear as requisições para o NLB, e aplicar regras de segurança antes de chegar na aplicação. O API Gateway também pode fornecer funcionalidades adicionais como rate limiting, autenticação, e monitoramento.
  - JWT (JSON Web Tokens) para autenticação e autorização, garantindo que apenas clientes autorizados possam acessar a API REST. O API Gateway pode validar os tokens antes de encaminhar as requisições para o NLB.
  - Configurações de CORS (Cross-Origin Resource Sharing) para controlar quais domínios podem acessar a API, prevenindo ataques de origem cruzada.

### Infra e CI/CD
Implementar pipelines de CI/CD para automação do build, testes e deploy:
  - Usar GitHub Actions para criar pipelines que executem os testes automatizados a cada push ou pull request.
  - Configurar o pipeline para construir as imagens Docker dos serviços, rodar os testes de integração usando o LocalStack e PostgreSQL em containers, e fazer o deploy automático para um ambiente de staging ou produção.
  - Cada serviço, banco de dados, tópico, etc teriam seu próprio pipeline, incluindo repositórios separados. 
  - Todos os itens teriam sua configuração utilizando IaC (Infrastructure as Code) com Terraform, garantindo que a infraestrutura seja versionada e reproduzível.

### Containerização e Orquestração
Para cada serviço, utilizaria:
  - ECS (Elastic Container Service) para orquestrar os containers Docker, garantindo alta disponibilidade e escalabilidade automática. 
  - Configurar o ECS para usar Fargate, eliminando a necessidade de gerenciar servidores ou clusters, e permitindo que os serviços sejam escalados automaticamente com base na demanda.
  
### Banco de Dados
Para o banco de dados:
  - Seria provisionado utilizando Aurora Serverless, garantindo escalabilidade automática e alta disponibilidade sem a necessidade de gerenciar instâncias de banco.
  - Criaria Read Replicas para distribuir a carga de leitura, melhorando a performance das consultas de saldo.
     - Pensando no teorema PACELC, optaria por uma configuração que priorize a consistência (C) para as transações financeiras e para as consultas de saldo, garantindo que os dados estejam sempre atualizados e corretos, mesmo que isso possa resultar em latência ligeiramente maior durante picos de carga. 
     - Para as consultas de saldo, poderia configurar o Aurora para usar Read Replicas, mas com uma estratégia de leitura que priorize a consistência (ex: leitura direta na instância primária) para garantir que os usuários vejam o saldo mais atualizado possível.
  - Implementaria backups automáticos e estratégias de recuperação de desastres para garantir a integridade dos dados.

### DQL
Para erros de processamento, implementaria uma Dead Letter Queue (DLQ) no SQS para armazenar mensagens que não puderam ser processadas após um número configurável de tentativas. Isso permitiria:
  - Analisar as mensagens com falha posteriormente para identificar padrões ou problemas recorrentes.
  - Implementar um processo manual ou automatizado para reprocessar as mensagens da DLQ após corrigir os problemas subjacentes.