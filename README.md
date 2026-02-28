# Neurosymbolic Product Search

This repo shows how to combine Neo4j full-text search with local Sentence-Transformers embeddings (via FastAPI) and a Spring Boot REST API, mirroring the public demo at [https://jewelry.desertrosedesigns.net](https://jewelry.desertrosedesigns.net).

## What’s inside
- `data/products.csv`: sample catalog to import.
- `src/main/java/net/desertrosedesigns/delta/search`: Spring Boot API, Neo4j wiring, ingestion, search services, and DTOs.
- `python/`: FastAPI embedding service backed by `sentence-transformers/all-MiniLM-L6-v2`.
- `Dockerfile` + `python/Dockerfile` + `docker-compose.yml`: containerized Neo4j + embedding + Java stack.
- `.env`: shared configuration for Neo4j creds and the embedding endpoint.
- `neo4j.env`: Neo4j container only consumes `NEO4J_AUTH=neo4j/password`.

## Architecture overview
1. **Neo4j** ingests `Product` nodes with fields (`title`, `description`, `item_details`, `categories`, `text`) plus the `embedding` vector. A full-text index `productTextIndex` powers text search coverage across those fields.
2. **Embedding Service** (FastAPI) exposes a POST `/embed` that encodes arbitrary text using `sentence-transformers/all-MiniLM-L6-v2`. It runs locally in Docker so there are no API keys.
3. **Spring Boot API** handles CSV ingestion, calls the embedding service, upserts nodes, and exposes REST endpoints for text, vector, and hybrid rankings (0.4 text / 0.6 vector weighting).
4. **Docker Compose** brings up Neo4j, the FastAPI embedder, and the Spring app together for an end-to-end demo.

## Prerequisites
- Docker Desktop (Linux containers). This stack pulls Linux-based Neo4j and Python images.
- `mvn` (if you want to run the Java tests outside Docker).

## Step-by-step run instructions
1. Make sure `data/products.csv` is present (the repo already includes a sample).
2. Start Docker Compose:
   ```bash
   docker compose up --build
   ```
  The Spring app mounts the repo `data/` directory at `/data`; place `products.csv` there so the importer reads `/data/products.csv`.
3. Wait for Neo4j (logs show `Remote interface available at http://0.0.0.0:7474/`).
4. Trigger ingestion/import:
   ```bash
   curl http://localhost:8080/api/products/import
   ```
5. Try each endpoint:
   ```bash
   curl "http://localhost:8080/api/search/text?q=hoop&limit=5"
   curl "http://localhost:8080/api/search/embedding?q=hoop&limit=5"
   curl "http://localhost:8080/api/search/hybrid?q=hoop&limit=5"
   ```
6. Optional: use Neo4j Browser (`http://localhost:7474`, credentials from `.env`) to inspect nodes and indexes.

## REST API reference
| Path | Description |
| --- | --- |
| `GET /api/products/import` | Reads `data/products.csv`, calls the embedding service, and upserts Neo4j `Product` nodes while creating the `productTextIndex`. |
| `GET /api/search/text?q=<query>&limit=<n>` | Queries Neo4j full-text index across `title`, `description`, `item_details`, `categories`, and `text`. |
| `GET /api/search/embedding?q=<query>&limit=<n>` | Computes cosine similarity between the query embedding and every stored vector. |
| `GET /api/search/hybrid?q=<query>&limit=<n>` | Scores the full-text candidates by their stored embeddings and returns `combinedScore = 0.4 * textScore + 0.6 * vecScore`. |

All responses return `SearchResultDto` objects with the Neo4j product info plus `textScore`, `vectorScore`, and `combinedScore`.

## Neo4j Cypher snippets
```cypher
CREATE FULLTEXT INDEX productTextIndex IF NOT EXISTS
FOR (p:Product)
ON EACH [p.title, p.description, p.item_details, p.categories, p.text];
```

Use the hybrid query shown in the previous plan description if you want to reproduce the weighting inside Neo4j (text score + cosine similarity via GDS).

## Embedding service contract
- POST `/embed` with JSON `{"inputs": ["your text"]}`.
- Response `{"embeddings": [[...]]}` contains the vector for each input.
- The Spring app caches the results so repeated texts don’t hit the embedder every time.

## Testing
- Run the Java unit/controller tests locally:
  ```bash
  mvn test
  ```
- Or inside Docker:
  ```bash
  docker compose run --rm search-app mvn test
  ```

## Docker Compose tips
- `.env` defines:
  ```
  NEO4J_AUTH=neo4j/password
  NEO4J_URI=bolt://neo4j:7687
  NEO4J_USERNAME=neo4j
  NEO4J_PASSWORD=password
  EMBEDDING_SERVICE_URL=http://embedding-service:5000/embed
  ```
- `docker compose up --build` builds each service, pulls Neo4j, and wires them together.
- Use `docker compose down` to tear down containers and volumes.

## Manual validation checklist
1. Start the stack with `docker compose up --build`.
2. Run `/api/products/import` to populate Neo4j.
3. Call each search endpoint and confirm scores align with expectation.
4. Compare against the live site at [https://jewelry.desertrosedesigns.net](https://jewelry.desertrosedesigns.net) for reference.

## Notes
- Embeddings stay local—no external API key required.
- `data/products.csv` already includes the text fields used for embeddings.
- Adjust `.env` variables if your Neo4j/embedding service endpoints differ.
