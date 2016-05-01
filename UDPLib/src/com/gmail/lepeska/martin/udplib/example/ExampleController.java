package com.gmail.lepeska.martin.udplib.example;

import com.gmail.lepeska.martin.udplib.AGroupNetwork;
import com.gmail.lepeska.martin.udplib.util.ConfigLoader;
import com.gmail.lepeska.martin.udplib.IGroupListener;
import com.gmail.lepeska.martin.udplib.StoredMessage;
import com.gmail.lepeska.martin.udplib.UDPLibException;
import com.gmail.lepeska.martin.udplib.client.GroupUser;
import com.gmail.lepeska.martin.udplib.example.dialogs.CreateDialog;
import com.gmail.lepeska.martin.udplib.example.dialogs.ExploreDialog;
import com.gmail.lepeska.martin.udplib.example.dialogs.JoinDialog;
import com.gmail.lepeska.martin.udplib.explore.AvailableServerRecord;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;

/**
 * Example chat using UDPLib
 *
 * @author Martin Lepeška
 */
public class ExampleController implements Initializable, IGroupListener {

    @FXML
    private ListView<GroupUser> userView;
    @FXML
    private ListView<?> fileView;
    @FXML
    private ListView<String> chatBox;
    @FXML
    private TextField sendBox;
    @FXML
    private WebView docView;
    @FXML
    private Text versionText;

    private AGroupNetwork network;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //UDPLib init
        ConfigLoader.loadConfig();
        
        
        versionText.setText(versionText.getText()+ConfigLoader.VERSION);
        
        docView.getEngine().load("https://github.com/Mytrin/UDPLib"); //TODO doc

    }    

    @FXML
    private void showFile(MouseEvent event) {
    }

    @FXML
    private void create(MouseEvent event) {
        if(network != null){
            network.leave();
            network = null;
        }
        network = CreateDialog.show();
        
        if(network != null){
            network.addListener(this);
            network.start();
        }
    }

    @FXML
    private void connect(MouseEvent event) {
        if(network != null){
            network.leave();
            network = null;
        }
        network = JoinDialog.show();
        
        if(network != null){
            network.addListener(this);
             try{
                 network.start();
             }catch(UDPLibException e){
                 showErrorAlert(e);
             }
        }
    }

    @FXML
    private void disconnect(MouseEvent event) {
        if(network != null){
            network.leave();
            network = null;
        }
    }

    @FXML
    private void explore(MouseEvent event) {
        AvailableServerRecord selected = ExploreDialog.show();
        if(selected != null){
            if(network != null){
                network.leave();
                network = null;
            }
            JoinDialog.preset(selected.server.getHostName(), ""+selected.port);
            JoinDialog.show();
        }
    }

    @FXML
    private void send(Event event) {
         if(network != null){
             String message = sendBox.getText();
             network.sendGroupMessage(message);
             chatBox.getItems().add(network.getUserName()+": "+message);
             sendBox.setText("");
         }
    }

    @FXML
    private void whisper(MouseEvent event) {
        if(network != null){
            GroupUser target = userView.getSelectionModel().getSelectedItem();
            String message = sendBox.getText();
            if(target != null){
                network.sendMessage(target, message);
                chatBox.getItems().add(network.getUserName()+"->"+target.name+": "+message);
                
                sendBox.setText("");
            }
        }
    }

    //IGROUPNETWORK METHODS
    @Override
    public void joined(GroupUser me) {
        Platform.runLater(() -> {
            userView.getItems().clear();
            userView.getItems().add(me);
            chatBox.getItems().add("Joined group network...");
        });
    }

    @Override
    public void userKicked(GroupUser who) {
       Platform.runLater(() -> {
        userView.getItems().remove(who);
       });
    }

    @Override
    public void userJoined(GroupUser who) {
       Platform.runLater(() -> {
        userView.getItems().add(who);
       });
    }

    @Override
    public void mesageReceived() {
        Platform.runLater(() -> {
            List<StoredMessage> messages = network.getMessages();
        
            messages.stream().forEach((message) -> {
                chatBox.getItems().add(message.sender.name+(!message.isMulticast?"(private)":"")+": "+message.message);
            });
        });
    }

    @Override
    public void kicked() {
        Platform.runLater(() -> {
            userView.getItems().clear();
            chatBox.getItems().add("Kicked from group network...");
        });
    }
    
    public static final void showErrorAlert(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(Example.NAME);
        alert.setHeaderText("Error");
        alert.setContentText(e.toString());

        alert.showAndWait();
    }
}
