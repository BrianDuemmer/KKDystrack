package application;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class PropMgrController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private Button saveBtn;

	@FXML
	private TextField playlistRootEntry;

	@FXML
	private Button fussbotBrowse;

	@FXML
	private Button playlistRootBrowse;

	@FXML
	private Button cancelBtn;

	@FXML
	private TextField fussbotDBEntry;

	@FXML
	void openBrowse(ActionEvent event) {
		String lastDirStr = Preferences.userNodeForPackage(DysMain.class).get("lastDir", "");
		
		if(lastDirStr.trim().isEmpty())
			lastDirStr = System.getProperty("user.home");
		
		File lastDir = new File(lastDirStr);
		
		// do the proper thing for each different entry box
		Button caller = (Button) event.getSource();
		if(caller.equals(fussbotBrowse)) // select a file
		{ 
			FileChooser fc = new FileChooser();
			fc.setTitle("Select file...");
			fc.setInitialDirectory(lastDir);
			fc.setSelectedExtensionFilter(new ExtensionFilter("Sqlite Databases (*.sqlite)", "*.sqlite"));
			File sel = fc.showOpenDialog(cancelBtn.getScene().getWindow());
			
			if(sel != null && sel.exists()) {// good catch-all for any bad entry. Only save when good
				fussbotDBEntry.setText(sel.getAbsolutePath());
				Preferences.userNodeForPackage(DysMain.class).put("lastDir", sel.getAbsolutePath());
			}
			
		} else if (caller.equals(playlistRootBrowse)) {
			DirectoryChooser dc = new DirectoryChooser();
			dc.setTitle("Select Directory");
			dc.setInitialDirectory(lastDir);
			File sel = dc.showDialog(cancelBtn.getScene().getWindow());
			
			if(sel != null && sel.exists()) {
				playlistRootEntry.setText(sel.getAbsolutePath());
				Preferences.userNodeForPackage(DysMain.class).put("lastDir", sel.getAbsolutePath());
			}
			
		}
	}


	@FXML
	void saveAction(ActionEvent event) {
		Preferences pref = Preferences.userNodeForPackage(DysMain.class);
		pref.put("fussbotDB", fussbotDBEntry.getText());
		pref.put("playlistRoot", playlistRootEntry.getText());
		
		cancelAction(null);
	}

	@FXML
	void cancelAction(ActionEvent event) {
		Stage s =((Stage) cancelBtn.getScene().getWindow());
		s.close();
	}

	@FXML
	void initialize() {
		assert saveBtn != null : "fx:id=\"addToQueueBtn\" was not injected: check your FXML file 'propEntry.fxml'.";
		assert playlistRootEntry != null : "fx:id=\"playlistRootEntry\" was not injected: check your FXML file 'propEntry.fxml'.";
		assert fussbotBrowse != null : "fx:id=\"fussbotBrowse\" was not injected: check your FXML file 'propEntry.fxml'.";
		assert playlistRootBrowse != null : "fx:id=\"playlistRootBrowse\" was not injected: check your FXML file 'propEntry.fxml'.";
		assert cancelBtn != null : "fx:id=\"selectSongFileBtn\" was not injected: check your FXML file 'propEntry.fxml'.";
		assert fussbotDBEntry != null : "fx:id=\"fussbotDBEntry\" was not injected: check your FXML file 'propEntry.fxml'.";

		Preferences pref = Preferences.userNodeForPackage(DysMain.class);
		
		fussbotDBEntry.setText(pref.get("fussbotDB", ""));
		playlistRootEntry.setText(pref.get("playlistRoot", ""));
	}
}




