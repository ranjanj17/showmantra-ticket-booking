package com.showmantra.agent;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of Spring AI's EmbeddingModel interface that interacts natively
 * with Google's Gemini API for generating text embeddings.
 * This class uses a RestTemplate to make HTTP POST requests to the Gemini embedding endpoint.
 */
@Component
@Primary
public class GeminiNativeEmbeddingModel implements EmbeddingModel {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Converts a Document object into an embedding vector.
     * 
     * @param document the document whose content needs to be embedded
     * @return the resulting float array embedding
     */
    @Override
    public float[] embed(Document document) {
        return embed(document.getFormattedContent());
    }

    /**
     * Converts a raw text string into an embedding vector by calling the Gemini API.
     * 
     * @param text the input text string to embed
     * @return the resulting float array embedding
     */
    @Override
    public float[] embed(String text) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent?key=" + apiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of(
            "model", "models/gemini-embedding-001",
            "content", Map.of("parts", List.of(Map.of("text", text)))
        );
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
        if (response != null && response.containsKey("embedding")) {
            Map<String, Object> embeddingNode = (Map<String, Object>) response.get("embedding");
            List<Double> values = (List<Double>) embeddingNode.get("values");
            float[] result = new float[values.size()];
            for (int i = 0; i < values.size(); i++) result[i] = values.get(i).floatValue();
            return result;
        }
        return new float[768];
    }

    /**
     * Handles batch requests for multiple instructions/texts to be embedded.
     * 
     * @param request the embedding request containing the list of texts
     * @return an EmbeddingResponse containing the generated embeddings
     */
    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();
        int index = 0;
        for (String text : request.getInstructions()) {
            embeddings.add(new Embedding(embed(text), index++));
        }
        return new EmbeddingResponse(embeddings);
    }
}
