package com.gmail.lepeska.martin.udplib.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Example extends Application {
    
    public static final String NAME = "UDPLib";
    
    @Override
    public void start(Stage primaryStage) {
        try{
            Parent root = FXMLLoader.load(getClass().getResource("GUI.fxml"));
        
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle(NAME);
            primaryStage.show();
        }catch(Exception e){
            System.err.println("GUI xml not found! "+e);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
