package com.priyanshi.rag_backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ChatService {

    @Autowired
    private EmbeddingService embeddingService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String OLLAMA_CHAT_URL = "http://localhost:11434/api/generate";

    public String answerQuestion(String question) {
        // Step 1: Find relevant chunks
        List<String> relevantChunks = embeddingService.findRelevantChunks(question, 3);

        // Step 2: Build a prompt combining the chunks + question
        StringBuilder context = new StringBuilder();
        for (String chunk : relevantChunks) {
            context.append(chunk).append("\n\n");
        }

        String prompt = "Answer the question based only on the following context. " +
                "If the answer isn't in the context, say you don't know.\n\n" +
                "Context:\n" + context +
                "\nQuestion: " + question +
                "\nAnswer:";

        // Step 3: Send to llama3.2 via Ollama
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = objectMapper.writeValueAsString(
                 new ChatRequest("llama3.2:1b", prompt, false)        
            );

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(OLLAMA_CHAT_URL, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.get("response").asText();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get answer from Ollama: " + e.getMessage());
        }
    }

    private static class ChatRequest {
        public String model;
        public String prompt;
        public boolean stream;

        public ChatRequest(String model, String prompt, boolean stream) {
            this.model = model;
            this.prompt = prompt;
            this.stream = stream;
        }
    }
}