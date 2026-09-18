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
import javafx.scene.layout.Priority;

/**
 * One message in the conversation: an avatar beside the text that was said.
 * <p>
 * The layout lives in {@code DialogBox.fxml}, which uses the {@code fx:root} construct:
 * instead of the FXML creating the box, this class loads the FXML into <em>itself</em>.
 * That is what lets a dialog box be created with {@code new} as many times as there are
 * messages, rather than existing once like the main window does.
 * <p>
 * The conversation is not between two people: it is between a person and a program, and
 * the three factory methods below give each kind of message a shape that matches what it
 * is. What the user typed is a short, solid gold bubble pushed to the right; what the
 * chatbot replied is a white textbox of its own, outlined rather than filled, spanning
 * the width because replies are the long ones; an error is that same white-vs-filled
 * distinction inverted - a red outline around a red-tinted fill - so a mistyped command
 * is obvious before it has been read.
 * <p>
 * The constructor is private because those three methods say something it cannot: which
 * kind of message this is.
 */
public class DialogBox extends HBox {

    /**
     * How much of the window's width the user's bubble may take before it wraps.
     * Short of the full width so that a bubble always reads as one side of a
     * conversation rather than as another full-width paragraph.
     */
    private static final double USER_MAX_WIDTH_FRACTION = 0.82;

    /**
     * Warning sign (U+26A0) shown at the start of an error, and the gap after it.
     * <p>
     * Built from its code point rather than typed as the character itself so that this
     * file stays pure ASCII: a literal warning sign would be stored as bytes that only
     * read back correctly if the compiler is told the file is UTF-8, which is a thing
     * builds get wrong silently on one machine and not another.
     */
    private static final String ERROR_PREFIX = Character.toString(0x26a0) + "  ";

    @FXML
    private Label dialog;
    @FXML
    private ImageView displayPicture;

    /**
     * Creates a dialog box showing the given text next to the given avatar.
     *
     * @param text the message text.
     * @param img the speaker's avatar, or null for no avatar.
     * @param styleClass the CSS class in {@code amadeus.css} that colours this kind of message.
     */
    private DialogBox(String text, Image img, String styleClass) {
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
        dialog.getStyleClass().add(styleClass);
        displayPicture.setImage(img);
    }

    /**
     * Returns a dialog box for something the user said: a bubble aligned to the right,
     * with the avatar mirrored to sit on its outer edge.
     *
     * @param text what the user typed.
     * @param img the user's avatar.
     * @return a dialog box ready to be added to the conversation.
     */
    public static DialogBox getUserDialog(String text, Image img) {
        DialogBox box = new DialogBox(text, img, "message-user");
        box.setAlignment(Pos.TOP_RIGHT);
        // Binding rather than setting a fixed number keeps the limit correct while the
        // window is being resized, which a fixed number could not do.
        box.dialog.maxWidthProperty().bind(box.widthProperty().multiply(USER_MAX_WIDTH_FRACTION));
        box.flip();
        return box;
    }

    /**
     * Returns a dialog box for something the chatbot said: a white textbox outlined in
     * the accent colour, spanning the width, with the avatar leading it.
     *
     * @param text what the chatbot replied.
     * @param img the chatbot's avatar.
     * @return a dialog box ready to be added to the conversation.
     */
    public static DialogBox getAmadeusDialog(String text, Image img) {
        return spanningBox(text, img, "message-bot");
    }

    /**
     * Returns a dialog box for a reply reporting that something went wrong, laid out like
     * any other reply from the chatbot but outlined and tinted red and opened with a
     * warning sign.
     *
     * @param text what the chatbot said about the problem.
     * @param img the chatbot's avatar.
     * @return a dialog box ready to be added to the conversation.
     */
    public static DialogBox getErrorDialog(String text, Image img) {
        return spanningBox(ERROR_PREFIX + text, img, "message-error");
    }

    /**
     * Returns a left-aligned box, avatar first, whose label is allowed to grow into the
     * whole remaining width.
     * <p>
     * Both kinds of reply from the chatbot want exactly this, so it is written once here.
     * <p>
     * Both grow calls are needed: {@code setHgrow} offers the label the leftover width,
     * and lifting {@code maxWidth} lets it actually take it - a label will not grow past
     * the width of its own text otherwise. Without the pair, a short error would be a stub
     * of red floating next to the avatar instead of a box spanning the row, and would
     * change width from one message to the next.
     */
    private static DialogBox spanningBox(String text, Image img, String styleClass) {
        DialogBox box = new DialogBox(text, img, styleClass);
        box.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(box.dialog, Priority.ALWAYS);
        box.dialog.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    /** Reverses the order of the avatar and the text, so the avatar ends up on the right. */
    private void flip() {
        ObservableList<Node> children = FXCollections.observableArrayList(this.getChildren());
        Collections.reverse(children);
        getChildren().setAll(children);
    }
}
