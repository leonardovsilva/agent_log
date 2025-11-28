package com.assistant;

import com.assistant.bridge.WebAppBridge;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        // Set black background to match app theme
        webView.setPageFill(Color.BLACK);

        // Load index.html
        URL url = getClass().getResource("/index.html");
        if (url != null) {
            webEngine.load(url.toExternalForm());
        } else {
            System.err.println("Could not find index.html!");
        }

        // Register Bridge
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaBridge", new WebAppBridge(webEngine));
                System.out.println("JavaBridge registered!");
            }
        });

        StackPane root = new StackPane(webView);
        root.setStyle("-fx-background-color: black;");
        
        Scene scene = new Scene(root, 1200, 800);
        scene.setFill(Color.BLACK);

        stage.setTitle("Assistente de Erros");
        stage.setScene(scene);
        
        // Try to set icon
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/favicon.ico")));
        } catch (Exception e) {
            // Ignore if icon not found
        }

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
