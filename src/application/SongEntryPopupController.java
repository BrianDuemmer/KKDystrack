/**
 * Sample Skeleton for 'songEntry.fxml' Controller Class
 */

package application;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import rc.QueueEntry;
import rc.Rating;
import rc.Song;
import rc.Viewer;

public class SongEntryPopupController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="addToQueueBtn"
    private Button addToQueueBtn; // Value injected by FXMLLoader

    @FXML // fx:id="songNameEntry"
    private TextField songNameEntry; // Value injected by FXMLLoader

    @FXML // fx:id="priorityEntry"
    private TextField priorityEntry; // Value injected by FXMLLoader

    @FXML // fx:id="selectSongFileBtn"
    private Button selectSongFileBtn; // Value injected by FXMLLoader
    
    @FXML // fx:id="cancelBtn"
    private Button cancelBtn; // Value injected by FXMLLoader

    @FXML // fx:id="songOstEntry"
    private TextField songOstEntry; // Value injected by FXMLLoader
    
    @FXML
    void onCancelHit(ActionEvent event)
    {
    	// Close down the scene
    	Stage stage = (Stage) cancelBtn.getScene().getWindow();
		stage.close();
    }
    
    
    @FXML
    void priorityOnAction(ActionEvent event) 
    {
    	try { new Integer(priorityEntry.getText()); } // See if the string the user entered is a parseable number
    	catch (NumberFormatException e) 
    	{ 
    		new Alert(AlertType.ERROR, "Please enter a valid number for priority").showAndWait();
    		priorityEntry.setText("0");
    	}
    }

    @FXML
    void addToQueueAction(ActionEvent event) 
    {
    	Song song = new Song(songNameEntry.getText(), songOstEntry.getText());
    	int priority = 0;
    	
    	// warn the user if the name / ost they entered doesn't work
    	if(song.getSongID().isEmpty())
    	{
    		new Alert(AlertType.ERROR, "Could not find a matching songID for this name / ost! Check that the name and ost were entered correctly and that the Playlist table is up to date.").showAndWait();
    		return; // We can't continue on bad input
    	} 
    	
    	// Try to parse the priority, but make sure it's good. Break if it isn't.
    	try {priority = new Integer(priorityEntry.getText()); }
    	catch(NumberFormatException e)
    	{
    		new Alert(AlertType.ERROR, "Please enter a valid number for priority").showAndWait();
    		return; // bad priority
    	}
    	
    	// The entry was good and can be added
    	
    	/**@TODO Add a proper changeable username. Now it's just dysbot's info */
    	Viewer vw = Viewer.dysbot;
    	Rating r = new Rating(song.getSongID());
    	
    	// format queue entry
    	QueueEntry q = new QueueEntry(vw, System.currentTimeMillis() / 1000L, r, song);
    	q.setPriority(priority);
    	
    	// Write it to the database
    	q.writeToDB();
    	
    	
    	// Close down the scene. The table will be updated by the main controller when this finishes
    	Stage stage = (Stage) cancelBtn.getScene().getWindow();
		stage.close();
    }

    @FXML
    void selectSongFileOnAction(ActionEvent event) 
    {
    	Song song;
    	
    	FileChooser fc = new FileChooser();
    	File sel = fc.showOpenDialog(null);
    	
    	if(sel != null && sel.isFile()) // If a valid file was selected
    	{
    		String songID = sel.getParentFile().getName() +"/"+ sel.getName();
    		song = new Song(songID);
    		
    		if(song.getSongName().isEmpty()) // warn the user, error condition
    			new Alert(AlertType.WARNING, "Unable to construct song! Check error log for more details!").showAndWait();
    		
    		else // user selected a valid song
    		{
    			this.songNameEntry.setText(song.getSongName());
    			this.songOstEntry.setText(song.getOstName());
    		}
    	} else { new Alert(AlertType.ERROR, "Please select a valid song file").showAndWait(); } // User picked a bad file / folder
    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert addToQueueBtn != null : "fx:id=\"addToQueueBtn\" was not injected: check your FXML file 'songEntry.fxml'.";
        assert songNameEntry != null : "fx:id=\"songNameEntry\" was not injected: check your FXML file 'songEntry.fxml'.";
        assert priorityEntry != null : "fx:id=\"priorityEntry\" was not injected: check your FXML file 'songEntry.fxml'.";
        assert selectSongFileBtn != null : "fx:id=\"selectSongFileBtn\" was not injected: check your FXML file 'songEntry.fxml'.";
        assert songOstEntry != null : "fx:id=\"songOstEntry\" was not injected: check your FXML file 'songEntry.fxml'.";

    }
}











