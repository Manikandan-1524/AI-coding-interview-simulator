package com.aisimulator.backend;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "*")
public class SubmitController {

    private final CodeRunnerService codeRunnerService;
    private final AiFeedbackService aiFeedbackService;
    private final QuestionGeneratorService questionGeneratorService;
    private final RateLimiterService rateLimiterService;
    private final SubmissionRepository submissionRepository;
    private final InterviewerService interviewerService;
    private final WeakAreaService weakAreaService;

    public SubmitController(CodeRunnerService codeRunnerService,
                             AiFeedbackService aiFeedbackService,
                             QuestionGeneratorService questionGeneratorService,
                             RateLimiterService rateLimiterService,
                             SubmissionRepository submissionRepository,
                             InterviewerService interviewerService,
                             WeakAreaService weakAreaService) {
        this.codeRunnerService = codeRunnerService;
        this.aiFeedbackService = aiFeedbackService;
        this.questionGeneratorService = questionGeneratorService;
        this.rateLimiterService = rateLimiterService;
        this.submissionRepository = submissionRepository;
        this.interviewerService = interviewerService;
        this.weakAreaService = weakAreaService;
    }

    public static class SubmitRequest {
        public String username;
        public String questionText;
        public String topic;
        public String code;
        public List<Map<String, String>> testCases;
    }

    public static class FollowUpRequest {
        public String questionText;
        public String code;
    }

    public static class FollowUpAnswerRequest {
        public String followUpQuestion;
        public String answer;
        public String code;
    }

    @PostMapping("/submit")
    public String submitCode(@RequestBody SubmitRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();

        if (!rateLimiterService.isAllowed(ip)) {
            return "You're submitting too quickly! Please wait a minute and try again.";
        }

        String testResults = codeRunnerService.runCode(request.code, request.testCases);
        String aiFeedback = aiFeedbackService.getFeedback(request.code);

        boolean allPassed = testResults.contains("PASS") && !testResults.contains("FAIL") && !testResults.contains("ERROR");

        Submission submission = new Submission();
        submission.setUsername(request.username);
        submission.setQuestionText(request.questionText);
        submission.setTopic(request.topic);
        submission.setSubmittedCode(request.code);
        submission.setTestResults(testResults);
        submission.setAllTestsPassed(allPassed);
        submission.setSubmittedAt(LocalDateTime.now());
        submissionRepository.save(submission);

        return "=== TEST RESULTS ===\n" + testResults +
               "\n=== AI COACH FEEDBACK ===\n" + aiFeedback;
    }

    @GetMapping("/generate-question")
    public String generateQuestion(@RequestParam(defaultValue = "arrays") String topic, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();

        if (!rateLimiterService.isAllowed(ip)) {
            return "{ \"error\": \"You're requesting too quickly! Please wait a minute and try again.\" }";
        }

        return questionGeneratorService.generateQuestion(topic);
    }

    @GetMapping("/history/{username}")
    public List<Submission> getHistory(@PathVariable String username) {
        return submissionRepository.findByUsernameOrderBySubmittedAtDesc(username);
    }

    @GetMapping("/weak-areas/{username}")
    public Map<String, Object> getWeakAreas(@PathVariable String username) {
        return weakAreaService.analyze(username);
    }

    @PostMapping("/follow-up")
    public String getFollowUp(@RequestBody FollowUpRequest request) {
        return interviewerService.generateFollowUp(request.questionText, request.code);
    }

    @PostMapping("/follow-up-answer")
    public String submitFollowUpAnswer(@RequestBody FollowUpAnswerRequest request) {
        return interviewerService.evaluateAnswer(request.followUpQuestion, request.answer, request.code);
    }
}