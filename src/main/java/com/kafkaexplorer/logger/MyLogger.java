package com.kafkaexplorer.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;


public class MyLogger {

    private MyLogger() {
    }

    private static Logger logger = LoggerFactory.getLogger(MyLogger.class);

    private static Logger getLogger(){
        if(logger == null){
            new MyLogger();
        }
        return logger;
    }
    public static void logDebug(String msg){
        getLogger().debug(msg);
    }

}
