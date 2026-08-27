package amadeus;

import amadeus.parser.Parser;
import amadeus.storage.Storage;
import amadeus.task.Task;
import amadeus.task.TaskDateTime;
import amadeus.ui.Ui;

import java.time.LocalDate;
import java.util.*;
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

    /**
     * Writes the current list to disk and tells the user if that failed.
     * <p>
     * Called after every command that changes the list, so the saved file is
     * never out of date. A failure is reported but not thrown any further: the
     * change the user just made is still valid in memory, so there is no reason
     * to end the session over it.
     */
    private static void save(Storage storage, List<Task> tasks, Ui ui) {
        try {
            storage.save(tasks);
        } catch (AmadeusException e) {
            ui.showError(e.getMessage());
        }
    }

    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();

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
                ui.show(warning);
                hasLoadMessage = true;
            }
            if (!tasks.isEmpty()) {
                ui.show("I've loaded " + tasks.size() + " task(s) from your last session.");
                hasLoadMessage = true;
            }
        } catch (AmadeusException e) {
            // The list could not be read at all, so the session starts empty
            // rather than stopping; the user is told why.
            ui.showError(e.getMessage());
            hasLoadMessage = true;
        }
        if (hasLoadMessage) {
            ui.showLine();
        }

        while (ui.hasNextCommand()) {
            String input = ui.readCommand();
            ui.showLine();

            // One try/catch around the whole command covers every way the input
            // can be wrong, so each branch below can assume its input is valid.
            try {
                String commandWord = Parser.parseCommandWord(input);

                switch (commandWord) {
                case "bye":
                    ui.showGoodbye();
                    ui.showLine();
                    ui.close();
                    return;

                case "list":
                    ui.show("Here are the " + tasks.size() + " task(s) in your list:");
                    ui.showTaskList(tasks);
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
                        ui.show("Nothing is happening on " + TaskDateTime.format(date) + ", sir.");
                    } else {
                        ui.show("Here are the " + matches.size()
                                + " task(s) on " + TaskDateTime.format(date) + ":");
                        ui.showTaskList(matches);
                    }
                    break;
                }

                case "mark": {
                    Task task = tasks.get(Parser.parseTaskIndex(input, tasks.size()));
                    task.markAsDone();
                    save(storage, tasks, ui);
                    ui.show("Fantastic! I've marked this task as done:");
                    ui.showTask(task);
                    break;
                }

                case "delete": {
                    int index = Parser.parseTaskIndex(input, tasks.size());
                    Task tmp = tasks.get(index);
                    tasks.remove(index);
                    save(storage, tasks, ui);
                    ui.show("Fantastic! I've removed this task:");
                    ui.showTask(tmp);
                    ui.show("Now you have " + tasks.size() + " task(s) in your list");
                    break;
                }

                case "unmark": {
                    Task task = tasks.get(Parser.parseTaskIndex(input, tasks.size()));
                    task.markAsNotDone();
                    save(storage, tasks, ui);
                    ui.show("OK, it has been marked as undone:");
                    ui.showTask(task);
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
                    save(storage, tasks, ui);

                    ui.show("Got it added:");
                    ui.showTask(task);
                    ui.show("Now you have " + tasks.size() + " task(s) in your list");
                    break;
                }

                default:
                    throw new AmadeusException("A million apologies, I don't know what '"
                            + commandWord + "' means.");
                }
            } catch (AmadeusException e) {
                // The exception message is written to be read by the user, so it
                // can simply be printed as the bot's reply.
                ui.showError(e.getMessage());
            }

            ui.showLine();
        }

        ui.close();
    }
}
