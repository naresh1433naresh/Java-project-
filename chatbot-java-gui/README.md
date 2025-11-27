# Chatbot Java GUI (Maven) — Example Project

This is a simple Java Swing GUI chatbot that calls the OpenAI Chat Completions API.
It is packaged as a Maven project and can be opened with Apache NetBeans or any IDE that supports Maven.

## Features
- Simple Swing UI (chat area, input field, send button).
- API key entry (or read from environment variable `OPENAI_API_KEY`).
- Uses OkHttp + Gson for HTTP + JSON.

## How to run
1. Open this folder in Apache NetBeans (or import the Maven project).
2. Provide an OpenAI API key in the UI, or set an environment variable `OPENAI_API_KEY`.
   - To set env var (example on Linux/macOS):
     ```
     export OPENAI_API_KEY="sk-xxxx"
     ```
   - On Windows (PowerShell):
     ```
     setx OPENAI_API_KEY "sk-xxxx"
     ```
3. Build & run the `com.example.chatbot.Main` class.
   - Or run via Maven: `mvn package` then `java -jar target/chatbot-java-gui-1.0-SNAPSHOT.jar` (if you create a runnable jar).

## Notes
- This example uses synchronous HTTP calls for simplicity.
- Replace model name if necessary (default: `gpt-3.5-turbo`).
- Use responsibly and don't commit your API key.

