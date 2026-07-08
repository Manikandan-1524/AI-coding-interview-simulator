package com.aisimulator.backend;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AiFeedbackService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.groq.com/openai/v1")
            .build();

    public String getFeedback(String studentCode) {
        try {
            String prompt = "You are a friendly coding interview coach. " +
                    "A student submitted this Java method as a solution to a coding interview question:\n\n" +
                    studentCode +
                    "\n\nGive short, encouraging feedback (3-4 sentences max) on their approach, " +
                    "code style, and efficiency. If there's a better approach, briefly mention it. " +
                    "Keep it simple and beginner-friendly.";

            Map<String, Object> requestBody = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    )
            );

            Map response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            var choices = (List) response.get("choices");
            var firstChoice = (Map) choices.get(0);
            var message = (Map) firstChoice.get("message");

            return (String) message.get("content");

        } catch (Exception e) {
            return "AI Feedback unavailable right now: " + e.getMessage();
        }
    }
}