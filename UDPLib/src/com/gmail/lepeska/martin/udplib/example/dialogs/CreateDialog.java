package com.gmail.lepeska.martin.udplib.example.dialogs;

import com.gmail.lepeska.martin.udplib.AGroupNetwork;
import com.gmail.lepeska.martin.udplib.server.ServerGroupNetwork;
import java.net.UnknownHostException;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class CreateDialog {
    private static final Dialog<AGroupNetwork> DIALOG = new Dialog<>();
    private static final GridPane EDIT_GRID = new GridPane();

    private static final TextField HOST_ADDRESS = new TextField();
    private static final TextField GROUP_ADDRESS = new TextField();
    private static final TextField PORT = new TextField();
    private static final TextField USERNAME = new TextField();
    private static final TextField PASSWORD = new TextField();
    
    private static final ButtonType OK_BTN = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
    private static final ButtonType EXIT_BTN = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    
    static{
        DIALOG.setTitle("Create new network");
        DIALOG.setHeaderText("* signs are compulsatory");
        DIALOG.getDialogPane().setContent(EDIT_GRID);
        
        EDIT_GRID.setHgap(10);
        EDIT_GRID.setVgap(10);
        
        EDIT_GRID.add(new Label("Host IP"), 0, 0);
        EDIT_GRID.add(HOST_ADDRESS, 1, 0);
            HOST_ADDRESS.setPromptText("192.168.1.1");
            
        EDIT_GRID.add(new Label("Group address"), 0, 1);
        EDIT_GRID.add(GROUP_ADDRESS, 1, 1);
            GROUP_ADDRESS.setPromptText("224.0.2.0 - 238.0.0.0");
        
        EDIT_GRID.add(new Label("User name(*)"), 0, 2);
        EDIT_GRID.add(USERNAME, 1, 2);
            USERNAME.setPromptText("Serverator");
        
        EDIT_GRID.add(new Label("Password"), 0, 3);
        EDIT_GRID.add(PASSWORD, 1, 3);
            PASSWORD.setPromptText("123456bestpw");
        
        EDIT_GRID.add(new Label("Port"), 0, 4);
        EDIT_GRID.add(PORT, 1, 4);
            PORT.setPromptText("1023-65535");

        DIALOG.getDialogPane().getButtonTypes().addAll(OK_BTN, EXIT_BTN);
        
        DIALOG.setResultConverter(btn -> {
            AGroupNetwork network = null;
            
            String password = PASSWORD.getText().equals("")?null:PASSWORD.getText();
            
            if (btn != EXIT_BTN) {
                try{
                    if(USERNAME.getText().equals("")){
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Cannot create GroupNetwork");
                        alert.setHeaderText("Missing username value!");
                        alert.setContentText("Username is compulsory!");

                        alert.showAndWait();
                    }else{
                        if(GROUP_ADDRESS.getText().equals("")){
                            network = new ServerGroupNetwork(USERNAME.getText(), password);
                        }else if(HOST_ADDRESS.getText().equals("") || PORT.getText().equals("")){
                            network = new ServerGroupNetwork(USERNAME.getText(), password);
                        }else if(GROUP_ADDRESS.getText().equals("")){
                            network = new ServerGroupNetwork(USERNAME.getText(), password, HOST_ADDRESS.getText(), Integer.parseInt(PORT.getText()));
                        }else{
                            network = new ServerGroupNetwork(USERNAME.getText(), password, HOST_ADDRESS.getText(), GROUP_ADDRESS.getText(), Integer.parseInt(PORT.getText()));
                        }
                    }
                }catch(UnknownHostException | NumberFormatException e){
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Cannot create GroupNetwork");
                    alert.setHeaderText("Values were not valid!");
                    alert.setContentText(e.toString());
                    alert.show();
                }
            }
            
            return network;
       });
    }
    
    public static AGroupNetwork show(){
       Optional<AGroupNetwork> newNetwork = DIALOG.showAndWait();
       
       if(newNetwork.isPresent()){
           return newNetwork.get();
       }
        
       return null;
    }
    
    private CreateDialog(){};
}