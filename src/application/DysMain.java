package application;
	

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import db.DatabaseIO;
import db.MySQLDatabaseIO;
import db.RCTables;
import db.SQLiteDatabaseIO;
import foobarIO.Foobar;
import http.ServerIO;
import javafx.application.Application;
import javafx.stage.Stage;
import rc.RequestControl;
import rc.viewer.Viewer;
import rc.viewer.ViewerFactory;
import util.TimedTasks;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;




/**
 * This is the primary controller of the entire system.
 * This is the device that decides when requests should open and close,
 * decide which songs should be sent to foobarIO when, etc. 
 * 
 * @author Duemmer
 *
 */
public class DysMain extends Application 
{
	// constants
	public static final String appDir = System.getenv("APPDATA") + "\\KKDystrack";
	public static final String version ="v 1.1.1";
	public static final String foobarPath = "c:\\program files (x86)\\foobar2000\\foobar2000.exe";
	public static final int simulatorUpdateMillis = 50;
	public static final int varCalcUpdateMillis = 1000;
	public static final int UIUpdateMillis = 300;
	public static String queueDumpPath = appDir+ "\\queue.csv";
	public static final int SERVER_PORT = 7095;
	
	private static final String remoteDbUser = "dystify_dev";
	private static final String remoteDbPass = "foobarbaz3001";
	private static final String remoteDbHost = "dystify.com";
	private static final String remoteDbName = "dystify_dystrack_server";
	private static final String remoteDBArgs = "?rewriteBatchedStatements=true";
	private static final int remoteDbPort = 3306;
	
	
	// Actors
	public static RequestControl rc;
	public static DatabaseIO remoteDB;
	public static DatabaseIO localDB;
	public static Foobar foobar;
	public static ServerIO server;
	
	
	/** A typical alert for a database error*/
	public static Alert databaseErrorAlert;
	
	
	
	
	
	// Periodic update thread pools - make sure to make it daemon / set a good name
	public static ScheduledExecutorService UIUpdateService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setDaemon(true);
			t.setName("UI Updates");
			return t;
		}
	});
	
	
	
	
	public static ScheduledExecutorService varCalcService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setDaemon(true);
			t.setName("Variable calculation");
			return t;
		}
	});
	
	
	
	
	
	
	@Override public void start(Stage primaryStage) {
		try {
			DysMain.databaseErrorAlert = new Alert(AlertType.ERROR);
			DysMain.databaseErrorAlert.setTitle("Database Error");
			DysMain.databaseErrorAlert.setHeaderText("Database Error");
			DysMain.databaseErrorAlert.setContentText("An error occured trying to acces the database. Check the error log for more info");
			
			AnchorPane root = (AnchorPane)FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
			Scene scene = new Scene(root);
			//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("resource/KKDystrack.png")));
			
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setTitle("K. K. DysTrack - " +version);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	@Override public void stop()
	{
		// Stop all update threads, provided they aren't null
		if(UIUpdateService != null)
			UIUpdateService.shutdown();
		System.exit(0);
	}
	
	
	
	public static void main(String[] args) 
	{
		new File(appDir).mkdirs(); // verify the property app directory exists
		
		remoteDB = new MySQLDatabaseIO(remoteDbHost, remoteDbUser, remoteDbPass, remoteDbName, remoteDbPort, remoteDBArgs);
		localDB = new SQLiteDatabaseIO(appDir + "\\KKDystrack.sqlite");
		server = new ServerIO(SERVER_PORT);
		
		TimedTasks.startBuck();
		
		
		// This method won't finish until the application exits
		launch(args);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
