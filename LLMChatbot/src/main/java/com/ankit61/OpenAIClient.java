package com.ankit61;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class OpenAIClient {

    private static final String API_KEY = "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";

    public static String askGPT(String prompt) throws Exception {

        URL url = new URL("https://api.openai.com/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String safePrompt = escape(prompt);

        String payload =
                "{"
                        + "\"model\": \"gpt-4o-mini\","
                        + "\"messages\": ["
                        + "  {\"role\": \"system\", \"content\": \"You are a helpful assistant\"},"
                        + "  {\"role\": \"user\", \"content\": \"" + safePrompt + "\"}"
                        + "]"
                        + "}";


        OutputStream os = conn.getOutputStream();
        os.write(payload.getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();

        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream()
                )
        );

        StringBuilder json = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            json.append(line);
        }
        br.close();

        return extractContent(json.toString());

    }

    private static String extractContent(String json) {

        int contentPos = json.indexOf("\"content\"");
        if (contentPos == -1) return "No content found.";

        int colon = json.indexOf(":", contentPos);
        int start = json.indexOf("\"", colon + 1) + 1;

        StringBuilder sb = new StringBuilder();
        boolean escape = false;

        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escape) {
                sb.append(c == 'n' ? '\n' : c == 't' ? '\t' : c);
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }


    private static String escape(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

}
