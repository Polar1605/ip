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
                for (int i = 0; i < taskCount; i++) {
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
                System.out.println(" Good job! I've marked this task as done:");
                Amadeus.printTask(task);
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
                System.out.println(" OK, marked as undone:");
                Amadeus.printTask(task);
                System.out.println(DIVIDER);
                continue;
            }

            if (taskCount == MAX_TASKS) {
                System.out.println(" My list is full.");
                System.out.println(DIVIDER);
                continue;
            }

            if(input.startsWith("todo")) {
                //trim away the todo to get the description after
                String description = input.substring("todo ".length()).trim();
                tasks[taskCount] = new Todo(description);
                taskCount++;
                System.out.println(" Got it added: " + input);
                Amadeus.printTask(tasks[taskCount - 1]);
                System.out.println(" Now you have " + taskCount + " in your list");
                System.out.println(DIVIDER);
                continue;
            }

            if (input.startsWith("deadline ")) {
                // A deadline is written as: deadline <description> /by <time>
                int byIdx = input.indexOf("/by");

                if (byIdx == -1) {
                    System.out.println(" Sorry Sir, please use: deadline <description> /by <time>");
                    System.out.println(DIVIDER);
                    continue;
                }

                String description = input.substring("deadline ".length(), byIdx).trim();
                String by = input.substring(byIdx + "/by".length()).trim();

                tasks[taskCount] = new Deadline(description, by);
                taskCount++;

                System.out.println(" Got it added:");
                // taskCount has already moved on to the next free slot, so the task
                // just stored is at taskCount - 1.
                Amadeus.printTask(tasks[taskCount - 1]);
                System.out.println(" Now you have " + taskCount + " task(s) in your list");
                System.out.println(DIVIDER);
                continue;
            }

            if (input.startsWith("event ")) {
                // A deadline is written as: deadline <description> /by <time>
                int fromIdx = input.indexOf("/from");
                int toIdx = input.indexOf("/to");
                if (fromIdx == -1 || toIdx == -1) {
                    System.out.println(" Sorry Sir, please use: event <description> /from <time> /to <time>");
                    System.out.println(DIVIDER);
                    continue;
                }

                // The description sits between the command word and "/by",
                // and the due date is everything after "/by".
                String description = input.substring("deadline ".length(), fromIdx).trim();
                String from = input.substring(fromIdx + "/from".length(), toIdx).trim();
                String to = input.substring(toIdx + "/to".length()).trim();

                tasks[taskCount] = new Event(description, from, to);
                taskCount++;

                System.out.println(" Got it added:");
                // taskCount has already moved on to the next free slot, so the task
                // just stored is at taskCount - 1.
                Amadeus.printTask(tasks[taskCount - 1]);
                System.out.println(" Now you have " + taskCount + " task(s) in your list");
                System.out.println(DIVIDER);
                continue;
            }
        }

        scanner.close();
    }
}
