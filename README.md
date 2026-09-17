# CRM de Customer Service — Monólito Spring Boot + React (Next.js)

Aplicação de atendimento ao cliente (CRM) desenvolvida como monólito simples
com **Spring Boot** (back-end) e **Next.js/React** (front-end), organizada em
camadas (controller/service/repository) e por bounded contexts de
Domain-Driven Design (Customer, Agent, Ticket).

Documentação complementar:

- [docs/ARQUITETURA.md](docs/ARQUITETURA.md) — arquitetura da Etapa 1
  (componentes e sequência).
- [docs/PERSISTENCIA.md](docs/PERSISTENCIA.md) — **Etapa 2 (branch `TP2`)**:
  design da camada de persistência, modelo de dados, repositórios Spring Data,
  histórico/auditoria (Envers) e testes.

## Stack

| Camada | Tecnologia |
|---|---|
| Back-end | Java 21, Spring Boot 3.3 (Web, Data JPA, Validation), Spring Data Envers, Hibernate Envers, H2 (arquivo), Maven |
| Front-end | Next.js 16 (App Router), React 18, JavaScript |
| Testes | JUnit 5, Spring Boot Test (`@DataJpaTest`, `@SpringBootTest`, MockMvc), AssertJ |
| Documentação | Markdown + diagramas Mermaid |

## Estrutura do repositório

```
crm-customer-service/
├── backend/     # API REST Spring Boot (monólito, camadas + bounded contexts)
│   ├── data/    # banco H2 em arquivo (gerado em runtime, ignorado pelo Git)
│   └── src/main/java/com/pb/crm/
│       ├── audit/      # AuditableEntity, entidade de revisão Envers, DTO de revisão
│       ├── config/     # PersistenceConfig (JPA Auditing + Envers), RequestActor, DataSeeder
│       ├── common/     # exceções, handler global, paginação, CORS
│       ├── customer/   # bounded context Cliente
│       ├── agent/      # bounded context Atendente
│       └── ticket/     # bounded context Ticket (agregado com interações e histórico)
├── frontend/    # Aplicação React (Next.js) que consome a API
└── docs/        # Documentação de arquitetura e persistência
```

## Pré-requisitos

- **Java 21+** (JDK) — para o back-end
- **IntelliJ IDEA** — para abrir e rodar o back-end (detecta o Maven
  automaticamente via `backend/pom.xml`)
- **Node.js 18+** e **npm** — para o front-end

Não é necessário instalar o Maven manualmente: o projeto inclui o **Maven
Wrapper** (`backend/mvnw` / `backend/mvnw.cmd`), e o IntelliJ também traz um
Maven embutido.

## Como executar

### 1. Back-end (porta 8080)

Abra a pasta `backend/` no IntelliJ como projeto Maven (ele será detectado
automaticamente pelo `pom.xml`) e rode a classe
`com.pb.crm.CrmCustomerServiceApplication` (botão ▶ ao lado do `main`).

Alternativamente, via terminal:

```bash
cd backend
./mvnw spring-boot:run        # Linux/macOS
mvnw.cmd spring-boot:run      # Windows
```

A API sobe em `http://localhost:8080`.

O banco H2 é criado em **`backend/data/crmdb.mv.db`** na primeira execução e
os dados **persistem entre reinícios**. Na primeira subida (banco vazio) o
`DataSeeder` carrega clientes, atendentes e tickets de exemplo, já com
histórico de status e revisões. Para começar do zero, pare a aplicação e apague
a pasta `backend/data/`. Para subir sem carga inicial, use
`app.seed.enabled=false`.

Console H2: `http://localhost:8080/h2-console` (JDBC URL
`jdbc:h2:file:./data/crmdb`, usuário `sa`, sem senha). Além das tabelas de
negócio, é possível inspecionar as tabelas de auditoria `*_AUD`, `REVINFO` e
`TICKET_STATUS_HISTORY`.

> **Windows:** a aplicação define `jdk.net.unixdomain.tmpdir` para
> `%SystemRoot%\Temp` ao iniciar, contornando uma falha do JDK ao abrir o
> loopback do NIO em alguns perfis de usuário ("Unable to establish loopback
> connection"). Se preferir, defina a propriedade nas opções de VM do IntelliJ.

#### Endpoints

Cabeçalho opcional em qualquer requisição: `X-Actor: <nome>` — identifica quem
está operando; é gravado em `created_by`/`updated_by`, no histórico de status e
nas revisões Envers (padrão: `system`).

| Recurso | Endpoints |
|---|---|
| Clientes | `GET/POST /api/customers`, `GET/PUT/DELETE /api/customers/{id}`, `GET /api/customers/search?q=&page=&size=&sort=`, `GET /api/customers/{id}/revisions` |
| Atendentes | `GET/POST /api/agents` (`?active=true`), `GET/PUT/DELETE /api/agents/{id}`, `GET /api/agents/{id}/revisions` |
| Tickets | `GET /api/tickets` (`?status=&customerId=`), `POST /api/tickets`, `GET/PUT/DELETE /api/tickets/{id}`, `PATCH /api/tickets/{id}/status` (`{"status","reason"}`), `POST /api/tickets/{id}/interactions` |
| Consultas de tickets | `GET /api/tickets/search?status=&priority=&customerId=&agentId=&subject=&createdFrom=&createdTo=&page=&size=&sort=`, `GET /api/tickets/stats` |
| Histórico | `GET /api/tickets/{id}/status-history`, `GET /api/tickets/{id}/revisions` |

Respostas de erro: `404` (não encontrado), `400` (validação/parâmetro
inválido), `409` (regra de negócio, duplicidade, vínculo de integridade ou
conflito de versão).

Exemplo com `curl`:

```bash
curl -X PATCH http://localhost:8080/api/tickets/1/status \
  -H "Content-Type: application/json" -H "X-Actor: maria" \
  -d '{"status":"RESOLVED","reason":"cliente confirmou a solucao"}'

curl http://localhost:8080/api/tickets/1/status-history
curl http://localhost:8080/api/tickets/1/revisions
```

### 2. Front-end (porta 3000)

```bash
cd frontend
npm install
npm run dev
```

Acesse `http://localhost:3000`. O front-end espera o back-end em
`http://localhost:8080/api` (configurável via `NEXT_PUBLIC_API_URL`, veja
`frontend/.env.local.example`). O nome enviado no cabeçalho `X-Actor` pode ser
alterado com `NEXT_PUBLIC_ACTOR`.

A página de detalhe do ticket exibe, além das interações, a **linha do tempo
de status** e as **revisões de auditoria** do ticket.

> Rode o back-end **antes** do front-end para que o dashboard e as listagens
> carreguem os dados corretamente.

### 3. Testes do back-end

```bash
cd backend
./mvnw test          # Linux/macOS
mvnw.cmd test        # Windows
```

Os testes usam o perfil `test` (H2 em memória) e cobrem repositórios,
auditoria/Envers, lock otimista, regras do agregado e a API REST. Detalhes em
[docs/PERSISTENCIA.md](docs/PERSISTENCIA.md#7-testes-automatizados).
