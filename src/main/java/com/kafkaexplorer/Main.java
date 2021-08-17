package com.kafkaexplorer;

import com.kafkaexplorer.logger.MyLogger;
import com.kafkaexplorer.utils.HostServicesProvider;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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

    public void placeMarker(Node newNode) {
        System.out.println("Focus changed!!!!!!!!!!!!!!!!!!!!!!!!!!");

    }

    public static void main(String[] args) {
        launch(args);
    }
}
