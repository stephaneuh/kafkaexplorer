package com.kafkaexplorer.utils;

import javafx.application.HostServices;

//HostServicesProvider singleton, so it can be accessed from a javafx controller and not only from the Application Class
public enum HostServicesProvider {

    INSTANCE ;

    private HostServices hostServices ;
    public void init(HostServices hostServices) {
        if (this.hostServices != null) {
            throw new IllegalStateException("Host services already initialized");
        }
        this.hostServices = hostServices ;
    }
    public HostServices getHostServices() {
        if (hostServices == null) {
            throw new IllegalStateException("Host services not initialized");
        }
        return hostServices ;
    }
}


