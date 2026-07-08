package com.aisimulator.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service
public class CodeRunnerService {

    public String runCode(String studentMethod, List<Map<String, String>> testCases) {
        try {
            Path tempDir = Files.createTempDirectory("student_code");
            Path javaFile = tempDir.resolve("Main.java");

            StringBuilder testCode = new StringBuilder();
            int i = 1;
            for (Map<String, String> test : testCases) {
                String call = test.get("call");
                String expected = sanitizeExpected(test.get("expected"));
                testCode.append(
                    "        try {\n" +
                    "            var result" + i + " = " + call + ";\n" +
                    "            var expected" + i + " = " + expected + ";\n" +
                    "            boolean pass" + i + " = java.util.Objects.deepEquals(result" + i + ", expected" + i + ");\n" +
                    "            System.out.println(\"Test " + i + ": \" + (pass" + i + " ? \"PASS\" : \"FAIL\") + \" (expected \" + fmt(expected" + i + ") + \", got \" + fmt(result" + i + ") + \")\");\n" +
                    "        } catch (Exception e) {\n" +
                    "            System.out.println(\"Test " + i + ": ERROR - \" + e.getMessage());\n" +
                    "        }\n"
                );
                i++;
            }

            String fullCode =
                "import java.util.*;\n" +
                "public class Main {\n" +
                "    " + studentMethod + "\n" +
                "\n" +
                "    static String fmt(Object o) {\n" +
                "        if (o instanceof int[]) return Arrays.toString((int[]) o);\n" +
                "        if (o instanceof String[]) return Arrays.toString((String[]) o);\n" +
                "        if (o instanceof boolean[]) return Arrays.toString((boolean[]) o);\n" +
                "        return String.valueOf(o);\n" +
                "    }\n" +
                "\n" +
                "    public static void main(String[] args) {\n" +
                testCode +
                "    }\n" +
                "}";

            Files.writeString(javaFile, fullCode);

            Process compile = new ProcessBuilder("javac", javaFile.toString())
                    .directory(tempDir.toFile())
                    .redirectErrorStream(true)
                    .start();

            boolean compiled = compile.waitFor(10, TimeUnit.SECONDS);
            String compileOutput = readOutput(compile);

            if (!compiled || compile.exitValue() != 0) {
                return "Compilation Error:\n" + compileOutput;
            }

            Process run = new ProcessBuilder("java", "-cp", tempDir.toString(), "Main")
                    .redirectErrorStream(true)
                    .start();

            boolean finished = run.waitFor(5, TimeUnit.SECONDS);

            if (!finished) {
                run.destroyForcibly();
                return "Error: Code took too long to run (possible infinite loop).";
            }

            return readOutput(run);

        } catch (Exception e) {
            return "Server Error: " + e.getMessage();
        }
    }

    // Fixes common AI mistakes, like writing [1, 3] instead of proper Java: new int[]{1, 3}
    private String sanitizeExpected(String expected) {
        String trimmed = expected.trim();
        if (trimmed.equals("null")) {
            return "(Object) null";
        }
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String inner = trimmed.substring(1, trimmed.length() - 1);
            return "new int[]{" + inner + "}";
        }
        return expected;
    }

    private String readOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        return output.toString();
    }
}