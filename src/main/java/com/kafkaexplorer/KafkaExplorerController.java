package com.kafkaexplorer;

import com.kafkaexplorer.logger.MyLogger;
import com.kafkaexplorer.model.Cluster;
import com.kafkaexplorer.utils.ConfigStore;
import com.kafkaexplorer.utils.HostServicesProvider;
import com.kafkaexplorer.utils.UI;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

public class KafkaExplorerController implements Initializable {

    public ProgressIndicator progBar2;
    @FXML
    private TreeView<String> kafkaTree;

    @FXML
    private SplitPane mainContent;

    private Cluster[] clusters;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            new UI().refreshClusterList(kafkaTree);


            MenuItem mi1 = new MenuItem("Copy");
            mi1.setOnAction((ActionEvent event) -> {
                TreeItem item = kafkaTree.getSelectionModel().getSelectedItem();
                //returns System Clipboard
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(item.getValue().toString());
                clipboard.setContent(content);
            });

            ContextMenu menu = new ContextMenu();
            menu.getItems().add(mi1);
            kafkaTree.setContextMenu(menu);
        } catch (IOException e) {
            MyLogger.logError(e);
        }

    }

    public void onMouseClicked(MouseEvent mouseEvent) {

        //Open the topicBrowser screen
        try {
            clusters = new ConfigStore().loadClusters();
            // Get selected Node
            Node node = mouseEvent.getPickResult().getIntersectedNode();

            //Ensure that user clicked on a TreeCell
            if (node instanceof Text || (node instanceof TreeCell && ((TreeCell) node).getText() != null)) {
                TreeItem selectedItem = (TreeItem) kafkaTree.getSelectionModel().getSelectedItem();

                //selectedItem is a cluster, display cluster config
                if (selectedItem.getParent() != null && selectedItem.getParent().getValue() == "Kafka Clusters") {
                    FXMLLoader clusterConfigLoader = new FXMLLoader(getClass().getResource("/clusterConfig.fxml"));
                    GridPane mainRoot = clusterConfigLoader.load();
                    ClusterConfigController clusterConfigController = clusterConfigLoader.getController();
                    //find selected cluster from Clusters Array
                    Cluster selectedCluster = null;

                    for (int i = 0; i < clusters.length; i++) {
                        if (clusters[i].getName().equals(selectedItem.getValue())) {
                            selectedCluster = new Cluster(clusters[i]);
                        }
                    }

                    if (selectedCluster != null) {
                        clusterConfigController.populateScreen(selectedCluster, kafkaTree);
                        // mainContent.getChildren().setAll(mainRoot);

                        unloadPreviousController(mainContent);

                        mainContent.getItems().add(mainRoot);

                    } else {
                        //todo
                        //  mainContent.getChildren().clear();
                    }

                } //If selectedItem is a topic, display topic browser screen
                else if (selectedItem.getParent() != null && selectedItem.getParent().getGraphic() instanceof HBox) {

                    FXMLLoader topicBrowserLoader = new FXMLLoader(getClass().getResource("/topicBrowser.fxml"));

                    VBox mainRoot = topicBrowserLoader.load();

                    //Display Progress bar
                    progBar2.setVisible(true);

                    TopicBrowserController topicBrowserController = topicBrowserLoader.getController();

                    //Get cluster info from cluster name
                    Cluster cluster = new ConfigStore().getClusterByName(selectedItem.getParent().getParent().getValue().toString());

                    topicBrowserController.populateScreen(cluster, selectedItem.getValue().toString(), kafkaTree);
                    //delete: mainContent.getChildren().setAll(mainRoot);
                    MyLogger.logInfo("Node mainContent.getItems().size()  " + mainContent.getItems().size());

                    unloadPreviousController(mainContent);

                    mainContent.getItems().add(mainRoot);
                } //If selectedItem is a consumer group, display consumer group screen
                else if (selectedItem.getParent() != null && selectedItem.getParent().getValue() == "consumer-groups") {
                    FXMLLoader consumerGroupBrowserLoader = new FXMLLoader(getClass().getResource("/consumerBrowser.fxml"));
                    VBox mainRoot = consumerGroupBrowserLoader.load();

                    //Display Progress bar
                    progBar2.setVisible(true);

                    ConsumerGroupController consumerGroupBrowserController = consumerGroupBrowserLoader.getController();

                    //Get cluster info from cluster name
                    Cluster cluster = new ConfigStore().getClusterByName(selectedItem.getParent().getParent().getValue().toString());
                    consumerGroupBrowserController.populateScreen(cluster, selectedItem.getValue().toString(), kafkaTree);

                    unloadPreviousController(mainContent);

                    mainContent.getItems().add(mainRoot);
                }
            }
        } catch (Exception e) {
            MyLogger.logError(e);
        }
    }

    private void unloadPreviousController(SplitPane mainContent) {

        if (mainContent.getItems().size() > 1) {
            mainContent.getItems().remove(1);
        } else {
            MyLogger.logInfo("Unload not necessary");
        }

    }


    public void addCluster(MouseEvent mouseEvent) {

        try {
            clusters = new ConfigStore().loadClusters();
            Cluster c1 = new Cluster();
            c1.setName("New Cluster");

            c1.setId(UUID.randomUUID().toString().replace("-", ""));

            new ConfigStore().addCluster(c1);
            new UI().refreshClusterList(kafkaTree);

        } catch (IOException e) {
            MyLogger.logError(e);
        }


    }

    public void onHelpClicked(MouseEvent mouseEvent) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setGraphic(null);

        VBox content = new VBox();
        Label label3 = new Label("\nWebsite: \n");

        Image image = new Image("/ke-icon-text.png");
        ImageView imageView = new ImageView();
        imageView.setImage(image);

        Hyperlink link = new Hyperlink();
        link.setText("http://kafkaexplorer.com");
        link.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    HostServicesProvider.INSTANCE.getHostServices().showDocument("https://kafkaexplorer.com");
                } catch (Exception e1) {
                    MyLogger.logError(e1);
                }
            }
        });

        Label label2 = new Label("Apache License 2.0\n\n");
        Label label5 = new Label("Contributors: \n");

        Hyperlink contribLink = new Hyperlink();
        contribLink.setText("Stephane Ris (https://github.com/stephaneuh)");
        contribLink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    HostServicesProvider.INSTANCE.getHostServices().showDocument("https://github.com/stephaneuh");
                } catch (Exception e1) {
                    MyLogger.logError(e1);
                }
            }
        });

        Hyperlink linkLogs = new Hyperlink();
        linkLogs.setText("\n [Show Log Folder]");
        linkLogs.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    MyLogger.logInfo("Initiate show logs folder.");
                    HostServicesProvider.INSTANCE.getHostServices().showDocument(System.getProperty("user.dir") + "/logs");
                } catch (Exception e1) {
                    MyLogger.logError(e1);
                }
            }
        });

        content.getChildren().addAll(imageView, label3, link, label2, label5, contribLink, linkLogs);
        content.setAlignment(Pos.CENTER);

        alert.getDialogPane().setContent(content);

        alert.initStyle(StageStyle.UNDECORATED);

        alert.showAndWait();

    }
}
