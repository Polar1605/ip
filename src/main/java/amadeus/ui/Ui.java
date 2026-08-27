package amadeus.ui;

import java.util.List;
import java.util.Scanner;

import amadeus.task.Task;

/**
 * Everything the chatbot says and everything the user types.
 * <p>
 * This class is the only place in the app that touches {@code System.in} and
 * {@code System.out}. Keeping input and output in one class means the rest of
 * the code can decide <em>what</em> to say without also deciding <em>how</em>
 * it reaches the user, so swapping the console for, say, a window later would
 * only change this file.
 * <p>
 * Every message the bot speaks is printed with a single leading space, which is
 * what visually sets the bot's voice apart from the line the user just typed.
 * That space is added here rather than by each caller, so no caller can forget it.
 */
public class Ui {

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

    /** Reads the user's commands. Owned by Ui so nothing else has to know about System.in. */
    private final Scanner scanner;

    /** Creates a Ui that reads the user's commands from standard input. */
    public Ui() {
        this.scanner = new Scanner(System.in);
    }

    /** Prints the banner and greeting shown when the app starts. */
    public void showWelcome() {
        showLine();
        System.out.println(BANNER);
        System.out.println("Hello I'm " + NAME + ".");
        System.out.println("Sir what do you need assistance with");
        showLine();
    }

    /**
     * Returns true if there is another command to read.
     * <p>
     * Asked before every {@link #readCommand()} so the loop ends cleanly when the
     * input runs out before "bye", which is what happens when input is piped in
     * from a file.
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /** Returns the next line the user typed, exactly as typed. */
    public String readCommand() {
        return scanner.nextLine();
    }

    /** Prints the horizontal rule that brackets each reply. */
    public void showLine() {
        System.out.println(DIVIDER);
    }

    /** Prints one line in the bot's voice. */
    public void show(String message) {
        System.out.println(" " + message);
    }

    /**
     * Prints a message describing something that went wrong.
     * <p>
     * Identical to {@link #show(String)} today; it exists as its own method so
     * that error output can later be styled differently (coloured, say) without
     * hunting down which calls were errors.
     */
    public void showError(String message) {
        show(message);
    }

    /** Prints a single task indented, the way it appears in confirmation messages. */
    public void showTask(Task task) {
        System.out.println("    " + task);
    }

    /**
     * Prints tasks as a numbered list starting at 1, because that is the number
     * the user types back in commands such as "mark 2".
     */
    public void showTaskList(List<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println(" " + (i + 1) + "." + tasks.get(i));
        }
    }

    /** Prints the farewell shown when the user types "bye". */
    public void showGoodbye() {
        show("Buh bye ");
    }

    /** Releases the input source. Called once, when the app is about to end. */
    public void close() {
        scanner.close();
    }
}
