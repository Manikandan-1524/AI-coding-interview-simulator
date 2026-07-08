package com.aisimulator.backend;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class InterviewerService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.groq.com/openai/v1")
            .build();

    // Step 1: AI asks a natural follow-up question, like a real interviewer would
    public String generateFollowUp(String questionText, String studentCode) {
        try {
            String prompt = "You are conducting a live coding interview. The candidate was asked:\n" + questionText +
                    "\n\nThey submitted this solution:\n" + studentCode +
                    "\n\nAsk ONE short, natural follow-up question a real interviewer would ask next " +
                    "(e.g. about time/space complexity, edge cases, how to optimize, or what happens with different input sizes). " +
                    "Keep it to one sentence, conversational, like you're speaking out loud. Do not answer it yourself.";

            return callGroq(prompt);

        } catch (Exception e) {
            return "Could not generate a follow-up question right now.";
        }
    }

    // Step 2: AI evaluates the candidate's answer to that follow-up
    public String evaluateAnswer(String followUpQuestion, String studentAnswer, String studentCode) {
        try {
            String prompt = "You are a friendly coding interviewer. You asked the candidate this follow-up question:\n" +
                    "\"" + followUpQuestion + "\"\n\n" +
                    "Their code was:\n" + studentCode + "\n\n" +
                    "Their answer was:\n\"" + studentAnswer + "\"\n\n" +
                    "Give short, honest, encouraging feedback (2-3 sentences) on whether their answer is correct and complete. " +
                    "If they missed something important, gently point it out. Speak like a real interviewer giving live feedback.";

            return callGroq(prompt);

        } catch (Exception e) {
            return "Could not evaluate your answer right now.";
        }
    }

    private String callGroq(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", "llama-3.3-70b-versatile",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
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
        return ((String) message.get("content")).trim();
    }
}