package com.gmail.lepeska.martin.udplib.example;

import com.gmail.lepeska.martin.udplib.AGroupNetwork;
import com.gmail.lepeska.martin.udplib.ConfigLoader;
import com.gmail.lepeska.martin.udplib.IGroupListener;
import com.gmail.lepeska.martin.udplib.StoredMessage;
import com.gmail.lepeska.martin.udplib.client.GroupUser;
import com.gmail.lepeska.martin.udplib.example.dialogs.CreateDialog;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
        network = CreateDialog.zobrazDialog();
        
        if(network != null){
            network.addListener(this);
            network.start();
        }
    }

    @FXML
    private void connect(MouseEvent event) {
        
        //network.addListener(this);
        
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
    }

    @FXML
    private void send(MouseEvent event) {
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
    public void joined() {
        chatBox.getItems().add("Joined group network...");
    }

    @Override
    public void userKicked(GroupUser who) {
       userView.getItems().remove(who);
    }

    @Override
    public void userJoined(GroupUser who) {
       userView.getItems().add(who);
    }

    @Override
    public void mesageReceived() {
        List<StoredMessage> messages = network.getMessages();
        
        messages.stream().forEach((message) -> {
            chatBox.getItems().add(message.sender.name+(!message.isMulticast?"(private)":"")+": "+message.message);
        });
    }

    @Override
    public void kicked() {
        chatBox.getItems().add("Kicked from group network...");
    }
}
