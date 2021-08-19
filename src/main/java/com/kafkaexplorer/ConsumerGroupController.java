package com.kafkaexplorer;

import com.kafkaexplorer.logger.MyLogger;
import com.kafkaexplorer.model.Cluster;
import com.kafkaexplorer.utils.KafkaLib;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.VBox;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.net.URL;
import java.util.*;

public class ConsumerGroupController implements Initializable {
    @FXML
    public TextField consumerGroupName;
    @FXML
    public TableView partitionOffsetTable;
    @FXML
    public VBox rootNodeGroups;
    @FXML
    private TreeView<String> kafkaTreeRef;

    private Cluster cluster;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void populateScreen(Cluster cluster, String consumerGroupName, TreeView<String> clusterTreeView) {

        this.consumerGroupName.setText(consumerGroupName);
        this.kafkaTreeRef = clusterTreeView;
        this.cluster = cluster;

        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {

                TableColumn<Map, Object> partitionColumn = new TableColumn<>("Topic-Partition");
                partitionColumn.setCellValueFactory(new MapValueFactory<>("Topic-Partition"));
                partitionColumn.setPrefWidth(400);

                TableColumn<Map, Object> offsetColumn = new TableColumn<>("Offset");
                offsetColumn.setCellValueFactory(new MapValueFactory<>("Offset"));
                offsetColumn.setPrefWidth(200);

                TableColumn<Map, Object> lagColumn = new TableColumn<>("Lag");
                lagColumn.setCellValueFactory(new MapValueFactory<>("Lag"));
                lagColumn.setPrefWidth(100);

                TableColumn<Map, Object> consumerColumn = new TableColumn<>("Consumer-id");
                consumerColumn.setCellValueFactory(new MapValueFactory<>("Consumer-id"));
                consumerColumn.setPrefWidth(400);

                partitionOffsetTable.getColumns().add(partitionColumn);
                partitionOffsetTable.getColumns().add(offsetColumn);
                partitionOffsetTable.getColumns().add(lagColumn);
                partitionOffsetTable.getColumns().add(consumerColumn);

                ObservableList<Map<String, Object>> items = FXCollections.<Map<String, Object>>observableArrayList();

                KafkaLib kafkaConnector = new KafkaLib();
                DescribeConsumerGroupsResult consumerGroupInfo = kafkaConnector.getConsumerGroupInfo(cluster, consumerGroupName);
                ListConsumerGroupOffsetsResult listConsumerGroupOffsetsResult = kafkaConnector.getConsumerGroupOffsets(cluster, consumerGroupName);

                final Map<org.apache.kafka.common.TopicPartition, OffsetAndMetadata> partitionsToOffsets = listConsumerGroupOffsetsResult.partitionsToOffsetAndMetadata().get();

                partitionsToOffsets.forEach((topicPartition, offsetAndMetadata) -> {

                    Map<String, Object> item1 = new HashMap<>();
                    item1.put("Topic-Partition", topicPartition);
                    item1.put("Offset", offsetAndMetadata.offset());
                    item1.put("Lag", offsetAndMetadata.leaderEpoch());
                    item1.put("Consumer-id", offsetAndMetadata.metadata());

                    items.add(item1);

                });

                partitionOffsetTable.getItems().addAll(items);

                return 0;
            }

            @Override
            protected void succeeded() {
                ((ProgressIndicator)rootNodeGroups.getScene().lookup("#progBar2")).setVisible(false);
                super.succeeded();
            }

            @Override
            protected void cancelled() {
                ((ProgressIndicator)rootNodeGroups.getScene().lookup("#progBar2")).setVisible(false);
                super.cancelled();
            }

            @Override
            protected void failed() {
                ((ProgressIndicator)rootNodeGroups.getScene().lookup("#progBar2")).setVisible(false);
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
}
