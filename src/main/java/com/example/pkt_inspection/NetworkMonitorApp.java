package com.example.pkt_inspection;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.io.IOException;

public class NetworkMonitorApp extends Application {
    private NetworkMonitorController controller;
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("NetworkMonitor.fxml"));
            Parent root = loader.load();
            controller = loader.getController();
            
            Scene scene = new Scene(root);
//            scene.getStylesheets().add(getClass().getResource("/com/example/pkt_inspection/styles.css").toExternalForm());
//
            primaryStage.setTitle("Network Intrusion Detection System");
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(e -> {
                if (controller != null) {
                    controller.shutdown();
                }
            });
            primaryStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.shutdown();
        }
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}