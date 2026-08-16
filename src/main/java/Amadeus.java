/**
 * Entry point of the Amadeus chatbot.
 * At this level the bot only greets the user and exits immediately.
 */
import java.util.Scanner;
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
        System.out.println("I'm " + NAME + ".");
        System.out.println("What do you want");
        System.out.println(DIVIDER);

        // No input is read yet, so the bot says goodbye right away and exits.
        Scanner scanner = new Scanner(System.in);
        while (true) {
            // scanner waits for user input
            String input = scanner.nextLine();
            System.out.println("____________________________________________________________");

            if (input.equals("bye")) {
                System.out.println(" Buh bye ");
                System.out.println("____________________________________________________________");
                break;
            }

            System.out.println(" " + input);
            System.out.println("____________________________________________________________");
        }

        scanner.close();
    }
}
