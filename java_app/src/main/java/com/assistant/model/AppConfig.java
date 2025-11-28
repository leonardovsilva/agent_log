package com.assistant.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class AppConfig {
    private static final String CONFIG_FILE = "config.json";
    private String openAiApiKey;
    private String openAiModel;

    public AppConfig() {
        loadConfig();
    }

    private void loadConfig() {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try {
                JsonNode root = mapper.readTree(file);
                if (root.has("OPENAI_API_KEY")) {
                    this.openAiApiKey = root.get("OPENAI_API_KEY").asText();
                }
                if (root.has("OPENAI_MODEL")) {
                    this.openAiModel = root.get("OPENAI_MODEL").asText();
                }
            } catch (IOException e) {
                System.err.println("Error loading config.json: " + e.getMessage());
            }
        } else {
            System.out.println("config.json not found, using defaults or environment variables.");
        }
        
        // Fallback to environment variables
        if (this.openAiApiKey == null) {
            this.openAiApiKey = System.getenv("OPENAI_API_KEY");
        }
        if (this.openAiModel == null) {
            this.openAiModel = System.getenv("OPENAI_MODEL");
        }
        
        // Default model
        if (this.openAiModel == null) {
            this.openAiModel = "gpt-5-nano";
        }
    }

    public String getOpenAiApiKey() {
        return openAiApiKey;
    }

    public String getOpenAiModel() {
        return openAiModel;
    }
}
