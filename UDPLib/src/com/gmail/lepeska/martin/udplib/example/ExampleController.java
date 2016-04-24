package com.gmail.lepeska.martin.udplib.example;

import com.gmail.lepeska.martin.udplib.AGroupNetwork;
import com.gmail.lepeska.martin.udplib.ConfigLoader;
import com.gmail.lepeska.martin.udplib.client.GroupUser;
import java.net.URL;
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
public class ExampleController implements Initializable {

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

    AGroupNetwork network;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        versionText.setText(versionText.getText()+ConfigLoader.VERSION);
        
        docView.getEngine().load("https://github.com/Mytrin/UDPLib"); //TODO doc
    }    

    @FXML
    private void showFile(MouseEvent event) {
    }

    @FXML
    private void create(MouseEvent event) {
    }

    @FXML
    private void connect(MouseEvent event) {
    }

    @FXML
    private void disconnect(MouseEvent event) {
    }

    @FXML
    private void explore(MouseEvent event) {
    }

    @FXML
    private void send(MouseEvent event) {
    }

    @FXML
    private void whisper(MouseEvent event) {
    }
    
}
