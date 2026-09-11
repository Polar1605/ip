package amadeus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import amadeus.parser.Parser;
import amadeus.storage.Storage;
import amadeus.task.Task;
import amadeus.task.TaskDateTime;
import amadeus.ui.Ui;

/**
 * The Amadeus chatbot itself: the task list and the rules for changing it.
 * <p>
 * This class understands what each command should do, and delegates how it is done -
 * parsing to {@link Parser}, saving and loading to {@link Storage}. What it deliberately
 * does <em>not</em> do is decide how a reply reaches the user: {@link #getResponse(String)}
 * hands back the reply as text, so the same logic drives both the console loop in
 * {@link #main(String[])} and the JavaFX window in {@link amadeus.ui.MainWindow}.
 * <p>
 * An {@link AmadeusException} raised by any command is caught here and returned as the
 * reply, so a mistyped command produces a message rather than ending the session.
 */
public class Amadeus {

    private static final int MAX_TASKS = 100;

    /**
     * Where the task list is kept between runs.
     * The folder and the file name are separate so that Storage can join them
     * with whatever separator the current operating system uses, and the path is
     * relative to the folder the app is started from so it works on any computer.
     */
    private static final String DATA_FOLDER = "data";
    private static final String DATA_FILE = "amadeus.txt";

    private final Storage storage;
    private final List<Task> tasks;

    /**
     * Anything worth telling the user about the save file, collected while loading.
     * Kept as a field because loading happens in the constructor but the messages are
     * only shown once the user interface is ready to display them.
     */
    private final List<String> loadMessages = new ArrayList<>();

    /** Whether the user has asked to quit. Read by the window so it knows to close. */
    private boolean isExit = false;

    /**
     * Creates a chatbot whose task list is loaded from the save file.
     * <p>
     * A missing or damaged file is not fatal: the session simply starts with whatever
     * could be read, and the reason is recorded for {@link #getWelcome()} to report.
     */
    public Amadeus() {
        this.storage = new Storage(DATA_FOLDER, DATA_FILE);

        List<Task> loaded = new ArrayList<>();
        try {
            loaded = storage.load();
            loadMessages.addAll(storage.getLoadWarnings());
            if (!loaded.isEmpty()) {
                loadMessages.add("I've loaded " + loaded.size() + " task(s) from your last session.");
            }
        } catch (AmadeusException e) {
            // The list could not be read at all, so the session starts empty rather
            // than stopping; the user is told why.
            loadMessages.add(e.getMessage());
        }
        this.tasks = loaded;
    }

    /**
     * Starts the chatbot in the console and runs it until the user types "bye"
     * or the input ends.
     *
     * @param args command line arguments; not used, as the save file location is fixed.
     */
    public static void main(String[] args) {
        Ui ui = new Ui();
        Amadeus amadeus = new Amadeus();

        ui.showWelcome();
        if (!amadeus.loadMessages.isEmpty()) {
            ui.showReply(String.join("\n", amadeus.loadMessages));
            ui.showLine();
        }

        while (ui.hasNextCommand()) {
            String input = ui.readCommand();
            ui.showLine();
            ui.showReply(amadeus.getResponse(input));
            ui.showLine();

            if (amadeus.isExit()) {
                break;
            }
        }

        ui.close();
    }

    /**
     * Returns the greeting shown before the user has typed anything, including any
     * notes about the save file.
     */
    public String getWelcome() {
        List<String> lines = new ArrayList<>();
        lines.add("Hello, I'm Amadeus.");
        lines.add("Sir, what do you need assistance with?");
        lines.addAll(loadMessages);
        return String.join("\n", lines);
    }

    /**
     * Returns true once the user has entered the "bye" command.
     * <p>
     * The window uses this to close itself, which the console loop does by simply
     * running out of commands.
     */
    public boolean isExit() {
        return isExit;
    }

    /**
     * Runs one command and returns what the chatbot has to say about it.
     * <p>
     * This is the single entry point for both user interfaces. Every way the input can
     * be wrong ends in an {@link AmadeusException}, which is caught here and turned into
     * the reply, so the caller never has to handle failure itself.
     *
     * @param input the whole line the user typed, exactly as typed.
     * @return the reply, possibly several lines separated by {@code \n}.
     */
    public String getResponse(String input) {
        List<String> lines = new ArrayList<>();

        // One try/catch around the whole command covers every way the input can be
        // wrong, so each branch below can assume its input is valid.
        try {
            String commandWord = Parser.parseCommandWord(input);

            switch (commandWord) {
                case "bye":
                    isExit = true;
                    lines.add("Buh bye ");
                    break;

                case "list":
                    lines.add("Here are the " + tasks.size() + " task(s) in your list:");
                    addTaskList(lines, tasks);
                    break;

                case "on": {
                    // Asks every task whether it falls on that day, so todos
                    // (which have no date) simply answer no.
                    LocalDate date = Parser.parseDate(input);
                    List<Task> matches = tasks.stream()
                            .filter(task -> task.occursOn(date))
                            .toList();

                    if (matches.isEmpty()) {
                        lines.add("Nothing is happening on " + TaskDateTime.format(date) + ", sir.");
                    } else {
                        lines.add("Here are the " + matches.size()
                                + " task(s) on " + TaskDateTime.format(date) + ":");
                        addTaskList(lines, matches);
                    }
                    break;
                }

                case "find": {
                    // Asks every task whether its description contains the keyword,
                    // mirroring how "on" asks every task about a date.
                    String keyword = Parser.parseKeyword(input);
                    List<Task> matches = tasks.stream()
                            .filter(task -> task.descriptionContains(keyword))
                            .toList();

                    if (matches.isEmpty()) {
                        lines.add("I found no tasks matching '" + keyword + "', sir.");
                    } else {
                        lines.add("Here are the matching tasks in your list:");
                        addTaskList(lines, matches);
                    }
                    break;
                }

                case "mark": {
                    Task task = tasks.get(Parser.parseTaskIndex(input, tasks.size()));
                    task.markAsDone();
                    save(lines);
                    lines.add("Fantastic! I've marked this task as done:");
                    addTask(lines, task);
                    break;
                }

                case "delete": {
                    int index = Parser.parseTaskIndex(input, tasks.size());
                    Task removed = tasks.get(index);
                    tasks.remove(index);
                    save(lines);
                    lines.add("Fantastic! I've removed this task:");
                    addTask(lines, removed);
                    lines.add("Now you have " + tasks.size() + " task(s) in your list");
                    break;
                }

                case "unmark": {
                    Task task = tasks.get(Parser.parseTaskIndex(input, tasks.size()));
                    task.markAsNotDone();
                    save(lines);
                    lines.add("OK, it has been marked as undone:");
                    addTask(lines, task);
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
                    save(lines);

                    lines.add("Got it added:");
                    addTask(lines, task);
                    lines.add("Now you have " + tasks.size() + " task(s) in your list");
                    break;
                }

                default:
                    throw new AmadeusException("A million apologies, I don't know what '"
                            + commandWord + "' means.");
            }
        } catch (AmadeusException e) {
            // The exception message is written to be read by the user, so it can
            // simply become the reply.
            lines.add(e.getMessage());
        }

        return String.join("\n", lines);
    }

    /**
     * Appends one task on its own indented line, the way it appears in confirmation
     * messages such as "Got it added:".
     */
    private void addTask(List<String> lines, Task task) {
        lines.add("   " + task);
    }

    /**
     * Appends tasks as a numbered list starting at 1, because that is the number the
     * user types back in commands such as "mark 2".
     */
    private void addTaskList(List<String> lines, List<Task> tasksToShow) {
        for (int i = 0; i < tasksToShow.size(); i++) {
            lines.add((i + 1) + "." + tasksToShow.get(i));
        }
    }

    /**
     * Writes the current list to disk, appending a note if that failed.
     * <p>
     * Called after every command that changes the list, so the saved file is never out
     * of date. A failure is reported but not thrown any further: the change the user
     * just made is still valid in memory, so there is no reason to end the session
     * over it.
     */
    private void save(List<String> lines) {
        try {
            storage.save(tasks);
        } catch (AmadeusException e) {
            lines.add(e.getMessage());
        }
    }
}
