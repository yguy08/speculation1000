package com.speculation1000.specvault.vault;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.speculation1000.specvault.listview.Displayable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;

public class VaultMainControl extends BorderPane implements Initializable {
	
	@FXML private ListView<Displayable> listViewDisplay;
	
	@FXML private Button newEntriesBtn;
	
	@FXML private Button showCloseBtn;
	
	@FXML private Button settingsBtn;
	
	@FXML private Button clearListBtn;
	
	@FXML private Button backTestBtn;
	
	private ObservableList<Displayable> mainObsList = FXCollections.observableArrayList();
	
	private static VaultMainControl vaultMainControl;
    
	public VaultMainControl() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("VaultMainView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		
	}
	
}