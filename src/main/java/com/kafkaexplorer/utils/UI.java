package com.kafkaexplorer.utils;

import com.kafkaexplorer.ClusterConfigController;
import com.kafkaexplorer.logger.MyLogger;
import com.kafkaexplorer.model.Cluster;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public class UI {

    public UI() {
    }

    public static boolean confirmationDialog(Alert.AlertType alertType, String statement) {
        Alert alert = new Alert(alertType, statement);
        alert.initStyle(StageStyle.UNDECORATED);
        Optional<ButtonType> choose = alert.showAndWait();
        return choose.get() == ButtonType.OK;
    }

    public void refreshClusterList(TreeView<String>  kafkaTree, Cluster clusterToSelect, SplitPane mainContent) throws IOException {

        Cluster[] clusters = new ConfigStore().loadClusters();

        TreeItem<String> root = new TreeItem<>("Kafka Clusters");

        for (int i = 0; i < clusters.length; i++) {
            //build kafka cluster tree
            Node rootIcon = new ImageView(new Image(getClass().getResourceAsStream("/kafka-icon-grey.png")));
            TreeItem<String> clusterItem = new TreeItem<String>(clusters[i].getName(), rootIcon);
            MyLogger.logDebug("ADD items: " + clusters[i].getName());

            root.getChildren().add(clusterItem);
        }

        kafkaTree.setRoot(null);
        kafkaTree.setRoot(root);
        root.setExpanded(true);

        //select one cluster in the list
        if (clusterToSelect != null) {
            for(TreeItem<String> treeItem:root.getChildren()){
                if(treeItem.getValue().equalsIgnoreCase(clusterToSelect.getName())){
                    kafkaTree.getSelectionModel().select(treeItem);

                    mainContent.getItems().clear();


//                    FXMLLoader clusterConfigLoader = new FXMLLoader(getClass().getResource("/clusterConfig.fxml"));
//                    GridPane mainRoot = clusterConfigLoader.load();
//                    ClusterConfigController clusterConfigController = clusterConfigLoader.getController();





                    break;
                }
            }
        }
        MyLogger.logDebug("KafkaExplorerController initialized! ");
    }


    public void manageFav(String topicName, String clusterName, Boolean isFav){
        MyLogger.logDebug(topicName + " >> " + clusterName + " >> " + isFav);


    }
}
