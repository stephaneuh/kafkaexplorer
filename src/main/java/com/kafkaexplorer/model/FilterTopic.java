package com.kafkaexplorer.model;

public class FilterTopic {
    private String name;

    public FilterTopic(){

    }

    public FilterTopic(FilterTopic topic) {
        this.name = topic.name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
