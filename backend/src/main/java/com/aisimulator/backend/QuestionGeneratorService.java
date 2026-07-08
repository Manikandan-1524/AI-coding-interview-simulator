package com.aisimulator.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class QuestionGeneratorService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.groq.com/openai/v1")
            .build();

    private final Map<String, List<String>> recentlyUsed = new ConcurrentHashMap<>();

    private static final Map<String, List<String>> PROBLEM_BANK = new HashMap<>();
    static {
        PROBLEM_BANK.put("arrays", Arrays.asList(
            "Find the second largest element in an array",
            "Rotate an array to the right by k steps",
            "Find all pairs in an array that sum to a target value",
            "Find the missing number in an array of 1 to n",
            "Move all zeros in an array to the end while keeping order of other elements",
            "Find the maximum sum of any contiguous subarray (Kadane's algorithm)",
            "Merge two sorted arrays into one sorted array",
            "Find the intersection of two arrays (common elements)",
            "Find the first non-repeating element in an array",
            "Reverse an array in place without using extra space",
            "Find the majority element that appears more than n/2 times",
            "Find the equilibrium index where left sum equals right sum",
            "Count the number of subarrays with a given sum",
            "Find the smallest positive number missing from an unsorted array",
            "Sort an array of 0s, 1s, and 2s without using a sorting library (Dutch flag problem)",
            "Find leaders in an array (elements greater than all elements to their right)",
            "Calculate the running average of the last k elements",
            "Find the peak element in an array (greater than its neighbors)"
        ));
        PROBLEM_BANK.put("strings", Arrays.asList(
            "Check if two strings are anagrams of each other",
            "Find the first non-repeating character in a string",
            "Reverse the order of words in a sentence",
            "Check if a string is a valid palindrome ignoring case and spaces",
            "Count vowels and consonants in a string",
            "Find the longest common prefix among a list of strings",
            "Remove all duplicate characters from a string while keeping first occurrence order",
            "Check if one string is a rotation of another",
            "Count the number of words in a sentence",
            "Convert a string to title case (capitalize first letter of each word)",
            "Compress a string using counts of repeated characters (e.g. aab -> a2b1)",
            "Check if a string contains only digits",
            "Find the most frequently occurring character in a string",
            "Determine if two strings are one edit distance apart",
            "Find the length of the longest substring without repeating characters"
        ));
        PROBLEM_BANK.put("loops", Arrays.asList(
            "Print all prime numbers up to n using nested loops",
            "Calculate the factorial of a number using a loop",
            "Find the sum of digits of a number",
            "Check if a number is a palindrome using loops (without converting to string)",
            "Print the Fibonacci sequence up to n terms",
            "Find the GCD of two numbers using a loop",
            "Count the number of even and odd numbers in a range",
            "Find the largest digit in a number",
            "Check if a number is an Armstrong number",
            "Count how many times a digit appears in a number",
            "Find the sum of all multiples of 3 or 5 below n",
            "Reverse the digits of an integer using a loop",
            "Check if a number is a perfect number"
        ));
        PROBLEM_BANK.put("recursion", Arrays.asList(
            "Calculate factorial of a number using recursion",
            "Calculate the nth Fibonacci number using recursion",
            "Find the sum of an array using recursion",
            "Check if a string is a palindrome using recursion",
            "Calculate the power of a number (base^exponent) using recursion",
            "Find the greatest common divisor using recursion",
            "Reverse a string using recursion",
            "Count the number of digits in a number using recursion",
            "Find the maximum element in an array using recursion",
            "Convert a decimal number to binary using recursion",
            "Calculate the sum of digits of a number using recursion"
        ));
        PROBLEM_BANK.put("sorting", Arrays.asList(
            "Implement bubble sort on an integer array",
            "Implement selection sort on an integer array",
            "Implement insertion sort on an integer array",
            "Sort an array of strings by their length",
            "Sort an array and then find the kth smallest element",
            "Check if an array is sorted in ascending order",
            "Sort an array of even numbers first, then odd numbers",
            "Merge two sorted arrays into one sorted array without using built-in sort",
            "Sort an array of 0s and 1s using a single pass (two-pointer technique)",
            "Find the kth largest element in an unsorted array"
        ));
        PROBLEM_BANK.put("searching", Arrays.asList(
            "Implement binary search on a sorted array",
            "Find the first and last position of a target in a sorted array",
            "Search for a target in a rotated sorted array",
            "Find the square root of a number using binary search",
            "Find the closest number to a target in a sorted array",
            "Count how many numbers in a sorted array are less than a target",
            "Find the index of the smallest element in a rotated sorted array",
            "Implement linear search and return all indices where the target is found"
        ));
        PROBLEM_BANK.put("math", Arrays.asList(
            "Check if a number is prime",
            "Find all prime numbers up to n using the Sieve of Eratosthenes",
            "Calculate the greatest common divisor (GCD) of two numbers",
            "Calculate the least common multiple (LCM) of two numbers",
            "Check if a number is a perfect square",
            "Find the sum of all divisors of a number",
            "Convert a Roman numeral string to an integer",
            "Calculate the power of a number using fast exponentiation",
            "Check if two numbers are coprime",
            "Find the number of trailing zeros in the factorial of a number"
        ));
        PROBLEM_BANK.put("matrix", Arrays.asList(
            "Find the sum of all elements in a 2D matrix (flatten it into row sums)",
            "Transpose a square matrix in place",
            "Find the maximum element in each row of a matrix",
            "Check if a matrix is a magic square (all rows/columns/diagonals sum equally)",
            "Find the sum of the main diagonal of a square matrix",
            "Rotate a square matrix 90 degrees clockwise",
            "Find the row with the maximum sum in a 2D matrix",
            "Search for a target value in a row-wise and column-wise sorted matrix"
        ));
        PROBLEM_BANK.put("bitmanipulation", Arrays.asList(
            "Count the number of set bits (1s) in an integer's binary representation",
            "Check if a number is a power of two using bit manipulation",
            "Find the single number that appears once while others appear twice, using XOR",
            "Swap two numbers without using a temporary variable",
            "Check if the nth bit of a number is set",
            "Find the missing number in an array using XOR",
            "Convert an integer to its binary string representation",
            "Count the number of bits needed to convert one integer to another"
        ));
    }

    public String generateQuestion(String topic) {
        try {
            List<String> problems = PROBLEM_BANK.getOrDefault(topic, PROBLEM_BANK.get("arrays"));
            List<String> used = recentlyUsed.computeIfAbsent(topic, k -> new ArrayList<>());

            if (used.size() >= problems.size() - 2) {
                used.clear();
            }

            List<String> available = new ArrayList<>(problems);
            available.removeAll(used);
            if (available.isEmpty()) available = problems;

            String chosenProblem = available.get(new Random().nextInt(available.size()));
            used.add(chosenProblem);

            String prompt = "Write full coding interview question details for EXACTLY this problem (do not change or replace it): \"" + chosenProblem + "\"\n\n" +
                    "CRITICAL: Before writing your final answer, carefully work through each test case step-by-step to make sure the expected output is actually correct. Double-check tricky cases especially involving duplicates or edge cases. Only include test cases you are 100% certain are correct.\n\n" +
                    "The method must take simple parameters (int[], int, String, boolean, or int[][] for matrix problems) and return a simple type (int, int[], String, boolean, or int[][]).\n" +
                    "Provide 3 test cases as REAL, VALID Java code expressions calling the method directly, e.g. \"countOccurrences(new int[]{1,5,2,5,3,5,4}, 5)\" and expected results as Java literals, e.g. \"3\" or \"new int[]{0,1}\" or \"\\\"hello\\\"\". For matrix parameters use format like \"new int[][]{{1,2},{3,4}}\".\n\n" +
                    "Reply ONLY with valid JSON, no extra text, in this exact format:\n" +
                    "{\n" +
                    "  \"questionText\": \"description of the problem, with an example\",\n" +
                    "  \"methodSignature\": \"public static int[] methodName(int[] nums, int target)\",\n" +
                    "  \"testCases\": [\n" +
                    "    { \"call\": \"methodName(actual, java, arguments)\", \"expected\": \"expectedResultAsJavaLiteral\" },\n" +
                    "    { \"call\": \"methodName(actual, java, arguments)\", \"expected\": \"expectedResultAsJavaLiteral\" }\n" +
                    "  ]\n" +
                    "}";

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
            String rawText = (String) message.get("content");

            rawText = rawText.replace("```json", "").replace("```", "").trim();

            return rawText;

        } catch (Exception e) {
            return "{ \"error\": \"Could not generate question: " + e.getMessage() + "\" }";
        }
    }
}