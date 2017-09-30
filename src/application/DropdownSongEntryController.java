package application;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import db.RCTables;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import rc.QueueEntry;
import rc.Rating;
import rc.Song;
import rc.viewer.Viewer;


/**
 * Controls the web style song entry - with 2 dropdowns.
 * First one for OST, second for song. Simply select the OST, thenn the song 
 * list will be populated with all of that OST's songs. pick one - submit
 * @author Duemmer
 *
 */
public class DropdownSongEntryController {

	@FXML private ResourceBundle resources;
	@FXML private URL location;
	@FXML  private ComboBox<String> songDropdown;
	@FXML  private ComboBox<String> ostDropdown;
	@FXML private Button cancelBtn;
	@FXML private Button songFileBtn;
	@FXML  private Button addBtn;
	@FXML private TextField priority;


	// Used for storing OST / Song data
	private ObservableList<String> ostList = FXCollections.observableArrayList();
	private ObservableList<String> songList = FXCollections.observableArrayList();

	@FXML void songDropdownOnAction(ActionEvent event) { }

	@FXML void priorityOnAction(ActionEvent event)
	{
		try 
		{ 
			Integer.parseInt(priority.getText());
		} catch(Exception e) { 
			new Alert(AlertType.ERROR, "Please enter a valid number for priority").showAndWait(); 
			priority.setText("0");
		}
	}







	/**
	 * When this runs, a new song has been selected, so update that list accordingly
	 * @param event
	 */
	@FXML  void ostDropdownOnAction(ActionEvent event) 
	{
		// Empty out the old values, add an empty one at the start as a placeholder
		songList.clear();

		String ost = (String) ostDropdown.getValue();
		String sql = "SELECT song_name FROM " +RCTables.playlistTable.getName()+ " WHERE ost_name = ?";

		Thread t = new Thread(() -> {
			RCTables.playlistTable.verifyExists(DysMain.remoteDB);

			try { // Read the database values, warn on error
				PreparedStatement ps = DysMain.remoteDB.getDb().prepareStatement(sql);
				ps.setString(1, ost);
				ResultSet rs = DysMain.remoteDB.execRaw(ps);

				while(rs.next())
					songList.add(rs.getString(1));

				rs.close();
			} catch (SQLException e) {
				DysMain.databaseErrorAlert.showAndWait();
				e.printStackTrace();
			}
		});

		t.setDaemon(true);
		t.setName("popSongs");
		t.start();
	}








	@FXML  void addBtnOnAction(ActionEvent event) 
	{
		String ostSel = ((String) ostDropdown.getValue());
		String songSel = ((String) songDropdown.getValue());

		if(ostSel == null || ostSel.trim().isEmpty()) // if an empty / null ost field, spit out an error, don't continue
			new Alert(AlertType.ERROR, "Please enter a valid OST").showAndWait();

		else if(songSel == null || songSel.trim().isEmpty()) // if an empty / null ost field, spit out an error, don't continue
			new Alert(AlertType.ERROR, "Please enter a valid Song").showAndWait();

		else { // Their selections are good, so continue
			Song s = new Song(songSel, ostSel);

			if(s.getSongID().trim().isEmpty()) // bad song ID, not in the playlist (Should be impossible for this to happen, but add it for shits and giggles
				new Alert(AlertType.ERROR, "bad songID for OST \"" +s.getOstName()+ "\" and song \"" +s.getSongName()+ "\"").showAndWait();

			else { // we have a valid playlist entry selected ready to add. we can also safely close the window
				Rating r = new Rating(s.getSongID());

				QueueEntry q = /*new QueueEntry(Viewer.dysbot, System.currentTimeMillis() / 1000L, r, s);*/null;
				q.setPriority(new Integer(priority.getText()));
				q.writeToDB();

				((Stage) songDropdown.getScene().getWindow()).close(); // close this window
			}
		}
	}







	@FXML void cancelBtnOnAction(ActionEvent event) { ((Stage) songDropdown.getScene().getWindow()).close(); /* close this window*/ }

	/**
	 * When this runs, use the classic text entry adder
	 * @param event
	 */
	@FXML  void songFileBtnOnAction(ActionEvent event) 
	{
		Parent root;
		try {
			root = FXMLLoader.load(getClass().getResource("songEntry.fxml"));
			Stage stage = new Stage();
			stage.setTitle("Enter a song");
			stage.setScene(new Scene(root));
			stage.setResizable(false);
			stage.show();

			//			((Stage) songDropdown.getScene().getWindow()).close(); // close this window

		} catch(Exception e)
		{
			e.printStackTrace();
			Alert a = new Alert(AlertType.ERROR, "Error loading alternate entry form");
			a.setTitle("FXML Error");
			a.showAndWait();
		}
	}







	@FXML void initialize() 
	{
		assert songDropdown != null : "fx:id=\"songDropdown\" was not injected: check your FXML file 'DropdownSongEntry.fxml'.";
		assert ostDropdown != null : "fx:id=\"ostDropdown\" was not injected: check your FXML file 'DropdownSongEntry.fxml'.";
		assert cancelBtn != null : "fx:id=\"cancelBtn\" was not injected: check your FXML file 'DropdownSongEntry.fxml'.";
		assert songFileBtn != null : "fx:id=\"songFileBtn\" was not injected: check your FXML file 'DropdownSongEntry.fxml'.";
		assert addBtn != null : "fx:id=\"addBtn\" was not injected: check your FXML file 'DropdownSongEntry.fxml'.";

		// bind the lists to the controls
		ostDropdown.setItems(ostList);
		songDropdown.setItems(songList);

		// Populate the OSTs
		Task<ArrayList<String>> getOSTs = new Task<ArrayList<String>>()
		{
			@Override
			protected ArrayList<String> call() throws Exception
			{
				ArrayList<String> osts = new ArrayList<String>();
				try {
					// get a 1-column list containing all unique, non null/empty OSTs
					RCTables.playlistTable.verifyExists(DysMain.remoteDB);
					String sql = "SELECT DISTINCT ost_name FROM " +RCTables.playlistTable.getName() +" WHERE ost_name IS NOT NULL AND NOT ost_name = '';";
					ResultSet rs = DysMain.remoteDB.execRaw(sql);

					// Add the OSTs, throw something to catch the alert routine if we don't end up getting any OSTs
					while(rs.next())
						osts.add(rs.getString(1));

					if(osts.isEmpty())
						throw new RuntimeException("No OSTs found In Playlist!");

					rs.close();
				} catch(Exception e) {
					e.printStackTrace();
					Platform.runLater(() -> { DysMain.databaseErrorAlert.showAndWait(); }); // warn the user
				}

				// Add an empty string in the front as a default value
				osts.add(0, "");
				return osts;
			}

			// Empty the list then add all the parsed OSTs
			@Override protected void succeeded() {
				ostList.clear();
				ostList.addAll(getValue());
			}
		};

		
		
		
		Thread t = new Thread(getOSTs);
		t.setDaemon(true);
		t.setName("getOSTs");
		t.start();
		
		// bind priority off focus listener
		priority.focusedProperty().addListener((arg0, oldVal, newVal) -> { 
			if(oldVal.booleanValue())
				priorityOnAction(null); 
		});
	}





}





