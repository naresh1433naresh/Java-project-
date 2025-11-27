package com.example.chatbot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.Executors;

public class Main {

    private static final String DEFAULT_MODEL = "gpt-3.5-turbo";
    private final JFrame frame;
    private final JTextArea chatArea;
    private final JTextField inputField;
    private final JButton sendButton;
    private final JTextField apiKeyField;
    private final JComboBox<String> modelBox;
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public Main() {
        frame = new JFrame("Chatbot AI (Java Swing)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(chatArea);
        frame.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        inputField = new JTextField();
        bottom.add(inputField, BorderLayout.CENTER);

        sendButton = new JButton("Send");
        bottom.add(sendButton, BorderLayout.EAST);

        frame.add(bottom, BorderLayout.SOUTH);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("API Key:"));
        apiKeyField = new JTextField(30);
        String envKey = System.getenv("OPENAI_API_KEY");
        if (envKey != null && !envKey.isEmpty()) {
            apiKeyField.setText(envKey);
        }
        top.add(apiKeyField);

        top.add(new JLabel("Model:"));
        modelBox = new JComboBox<>(new String[]{DEFAULT_MODEL, "gpt-4", "gpt-4o", "gpt-4o-mini"});
        modelBox.setSelectedItem(DEFAULT_MODEL);
        top.add(modelBox);

        frame.add(top, BorderLayout.NORTH);

        // Action: send when pressing button or Enter
        ActionListener sendAction = e -> {
            String apiKey = apiKeyField.getText().trim();
            if (apiKey.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter your OpenAI API key (or set OPENAI_API_KEY env var).", "Missing API Key", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String message = inputField.getText().trim();
            if (message.isEmpty()) return;
            appendToChat("You: " + message);
            inputField.setText("");
            sendButton.setEnabled(false);
            // Run network call off the EDT
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    String reply = callChatApi(apiKey, (String) modelBox.getSelectedItem(), message);
                    SwingUtilities.invokeLater(() -> appendToChat("AI: " + reply));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        appendToChat("Error: " + ex.getMessage());
                        ex.printStackTrace();
                    });
                } finally {
                    SwingUtilities.invokeLater(() -> sendButton.setEnabled(true));
                }
            });
        };

        sendButton.addActionListener(sendAction);
        inputField.addActionListener(sendAction);

        frame.setVisible(true);
    }

    private void appendToChat(String text) {
        chatArea.append(text + "\n\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private String callChatApi(String apiKey, String model, String userMessage) throws IOException {
        // Prepare JSON body for the Chat Completions endpoint
        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        JsonArray messages = new JsonArray();

        JsonObject sys = new JsonObject();
        sys.addProperty("role", "system");
        sys.addProperty("content", "You are a helpful assistant.");
        messages.add(sys);

        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", userMessage);
        messages.add(user);

        body.add("messages", messages);
        body.addProperty("max_tokens", 800);
        body.addProperty("temperature", 0.6);

        RequestBody reqBody = RequestBody.create(body.toString(), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Accept", "application/json")
                .post(reqBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String err = response.body() != null ? response.body().string() : "empty response";
                throw new IOException("Unexpected code " + response.code() + ": " + err);
            }
            String respBody = response.body() != null ? response.body().string() : "";
            JsonObject json = gson.fromJson(respBody, JsonObject.class);
            // Extract text from choices[0].message.content
            JsonArray choices = json.has("choices") ? json.getAsJsonArray("choices") : null;
            if (choices != null && choices.size() > 0) {
                JsonObject first = choices.get(0).getAsJsonObject();
                JsonObject message = first.has("message") ? first.getAsJsonObject("message") : null;
                if (message != null && message.has("content")) {
                    return message.get("content").getAsString().trim();
                } else if (first.has("text")) {
                    return first.get("text").getAsString().trim();
                }
            }
            return "No reply (empty).";
        }
    }

    public static void main(String[] args) {
        // Ensure UI is created on the EDT
        SwingUtilities.invokeLater(Main::new);
    }
}
