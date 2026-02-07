## 🎯 Overview

SplitSpend is a **production-minded backend service** for managing shared expenses within groups and calculating optimized settlements between members.

&gt; **Philosophy:** *Start simple, stay correct, evolve smart.*  
&gt; Building as a well-structured monolith with clear domain boundaries, designed to gracefully evolve into microservices as complexity demands.

---

## 🚨 Problem Statement

Managing shared expenses (trips, roommates, teams) is painful:

| Challenge | Impact |
|-----------|--------|
| 🔢 **Multi-user tracking** | Lost receipts, unclear who paid what |
| ⚖️ **Complex split logic** | Uneven splits, percentages, shares |
| 📊 **Balance chaos** | Hard to know who owes whom |
| 🔄 **Settlement mess** | Too many transactions to settle debts |

---

## ✅ Solution

SplitSpend provides a **robust ledger-based system**:

- **👥 Group Management** – Organize expenses by context (trip, apartment, project)
- **🧮 Deterministic Splits** – Equal, custom amount, or percentage-based
- **📒 Ledger Tracking** – Immutable balance entries for full audit trail
- **⚡ Optimized Settlements** – Minimize transactions with graph-based algorithms

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| **Language** | Java 21 (LTS) |
| **Framework** | Spring Boot 3.x |
| **Architecture** | Modular Monolith → Microservices |
| **Database** | MySQL + Spring Data JPA |
| **API** | REST (JSON) + OpenAPI/Swagger |
| **Build** | Maven |

---

## 🏗️ Architecture

### Domain-Driven Design
The system is logically partitioned into isolated domains:

com.splitspend
├── 👤 user/          # User management & authentication
├── 👥 group/         # Group creation & membership
├── 💸 expense/       # Expense creation & split logic
└── 🏦 settlement/    # Balance calculation & optimization



> **Why Modular Monolith?**  
> *"Prioritizes simplicity and correctness first, while keeping scalability and service decomposition as an explicit future step."*

### Data Model (Ledger-Based)

```
User ||--o{ GroupMember : belongs_to
Group ||--o{ GroupMember : contains
Group ||--o{ Expense : has
Expense ||--|{ BalanceEntry : generates
BalanceEntry ||--o{ Settlement : aggregates
```

Normalized schema – Optimized for correctness over performance (read models later)
Immutable ledger – Every expense creates traceable balance entries
Aggregated settlements – Derived from net balances, not individual transactions

**🗺️ Roadmap**

| Phase       | Status           | Focus                                                  |
| ----------- | ---------------- | ------------------------------------------------------ |
| **Phase 1** | 🚧 *In Progress* | Foundation, domain modeling, health checks             |
| **Phase 2** | ⏳ Planned        | Expense creation, split strategies, validation         |
| **Phase 3** | ⏳ Planned        | Settlement engine, minimal-transaction algorithm       |
| **Phase 4** | ⏳ Planned        | Transactions, idempotency, API versioning, tests       |
| **Phase 5** | ⏳ Planned        | Kafka async, service extraction, Docker, observability |


**🚀 Getting Started**
# Clone the repository
git clone https://github.com/yourusername/splitspend.git

# Build with Maven
./mvnw clean install

# Run the application
./mvnw spring-boot:run

# API Documentation (when running)
open http://localhost:8080/swagger-ui.html


📝 License
MIT © [Tejas]
