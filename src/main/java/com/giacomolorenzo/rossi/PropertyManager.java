package com.giacomolorenzo.rossi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class PropertyManager {

    private static final Logger logger = Logger.getLogger(PropertyManager.class.getName());
    private static Properties properties = null;

    private PropertyManager (){}

    public static Properties loadProperties(){
        if(properties == null){
            properties = new Properties();
            try(InputStream input = Main.class.getResourceAsStream("/config.properties")){
                properties = new Properties();
                if (input == null){
                    logger.severe("Impossibile trovare il file config.properties");
                    return properties;
                }
                properties.load(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return properties;
    }
}
