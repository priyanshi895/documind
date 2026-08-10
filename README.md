# DocuMind — RAG-Based Document Q&A Assistant

DocuMind lets you upload a PDF and ask natural-language questions about its content. It uses Retrieval-Augmented Generation (RAG) to ground every answer in the actual document, rather than relying on a model's general knowledge — combined with a locally-run LLM, so the entire pipeline runs at zero API cost.

## How it works

1. **Upload** — A PDF is uploaded and its text is extracted using Apache PDFBox.
2. **Chunk** — The extracted text is split into overlapping ~800-character chunks to preserve context across boundaries.
3. **Embed** — Each chunk is converted into a vector embedding using Ollama's `nomic-embed-text` model, running entirely locally.
4. **Store** — Chunks and their embeddings are stored in PostgreSQL using the `pgvector` extension.
5. **Retrieve** — When a question is asked, it's embedded the same way, and the most semantically similar chunks are retrieved via vector similarity search.
6. **Generate** — The retrieved chunks and the question are passed to a local LLM (via Ollama), which generates an answer grounded in the retrieved context.

## Tech Stack

**Backend:** Java, Spring Boot, PostgreSQL + pgvector, Apache PDFBox
**Frontend:** React, Vite, Tailwind CSS
**AI/ML:** Ollama (local LLM runtime) — `nomic-embed-text` for embeddings, `llama3.2:1b` for generation
**Infrastructure:** Docker (Postgres container)

## Why local LLMs (Ollama) instead of a cloud API?

This project deliberately uses Ollama to run both the embedding and generation models locally rather than calling a cloud API like OpenAI. This keeps the entire pipeline free to run, demonstrates self-hosted AI infrastructure, and avoids any dependency on external API keys or rate limits.

## Running Locally

### Prerequisites
- Java 21+
- Node.js 18+
- Docker Desktop
- [Ollama](https://ollama.com) installed, with these models pulled:
  ollama pull nomic-embed-text
  ollama pull llama3.2:1b
### 1. Start the database
```bash
docker run --name rag-postgres -e POSTGRES_PASSWORD=postgres123 -e POSTGRES_DB=ragdb -p 5432:5432 -d pgvector/pgvector:pg16
docker exec -it rag-postgres psql -U postgres -d ragdb -c "CREATE EXTENSION vector;"
docker exec -it rag-postgres psql -U postgres -d ragdb -c "CREATE TABLE document_chunks (id SERIAL PRIMARY KEY, content TEXT NOT NULL, embedding VECTOR(768));"
```

### 2. Start the backend
```bash
cd rag-backend
./mvnw spring-boot:run
```
Runs on `http://localhost:8081`

### 3. Start the frontend
```bash
cd rag-frontend
npm install
npm run dev
```
Runs on `http://localhost:5173`

## Project Structure
documind/
├── rag-backend/ # Spring Boot API, PDF processing, RAG logic
└── rag-frontend/ # React chat interface
## Key Technical Decisions

- **pgvector over a dedicated vector database** — keeps infrastructure simple (one database for both relational and vector data) rather than introducing a separate specialized service.
- **Overlapping chunks** — a 100-character overlap between chunks prevents important context from being awkwardly split at chunk boundaries.
- **Raw SQL with explicit casting for vector inserts** — Hibernate/JPA cannot automatically cast a Java `String` to Postgres's `vector` type, so vector inserts use `JdbcTemplate` with an explicit `CAST(? AS vector)`.

## Known Limitations

- **Response latency on modest hardware** — local LLM inference via Ollama can take significantly longer without a dedicated GPU. This was isolated as a hardware/inference constraint (not a retrieval bug) by testing against a freshly cleared, minimal dataset and observing similar latency.
- **No document isolation** — all uploaded documents currently share a single chunk table, so multiple uploads can mix content in retrieval. Tagging chunks with a document ID is a planned improvement.

## Future Improvements

- Tag chunks with a document ID to support multiple documents without cross-contamination
- Streaming responses instead of waiting for the full answer
- Persistent chat history across sessions
- Optional cloud API fallback (OpenAI/Claude) for faster inference in production