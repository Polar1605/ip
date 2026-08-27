package amadeus;

/**
 * Signals that the chatbot could not carry out what the user asked.
 * <p>
 * Every message carried by this exception is written for the user to read, so a
 * caller can print {@link #getMessage()} directly instead of translating an
 * error code into words. It extends {@code Exception} rather than
 * {@code RuntimeException} so that the compiler forces each caller to decide
 * what to do about it, which is what keeps a bad command from ending the session.
 */
public class AmadeusException extends Exception {

    /**
     * Creates an exception carrying a message meant to be shown to the user.
     *
     * @param message what went wrong, phrased for the user rather than for a developer.
     */
    public AmadeusException(String message) {
        super(message);
    }
}
