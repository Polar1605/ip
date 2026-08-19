/**
 * Entry point of the Amadeus chatbot.
 * The bot greets the user, then repeatedly reads a line of input and either
 * stores it as a task, lists everything stored so far, or exits.
 * <p>
 * Understanding and validating the input is delegated to {@link Parser}; this
 * class only decides what to do once the input has been understood.
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

    /** Prints a single task indented, the way it appears in confirmation messages. */
    private static void printTask(Task task) {
        System.out.println("    " + task);
    }

    public static void main(String[] args) {
        System.out.println(DIVIDER);
        System.out.println(BANNER);
        System.out.println("Hello I'm " + NAME + ".");
        System.out.println("Sir what do you need assistance with");
        System.out.println(DIVIDER);

        // Tasks entered
        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;

        Scanner scanner = new Scanner(System.in);
        // hasNextLine() stops the loop cleanly if the input runs out before "bye",
        // which is what happens when input is piped in from a file.
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            System.out.println(DIVIDER);

            // One try/catch around the whole command covers every way the input
            // can be wrong, so each branch below can assume its input is valid.
            try {
                String commandWord = Parser.parseCommandWord(input);

                switch (commandWord) {
                case "bye":
                    System.out.println(" Buh bye ");
                    System.out.println(DIVIDER);
                    scanner.close();
                    return;

                case "list":
                    System.out.println(" Here are the " + taskCount + " task(s) in your list:");
                    for (int i = 0; i < taskCount; i++) {
                        System.out.println(" " + (i + 1) + "." + tasks[i]);
                    }
                    break;

                case "mark": {
                    Task task = tasks[Parser.parseTaskIndex(input, taskCount)];
                    task.markAsDone();
                    System.out.println(" Fantastic! I've marked this task as done:");
                    printTask(task);
                    break;
                }

                case "unmark": {
                    Task task = tasks[Parser.parseTaskIndex(input, taskCount)];
                    task.markAsNotDone();
                    System.out.println(" OK, it has been marked as undone:");
                    printTask(task);
                    break;
                }

                case "todo":
                case "deadline":
                case "event": {
                    if (taskCount == MAX_TASKS) {
                        throw new AmadeusException("My list is full, a thousand apologies.");
                    }

                    // The parser builds the right kind of Task and throws if the
                    // line is malformed, so nothing is stored on a bad command.
                    Task task;
                    if (commandWord.equals("todo")) {
                        task = Parser.parseTodo(input);
                    } else if (commandWord.equals("deadline")) {
                        task = Parser.parseDeadline(input);
                    } else {
                        task = Parser.parseEvent(input);
                    }

                    tasks[taskCount] = task;
                    taskCount++;

                    System.out.println(" Got it added:");
                    printTask(task);
                    System.out.println(" Now you have " + taskCount + " task(s) in your list");
                    break;
                }

                default:
                    throw new AmadeusException("A million apologies, I don't know what '"
                            + commandWord + "' means.");
                }
            } catch (AmadeusException e) {
                // The exception message is written to be read by the user, so it
                // can simply be printed as the bot's reply.
                System.out.println(" " + e.getMessage());
            }

            System.out.println(DIVIDER);
        }

        scanner.close();
    }
}
