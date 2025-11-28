package com.assistant.bridge;

import com.assistant.model.AppConfig;
import com.assistant.service.AiService;
import com.assistant.service.DatabaseService;
import com.assistant.service.FileService;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;

import java.io.File;

public class WebAppBridge {
    private final AiService aiService;
    private final DatabaseService databaseService;
    private final FileService fileService;
    private final WebEngine webEngine;

    public WebAppBridge(WebEngine webEngine) {
        this.webEngine = webEngine;
        AppConfig config = new AppConfig();
        this.aiService = new AiService(config);
        this.databaseService = new DatabaseService();
        this.fileService = new FileService();
    }

    public void analyzeFile(String filePath, String customPrompt) {
        new Thread(() -> {
            try {
                String data;
                if (filePath != null && !filePath.isEmpty()) {
                    File file = new File(filePath);
                    if (!file.exists()) {
                        sendError("File not found: " + filePath);
                        return;
                    }
                    data = fileService.parseFile(file);
                } else {
                    data = "No file provided. Generating generic report.";
                }

                String analysisJson = aiService.analyzeSpreadsheet(data, customPrompt);
                
                // Save to history
                databaseService.saveHistory(customPrompt + " | " + (filePath != null ? filePath : "No File"), analysisJson);

                // Send back to UI
                Platform.runLater(() -> {
                    webEngine.executeScript("receiveAnalysis(" + analysisJson + ")");
                });

            } catch (Exception e) {
                e.printStackTrace();
                sendError(e.getMessage());
            }
        }).start();
    }

    private void sendError(String message) {
        Platform.runLater(() -> {
            // Escape special characters for JS string
            String escapedMessage = message.replace("\\", "\\\\")
                                           .replace("'", "\\'")
                                           .replace("\"", "\\\"")
                                           .replace("\n", "\\n")
                                           .replace("\r", "");
            webEngine.executeScript("receiveError('" + escapedMessage + "')");
        });
    }

    public void chooseFile() {
        Platform.runLater(() -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Selecionar Arquivo de Log ou Excel");
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                webEngine.executeScript("fileSelected('" + file.getAbsolutePath().replace("\\", "\\\\") + "')");
            }
        });
    }
}
