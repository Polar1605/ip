/**
 * Entry point of the Amadeus chatbot.
 * At this level the bot only greets the user and exits immediately.
 */
public class Amadeus {
    /** Horizontal rule used to separate the chatbot's messages from the rest of the output. */
    private static final String DIVIDER = "____________________________________________________________";

    /** Name the chatbot introduces itself with. */
    private static final String NAME = "Amadeus";

    /**
     * ASCII-art banner shown on startup.
     * Each backslash is written as "\\" because a lone backslash starts an escape
     * sequence in a Java string literal.
     */
    private static final String BANNER =
              "     _     __  __      _     ____   _____  _   _  ____  \n"
            + "    / \\   |  \\/  |    / \\   |  _ \\ | ____|| | | |/ ___| \n"
            + "   / _ \\  | |\\/| |   / _ \\  | | | ||  _|  | | | |\\___ \\ \n"
            + "  / ___ \\ | |  | |  / ___ \\ | |_| || |___ | |_| | ___) |\n"
            + " /_/   \\_\\|_|  |_| /_/   \\_\\|____/ |_____| \\___/ |____/ ";

    public static void main(String[] args) {
        System.out.println(DIVIDER);
        System.out.println(BANNER);
        System.out.println("Hello! I'm " + NAME + ".");
        System.out.println("What can I do for you?");
        System.out.println(DIVIDER);

        // No input is read yet, so the bot says goodbye right away and exits.
        System.out.println("Bye. Hope to see you again soon!");
        System.out.println(DIVIDER);
    }
}
