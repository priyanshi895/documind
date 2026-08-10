package com.priyanshi.rag_backend;
import org.springframework.jdbc.core.JdbcTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.ArrayList;

@Service
public class EmbeddingService {

    @Autowired
    private DocumentChunkRepository repository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String OLLAMA_URL = "http://localhost:11434/api/embeddings";

    // Calls Ollama to get the embedding for a piece of text
    public List<Double> getEmbedding(String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = objectMapper.writeValueAsString(
                new EmbeddingRequest("nomic-embed-text", text)
            );

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(OLLAMA_URL, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode embeddingNode = root.get("embedding");

            List<Double> embedding = new ArrayList<>();
            for (JsonNode value : embeddingNode) {
                embedding.add(value.asDouble());
            }
            return embedding;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get embedding from Ollama: " + e.getMessage());
        }
    }

    // Converts embedding to Postgres vector format: [0.1,0.2,0.3,...]
    public String formatEmbeddingForPostgres(List<Double> embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.size(); i++) {
            sb.append(embedding.get(i));
            if (i < embedding.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    // Takes a chunk of text, embeds it, and saves it to the database
    public void embedAndSaveChunk(String chunkText) {
    List<Double> embedding = getEmbedding(chunkText);
    String formattedEmbedding = formatEmbeddingForPostgres(embedding);

    jdbcTemplate.update(
        "INSERT INTO document_chunks (content, embedding) VALUES (?, CAST(? AS vector))",
        chunkText, formattedEmbedding);
    }

    // Finds the most relevant chunks for a given question
    public List<String> findRelevantChunks(String question, int topK) {
    List<Double> questionEmbedding = getEmbedding(question);
    String formattedEmbedding = formatEmbeddingForPostgres(questionEmbedding);

    return jdbcTemplate.query(
        "SELECT content FROM document_chunks ORDER BY embedding <-> CAST(? AS vector) LIMIT ?",
        (rs, rowNum) -> rs.getString("content"),
        formattedEmbedding, topK);
    }

    // Small helper class to build the JSON request Ollama expects
    private static class EmbeddingRequest {
        public String model;
        public String prompt;

        public EmbeddingRequest(String model, String prompt) {
            this.model = model;
            this.prompt = prompt;
        }
    }
}