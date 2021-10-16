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

import org.apache.kafka.clients.admin.DescribeConsumerGroupsResult;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;

import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ConsumerGroupController implements Initializable {
    @FXML
    public TextField consumerGroupName;
    @FXML
    public TextField consumerGroupStatus;
    @FXML
    public TableView partitionOffsetTable;
    @FXML
    public VBox rootNodeGroups;
    @FXML
    private TreeView<String> kafkaTreeRef;

    private Cluster cluster;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        partitionOffsetTable.setVisible(true);
    }

    public void populateScreen(Cluster cluster, String consumerGroupName, TreeView<String> clusterTreeView) {
        // ((ProgressIndicator)rootNodeGroups.getScene().lookup("#progBar2")).setVisible(true);
        this.consumerGroupName.setText(consumerGroupName);
        this.kafkaTreeRef = clusterTreeView;
        this.cluster = cluster;

        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {

                TableColumn<Map, Object> consumerColumn = new TableColumn<>("Consumer id");
                consumerColumn.setCellValueFactory(new MapValueFactory<>("Consumer id"));
                consumerColumn.setPrefWidth(80);

                TableColumn<Map, Object> hostColumn = new TableColumn<>("Host");
                hostColumn.setCellValueFactory(new MapValueFactory<>("Host"));
                hostColumn.setPrefWidth(80);

                TableColumn<Map, Object> partitionColumn = new TableColumn<>("Topic-Partition");
                partitionColumn.setCellValueFactory(new MapValueFactory<>("Topic-Partition"));
                partitionColumn.setPrefWidth(200);

                TableColumn<Map, Object> offsetColumn = new TableColumn<>("Current offset");
                offsetColumn.setCellValueFactory(new MapValueFactory<>("Current offset"));
                offsetColumn.setPrefWidth(90);

                TableColumn<Map, Object> endColumn = new TableColumn<>("Topic End offset");
                endColumn.setCellValueFactory(new MapValueFactory<>("Topic End offset"));
                endColumn.setPrefWidth(90);

                TableColumn<Map, Object> lagColumn = new TableColumn<>("Consumer lag");
                lagColumn.setCellValueFactory(new MapValueFactory<>("Consumer lag"));
                lagColumn.setPrefWidth(80);

                partitionOffsetTable.getColumns().add(consumerColumn);
                partitionOffsetTable.getColumns().add(hostColumn);
                partitionOffsetTable.getColumns().add(partitionColumn);
                partitionOffsetTable.getColumns().add(offsetColumn);
                partitionOffsetTable.getColumns().add(endColumn);
                partitionOffsetTable.getColumns().add(lagColumn);


                ObservableList<Map<String, Object>> items = FXCollections.<Map<String, Object>>observableArrayList();

                KafkaLib kafkaConnector = new KafkaLib();

                DescribeConsumerGroupsResult consumerGroupInfo = kafkaConnector.getConsumerGroupInfo(cluster, consumerGroupName);
                final List<MemberDescription> members = new ArrayList<MemberDescription>(consumerGroupInfo.describedGroups().get(consumerGroupName).get().members());

                if (members.isEmpty()) {
                    consumerGroupStatus.setText("Not Active");
                } else {
                    consumerGroupStatus.setText("Active");
                }

                //Get offsets for this consumer group
                ListConsumerGroupOffsetsResult listConsumerGroupOffsetsResult = kafkaConnector.getConsumerGroupOffsets(cluster, consumerGroupName);
                final Map<org.apache.kafka.common.TopicPartition, OffsetAndMetadata> partitionsToOffsets = listConsumerGroupOffsetsResult.partitionsToOffsetAndMetadata().get();


                //Display active members assignations
                members.forEach(memberDescription -> {

                    memberDescription.assignment().topicPartitions().forEach(topicPartition -> {
                        Map<String, Object> item1 = new HashMap<>();
                        item1.put("Consumer id", memberDescription.consumerId());
                        item1.put("Host", memberDescription.host());
                        item1.put("Topic-Partition", topicPartition);

                        //get endoffset of this partition:
                        Long endoffsetMap = kafkaConnector.getTopicEndOffset(cluster, topicPartition);
                        item1.put("Topic End offset", endoffsetMap);


                        //search for consumerId and topic-partition in partitionsToOffsets
                        AtomicLong currentOffset = new AtomicLong(-1L);
                        partitionsToOffsets.forEach((topicPartition1, offsetAndMetadata) -> {
                            if (topicPartition1.toString().equalsIgnoreCase(topicPartition.toString())) {
                                currentOffset.set(offsetAndMetadata.offset());
                            }

                        });

                        item1.put("Current offset", currentOffset);

                        //compute Consumer lag
                        item1.put("Consumer lag", endoffsetMap - currentOffset.get());

                        items.add(item1);
                    });

                });

                //Display topic-partitions for this consumer group without any active member
                partitionsToOffsets.forEach((TopicPartitionFromListConsumerGroup, offsetAndMetadata) -> {
                    //find if this topic-partition is already linked to an active member
                    AtomicBoolean found = new AtomicBoolean(false);

                    members.forEach(memberDescription -> {
                        memberDescription.assignment().topicPartitions().forEach(TopicPartition -> {
                            if (TopicPartitionFromListConsumerGroup.toString().equalsIgnoreCase(TopicPartition.toString()))
                                found.set(true);
                        });
                    });

                    if (!found.get()) {
                        //this partition has no active consumer
                        Map<String, Object> item1 = new HashMap<>();
                        item1.put("Consumer id", "--");
                        item1.put("Host", "--");
                        item1.put("Topic-Partition", TopicPartitionFromListConsumerGroup.toString());
                        item1.put("Current offset", offsetAndMetadata.offset());
                        items.add(item1);
                    }


                });

                partitionOffsetTable.getItems().addAll(items);
                return 0;
            }

            @Override
            protected void succeeded() {
                //Workaround: force focus on an element of the page in order to refresh the tableview content
                consumerGroupStatus.requestFocus();
                consumerGroupStatus.deselect();
                super.succeeded();
                ((ProgressIndicator) rootNodeGroups.getScene().lookup("#progBar2")).setVisible(false);
                MyLogger.logInfo("Task succeeded");
            }

            @Override
            protected void cancelled() {
                // ((ProgressIndicator)rootNodeGroups.getScene().lookup("#progBar2")).setVisible(false);
                super.cancelled();
                MyLogger.logInfo("Task cancelled");
            }

            @Override
            protected void failed() {
                // ((ProgressIndicator)rootNodeGroups.getScene().lookup("#progBar2")).setVisible(false);
                super.failed();
                //show an alert Dialog
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setHeaderText("Error!");
                a.setContentText(this.getException().getMessage());
                a.show();
                MyLogger.logInfo("Task failed");
            }
        };

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }
}
