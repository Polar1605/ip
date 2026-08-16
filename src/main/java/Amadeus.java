/**
 * Entry point of the Amadeus chatbot.
 * The bot greets the user, then repeatedly reads a line of input and either
 * stores it as a task, lists everything stored so far, or exits.
 */
import java.util.Scanner;
public class Amadeus {
    /** Horizontal rule used to separate the chatbot's messages from the rest of the output. */
    private static final String DIVIDER = "____________________________________________________________";

    /** Name the chatbot introduces itself with. */
    private static final String NAME = "Amadeus";

    /** Largest number of tasks the bot can remember, as set by the level requirements. */
    private static final int MAX_TASKS = 100;

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
        System.out.println("Hello I'm " + NAME + ".");
        System.out.println("Sir what do you need assistance with");
        System.out.println(DIVIDER);

        // Tasks entered so far. A plain array cannot report how much of it is in
        // use (tasks.length is always MAX_TASKS), so taskCount tracks that separately
        // and doubles as the index of the next free slot.
        String[] tasks = new String[MAX_TASKS];
        int taskCount = 0;

        Scanner scanner = new Scanner(System.in);
        while (true) {
            // scanner waits for user input
            String input = scanner.nextLine();
            System.out.println(DIVIDER);

            if (input.equals("bye")) {
                System.out.println(" Bye bye ");
                System.out.println(DIVIDER);
                break;
            }

            if (input.equals("list")) {
                // Slots from taskCount onwards are still null, so stop there rather
                // than walking the whole array.
                for (int i = 0; i < taskCount; i++) {
                    // Tasks are numbered from 1 for the user, but stored from index 0.
                    System.out.println(" " + (i + 1) + ". " + tasks[i]);
                }
                System.out.println(DIVIDER);
                continue;
            }

            if (taskCount == MAX_TASKS) {
                System.out.println(" My list is full, I cannot remember any more.");
                System.out.println(DIVIDER);
                continue;
            }

            tasks[taskCount] = input;
            taskCount++;
            System.out.println(" added: " + input);
            System.out.println(DIVIDER);
        }

        scanner.close();
    }
}
