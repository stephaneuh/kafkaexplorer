package com.kafkaexplorer.utils;

import com.kafkaexplorer.logger.MyLogger;
import com.kafkaexplorer.model.Cluster;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaString;
import io.confluent.kafka.schemaregistry.client.rest.entities.SubjectVersion;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.client.security.basicauth.BasicAuthCredentialProvider;
import io.confluent.kafka.schemaregistry.client.security.basicauth.BasicAuthCredentialProviderFactory;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.*;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.SerializationException;
import io.confluent.kafka.schemaregistry.client.rest.RestService;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


public class KafkaLib {

    private static final byte MAGIC_BYTE = 0x0;
    public boolean continueBrowsing;
    private Properties props;

    public KafkaLib() {

         this.props = new Properties();

    }

    private Properties getProps() {
        return this.props;
    }

    private void setProps(Cluster cluster) {

        //consumer config
        this.props.put("bootstrap.servers", cluster.getHostname());
        this.props.put("security.protocol", cluster.getProtocol());
        this.props.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username='" + cluster.getApiKey() + "' password='" + cluster.getApiSecret() + "';");
        this.props.put("sasl.mechanism", cluster.getMechanism());

        this.props.put("default.api.timeout.ms", 5000);
        this.props.put("request.timeout.ms", 5000);
        //this.props.put("session.timeout.ms", 5000);
        this.props.put("auto.commit.interval.ms", "1000");

        this.props.put("group.id", cluster.getConsumerGroup());
        this.props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        this.props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        //producer config
        this.props.put(ProducerConfig.ACKS_CONFIG, "all");
        this.props.put(ProducerConfig.ACKS_CONFIG, "all");
        this.props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        this.props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        //set SSL Truststore if any provided in the config.yaml
        if (cluster.getTrustStoreJKS() != "") {
            this.props.put("ssl.truststore.location", cluster.getTrustStoreJKS());
            this.props.put("ssl.truststore.password", cluster.getTrustStoreJKSPwd());

            //Set SSL at JVM level to be used by the Schema Registry SSL Rest Clien
            System.setProperty("javax.net.ssl.trustStore",cluster.getTrustStoreJKS());
            System.setProperty("javax.net.ssl.trustStorePassword",cluster.getTrustStoreJKSPwd());

        }

    }

    public String connect(Cluster cluster) throws Exception{

        this.setProps(cluster);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>( this.getProps());
        consumer.close();

        return "OK";
    }

    public ArrayList<String> listTopics(Cluster cluster){

        Map<String, List<PartitionInfo>> topics;
        ArrayList<String> onlyTopicsName = new ArrayList<String>();

        this.setProps(cluster);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(this.getProps());
        topics = consumer.listTopics();
        consumer.close();

        Iterator<Map.Entry<String, List<PartitionInfo>>> iterator = topics.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, List<PartitionInfo>> entry = iterator.next();
            onlyTopicsName.add(entry.getKey());
        }
        Collections.sort(onlyTopicsName);

        return onlyTopicsName;
    }


    public List<PartitionInfo> getTopicPartitionInfo(Cluster cluster, String topicName){

        List<PartitionInfo> topicPartitions;

        this.setProps(cluster);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(this.getProps());
        topicPartitions = consumer.partitionsFor(topicName);
        consumer.close();

        return topicPartitions;
    }


    public void browseTopic(Cluster cluster, String topicName, String browseFrom, TableView messagesTable, Button startButton, Button stopButton, int partitionID, long offset, List<PartitionInfo> partitionInfo) {

        this.setProps(cluster);

        MyLogger.logDebug("Browsing topic: " + topicName + ", Type: " + browseFrom + ", from partition: " + partitionID + ", offset:" + offset);

        //props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        //if no offset exists for this consumer group on the partition: reset to earliest
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(this.getProps());




        //consumer.poll(0);  // without this, the assignment will be empty.
        //ConsumerRecords<String, String> records =

        if (browseFrom.equals("from-beginning")) { // Read from all partitions from the beginning
            //consumer.subscribe(Arrays.asList(topicName));
            //  consumer.seekToBeginning(consumer.assignment());

            for (int i = 0; i < partitionInfo.size(); i++) {
                TopicPartition topicPartition = new TopicPartition(topicName, partitionInfo.get(i).partition());
                consumer.assign(Collections.singleton(topicPartition));
                consumer.seekToBeginning(Collections.singleton(topicPartition));
            }

        } else //Read from a specific partition and offset
        {
                MyLogger.logDebug("Browsing from specific partition/offset");
                TopicPartition topicPartition = new TopicPartition(topicName, partitionID);

            consumer.assign(Collections.singleton(topicPartition));
            consumer.seek(topicPartition, offset);
        }
        //});


        RestService restService = new RestService(cluster.getSrUrl());
        CachedSchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(restService, 128);

        Map<String, String> headers = new HashMap<String, String>();

        try {
            headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString((cluster.getSrUser() + ":" + cluster.getSrPwd()).getBytes("UTF-8")));
            restService.setHttpHeaders(headers);


        } catch (IOException e) {
            MyLogger.logError(e);
        }


        try {
            long startTime = System.currentTimeMillis();

            while (continueBrowsing && (System.currentTimeMillis()-startTime) < 600000) { // timeout of 10 min
                ConsumerRecords<String, String> records = consumer.poll(5000);
                for (ConsumerRecord<String, String> record : records) {
                    if (!continueBrowsing){
                        break;
                    }
                    Map<String, Object> item1 = new HashMap<>();
                    item1.put("Offset", record.offset());
                    item1.put("Partition", record.partition());

                    Date date = new Date(record.timestamp());
                    Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    item1.put("Created", format.format(date).toString());

                    if (!continueBrowsing){
                        break;
                    }

                    byte[] payload = record.value().getBytes();

                    int schemaId = -1;

                    ByteBuffer buffer = ByteBuffer.wrap(payload);

                    if (( buffer == null || buffer.remaining() == 0) || (buffer.get() != MAGIC_BYTE)) {
                        item1.put("Schema Id", "N.A");
                        item1.put("Schema Type", "N.A");
                        item1.put("Message", record.value());
                        item1.put("Schema Subject", "N.A");
                    } else { // Schema Id found at the beginning of the message
                        schemaId = buffer.getInt();
                        item1.put("Schema Id", schemaId);

                        boolean schemaReceived = false;
                        // SchemaString
                        try{
                            SchemaString schemaString = restService.getId(schemaId);
                            schemaReceived = true;

                        if (!continueBrowsing){
                            break;
                        }
                        //Get the subject from SchemaId
                        List<String> schemaSubjects = restService.getAllSubjectsById(schemaId);
                        List<SubjectVersion> schemaSubjectsVersions = restService.getAllVersionsById(schemaId);

                        String subject = schemaSubjectsVersions.get(0).getSubject();
                        Integer version = schemaSubjectsVersions.get(0).getVersion();
                        item1.put("Schema Subject", subject + "(v" + version + ")");

                        String schemaType = schemaString.getSchemaType();

                        item1.put("Schema Type", schemaType);

                        if (schemaType.equalsIgnoreCase("JSON")){
                            item1.put("Message", record.value().substring(5));

                        }else if (schemaType.equalsIgnoreCase("AVRO")){
                            KafkaAvroDeserializer avroDeserializer = new KafkaAvroDeserializer(schemaRegistryClient);
                            GenericData.Record ir = (GenericData.Record) avroDeserializer.deserialize(subject, payload);
                            item1.put("Message", ir.toString());
                        }

                        }catch (RestClientException exception){
                            item1.put("Schema Id", "Error");
                            item1.put("Schema Type", "Error");
                            item1.put("Message", record.value());
                            item1.put("Schema Subject", "Error");
                        }

                    }

                    messagesTable.getItems().add(item1);
                    messagesTable.sort();

                    if (!continueBrowsing){
                        break;
                    }

                }

            }
            MyLogger.logInfo("Browsing thread stopped");
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            MyLogger.logDebug("Message Type not supported: " + errors.toString());

        } finally {
            consumer.close();
            startButton.setDisable(false);
            MyLogger.logInfo("Consumer successfully closed");
        }
    }

        public void produceMessage(Cluster cluster, String topicName, String record, Integer schemaId) {

        this.setProps(cluster);

        Producer<String, String> producer = new KafkaProducer<String, String>( this.getProps());

        MyLogger.logDebug("SchemaId" + schemaId);

        //Handle an exception from the callback
     try {
         producer.send(new ProducerRecord<String, String>(topicName, "", record));

     } catch (CompletionException e) {
         //todo validate behaviour

         MyLogger.logDebug("Can't produce" + e.getMessage());

            //show an alert Dialog
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText("Can't produce! You need to set the TOPIC WRITE ACLs.");
            a.setContentText(e.getMessage());
           a.show();
        }

        producer.flush();
        producer.close();

    }

    public KafkaFuture<Config> getTopicInfo(Cluster cluster, String topicName) {
    //Need to build an AdminClient (requires more privileges that a Kafka Consumer)
        List<PartitionInfo> topicPartitions;

        this.setProps(cluster);

        AdminClient adminClient = KafkaAdminClient.create(this.getProps());

            ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName.toString());

            //KafkaFuture<TopicDescription> descriptionFuture = adminClient.describeTopics(Collections.singleton(topicName.toString())).values().get(topicName.toString());
            KafkaFuture<Config> configFuture = adminClient.describeConfigs(Collections.singleton(resource)).values().get(resource);

        return configFuture;
    }

    public ArrayList<String> listConsumerGroups(Cluster cluster) throws ExecutionException, InterruptedException {

        Map<String, List<PartitionInfo>> topics;
        ArrayList<String> onlyTopicsName = new ArrayList<String>();

        this.setProps(cluster);
        AdminClient kafkaClient = AdminClient.create(this.getProps());

        List<String> groupIds = kafkaClient.listConsumerGroups().all().get().stream().map(s -> s.groupId()).collect(Collectors.toList());

        Collections.sort(groupIds);

        return (ArrayList<String>) groupIds;
    }

    public  DescribeConsumerGroupsResult getConsumerGroupInfo(Cluster cluster, String groupName) throws ExecutionException, InterruptedException {
        MyLogger.logInfo("Getting consumer group information for group: " + groupName);

        this.setProps(cluster);
        AdminClient kafkaClient = AdminClient.create(this.getProps());

        DescribeConsumerGroupsResult consumerInfo = kafkaClient.describeConsumerGroups(Arrays.asList(groupName));

        return consumerInfo;

    }

    public  ListConsumerGroupOffsetsResult  getConsumerGroupOffsets(Cluster cluster, String groupName) throws ExecutionException, InterruptedException {
        MyLogger.logInfo("Getting consumer group information for group: " + groupName);

        this.setProps(cluster);
        AdminClient kafkaClient = AdminClient.create(this.getProps());

        ListConsumerGroupOffsetsResult  offsetsInfo = kafkaClient.listConsumerGroupOffsets(groupName);

        MyLogger.logInfo("Getting consumer group information for group: " + groupName + " - DONE");

        return offsetsInfo;

    }




}
