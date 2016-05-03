package com.gmail.lepeska.martin.udplib.util;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class making easier handling XML files and loading basic library configuration.
 *
 * @author Martin Lepeška
 */
public final class ConfigLoader {
    /**Version of this library*/
    public static final String VERSION ="BETA";
    /** Indicator that some configuration was already loaded */
     private static boolean configLoaded = false;
     /** Loaded values */
     private static final HashMap<String, String> CONFIG = new HashMap<>();
     /** Singleton */
     private ConfigLoader(){}
     
    /**
     * Loads and sets new configuration from "udplib_config.xml".
     * @return true, if there was no exception, when parsing xml
     */
    public synchronized static final boolean loadConfig(){
        try{
            loadConfig(new File("udplib_config.xml"));
        }catch(Exception e){
            Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, "Config not loaded! ", e);
        }
        
        return configLoaded;
    }
     
    /**
     * Loads and sets new configuration.
     * @param source config file 
     * 
     * @throws java.lang.Exception 
     */
    public synchronized static final void loadConfig(File source) throws Exception{
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(source);

            NodeList configValues = doc.getDocumentElement().getElementsByTagName("item");

            for (int i = 0; i < configValues.getLength(); i++) {
                Node tag = configValues.item(i);
                CONFIG.put(tag.getAttributes().getNamedItem("name").getTextContent(), tag.getTextContent());
            }
            
            configLoaded = true;
    } 
    
    /**
     * @return true, if config was loaded
     */
    public synchronized static boolean isConfigLoaded() {
        return configLoaded;
    }

    /**
     * @param itemName name of tag
     * @return its value or null
     */
    public synchronized static String getString(String itemName){
        return CONFIG.get(itemName);
    }
    
    /**
     * @param itemName name of tag
     * @return its value or null
     */
    public synchronized static int getInt(String itemName){
        return Integer.parseInt(CONFIG.get(itemName));
    }
}