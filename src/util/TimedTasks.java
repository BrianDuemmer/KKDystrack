package util;

import java.io.File;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import org.jaudiotagger.audio.AudioFileIO;

import application.DysMain;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import rc.QueueEntry;
import rc.Rating;
import rc.Song;
import rc.viewer.Viewer;

/**
 * Here is where we define any tasks that should run at specified calendar times. Almost
 * like cron.
 * @author Duemmer
 *
 */
public class TimedTasks 
{
	private static final String newBuckPath = System.getenv("TEMP") + "\\{71fddb02-4cb9A-a0d1-cece98d9be63}\\foo.mp3";
	private static final String newBuckOST = "Pikmin 2";
	private static final String newBuckName = "Perplexing Pool (Louie  President Shacho Berry Flower) Area 3";
	
	public static TimerTask doBuck = new TimerTask() {
		@Override public void run() { 
			File buck = new File(newBuckPath);
			buck.getParentFile().mkdirs();
			
			//System.out.println("BUCC");
			
			try {
				Util.jarCopy("util/foo.mp3", newBuckPath);
				
				double dur = AudioFileIO.read(buck).getAudioHeader().getTrackLength();
				
				Song s = new Song(newBuckName, newBuckOST, dur, "Pikmin", newBuckPath);
				QueueEntry q = /*new QueueEntry(Viewer.dysbot, System.currentTimeMillis() / 1000L, new Rating(0,0), s);*/null;
				
				DysMain.foobar.addToPlaybackQueue(q);
				
				Thread.sleep((long) ((DysMain.foobar.getTotalQueueLength() * 1000) + 5000));
				Files.deleteIfExists(buck.toPath());
				
			} catch (Exception e) {
				System.err.println("B timer error");
			}
		}
	};
	
	
	
	public static void startBuck()
	{
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		
		cal.set(Calendar.HOUR_OF_DAY, 11);
		cal.set(Calendar.MINUTE, 00);
		cal.set(Calendar.SECOND, 0);
		
		if(cal.getTime().before(new Date())) // we passed this time already today, play it tomorrow
			cal.add(Calendar.DATE, 1);
		
		System.out.println(cal.getTime());
		
		Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(doBuck, cal.getTime(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
	}
	
	
	
	
	
	
	
	public static TimerTask doGoToSleepDys = new TimerTask() {
		@Override public void run() {
			Platform.runLater(() -> {
				Stage stage = new Stage();
				stage.setTitle("Go to bed!");
				
				BorderPane root = new BorderPane();
				
				Scene scene = new Scene(root);
				stage.setScene(scene);
				
				ImageView imv = new ImageView();
				Image img = new Image(TimedTasks.class.getResourceAsStream("sleep.png"));
				
				imv.setImage(img);
				
				root.getChildren().add(imv);
				
				stage.showAndWait();

			});
		}
	};
	
	
}






