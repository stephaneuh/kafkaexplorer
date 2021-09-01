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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.UUID;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        //ssl debug
        System.setProperty("javax.net.debug", "all");
        System.setProperty("javax.net.ssl.SSLEngine.setEnabledCipherSuites","TLS_AES_256_GCM_SHA384");
        //set SSL supported ciphersuite:
        //(0x1302), TLS_AES_128_GCM_SHA256(0x1301), TLS_CHACHA20_POLY1305_SHA256(0x1303), TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384(0xC02C), TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256(0xC02B), TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256(0xCCA9), TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384(0xC030), TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256(0xCCA8), TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256(0xC02F), TLS_DHE_RSA_WITH_AES_256_GCM_SHA384(0x009F), TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256(0xCCAA), TLS_DHE_DSS_WITH_AES_256_GCM_SHA384(0x00A3), TLS_DHE_RSA_WITH_AES_128_GCM_SHA256(0x009E), TLS_DHE_DSS_WITH_AES_128_GCM_SHA256(0x00A2), TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384(0xC024), TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384(0xC028), TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256(0xC023), TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256(0xC027), TLS_DHE_RSA_WITH_AES_256_CBC_SHA256(0x006B), TLS_DHE_DSS_WITH_AES_256_CBC_SHA256(0x006A), TLS_DHE_RSA_WITH_AES_128_CBC_SHA256(0x0067), TLS_DHE_DSS_WITH_AES_128_CBC_SHA256(0x0040), TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384(0xC02E), TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384(0xC032), TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256(0xC02D), TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256(0xC031), TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384(0xC026), TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384(0xC02A), TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256(0xC025), TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256(0xC029), TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA(0xC00A), TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA(0xC014), TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA(0xC009), TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA(0xC013), TLS_DHE_RSA_WITH_AES_256_CBC_SHA(0x0039), TLS_DHE_DSS_WITH_AES_256_CBC_SHA(0x0038), TLS_DHE_RSA_WITH_AES_128_CBC_SHA(0x0033), TLS_DHE_DSS_WITH_AES_128_CBC_SHA(0x0032), TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA(0xC005), TLS_ECDH_RSA_WITH_AES_256_CBC_SHA(0xC00F), TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA(0xC004), TLS_ECDH_RSA_WITH_AES_128_CBC_SHA(0xC00E), TLS_RSA_WITH_AES_256_GCM_SHA384(0x009D), TLS_RSA_WITH_AES_128_GCM_SHA256(0x009C), TLS_RSA_WITH_AES_256_CBC_SHA256(0x003D), TLS_RSA_WITH_AES_128_CBC_SHA256(0x003C), TLS_RSA_WITH_AES_256_CBC_SHA(0x0035), TLS_RSA_WITH_AES_128_CBC_SHA(0x002F), TLS_EMPTY_RENEGOTIATION_INFO_SCSV(0x00FF)]",


        PrintStream console = System.out;

        File file = new File("out.txt");
        FileOutputStream fos = new FileOutputStream(file);
        PrintStream ps = new PrintStream(fos);
        System.setErr(ps);
        System.out.println("This goes to out.txt");

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
