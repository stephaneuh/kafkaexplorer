package com.kafkaexplorer.model;

import java.util.ArrayList;
import java.util.List;

public class Clusters {
    public List<Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(List<Cluster> clusters) {
        this.clusters = clusters;
    }

    public Clusters() {}
    private List<Cluster> clusters = new ArrayList<Cluster>();
}
