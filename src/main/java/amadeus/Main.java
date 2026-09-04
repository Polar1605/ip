package amadeus;

import java.io.IOException;

import amadeus.ui.MainWindow;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * The JavaFX application: builds the window and puts a chatbot behind it.
 * <p>
 * The window's appearance is described in {@code MainWindow.fxml} rather than in code,
 * so this class only has to load that file, hand the resulting controller an
 * {@link Amadeus} to talk to, and show the stage.
 */
public class Main extends Application {

    private final Amadeus amadeus = new Amadeus();

    /**
     * Builds and shows the window.
     *
     * @param stage the window JavaFX has created for us to fill.
     */
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            AnchorPane root = fxmlLoader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Amadeus");
            stage.setMinHeight(220);
            stage.setMinWidth(417);

            // The controller only exists once the FXML has been loaded, which is why the
            // chatbot is injected here rather than passed to a constructor.
            fxmlLoader.<MainWindow>getController().setAmadeus(amadeus);
            stage.show();
        } catch (IOException e) {
            // The FXML is packaged inside the JAR, so failing to read it means the build
            // is broken rather than anything the user can act on. There is no window to
            // report it in, so the console is the only place left.
            System.err.println("Could not load the main window: " + e.getMessage());
        }
    }
}
