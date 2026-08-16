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

    /**
     * Converts the text typed after a "mark"/"unmark" command into an array index.
     * Prints an explanation and returns -1 when the text is not a usable task number,
     * so that the caller can simply skip the command instead of crashing.
     *
     * @param argument  the text following the command word, e.g. "2"
     * @param taskCount how many tasks are currently stored
     * @return the 0-based index of the requested task, or -1 if there isn't one
     */
    private static int parseTaskIndex(String argument, int taskCount) {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument.trim());
        } catch (NumberFormatException e) {
            // parseInt reports bad input by throwing rather than by returning a
            // value, so this has to be caught rather than tested with an if.
            System.out.println(" Sorry Sir, '" + argument.trim() + "' is not a task number.");
            return -1;
        }

        // Task numbers shown to the user start at 1 and stop at the number stored,
        // so anything outside that range would fall off the end of the array.
        if (taskNumber < 1 || taskNumber > taskCount) {
            System.out.println(" Sorry Sir, I only have " + taskCount + " task(s).");
            return -1;
        }

        return taskNumber - 1;
    }

    public static void main(String[] args) {
        System.out.println(DIVIDER);
        System.out.println(BANNER);
        System.out.println("Hello I'm " + NAME + ".");
        System.out.println("Sir what do you need assistance with");
        System.out.println(DIVIDER);

        // Tasks entered so far. A plain array cannot report how much of it is in
        // use (tasks.length is always MAX_TASKS), so taskCount tracks that separately
        // and doubles as the index of the next free slot.
        Task[] tasks = new Task[MAX_TASKS];
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
                System.out.println(" Here are the tasks in your list:");
                // Slots from taskCount onwards are still null, so stop there rather
                // than walking the whole array.
                for (int i = 0; i < taskCount; i++) {
                    // Tasks are numbered from 1 for the user, but stored from index 0.
                    // Task.toString() supplies the "[X] description" part.
                    System.out.println(" " + (i + 1) + "." + tasks[i]);
                }
                System.out.println(DIVIDER);
                continue;
            }

            if (input.startsWith("mark ")) {
                int index = parseTaskIndex(input.substring(5), taskCount);
                if (index == -1) {
                    System.out.println(DIVIDER);
                    continue;
                }
                Task task = tasks[index];
                task.markAsDone();
                System.out.println(" Nice! I've marked this task as done:");
                System.out.println("   " + task);
                System.out.println(DIVIDER);
                continue;
            }

            if (input.startsWith("unmark ")) {
                int index = parseTaskIndex(input.substring(7), taskCount);
                if (index == -1) {
                    System.out.println(DIVIDER);
                    continue;
                }
                Task task = tasks[index];
                task.markAsNotDone();
                System.out.println(" OK, I've marked this task as not done yet:");
                System.out.println("   " + task);
                System.out.println(DIVIDER);
                continue;
            }

            if (taskCount == MAX_TASKS) {
                System.out.println(" My list is full, I cannot remember any more.");
                System.out.println(DIVIDER);
                continue;
            }


            tasks[taskCount] = new Task(input);
            taskCount++;
            System.out.println(" added: " + input);
            System.out.println(DIVIDER);
        }

        scanner.close();
    }
}
