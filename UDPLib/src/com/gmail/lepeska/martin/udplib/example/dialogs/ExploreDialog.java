package com.gmail.lepeska.martin.udplib.example.dialogs;

import com.gmail.lepeska.martin.udplib.UDPLibException;
import com.gmail.lepeska.martin.udplib.explore.AvailableServerRecord;
import com.gmail.lepeska.martin.udplib.explore.ExploreRunnable;
import com.gmail.lepeska.martin.udplib.explore.IExploreListener;
import java.net.UnknownHostException;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

public class ExploreDialog {
    private static final Dialog<AvailableServerRecord> DIALOG = new Dialog<>();
    private static final GridPane EDIT_GRID = new GridPane();
    
    private static final ListView<AvailableServerRecord> SERVER_LIST = new ListView<>();
    private static final TextField GROUP_ADDRESS = new TextField();
    private static final TextField PORT = new TextField();
    
    private static final Button SCAN_BTN = new Button("Scan");
    private static final ProgressBar PROGRESS = new ProgressBar();
    
    private static final ButtonType OK_BTN = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
    private static final ButtonType EXIT_BTN = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    
    private static final IExploreListener LISTENER = new IExploreListener() {
        @Override
        public void receive(AvailableServerRecord record) {
            Platform.runLater(() -> {
                SERVER_LIST.getItems().add(record);
            });
            
        }

        @Override
        public void finished() {
            Platform.runLater(() -> {
                PROGRESS.setProgress(1);
            });
        }
    };
    
    static{
        DIALOG.setTitle("Scan for available networks");
        DIALOG.setHeaderText("* signs noncompulsatory, can be left blank");
        DIALOG.getDialogPane().setContent(EDIT_GRID);
        
        EDIT_GRID.setHgap(10);
        EDIT_GRID.setVgap(10);
        
        EDIT_GRID.add(SERVER_LIST, 0, 0);
        
        EDIT_GRID.add(new Label("Group address"), 0, 1);
        EDIT_GRID.add(GROUP_ADDRESS, 1, 1);
        
        EDIT_GRID.add(new Label("Port"), 0, 2);
        EDIT_GRID.add(PORT, 1, 2);

        EDIT_GRID.add(SCAN_BTN, 0, 3);
            SCAN_BTN.setOnMouseClicked((MouseEvent event) -> {
                try{
                    ExploreRunnable explorer;
                    if(GROUP_ADDRESS.getText().equals("") || PORT.getText().equals("")){
                        explorer = new ExploreRunnable(LISTENER);
                    }else{
                        explorer = new ExploreRunnable(GROUP_ADDRESS.getText(), Integer.parseInt(PORT.getText()), LISTENER);
                    }
                    
                    PROGRESS.setProgress(-1);
                    new Thread(explorer).start();
                }catch(UnknownHostException | UDPLibException | NumberFormatException e){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Cannot create GroupNetwork");
                    alert.setHeaderText("Values were not valid!");
                    alert.setContentText(e.toString());
                    alert.show();
                }
        });
        EDIT_GRID.add(PROGRESS, 1, 3);

        DIALOG.getDialogPane().getButtonTypes().addAll(OK_BTN, EXIT_BTN);
        
        DIALOG.setResultConverter(btn -> {
            AvailableServerRecord network = null;

            if (btn != EXIT_BTN) {
               network = SERVER_LIST.getSelectionModel().getSelectedItem();
            }
            
            return network;
       });
    }
    
    public static AvailableServerRecord show(){
       PROGRESS.setProgress(0);
       SERVER_LIST.getItems().clear();
       
       Optional<AvailableServerRecord> newNetwork = DIALOG.showAndWait();
       
       if(newNetwork.isPresent()){
           return newNetwork.get();
       }
        
       return null;
    }
    
    private ExploreDialog(){};
}