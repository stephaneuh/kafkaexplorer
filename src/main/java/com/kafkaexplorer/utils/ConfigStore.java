package com.kafkaexplorer.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.kafkaexplorer.model.Cluster;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.stream.IntStream;

public class ConfigStore {

    public ConfigStore() {
    }

    public HashMap<String, String> validateYamlConfig() {

        HashMap<String, String> errorList = new HashMap<String, String>();

        String path = System.getProperty("user.home") + File.separator + ".kafkaexplorer" + File.separator + "config.yaml";
        File file = new File(path);

        // Instantiating a new ObjectMapper as a YAMLFactory
        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        // Mapping the cluster Array from the YAML file to the Cluster class
        try {
            Cluster[] clusters = null;

            clusters = om.readValue(file, Cluster[].class);

            for (int i = 0; i < clusters.length; i++) {
                //for each cluster found, validate each fields
                //Todo do some basic validation on each field content
                clusters[i].getName();
                clusters[i].getHostname();
                clusters[i].getProtocol();
                clusters[i].getMechanism();
                clusters[i].getApiKey();
                clusters[i].getApiSecret();
            }

        } catch (IOException e) {
            errorList.put("config.yaml format error.", e.getMessage());
        }


        return errorList;

    }


     public Cluster[] loadClusters() throws IOException {

         //Load config.yaml file from the user.home/kafkaexplorer/config.yaml
        // String path = System.getProperty("user.home") + File.separator + ".kafkaexplorer" + File.separator + "config.yaml";

         File configDir = new File(System.getProperty("user.home"), ".kafkaExplorer");
         if (!configDir.isDirectory())
             if (!configDir.mkdirs())
                 throw new IOException("Failed to create directory");

         File file = new File(configDir, "config.yaml");

         if (!file.exists()) {
             //create empty config file
             Cluster[] emptyClusters = new Cluster[]{};
             saveYaml(emptyClusters);
         }

         // Instantiating a new ObjectMapper as a YAMLFactory
         ObjectMapper om = new ObjectMapper(new YAMLFactory());

         // Mapping the cluster Array from the YAML file to the Cluster class

         Cluster[] clusters = om.readValue(file, com.kafkaexplorer.model.Cluster[].class);
        return clusters;
     }



    public Cluster getClusterByName(String clusterName) {

        Cluster cluster = new Cluster();

        //Load config.yaml file from the user.home/kafkaexplorer/config.yaml
        String path = System.getProperty("user.home") + File.separator + ".kafkaexplorer" + File.separator + "config.yaml";
        File file = new File(path);

        // Instantiating a new ObjectMapper as a YAMLFactory
        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        // Mapping the cluster Array from the YAML file to the Cluster class
        try {
            Cluster[] clusters = null;

            clusters = om.readValue(file, Cluster[].class);

            for (int i = 0; i < clusters.length; i++) {
                if (clusters[i].getName().equals(clusterName))
                    cluster = clusters[i];
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Debug
        cluster.println();


        return cluster;
    }

    public boolean saveCluster(Cluster cluster) {
        System.out.println("Cluster to save: " + cluster.getId());
        //real yaml file
        try {
            Cluster[] clusters = this.loadClusters();

            //locate cluster to update
            for (int i=0; i < clusters.length; i++) {
                if (clusters[i].getId().equals(cluster.getId())) {
                    //update cluster

                    clusters[i] = cluster;
                }
            }

            //save file
            saveYaml(clusters);

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addCluster(Cluster cluster) {
        System.out.println("Cluster to add: " + cluster.getId());
        //real yaml file
        try {
            Cluster[] clusters = this.loadClusters();
            // Create another array of size +1
            Cluster[] anotherArray = new Cluster[clusters.length + 1];

            for (int i=0; i < clusters.length; i++) {
                anotherArray[i] = clusters[i];
            }

            anotherArray[anotherArray.length - 1] = cluster;

            //save file
            saveYaml(anotherArray);

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean deleteCluster(Cluster cluster) {
        System.out.println("Cluster to delete: " + cluster.getId());

         int indexToDelete = -1;
        //real yaml file
        try {
            Cluster[] clusters = this.loadClusters();

            // Create another array of size -1
            Cluster[] anotherArray = new Cluster[clusters.length - 1];

            //locate cluster to delete
            for (int i=0; i < clusters.length; i++) {

                if (clusters[i].getId().equals(cluster.getId())) {
                    //update cluster
                    indexToDelete = i;
                }
            }

            if (indexToDelete != -1)
            {
                // Copy the elements except the index
                // from original array to the other array
                for (int i = 0, k = 0; i < clusters.length; i++) {

                    // if the index is
                    // the removal element index
                    if (i == indexToDelete) {
                        continue;
                    }

                    // if the index is not
                    // the removal element index
                    anotherArray[k++] = clusters[i];
                }
                //save file
                saveYaml(anotherArray);
            }
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    private void saveYaml(Cluster[] clusters) throws IOException {

    //save file
    ObjectMapper om = new ObjectMapper(new YAMLFactory());
            om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            om.writeValue(new File(System.getProperty("user.home") + File.separator + ".kafkaexplorer" + File.separator + "config.yaml"), clusters);

    }

}

