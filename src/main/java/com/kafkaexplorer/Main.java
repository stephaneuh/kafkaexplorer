package com.kafkaexplorer;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Locale;
import java.util.UUID;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        Locale.setDefault(new Locale("en", "CA"));
        Parent root = FXMLLoader.load(getClass().getResource("/kafkaExplorer.fxml"));
        primaryStage.setTitle("Kafka Explorer (community-edition)");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/ke-logo-font-15.png")));
        Scene scene = new Scene(root, 1400, 800);
        scene.getStylesheets().add(getClass().getResource("/main.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
