package com.kafkaexplorer.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.PrintWriter;
import java.io.StringWriter;


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
    public static void logInfo(String msg){
        getLogger().info(msg);
    }
    public static void logError(Exception e){
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));

        for (StackTraceElement ste : e.getStackTrace()) {
            getLogger().error(ste.toString());
        }
        getLogger().error(errors.toString());

    }
}
