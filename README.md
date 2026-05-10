# Smart Financial Auditor

AI-powered banking regulatory compliance auditor that demonstrates the implementation of the **Retrieval-Augmented Generation (RAG)** pattern using two prominent Java AI frameworks: **Spring AI** and **LangChain4j**.

## 🚀 Overview

The Smart Financial Auditor is designed to help banking compliance officers analyze Polish financial regulations (KNF guidelines, internal policies, etc.) using LLMs. It features a dual-framework architecture, allowing side-by-side comparison of Spring AI and LangChain4j implementations for document ingestion and retrieval.

### Key Features
- **Document Ingestion:** Automated PDF parsing, chunking, and indexing into vector stores.
- **Dual RAG Implementation:**
    - **LangChain4j:** Using `AiService` with `ContentRetriever`.
    - **Spring AI:** Using `ChatClient` with `QuestionAnswerAdvisor`.
- **Specialized Auditor Persona:** Strict system prompts ensure the AI acts as a professional auditor and only answers based on provided context.
- **Hybrid LLM Support:** Seamlessly switch between **OpenAI** (cloud) and **Ollama** (local inference).
- **Modern Java:** Leverages **Java 21 Virtual Threads** for high-concurrency document processing.

## 🛠️ Tech Stack

- **Backend:** Java 21, Spring Boot 4.0.6
- **AI Frameworks:** 
  - Spring AI (2.0.0-M4)
  - LangChain4j (1.0.0)
- **Database:** PostgreSQL 16 + **pgvector**
- **LLM / Embeddings:**
  - **Local:** Ollama (Llama 3.2, nomic-embed-text)
  - **Cloud:** OpenAI (GPT-4o, text-embedding-3-small)
- **Infrastructure:** Docker, Kubernetes (K3s), Helm
- **CI/CD:** GitHub Actions (Build, Test, Publish, Deploy)

## 📋 Prerequisites

- **Docker & Docker Compose**
- **Java 21** (to run locally)
- **Maven 3.9+** (or use included `./mvnw`)
- **OpenAI API Key** (optional, required only for `prd` profile)

## 🏃 Getting Started

### 1. Local Development (with Ollama)

The local environment uses Ollama for self-hosted LLM inference and PostgreSQL with pgvector.

1.  **Start Infrastructure:**
    ```bash
    docker-compose up -d
    ```
2.  **Run Application:**
    ```bash
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
    ```

### 2. Kubernetes Deployment (Helm)

The project includes Helm charts for automated deployment to a K3s cluster.

```bash
helm upgrade --install smart-financial-auditor ./charts/smart-financial-auditor \
  --namespace smart-auditor --create-namespace
```

The Helm chart includes:
- **Application Deployment:** With environment-aware config.
- **PostgreSQL:** Using `pgvector/pgvector:pg16` with persistent storage.
- **Ollama:** With an init container that automatically pulls `llama3.2` and `nomic-embed-text`.

## 🔌 API Endpoints

### Document Ingestion
`POST /api/v1/documents/ingest`
- **Type:** `multipart/form-data`
- **Parameter:** `file` (PDF document)
- **Description:** Parses PDF, splits into segments, and stores embeddings in both `vector_store` (Spring AI) and `document_embeddings` (LangChain4j) tables.

### Banking Audit (RAG)
Analyze regulations using the ingested context.

- **LangChain4j Endpoint:** `POST /api/v1/audit/query`
- **Spring AI Endpoint:** `POST /api/v1/audit/spring-ai/query`

**Request Body:**
```json
{
  "question": "What are the key requirements for credit risk management according to KNF?"
}
```

### General Chat
`POST /api/v1/chat`
- **Description:** General interaction with the LLM (no RAG context).

## 🔄 CI/CD Workflow

The `.github/workflows/ci.yml` automates the entire lifecycle:
1.  **Build & Test:** Compiles code and runs tests using Testcontainers (PostgreSQL with pgvector).
2.  **Publish:** Builds a Docker image and pushes it to GitHub Container Registry (GHCR).
3.  **Helm:** Lints and packages the Helm chart.
4.  **Deploy:** Automatically deploys to a K3s cluster on every push to `main`.

---
Developed as a showcase for modern Java AI capabilities.
