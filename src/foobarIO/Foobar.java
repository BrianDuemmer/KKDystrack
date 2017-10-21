package foobarIO;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import application.DysMain;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import rc.QueueEntry;
import rc.queue.Song;

/**
 * This class does all of the interfacing with foobar2000, which is done through 
 * the command line. Unfortunately, we have no way to read data back from foobar,
 * only the ability to push data to it. <p/>
 * 
 * Brecause of this, we must keep a simulation of the length of foobar's queue, and it's state
 * via open loop methods. As such, drift and glitches is inevitable, so we will set foobar to 
 * autorestart periodically. <p/>
 * 
 * With all that in mind, it becomes highly unadvisable to tamper directly with foobar's playback 
 * queue in foobar itself: as much as possible should be done from the control program.
 * @author Duemmer
 *
 */
public class Foobar 
{
	/** foobar2000's .exe path*/
	private String root;

	/** If the queue length drops below this, add a song */
	public static double addSongTime = 30;
	
	/** If the current song's length is less than this, skip */
	public static double skipSongTime = 3;

	/** Length  in seconds of the playback queue*/
	private double totalQueueLength = 0;

	/** Time remaining on the current song */
	private double currSongRemaining = 0;

	/** Total length of the current song */
	private double totalCurrSongLength = 0;

	/** Specifies whether or not the playback queue is currently playing something */
	private boolean isPlaying = false;
	
	private boolean isPaused = false;

	/** Specifies the currently playing song */
	private Song currSong = null;

	/** Specifies a list of all the songs in queue */
	private Queue<Song> queue = new LinkedList<Song>();

	/** Last time (in nanosecods) that the update() method was called */
	private long lastUpdateNanos;

	private boolean overran = false;

	private Runnable addSongCallback = ()->{}; // safe, default do nothing runnable

	private ScheduledExecutorService updateSrv;








	/**
	 * Starts the state simulator, assuming foobar is in its natural startup state
	 */
	public Foobar(String foobarPath)
	{
		System.out.println("Starting Foobar state simulator...");

		this.root = foobarPath;

		if( !(new File(foobarPath).exists()))
			Platform.runLater(() -> {
				new Alert(AlertType.ERROR, "FATAL: Bad path to foobar provided, check settings!").showAndWait();
			});
	}





	/**
	 * Starts the connection to foobar. NOTE: this will also forcibly 
	 * restart foobar and wipe the playback queue so use with caution
	 * 
	 * @param addSongCallback this will be run when a song needs to be added to foobar.
	 * Be ready, as it'll be run almost immediately after this method is called
	 */
	public void startFoobar(Runnable addSongCallback)
	{
		resetFoobar();
		this.addSongCallback = addSongCallback;
	}








	/**
	 * This update routine is called often to keep tabs on foobar's status
	 * @throws Exception if the queue overruns
	 */
	private void doUpdate()
	{
		long currTime = System.nanoTime();
		double dsec = ((double)(currTime - lastUpdateNanos)) / 1000000000.0;
		lastUpdateNanos = currTime;

		if(isPlaying) // queue only proceeds if playing
		{
			totalQueueLength -= dsec;
			overran = totalQueueLength < 0;
			if(overran) // overran the queue
			{
				totalQueueLength = 0;
				System.out.println("Overran Foobar queue!");
			}

			currSongRemaining -= dsec;
			if(currSongRemaining < skipSongTime) // move to the next song
			{
				if(currSongRemaining > 0) // force skip if necessary
					playPlayback(); 
				
				totalQueueLength -= currSongRemaining; // we will reset at this point, so disregard what's left of the song
				currSong = queue.poll();
				currSongRemaining = currSong.getLength();
			}

			if(currSongRemaining < 0) // If we are STILL less than 0, then just set to 0 for safety
				currSongRemaining = 0;
		}

		if(totalQueueLength < addSongTime) // queue's almost empty, add a new song
			addSongCallback.run();

	}







	/**
	 * Executes the given command under this instance of foobar2000
	 */
	public void execFoobarCmd(String foobarCmd)
	{
		String cmd = "\"" +root+ "\" " +foobarCmd;
		try {
			Runtime.getRuntime().exec(cmd);


			/** That didn't work... it just hung there on those calls. Good thing we don't need the output. */
			// Since foobar doesn't output anything to stdout, we should safely be able to dispose of it
			//			p.getInputStream().close();
			//
			//			// Send error output to syserr in case of a bad command or something
			//			BufferedReader b = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			//			String line = "";
			//
			//			while((line = b.readLine()) != null)
			//				System.err.println(line);

		} catch (Exception e) {
			System.err.println("Error running foobar command");
			e.printStackTrace();
		}
	}






	public void stopPlayback() 
	{ 
		execFoobarCmd("/stop"); 
		isPlaying = false;
		isPaused = false;

		// update simulation vars
		totalQueueLength -= currSongRemaining;
		currSongRemaining = 0;
	}


	public void pausePlayback() 
	{ 
		execFoobarCmd("/pause"); 
		isPlaying = false;
		isPaused = true;
	}




	public void playPlayback() {		
		// update simulation vars, add a new song IF NECESSARY
		if(isPlaying) {
			totalQueueLength -= currSongRemaining;
			currSongRemaining = 0;
			addSongCallback.run();
		}
		
		execFoobarCmd("/play"); 
		isPlaying = true;
		isPaused = false;
	}
	
	
	
	
	
	public void skipSong() {
		currSong = queue.poll();
		
		totalQueueLength -= currSongRemaining;
		currSongRemaining = 0;
		
		if(currSong == null) // if the queue is empty make sure to add a song
			addSongCallback.run();
		
		currSongRemaining = currSong.getLength();
		
		execFoobarCmd("/skip");
	}






	/** Verifies that foobar is started fresh after this runs */
	public void resetFoobar()
	{		
		execFoobarCmd("/exit");
		// Delay for a bit because foobar is wonky
		try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }

		execFoobarCmd(""); // this will just open a new window

		// Reset the simulator variables
		currSong = null;
		currSongRemaining = 0;
		isPlaying = false;
		isPaused = false;
		overran = false;
		queue = new LinkedList<Song>();
		totalCurrSongLength = 0;
		totalQueueLength = 0;
		
		// get the updater running
		startUpdaterFresh();
	}









	/**
	 * Adds this song to foobar's playback queue. Note that this won't 
	 * Explicitly call {@link #play()}
	 */
	public void addToPlaybackQueue(QueueEntry song)
	{
		if(song.getSong().getSongID().isEmpty()) // bad song
			System.err.println("Bad song added to playback queue!");

		else {
			String songPath = song.getSong().getSongID();
			String fooCmd = "/context_command:\"add to playback queue\" \"" +songPath+ "\""; // /context_command:"add to playback queue" "songPath.mp3"

			totalQueueLength += song.getSong().getLength(); // update simulation data
			queue.add(song.getSong());

			execFoobarCmd(fooCmd);
		}
	}





	/**
	 * CLeanly makes sure the update service is shutdown, then re-creates it and runs it
	 */
	private void startUpdaterFresh()
	{
		try { // if anything happens, whether a null pointer or interrupted, just catch it and be done
			updateSrv.shutdownNow();
			updateSrv.awaitTermination(10000, TimeUnit.MILLISECONDS); // 10 seconds should be way more than enough
		}	catch (NullPointerException e) {} // this will always be thrown the first time, so ignore it
			catch (Exception e) { e.printStackTrace(); } // anything else is unexpected
		
		// create it fresh
		updateSrv = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			@Override public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				t.setName("Foobar Simulator");
				return t;
			}
		});
		
		// and start
		updateSrv.scheduleAtFixedRate(() -> { doUpdate(); }, 0, DysMain.simulatorUpdateMillis, TimeUnit.MILLISECONDS);
	}





	public boolean isPlaying() {
		return isPlaying;
	}





	public String getRoot() {
		return root;
	}





	public double getTotalQueueLength() {
		return totalQueueLength;
	}





	public double getCurrSongRemaining() {
		return currSongRemaining;
	}





	public double getTotalCurrSongLength() {
		return totalCurrSongLength;
	}





	public Song getCurrSong() {
		return currSong;
	}





	public Queue<Song> getQueue() {
		return queue;
	}





	public long getLastUpdateNanos() {
		return lastUpdateNanos;
	}





	public boolean isOverran() {
		return overran;
	}





	public ScheduledExecutorService getUpdateSrv() {
		return updateSrv;
	}





	public boolean isPaused() {
		return isPaused;
	}








}








