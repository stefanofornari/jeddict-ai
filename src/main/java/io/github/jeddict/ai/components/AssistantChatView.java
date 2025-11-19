package io.github.jeddict.ai.components;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class AssistantChatView extends BorderPane {

    private final javafx.scene.control.ScrollPane scrollPane;
    private final VBox chatBox;

    public AssistantChatView() {
        chatBox = new VBox(10);
        chatBox.setPadding(new javafx.geometry.Insets(10));

        scrollPane = new javafx.scene.control.ScrollPane(chatBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);

        setCenter(scrollPane);
    }

    public void appendUserMessage(String message) {
        Platform.runLater(() -> {
            TextArea label = new TextArea(message);
            label.setWrapText(true);
            label.setEditable(false);
            label.setStyle("-fx-control-inner-background: #e0f7fa; -fx-background-radius: 10; -fx-padding: 10;");
            label.setPrefRowCount(Math.min(5, message.split("\n").length));
            chatBox.getChildren().add(label);
        });
    }

    public void appendAssistantMessage(String message) {
        Platform.runLater(() -> {
            TextArea label = new TextArea(message);
            label.setWrapText(true);
            label.setEditable(false);
            label.setStyle("-fx-control-inner-background: #f0f0f0; -fx-background-radius: 10; -fx-padding: 10;");
            label.setPrefRowCount(Math.min(10, message.split("\n").length));
            chatBox.getChildren().add(label);
        });
    }

    public void appendHtmlMessage(String html, java.util.function.Consumer<String> linkHandler) {
        Platform.runLater(() -> {
            javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
            javafx.scene.web.WebEngine engine = webView.getEngine();
            engine.loadContent(html);

            // Basic height adjustment (approximate, as accurate height is hard in JavaFX
            // WebView without JS bridge)
            webView.setPrefHeight(200);

            if (linkHandler != null) {
                engine.locationProperty().addListener((obs, oldLoc, newLoc) -> {
                    if (newLoc != null && !newLoc.isEmpty()) {
                        linkHandler.accept(newLoc);
                        Platform.runLater(() -> engine.loadContent(html)); // Reset to content to prevent navigation
                    }
                });
            }

            chatBox.getChildren().add(webView);
        });
    }

    public void appendCodeMessage(String mimeType, String code) {
        Platform.runLater(() -> {
            TextArea codeArea = new TextArea(code);
            codeArea.setEditable(false);
            codeArea.setStyle("-fx-font-family: 'Courier New'; -fx-control-inner-background: #f5f5f5;");
            codeArea.setPrefRowCount(Math.min(20, code.split("\n").length));
            chatBox.getChildren().add(codeArea);
        });
    }

    public void addNode(javafx.scene.Node node) {
        Platform.runLater(() -> chatBox.getChildren().add(node));
    }

    public void clearChat() {
        Platform.runLater(() -> chatBox.getChildren().clear());
    }
}
