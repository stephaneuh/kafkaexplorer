package com.kafkaexplorer;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import com.jfoenix.controls.JFXTreeCell;
import com.kafkaexplorer.utils.ConfigStore;
import com.kafkaexplorer.utils.KafkaLib;
import com.kafkaexplorer.logger.MyLogger;
import com.kafkaexplorer.model.Cluster;
import com.kafkaexplorer.utils.UI;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
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
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.kafka.common.errors.SslAuthenticationException;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ClusterConfigController implements Initializable {

    @FXML
    public TextField name;
    public TextField securityType;
    public TextField jks;
    public TextField  jksPwd;
    public StackPane stack;
    public TextField bootstrap;
    public TextField saslMechanism;
    public TextField consumerGroup;
    public GridPane rootGridPane;
    public TextField srUrl;
    public TextField srUser;
    public TextField srPwd;
    public TextField apiKey;
    public TextField apiSecret;

    private Cluster cluster;
    private TreeView<String> kafkaTreeRef;

    public TextField getName() {
        return name;
    }

    public void setName(TextField name) {
        this.name = name;
    }

    public TextField getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(TextField bootstrap) {
        this.bootstrap = bootstrap;
    }

    public TextField getSaslMechanism() {
        return saslMechanism;
    }

    public void setSaslMechanism(TextField saslMechanism) {
        this.saslMechanism = saslMechanism;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void populateScreen(Cluster cluster, TreeView<String> clusterTreeView) {
        this.cluster = cluster;

        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {

                jks.setText(cluster.getTrustStoreJKS());
                jksPwd.setText(cluster.getTrustStoreJKSPwd());
                bootstrap.setText(cluster.getHostname());
                name.setText(cluster.getName());
                saslMechanism.setText(cluster.getMechanism());
                securityType.setText(cluster.getProtocol());
                apiKey.setText(cluster.getApiKey());
                apiSecret.setText(cluster.getApiSecret());

                consumerGroup.setText(cluster.getConsumerGroup());
                srUrl.setText(cluster.getSrUrl());
                srUser.setText(cluster.getSrUser());
                srPwd.setText(cluster.getSrPwd());

                kafkaTreeRef = clusterTreeView;

                return 0;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
            }

            @Override
            protected void cancelled() {
                super.cancelled();
            }

            @Override
            protected void failed() {
                super.failed();
                //show an alert Dialog
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setHeaderText("Error!");
                a.setContentText(this.getException().getMessage());
                a.show();
            }
        };

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    public void connectToKafka(MouseEvent mouseEvent) throws IOException {
        //connect to kafka cluster and list all topics

        Task task = new Task<Void>() {
            @Override
            public Void call() throws Exception {

                ((ProgressIndicator)rootGridPane.getScene().lookup("#progBar2")).setVisible(true);

                //Get and store topics list

                KafkaLib kafkaConnector = new KafkaLib();
                try {
                    kafkaConnector.connect(cluster);
                }
                catch (Exception e) {
                    MyLogger.logError(e);
                }

                MyLogger.logInfo("Connected to cluster");

                ArrayList<String> topics  = kafkaConnector.listTopics(cluster);

                cluster.setTopicList(topics);
                MyLogger.logInfo("List of topics received");
                //Build and expand kafkaTree
                for (TreeItem child : kafkaTreeRef.getRoot().getChildren()) {
                    //Locate cluster to update
                    if (child.getValue().equals(name.getText())) {
                        //remove any existing topics and consumers children
                        child.getChildren().clear();

                        //Create a SubTreeItem maned "topics"
                        TreeItem topicsRoot = new TreeItem("");

                        HBox hBox = new HBox(3);
                        //show-hide internal topics button
                        JFXToggleButton toggleButton1 = new JFXToggleButton();
                        toggleButton1.setId("hideInternal");
                        //filter input
                        JFXTextField searchField = new JFXTextField();
                        searchField.setMaxWidth(50);
                        searchField.setPromptText("filter...");

                        searchField.setOnKeyTyped   (new EventHandler() {
                            @Override
                            public void handle(Event event) {

                                JFXTextField searchField = (JFXTextField)event.getSource();
                                TreeItem treeItem = (TreeItem)((JFXTreeCell)((HBox)searchField.getParent()).getParent()).getTreeItem();
                                JFXToggleButton tb = (JFXToggleButton) searchField.getScene().lookup("#hideInternal");

                                boolean displayInternal = false;
                                if (!tb.isSelected()){
                                    displayInternal = true;
                                }

                                filterUITopics(treeItem, searchField.getText(), displayInternal);
                            }
                        });

                        toggleButton1.setText("hide internal)");
                        toggleButton1.setSelected(true);
                        toggleButton1.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                //empty topics list for this cluster
                                JFXToggleButton button = (JFXToggleButton)event.getSource();
                                TreeItem treeItem = (TreeItem)((JFXTreeCell)((HBox)button.getParent()).getParent()).getTreeItem();
                                treeItem.getChildren().clear();

                                filterUITopics(treeItem, searchField.getText(), !button.isSelected());
                            }

                        });

                        Label label = new Label("topics");
                        Label middleLabel = new Label("(");
                        hBox.setAlignment(Pos.CENTER_LEFT);

                        hBox.getChildren().addAll(label,searchField, middleLabel, toggleButton1);

                        topicsRoot.setGraphic(hBox);

                        child.getChildren().add(topicsRoot);
                        TreeItem topicsChildren = (TreeItem) child.getChildren().get(0);

                        filterUITopics(topicsChildren, "", false);

                        //Create a SubTreeItem maned "consumer groups"
                        TreeItem consumerNode = new TreeItem("consumer-groups");

                        //get consumer groups list
                        ArrayList<String> consumers = kafkaConnector.listConsumerGroups(cluster);
                        for (String consumerGroupName : consumers) {
                            TreeItem consumerItem = new TreeItem(consumerGroupName);
                            consumerNode.getChildren().add(consumerItem);
                        }
                        consumerNode.setExpanded(true);
                        child.getChildren().add(consumerNode);

                        child.setExpanded(true);
                        topicsChildren.setExpanded(true);


                    } else {
                        child.setExpanded(false);
                    }

                }
                ((ProgressIndicator)rootGridPane.getScene().lookup("#progBar2")).setVisible(false);
                return null;
            }
        };

        task.setOnFailed(evt -> {

            MyLogger.logError((Exception) task.getException());
            //show an alert Dialog
            Alert a = new Alert(Alert.AlertType.ERROR);
            StringWriter errors = new StringWriter();
            task.getException().printStackTrace(new PrintWriter(errors));

            if (errors.toString().toLowerCase().contains("timeout"))
                a.setContentText("Timeout! See logs for details");
            else
                a.setContentText("Can't connect! See logs for details");

            a.show();
            ((ProgressIndicator)rootGridPane.getScene().lookup("#progBar2")).setVisible(false);

            //change cluster icon to grey
            setClusterIconToGreen(name.getText(), false);

        });

        task.setOnSucceeded(evt -> {
            //change cluster icon to green
            setClusterIconToGreen(name.getText(), true);
            ((ProgressIndicator)rootGridPane.getScene().lookup("#progBar2")).setVisible(false);
        });

        new Thread(task).start();
    }

    public void saveCluster(MouseEvent mouseEvent) throws IOException {

        cluster.setTrustStoreJKS(jks.getText());
        cluster.setTrustStoreJKSPwd(jksPwd.getText());
        cluster.setHostname(bootstrap.getText());
        cluster.setName(name.getText());
        cluster.setMechanism(saslMechanism.getText());
        cluster.setProtocol(securityType.getText());
        cluster.setApiKey(apiKey.getText());
        cluster.setApiSecret(apiSecret.getText());
        cluster.setConsumerGroup(consumerGroup.getText());

        cluster.setSrUrl(srUrl.getText());
        cluster.setSrUser(srUser.getText());
        cluster.setSrPwd(srPwd.getText());

        new ConfigStore().saveCluster(cluster);

        //refresh cluster list
        new UI().refreshClusterList(kafkaTreeRef);

    }

    private void filterUITopics(TreeItem treeItem, String searchText, boolean displayInternal){

        //empty topics list for this cluster
        treeItem.getChildren().clear();

        boolean displayAllTopics = false;
        if (searchText.isEmpty()){
            displayAllTopics = true;
        }


        ArrayList<String> topics = cluster.getTopicList();

        // if selected (hide internal topics)
        if (!displayInternal) {
            for (String topicName : topics) {
                //by default, hide internal topics (starting by _)
                if (!topicName.startsWith("_") && (displayAllTopics || topicName.contains(searchText))) {

                    TreeItem topicItem = new TreeItem(topicName);

                    treeItem.getChildren().add(topicItem);
                }

            }

        }else
        {
            //if not selected (show all topics )
            for (String topicName : topics) {
                if (displayAllTopics || topicName.contains(searchText)) {
                    TreeItem topicItem = new TreeItem(topicName);
                    treeItem.getChildren().add(topicItem);
                }
            }
        }
    }

    private void setClusterIconToGreen(String clusterName, boolean isGreen) {

        Node clusterIconGreen = new ImageView(new Image(getClass().getResourceAsStream("/kafka-icon-green.png")));
        Node clusterIconGrey = new ImageView(new Image(getClass().getResourceAsStream("/kafka-icon-grey.png")));

        for (TreeItem child : kafkaTreeRef.getRoot().getChildren()) {

            if (child.getValue().equals(clusterName)) {
                if (isGreen)
                    child.setGraphic(clusterIconGreen);
                else
                    child.setGraphic(clusterIconGrey);
            }
        }
    }

    public void deleteCluster(MouseEvent mouseEvent) {
        //Ask to confirm deletion
        if (new UI().confirmationDialog(Alert.AlertType.CONFIRMATION, "Are you sure?")){
            new ConfigStore().deleteCluster(cluster);
            //refresh cluster list
            try {
                new UI().refreshClusterList(kafkaTreeRef);
                this.rootGridPane.getChildren().clear();

            } catch (IOException e) {
                MyLogger.logError(e);
            }
        }
    }

    public void browseJKS(MouseEvent mouseEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JKS files (*.jks)", "*.jks"));
        Stage stage = (Stage) rootGridPane.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null)
            this.jks.setText(selectedFile.getPath());
    }

    }
