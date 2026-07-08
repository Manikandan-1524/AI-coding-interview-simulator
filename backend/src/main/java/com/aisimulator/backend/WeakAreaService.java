package com.aisimulator.backend;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class WeakAreaService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.groq.com/openai/v1")
            .build();

    private final SubmissionRepository submissionRepository;

    public WeakAreaService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    public Map<String, Object> analyze(String username) {
        List<Submission> submissions = submissionRepository.findByUsernameOrderBySubmittedAtDesc(username);

        // Group by topic: count attempts and passes
        Map<String, int[]> topicStats = new LinkedHashMap<>(); // [attempts, passes]

        for (Submission s : submissions) {
            String topic = s.getTopic() != null ? s.getTopic() : "unknown";
            topicStats.putIfAbsent(topic, new int[]{0, 0});
            int[] stats = topicStats.get(topic);
            stats[0]++;
            if (s.isAllTestsPassed()) stats[1]++;
        }

        List<Map<String, Object>> topicList = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : topicStats.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("topic", entry.getKey());
            item.put("attempts", entry.getValue()[0]);
            item.put("passes", entry.getValue()[1]);
            double rate = entry.getValue()[0] == 0 ? 0 : (double) entry.getValue()[1] / entry.getValue()[0] * 100;
            item.put("passRate", Math.round(rate));
            topicList.add(item);
        }

        String aiSummary = generateSummary(topicList, submissions.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("topics", topicList);
        result.put("totalSubmissions", submissions.size());
        result.put("aiSummary", aiSummary);
        return result;
    }

    private String generateSummary(List<Map<String, Object>> topicList, int totalSubmissions) {
        if (totalSubmissions == 0) {
            return "You haven't submitted any solutions yet. Start practicing to see your progress here!";
        }

        try {
            StringBuilder statsText = new StringBuilder();
            for (Map<String, Object> item : topicList) {
                statsText.append(item.get("topic")).append(": ")
                        .append(item.get("passes")).append("/").append(item.get("attempts"))
                        .append(" passed (").append(item.get("passRate")).append("%)\n");
            }

            String prompt = "You are a supportive coding interview coach. Here is a student's practice history by topic:\n\n" +
                    statsText +
                    "\nWrite a short, encouraging 2-3 sentence summary. Point out their strongest topic and their weakest topic " +
                    "(lowest pass rate with at least 2 attempts), and give one specific, actionable tip for improving the weak area. " +
                    "Be warm and motivating, not critical.";

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

        } catch (Exception e) {
            return "Keep practicing across topics to build a well-rounded skill set!";
        }
    }
}