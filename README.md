# CRM de Customer Service — Monólito Spring Boot + React (Next.js)

Aplicação de atendimento ao cliente (CRM) desenvolvida como monólito simples
com **Spring Boot** (back-end) e **Next.js/React** (front-end), organizada em
camadas (controller/service/repository) e por bounded contexts de
Domain-Driven Design (Customer, Agent, Ticket).

Consulte [docs/ARQUITETURA.md](docs/ARQUITETURA.md) para a documentação
completa da arquitetura, com diagramas de componentes e de sequência.

## Stack

| Camada | Tecnologia |
|---|---|
| Back-end | Java 21, Spring Boot 3.3 (Web, Data JPA, Validation), H2 (em memória), Maven |
| Front-end | Next.js 16 (App Router), React 18, JavaScript |
| Documentação | Markdown + diagramas Mermaid |

## Estrutura do repositório

```
crm-customer-service/
├── backend/     # API REST Spring Boot (monólito, camadas + bounded contexts)
├── frontend/    # Aplicação React (Next.js) que consome a API
└── docs/        # Documentação de arquitetura (diagramas de componentes/sequência)
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

A API sobe em `http://localhost:8080`. Endpoints principais:

- `GET/POST /api/customers`, `GET/PUT/DELETE /api/customers/{id}`
- `GET/POST /api/agents`, `GET/PUT/DELETE /api/agents/{id}`
- `GET/POST /api/tickets`, `GET/PUT/DELETE /api/tickets/{id}`
- `PATCH /api/tickets/{id}/status`
- `POST /api/tickets/{id}/interactions`

O banco H2 em memória já sobe populado com dados de exemplo (ver
`backend/src/main/resources/data.sql`). Console H2:
`http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:crmdb`, usuário
`sa`, sem senha).

### 2. Front-end (porta 3000)

```bash
cd frontend
npm install
npm run dev
```

Acesse `http://localhost:3000`. O front-end espera o back-end em
`http://localhost:8080/api` (configurável via `NEXT_PUBLIC_API_URL`, veja
`frontend/.env.local.example`).

> Rode o back-end **antes** do front-end para que o dashboard e as listagens
> carreguem os dados corretamente.

### 3. Testes do back-end

```bash
cd backend
./mvnw test
```

