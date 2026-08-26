
import java.time.LocalDate;
import java.util.*;
public class Amadeus {
    /** Horizontal rule used to separate the chatbot's messages from the rest of the output. */
    private static final String DIVIDER = "____________________________________________________________";

    private static final int MAX_TASKS = 100;

    /**
     * Where the task list is kept between runs.
     * The folder and the file name are separate so that Storage can join them
     * with whatever separator the current operating system uses, and the path is
     * relative to the folder the app is started from so it works on any computer.
     */
    private static final String DATA_FOLDER = "data";
    private static final String DATA_FILE = "amadeus.txt";

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

    /** Prints a single task indented, the way it appears in confirmation messages. */
    private static void printTask(Task task) {
        System.out.println("    " + task);
    }

    /**
     * Writes the current list to disk and tells the user if that failed.
     * <p>
     * Called after every command that changes the list, so the saved file is
     * never out of date. A failure is reported but not thrown any further: the
     * change the user just made is still valid in memory, so there is no reason
     * to end the session over it.
     */
    private static void save(Storage storage, List<Task> tasks) {
        try {
            storage.save(tasks);
        } catch (AmadeusException e) {
            System.out.println(" " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println(DIVIDER);
        System.out.println(BANNER);
        System.out.println("Hello I'm " + NAME + ".");
        System.out.println("Sir what do you need assistance with");
        System.out.println(DIVIDER);

        // Tasks entered. Loaded from disk so the list survives between runs;
        // if there is no saved file yet, load() simply hands back an empty list.
        Storage storage = new Storage(DATA_FOLDER, DATA_FILE);
        List<Task> tasks = new ArrayList<>();

        // The flag keeps the greeting tidy: the divider below is only printed when
        // there was actually something to say about the saved file. A first-time
        // user, whose file does not exist yet, sees nothing extra at all.
        boolean hasLoadMessage = false;
        try {
            tasks = storage.load();
            for (String warning : storage.getLoadWarnings()) {
                System.out.println(" " + warning);
                hasLoadMessage = true;
            }
            if (!tasks.isEmpty()) {
                System.out.println(" I've loaded " + tasks.size() + " task(s) from your last session.");
                hasLoadMessage = true;
            }
        } catch (AmadeusException e) {
            // The list could not be read at all, so the session starts empty
            // rather than stopping; the user is told why.
            System.out.println(" " + e.getMessage());
            hasLoadMessage = true;
        }
        if (hasLoadMessage) {
            System.out.println(DIVIDER);
        }

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
                    System.out.println(" Here are the " + tasks.size() + " task(s) in your list:");
                    for (int i = 0; i < tasks.size(); i++) {
                        System.out.println(" " + (i + 1) + "." + tasks.get(i));
                    }
                    break;

                case "on": {
                    // Asks every task whether it falls on that day, so todos
                    // (which have no date) simply answer no.
                    LocalDate date = Parser.parseDate(input);
                    List<Task> matches = new ArrayList<>();
                    for (Task task : tasks) {
                        if (task.occursOn(date)) {
                            matches.add(task);
                        }
                    }

                    if (matches.isEmpty()) {
                        System.out.println(" Nothing is happening on "
                                + TaskDateTime.format(date) + ", sir.");
                    } else {
                        System.out.println(" Here are the " + matches.size()
                                + " task(s) on " + TaskDateTime.format(date) + ":");
                        for (int i = 0; i < matches.size(); i++) {
                            System.out.println(" " + (i + 1) + "." + matches.get(i));
                        }
                    }
                    break;
                }

                case "mark": {
                    Task task = tasks.get(Parser.parseTaskIndex(input, tasks.size()));
                    task.markAsDone();
                    save(storage, tasks);
                    System.out.println(" Fantastic! I've marked this task as done:");
                    printTask(task);
                    break;
                }

                case "delete": {
                    int index = Parser.parseTaskIndex(input, tasks.size());
                    Task tmp = tasks.get(index);
                    tasks.remove(index);
                    save(storage, tasks);
                    System.out.println(" Fantastic! I've removed this task:");
                    printTask(tmp);
                    System.out.println(" Now you have " + tasks.size() + " task(s) in your list");
                    break;
                }

                case "unmark": {
                    Task task = tasks.get(Parser.parseTaskIndex(input, tasks.size()));
                    task.markAsNotDone();
                    save(storage, tasks);
                    System.out.println(" OK, it has been marked as undone:");
                    printTask(task);
                    break;
                }

                case "todo":
                case "deadline":
                case "event": {
                    if (tasks.size() == MAX_TASKS) {
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

                    tasks.add(task);
                    save(storage, tasks);

                    System.out.println(" Got it added:");
                    printTask(task);
                    System.out.println(" Now you have " + tasks.size() + " task(s) in your list");
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
