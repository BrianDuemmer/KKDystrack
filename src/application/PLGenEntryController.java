/**
 * Sample Skeleton for 'plGenEntry.fxml' Controller Class
 */

package application;


import java.io.File;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import db.DatabaseIO;
import db.RCTables;
import db.SQLiteDatabaseIO;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import util.SongUtils;

public class PLGenEntryController 
{

	// will cause the scanAndUpdate method to terminate if true
	private boolean stop = false;

	// Specifies whether we shut down gracefully or not
	private boolean graceful = false;




	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="songLabel"
	private Label songLabel; // Value injected by FXMLLoader

	@FXML // fx:id="overallProgressBar"
	private ProgressBar overallProgressBar; // Value injected by FXMLLoader

	@FXML // fx:id="mainBtn"
	private Button mainBtn; // Value injected by FXMLLoader

	@FXML // fx:id="ostProgressBar"
	private ProgressBar ostProgressBar; // Value injected by FXMLLoader

	@FXML // fx:id="ostLabel"
	private Label ostLabel; // Value injected by FXMLLoader

	@FXML
	void onButtonHit(ActionEvent event) 
	{
		if(!graceful) // User forcefully stopped the application, so throw up an alert
		{
			Alert a = new Alert(AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
			a.setHeaderText("Playlist Generator Cancel");
			a.setContentText("Really abort playlist generation? This will result in an incomplete playlist.");
			a.showAndWait();

			if(a.getResult().equals(ButtonType.YES))
			{
				System.out.println("Aborted Playlist Generation");
				stop = true;

				Stage stage = (Stage) mainBtn.getScene().getWindow(); // Only close if they hit yes...
				stage.close();
			}
		} else {

			// ...Or we finished gracefully and they acknowledged it
			Stage stage = (Stage) mainBtn.getScene().getWindow();
			stage.close();
		}
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() 
	{
		assert songLabel != null : "fx:id=\"songLabel\" was not injected: check your FXML file 'plGenEntry.fxml'.";
		assert overallProgressBar != null : "fx:id=\"overallProgressBar\" was not injected: check your FXML file 'plGenEntry.fxml'.";
		assert mainBtn != null : "fx:id=\"cancelBtn\" was not injected: check your FXML file 'plGenEntry.fxml'.";
		assert ostProgressBar != null : "fx:id=\"ostProgressBar\" was not injected: check your FXML file 'plGenEntry.fxml'.";
		assert ostLabel != null : "fx:id=\"ostLabel\" was not injected: check your FXML file 'plGenEntry.fxml'.";


		// business logic
		scanAndUpdate();
	}








	public void scanAndUpdate()
	{
		Task<Void> tsk = new Task<Void>()
		{			
			// Used for calculating runtime metrics
			long tStart = System.currentTimeMillis();
			int numSongs = 0;

			// mark the song / ost the system is currently processing
			int currOst;
			int currSong;





			@Override protected Void call() throws Exception 
			{
				try {
					// update the local DB
					DysMain.remoteDB.upSync(DysMain.localDB, RCTables.playlistTable, true);

					// read playlist root, make sure it is valid
					String rootPath = Preferences.userNodeForPackage(DysMain.class).get("playlistRoot", "");
					File root = new File(rootPath);

					if(!rootPath.isEmpty() && root.isDirectory()) { // only go if it's valid

						// Format the statement to enter these into the database
						// We will either insert new records OR if we find a matching
						// song_id, we will update the song_name, song_ost, and song_length records only
						// Do it as a batch to expedite things 
						RCTables.playlistTable.verifyExists(DysMain.remoteDB);
						
						 DysMain.remoteDB.getDb().setAutoCommit(false);
						
						String sql = "INSERT INTO " +RCTables.playlistTable.getName()+ " (song_id, song_name, ost_name, song_length, song_franchise, song_alias) VALUES ("
								+ "?, "
								+ "?, "
								+ "?, "
								+ "?, "
								+ "?, "
								+ "? )";

						PreparedStatement ps = DysMain.remoteDB.getDb().prepareStatement(sql);

						// Now onto processing the songs
						List<File> osts = getOSTs(root);
						File[] ostArr = new File[osts.size()];
						ostArr = osts.toArray(ostArr);

						int ostCt = ostArr.length;

						for(currOst=0; currOst<ostCt; currOst++) {
							if(stop) { break; } // break if necessary
							File ost = ostArr[currOst];

							//update Overall Progress bar(percent done w/ OSTs) and OST label (in form OST: $ost_dir_name)
							Platform.runLater(() -> {
								overallProgressBar.setProgress( ((double)currOst+1) / ((double)ostCt) ); 
								ostLabel.setText("OST: " +ost.getName());
							});


							File[] songArr = ostArr[currOst].listFiles(SongUtils::isLegalExtension); // We only want to mess with song files
							int songCt = songArr.length;

							numSongs+= songCt; // add these songs to the running total

							for(currSong=0; currSong<songCt; currSong++) {
								if(stop) { break; } // break if necessary
								File song = songArr[currSong];

								//update OST Progress bar(percent done w/ Songs) and Song label (in form Song: $song_file_name)
								Platform.runLater(() -> {
									ostProgressBar.setProgress( ((double)currSong+1) / ((double)songCt) ); 
									songLabel.setText("Song: " +song.getName());
								});
								procSong(song, ost, ps); // do all the brute work on this current song
							}
						}

						
						
						// Execute the whole batch, shutdown
						Platform.runLater(() -> {
							ostLabel.setText("OST: Committing playlist...");
							songLabel.setText("Song: Committing playlist...");
							ostProgressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
							ostProgressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
						});
						
						// empty it first!
						DysMain.remoteDB.execRaw("DELETE FROM " +RCTables.playlistTable.getName());
						ps.executeBatch();
						DysMain.remoteDB.getDb().commit();
						DysMain.remoteDB.getDb().setAutoCommit(true);


						// Shutdown stuff
						graceful = !stop; //if stop hasn't been set, then it finished on its own 
						if(graceful) {
							Platform.runLater(() -> {
								ostLabel.setText("OST: Done");
								songLabel.setText("Song: Done");
								mainBtn.setText("OK");
								ostProgressBar.setProgress(1);
								ostProgressBar.setProgress(1);
							});
						}

						// Calculate / print final metrics
						double numSecs = ((double)(System.currentTimeMillis() -  tStart)) / 1000.0;
						double songsPerSec = numSongs / numSecs;
						System.out.println(String.format("Playlist generator processed %d songs in %.3f seconds at an average rate of %.4f songs per second", numSongs, numSecs, songsPerSec));
						
					} else { // playlist root is bad, alert and shutdown
						Platform.runLater(() -> {
							new Alert(AlertType.ERROR, "Playlist root \"" +rootPath+ "\" is an invalid or nonexistent directory! Cannot regenerate playlist").showAndWait(); 
							
							Stage stage = (Stage) mainBtn.getScene().getWindow();
							stage.close();
						});                             
					}

				} catch (Exception e) {
					e.printStackTrace();
					Platform.runLater(()-> { new Alert(AlertType.ERROR, "Failed to regenteate playlist!").showAndWait(); });
				}
				return null;
			}
		};


		Thread t = new Thread(tsk);
		t.setDaemon(true);
		t.setName("PlaylistGen");
		t.start();
	}





	private void procSong(File song, File ost, PreparedStatement ps)
	{
		// All the important pieces of info about the song
		String songName = "";
		String ostName = "";
		String songID = "";
		double dur = -1;


		try {
			/** Goodbye jid3lib, hello jaudiotagger */
			//			// Calculate duration, which is independent of audio file format
			//			AudioFileFormat aff = AudioSystem.getAudioFileFormat(song);
			//			dur = aff.getFrameLength() / aff.getFormat().getFrameRate();
			//
			//			if(SongUtils.getExt(song).equalsIgnoreCase("mp3"))
			//			{
			//				try {
			//				MP3File mp3 = new MP3File(song);
			//				songName = SongUtils.clean(mp3.getID3v2Tag().getSongTitle());
			//				ostName = SongUtils.clean(mp3.getID3v2Tag().getAlbumTitle());
			//				} catch (UnsupportedOperationException uop) { System.err.println("UnsupportedOperationException for song \"" +ost.getName()+ "\" - \"" +song.getName()+ "\""); }
			//				  catch (TagException tep) { System.err.println("TagException for song \"" +ost.getName()+ "\" - \"" +song.getName()+ "\""); }
			//			} 


			// look at how EASY this is :o
			AudioFile f = AudioFileIO.read(song);
			Tag t = f.getTag();

			dur = f.getAudioHeader().getTrackLength();
			songName = t.getFirst(FieldKey.TITLE);
			ostName = t.getFirst(FieldKey.ALBUM);



			// Just use file / directory names for song / ost names if they are empty (not an MP3 or missing tag data)
			if(songName.isEmpty())
				songName = song.getName().substring(0, song.getName().lastIndexOf('.')); // remove extension

			if(ostName.isEmpty())
				ostName = ost.getName();

			//			songID = ost.getName() +"\\"+ song.getName(); // SongID is just the path of the song relative to the playlist root directory
			songID = song.getAbsolutePath(); // using the absolute path uses a bit more database space, but saves on IOps and increases stability

			ps.setString(1, songID);
			ps.setString(2, songName);
			ps.setString(3, ostName);
			ps.setDouble(4, dur); 
			ps.setString(5, ""); // TODO setup franchise / aliases
			ps.setString(6, "");

			ps.addBatch();
		} 

		// Print a little bt about what happened then print the stack trace to stderr 
		catch (SQLException e) { System.err.println("Error writing to database for song " +song.getName());   e.printStackTrace(); }
		catch (Exception e) { System.out.println("Error reading song file " +song.getName());   e.getClass().getName(); }
	}
	
	




	private List<File> getOSTs(File root)
	{
		List<File> dirs = Arrays.asList(root.listFiles(File::isDirectory));
		List<File> noChildDirs = new ArrayList<File>();

		for(File i : dirs) {
			if(i.listFiles(File::isDirectory).length <= 0)
				noChildDirs.add(i);
			else
				noChildDirs.addAll(getOSTs(i));
		}

		if(dirs.isEmpty())
			noChildDirs.add(root);

		return noChildDirs;
	}


}

















