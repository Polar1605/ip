package amadeus.ui;

import java.io.IOException;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * One message in the conversation: an avatar beside the text that was said.
 * <p>
 * The layout lives in {@code DialogBox.fxml}, which uses the {@code fx:root} construct:
 * instead of the FXML creating the box, this class loads the FXML into <em>itself</em>.
 * That is what lets a dialog box be created with {@code new} as many times as there are
 * messages, rather than existing once like the main window does.
 * <p>
 * The constructor is private because the two factory methods below say something the
 * constructor cannot: which side of the conversation the message came from.
 */
public class DialogBox extends HBox {

    @FXML
    private Label dialog;
    @FXML
    private ImageView displayPicture;

    /**
     * Creates a dialog box showing the given text next to the given avatar.
     *
     * @param text what was said.
     * @param img the speaker's avatar, or null for no avatar.
     */
    private DialogBox(String text, Image img) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(DialogBox.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            // The FXML is packaged inside the JAR, so this means the build is broken
            // rather than anything the user did.
            System.err.println("Could not load a dialog box: " + e.getMessage());
        }

        // A successful load() injects both @FXML fields by reflection before returning.
        // If it failed instead, the catch above already reported why, so this documents
        // that dependency rather than silently trusting it: with assertions enabled it
        // fails loudly right here instead of as a confusing NullPointerException on the
        // next line.
        assert dialog != null && displayPicture != null
                : "FXMLLoader.load() should have injected the @FXML fields";
        dialog.setText(text);
        displayPicture.setImage(img);
    }

    /**
     * Returns a dialog box for something the user said, with the avatar on the right.
     *
     * @param text what the user typed.
     * @param img the user's avatar.
     * @return a dialog box ready to be added to the conversation.
     */
    public static DialogBox getUserDialog(String text, Image img) {
        return new DialogBox(text, img);
    }

    /**
     * Returns a dialog box for something the chatbot said, mirrored so that its avatar
     * sits on the left. The two sides of the conversation are told apart by that
     * mirroring alone, which is why no extra styling is needed.
     *
     * @param text what the chatbot replied.
     * @param img the chatbot's avatar.
     * @return a dialog box ready to be added to the conversation.
     */
    public static DialogBox getAmadeusDialog(String text, Image img) {
        DialogBox box = new DialogBox(text, img);
        box.flip();
        return box;
    }

    /** Reverses the order of the avatar and the text, and aligns the box to the left. */
    private void flip() {
        ObservableList<Node> children = FXCollections.observableArrayList(this.getChildren());
        Collections.reverse(children);
        getChildren().setAll(children);
        setAlignment(Pos.TOP_LEFT);
    }
}
