package de.lhind.mistral_ai_poc;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping("/api")
public class OllamaController {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody String requestBody) {
        try {
            // Build payload
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("model", "mistral");
            payload.put("stream", false);
            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", requestBody);
            messages.add(userMessage);
            payload.set("messages", messages);
            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/chat"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            String rawResponse = response.body();

            StringBuilder fullResponse = new StringBuilder();
            try {
                JsonNode node = objectMapper.readTree(rawResponse);
                if (node.has("message") && node.get("message").has("content")) {
                    fullResponse.append(node.get("message").get("content").asText());
                } else if (node.has("content")) {
                    fullResponse.append(node.get("content").asText());
                }
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("Failed to parse Ollama response. Raw response: " + rawResponse);
            }

            if (fullResponse.length() == 0) {
                return ResponseEntity.internalServerError().body("No 'content' field found in Ollama response. Raw response: " + rawResponse);
            }

            String cleanedResponse = fullResponse.toString().trim();

            return ResponseEntity.ok(cleanedResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
