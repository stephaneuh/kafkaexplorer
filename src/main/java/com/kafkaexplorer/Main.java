package com.kafkaexplorer;

import com.kafkaexplorer.utils.HostServicesProvider;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Locale;

public class Main extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        HostServicesProvider.INSTANCE.init(getHostServices());
        Locale.setDefault(new Locale("en", "CA"));
        Parent root = FXMLLoader.load(getClass().getResource("/kafkaExplorer.fxml"));
        primaryStage.setTitle("Kafka Explorer (community-edition)");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/ke-logo-font-15.png")));
        Scene scene = new Scene(root, 1280, 690); //1400, 800
        scene.getStylesheets().add(getClass().getResource("/main.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();


    }
}
