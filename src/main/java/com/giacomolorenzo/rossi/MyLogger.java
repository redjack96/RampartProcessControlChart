package com.giacomolorenzo.rossi;

import java.util.logging.Logger;

public class MyLogger {

    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private MyLogger() {
    }

    public static Logger get(){
        return logger;
    }
}
