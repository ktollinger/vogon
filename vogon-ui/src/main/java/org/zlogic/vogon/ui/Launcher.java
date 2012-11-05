/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.zlogic.vogon.data.DatabaseManager;
import org.zlogic.vogon.data.FinanceData;

/**
 * Main entry point for application. Performs initial initialization and loading
 * of Java FX code.
 *
 * @author Dmitry Zolotukhin
 */
public class Launcher extends Application {
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * FinanceData instance
	 */
	private FinanceData financeData = new FinanceData();

	/**
	 * Java FX entry point
	 *
	 * @param stage the Java FX stage
	 * @throws Exception
	 */
	@Override
	public void start(Stage stage) {
		initApplication();
		Parent root;
		FXMLLoader loader;
		try {
			//Load FXML
			loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
			loader.setResources(messages);//NOI18N
			loader.setLocation(getClass().getResource("MainWindow.fxml")); //NOI18N
			root = (Parent) loader.load(); //NOI18N
		} catch (IOException ex) {
			java.util.logging.Logger.getLogger(Launcher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
			return;
		}
		
		//Create scene
		Scene scene = new Scene(root);

		//Set scene properties
		stage.setTitle(messages.getString("MAINWINDOW_TITLE"));
		stage.getIcons().addAll(getIconImages());

		//Set data
		((MainWindowController) loader.getController()).setFinanceData(financeData);

		//Show scene
		stage.setScene(scene);
		//Add graceful shutdown procedure
		stage.setOnCloseRequest(new EventHandler<WindowEvent>(){
			private MainWindowController controller;
			public EventHandler<WindowEvent> setController(MainWindowController controller){
				this.controller = controller;
				return this;
			}
			
			@Override
			public void handle(WindowEvent t) {
				controller.completeTaskThread();
				DatabaseManager.getInstance().shutdown();
			}
		}.setController((MainWindowController) loader.getController()));
		//Show the scene
		stage.show();
	}

	/**
	 * Performs initialization of application's dependencies
	 */
	private void initApplication() {
		/*
		 * Configure logging to load config from classpath
		 */
		String loggingFile = System.getProperty("java.util.logging.config.file"); //NOI18N
		if (loggingFile == null || loggingFile.isEmpty()) {
			try {
				java.net.URL url = ClassLoader.getSystemClassLoader().getResource("logging.properties"); //NOI18N
				if (url != null)
					java.util.logging.LogManager.getLogManager().readConfiguration(url.openStream());
			} catch (IOException | SecurityException e) {
			}
		}
	}

	/**
	 * Returns the list of icons
	 *
	 * @return the list of icons
	 */
	public List<Image> getIconImages() {
		List<Image> images = new LinkedList<>();
		int[] iconSizes = new int[]{16, 24, 32, 48, 64, 128, 256, 512};
		for (int iconName : iconSizes)
			images.add(new Image("org/zlogic/vogon/ui/icon/vogon-tilt-" + Integer.toString(iconName) + ".png"));
		return images;
	}

	/**
	 * The main() method is ignored in correctly deployed JavaFX application.
	 * main() serves only as fallback in case the application can not be
	 * launched through deployment artifacts, e.g., in IDEs with limited FX
	 * support. NetBeans ignores main().
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}
}