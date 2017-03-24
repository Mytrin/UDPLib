package com.gmail.lepeska.martin.udplib.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class making easier handling XML files and loading basic library configuration.
 *
 * @author Martin Lepeška
 */
public final class ConfigLoader {
    /**Version of this library*/
    public static final String VERSION ="BETA 2";
     /** Loaded values */
     private static final HashMap<String, String> CONFIG = new HashMap<>();
     /** Singleton */
     private ConfigLoader(){}
          
    /**
     * Loads and sets new configuration.
     * @param source config file 
     * 
     */
    public synchronized static final boolean loadConfig(File source){
            try{
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(source);

                NodeList configValues = doc.getDocumentElement().getElementsByTagName("item");

                for (int i = 0; i < configValues.getLength(); i++) {
                    Node tag = configValues.item(i);
                    CONFIG.put(tag.getAttributes().getNamedItem("name").getTextContent(), tag.getTextContent());
                }
            }catch(ParserConfigurationException | SAXException | IOException | DOMException e){
                return false;
            }
            
            return true;
    } 
    
    /**
     * @param itemName name of tag
     * @param defaultValue returned value when item was not found
     * @return its value or null
     */
    public synchronized static String getString(String itemName, String defaultValue){
        if(CONFIG.get(itemName) != null){
           return CONFIG.get(itemName);
        }
        
        return defaultValue;
    }
    
    /**
     * @param itemName name of tag
     * @param defaultValue returned value when item was not found
     * @return its value or null
     */
    public synchronized static int getInt(String itemName, int defaultValue){
        try{
            String value = CONFIG.get(itemName);
            if(value == null){
                return defaultValue;
            }
            
            return Integer.parseInt(value);
        }catch(NumberFormatException e){
            Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, "Item "+itemName+" is not number! ", e);
        }
        
        return defaultValue;
    }
}