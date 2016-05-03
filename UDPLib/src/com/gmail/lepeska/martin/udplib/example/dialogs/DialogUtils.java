package com.gmail.lepeska.martin.udplib.example.dialogs;

import com.gmail.lepeska.martin.udplib.example.Example;
import java.io.File;
import java.nio.file.Files;
import java.util.LinkedList;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

/**
 *
 * @author Martin Lepeška
 */
public class DialogUtils {

    public static void showFileDialog(File file) {
        try {
            final LinkedList<String> data = new LinkedList<>();

            Files.lines(file.toPath()).forEach((String t) -> data.add(t));

            TextArea textArea = new TextArea();
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            
            String content = "";
            content = data.stream().map((line) -> line).reduce(content, String::concat);
            textArea.setText(content);
            
            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(new Label("Content of file "+file.getName()), 0, 0);
            expContent.add(textArea, 0, 1);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(Example.NAME);
            alert.setHeaderText(file.getName());                    
            alert.getDialogPane().setExpandableContent(expContent);

            alert.showAndWait();
        } catch (Exception e) {
            showErrorAlert(e);
        }
    }
    
    public static final void showErrorAlert(Exception e) {
        showErrorAlert(e.toString());
    }
    
    public static final void showErrorAlert(String e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(Example.NAME);
        alert.setHeaderText("Error");
        alert.setContentText(e);

        alert.showAndWait();
    }
    
    public static final void showInfo(String e) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(Example.NAME);
        alert.setHeaderText("Info");
        alert.setContentText(e);

        alert.showAndWait();
    }
}
