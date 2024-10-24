/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Admin
 */
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class QueueManagementApp extends Application {

    private WebSocketClient webSocketClient;
    private TextArea queueStatusArea;

    @Override
    public void start(Stage primaryStage) throws URISyntaxException {
        // Initialize WebSocket Client
        initializeWebSocketClient();

        // Create the JavaFX GUI
        queueStatusArea = new TextArea();
        queueStatusArea.setEditable(false);

        Button issueTicketButton = new Button("Issue New Ticket");
        Button callNextTicketButton = new Button("Call Next Ticket");
        Button refreshQueueButton = new Button("Refresh Queue Status");

        // Set button actions
        issueTicketButton.setOnAction(e -> sendWebSocketMessage("newTicket"));
        callNextTicketButton.setOnAction(e -> sendWebSocketMessage("nextTicket"));
        refreshQueueButton.setOnAction(e -> sendWebSocketMessage("queueStatus"));

        // Layout
        VBox layout = new VBox(10);
        layout.getChildren().addAll(issueTicketButton, callNextTicketButton, refreshQueueButton, queueStatusArea);

        Scene scene = new Scene(layout, 400, 300);

        // Set up the stage
        primaryStage.setScene(scene);
        primaryStage.setTitle("Queue Management System");
        primaryStage.show();
    }

    private void initializeWebSocketClient() throws URISyntaxException {
        URI serverUri = new URI("ws://localhost:8080");

        webSocketClient = new WebSocketClient(serverUri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                System.out.println("Connected to WebSocket Server");
                send("queueStatus"); // Fetch the current queue status on connection
            }

            @Override
            public void onMessage(String message) {
                // Update the queue status on the GUI
                Platform.runLater(() -> queueStatusArea.appendText(message + "\n"));
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("Connection closed: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                System.err.println("WebSocket Error: " + ex.getMessage());
            }
        };

        webSocketClient.connect();
    }

    private void sendWebSocketMessage(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message);
        } else {
            System.err.println("WebSocket is not connected.");
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        // Close WebSocket connection when the app is closed
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}