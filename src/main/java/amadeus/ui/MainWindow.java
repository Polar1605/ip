package amadeus.ui;

import amadeus.Amadeus;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controller for the main window: turns what the user types into dialog boxes.
 * <p>
 * The controls themselves are described in {@code MainWindow.fxml}; JavaFX creates them
 * and drops them into the {@code @FXML} fields below, matching each field to the
 * {@code fx:id} of the same name. This class only reacts to what the user does with
 * them, and asks {@link Amadeus} what to reply.
 */
public class MainWindow extends AnchorPane {

    /** How long the goodbye stays on screen before the window closes. */
    private static final Duration EXIT_DELAY = Duration.seconds(1.5);

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private Amadeus amadeus;

    private final Image userImage = loadImage("/images/DaUser.png");
    private final Image amadeusImage = loadImage("/images/DaAmadeus.png");

    /**
     * Prepares the window once JavaFX has finished building it.
     * <p>
     * Called automatically after the FXML is loaded, which is the earliest point at
     * which the {@code @FXML} fields above are guaranteed to exist - so anything that
     * touches a control belongs here rather than in a constructor.
     */
    @FXML
    public void initialize() {
        // Tying the scroll position to the container's height keeps the newest message
        // in view: every time the container grows, the pane scrolls to the bottom.
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    /**
     * Gives this window the chatbot it should talk to, and shows its greeting.
     *
     * @param amadeus the chatbot answering the user's commands.
     */
    public void setAmadeus(Amadeus amadeus) {
        this.amadeus = amadeus;
        dialogContainer.getChildren().add(
                DialogBox.getAmadeusDialog(amadeus.getWelcome(), amadeusImage));
    }

    /**
     * Loads a bundled image, or returns null if it is missing.
     * <p>
     * A missing avatar is not worth stopping the app for: an {@link javafx.scene.image.ImageView}
     * with no image simply draws nothing, so the conversation still works.
     */
    private static Image loadImage(String path) {
        var stream = MainWindow.class.getResourceAsStream(path);
        return stream == null ? null : new Image(stream);
    }

    /**
     * Shows the user's message and the chatbot's reply, then clears the input box.
     * <p>
     * Wired to both the Send button and the Enter key by {@code onAction} in the FXML,
     * so the two behave identically.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        if (input.isBlank()) {
            return;
        }

        String response = amadeus.getResponse(input);
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input, userImage),
                DialogBox.getAmadeusDialog(response, amadeusImage));
        userInput.clear();

        if (amadeus.isExit()) {
            // Further input would be pointless once the user has said goodbye, and the
            // pause gives them time to read the farewell before the window disappears.
            userInput.setDisable(true);
            sendButton.setDisable(true);

            PauseTransition pause = new PauseTransition(EXIT_DELAY);
            pause.setOnFinished(event -> Platform.exit());
            pause.play();
        }
    }
}
