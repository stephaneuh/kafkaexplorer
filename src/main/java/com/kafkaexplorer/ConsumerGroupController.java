package com.kafkaexplorer;

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
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
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

                TableColumn<Map, Object> topicColumn = new TableColumn<>("Topic");
                topicColumn.setCellValueFactory(new MapValueFactory<>("Topic"));
                topicColumn.setMaxWidth(25);

                TableColumn<Map, Object> partitionColumn = new TableColumn<>("Partition");
                partitionColumn.setCellValueFactory(new MapValueFactory<>("Partition"));

                TableColumn<Map, Object> offsetColumn = new TableColumn<>("Offset");
                offsetColumn.setCellValueFactory(new MapValueFactory<>("Offset"));


                partitionOffsetTable.getColumns().add(topicColumn);
                partitionOffsetTable.getColumns().add(partitionColumn);
                partitionOffsetTable.getColumns().add(offsetColumn);

                ObservableList<Map<String, Object>> items = FXCollections.<Map<String, Object>>observableArrayList();

                KafkaLib kafkaConnector = new KafkaLib();
                KafkaFuture<Map<String, ConsumerGroupDescription>> consumerGroupInfo = kafkaConnector.getConsumerGroupInfo(cluster, consumerGroupName);
                consumerGroupInfo.get().forEach((groupName, groupDescription) ->{
                      //     Map<String, Object> item1 = new HashMap<>();
                           System.out.println(">>>>>>>>" + groupName + " Size: " + groupDescription.members().size());




                    //   item1.put("Topic",  topicPartition.topic());
                         //  item1.put("Partition", topicPartition.partition());
                        //   item1.put("Offset", offsetAndMetadata.toString());
                         //   items.add(item1);
                });



               // for (final Map.Entry<org.apache.kafka.common.TopicPartition, OffsetAndMetadata> entry : consumerGroupInfo.get().entrySet()) {
              //      System.out.println("OKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK" + entry.getKey().partition() + ">>>" + entry.getValue().offset());
              //  }



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
