package amadeus.storage;

import amadeus.AmadeusException;
import amadeus.task.Deadline;
import amadeus.task.Event;
import amadeus.task.Task;
import amadeus.task.TaskDateTime;
import amadeus.task.Todo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the task list from disk when the chatbot starts and writes it back
 * whenever the list changes.
 * <p>
 * Each task is stored on its own line as pipe-separated fields, for example:
 * <pre>
 * T | 1 | read book
 * D | 0 | return book | 2019-06-06
 * E | 0 | project meeting | 2019-08-06 1400 | 2019-08-06 1600
 * </pre>
 * The first field is the task type, the second is 1 for done and 0 for not
 * done, and the rest are the task's own fields. Dates are written in the
 * machine-friendly form TaskDateTime accepts, not the form shown to the user,
 * so that loading is the exact reverse of saving.
 * <p>
 * Note: because "|" separates the fields, a description that itself contains a
 * "|" cannot be read back correctly. Such a line is treated as corrupted and
 * skipped rather than silently misread.
 */
public class Storage {

    /** Separator written between fields. Kept next to the parsing pattern below. */
    private static final String FIELD_SEPARATOR = " | ";

    /**
     * Pattern used to split a saved line back into fields.
     * "\\|" is a literal pipe (a bare "|" means "or" in a regular expression) and
     * the "\\s*" on either side lets the spaces around it be absent or repeated.
     */
    private static final String FIELD_SEPARATOR_PATTERN = "\\s*\\|\\s*";

    /** How many fields a line of each task type is expected to have. */
    private static final int TODO_FIELD_COUNT = 3;
    private static final int DEADLINE_FIELD_COUNT = 4;
    private static final int EVENT_FIELD_COUNT = 5;

    /** Where the tasks are stored, relative to the folder the app is run from. */
    private final Path filePath;

    /**
     * Problems found during the last {@link #load()}, one message per skipped line.
     * Collected instead of printed so that Storage stays free of user interface
     * code and the caller decides how to report them.
     */
    private final List<String> loadWarnings = new ArrayList<>();

    /**
     * @param first the first part of the path, e.g. "data"
     * @param more  the remaining parts, e.g. "amadeus.txt"
     *              <p>
     *              The path is assembled from separate parts rather than written as one string
     *              so that Java inserts the separator its own operating system uses
     *              ("\" on Windows, "/" elsewhere).
     */
    public Storage(String first, String... more) {
        this.filePath = Paths.get(first, more);
    }

    /** Returns the messages describing lines that could not be understood. */
    public List<String> getLoadWarnings() {
        return loadWarnings;
    }

    /**
     * Loads the saved tasks.
     * <p>
     * A missing file (or missing folder) simply means nothing has been saved yet,
     * so an empty list is returned instead of an error. Individual lines that
     * cannot be understood are skipped and recorded in {@link #getLoadWarnings()},
     * so one damaged line does not cost the user the rest of their list.
     *
     * @return the tasks that were read, in the order they were saved
     * @throws AmadeusException if the file exists but cannot be read at all
     */
    public List<Task> load() throws AmadeusException {
        loadWarnings.clear();
        List<Task> tasks = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return tasks;
        }
        if (Files.isDirectory(filePath)) {
            throw new AmadeusException("A thousand apologies, " + filePath
                    + " is a folder, so I cannot read your tasks from it.");
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Covers an unreadable file as well as one that is not valid UTF-8 text.
            throw new AmadeusException("A thousand apologies, I could not read " + filePath
                    + " (" + describe(e) + ").\n Starting with an empty list instead.");
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue; // A blank line carries no task, so there is nothing to complain about.
            }
            try {
                tasks.add(decodeTask(line));
            } catch (AmadeusException e) {
                // The line number is 1-based because that is how a text editor numbers lines.
                loadWarnings.add("Line " + (i + 1) + " of " + filePath + " was skipped: "
                        + e.getMessage());
            }
        }
        return tasks;
    }

    /**
     * Turns one saved line back into a Task.
     *
     * @throws AmadeusException if the line does not follow the expected format
     */
    private static Task decodeTask(String line) throws AmadeusException {
        String[] fields = line.split(FIELD_SEPARATOR_PATTERN);
        String type = fields[0];

        // The description and the times are checked here rather than in the Task
        // constructors because an empty one only ever means a damaged file.
        switch (type) {
        case "T":
            requireFieldCount(fields, TODO_FIELD_COUNT);
            Todo todo = new Todo(requireNonEmpty(fields[2], "description"));
            applyStatus(todo, fields[1]);
            return todo;

        case "D":
            requireFieldCount(fields, DEADLINE_FIELD_COUNT);
            Deadline deadline = new Deadline(requireNonEmpty(fields[2], "description"),
                    requireDate(fields[3], "due date"));
            applyStatus(deadline, fields[1]);
            return deadline;

        case "E":
            requireFieldCount(fields, EVENT_FIELD_COUNT);
            Event event = new Event(requireNonEmpty(fields[2], "description"),
                    requireDate(fields[3], "start date"),
                    requireDate(fields[4], "end date"));
            applyStatus(event, fields[1]);
            return event;

        default:
            throw new AmadeusException("'" + type + "' is not a task type I recognise");
        }
    }

    /**
     * Turns a file error into a short phrase worth showing the user.
     * <p>
     * Some file system errors carry only the file name as their message, which
     * would read as "could not save to data/x.txt (data/x.txt)". Naming the kind
     * of error instead tells the user something they did not already know.
     */
    private String describe(IOException e) {
        String type = e.getClass().getSimpleName();
        String reason = e.getMessage();
        if (reason == null || reason.trim().isEmpty() || reason.equals(filePath.toString())) {
            return type;
        }
        return type + ": " + reason;
    }

    /** @throws AmadeusException if the line does not have exactly the expected number of fields */
    private static void requireFieldCount(String[] fields, int expected) throws AmadeusException {
        if (fields.length != expected) {
            throw new AmadeusException("expected " + expected + " fields but found " + fields.length);
        }
    }

    /**
     * Reads a date out of a saved field.
     * <p>
     * TaskDateTime.parse phrases its complaint for someone typing a command, which
     * reads oddly inside a "line skipped" warning, so the message is shortened to
     * match the other checks here.
     *
     * @throws AmadeusException if the field is blank or is not a readable date
     */
    private static TaskDateTime requireDate(String field, String fieldName) throws AmadeusException {
        String text = requireNonEmpty(field, fieldName);
        try {
            return TaskDateTime.parse(text);
        } catch (AmadeusException e) {
            throw new AmadeusException("the " + fieldName + " '" + text + "' is not a date I can read");
        }
    }

    /** @throws AmadeusException if the field is blank */
    private static String requireNonEmpty(String field, String fieldName) throws AmadeusException {
        if (field.isEmpty()) {
            throw new AmadeusException("the " + fieldName + " is empty");
        }
        return field;
    }

    /**
     * Applies the saved done/not-done flag to a freshly created task.
     *
     * @throws AmadeusException if the flag is neither "1" nor "0"
     */
    private static void applyStatus(Task task, String statusField) throws AmadeusException {
        if (statusField.equals("1")) {
            task.markAsDone();
        } else if (!statusField.equals("0")) {
            throw new AmadeusException("'" + statusField + "' is not a done/not-done flag (expected 1 or 0)");
        }
    }

    /**
     * Writes the whole list to disk, replacing whatever was there before.
     * <p>
     * Rewriting the entire file is simpler than editing it in place, and with a
     * list this small the cost is not noticeable. Any missing folders on the way
     * to the file are created first, so the app works on a computer that has
     * never run it before.
     *
     * @throws AmadeusException if the tasks could not be written
     */
    public void save(List<Task> tasks) throws AmadeusException {
        List<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            lines.add(encodeTask(task));
        }

        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                // createDirectories does nothing if the folder already exists,
                // so there is no need to test for that first.
                Files.createDirectories(parent);
            }
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AmadeusException("A thousand apologies, I could not save to " + filePath
                    + " (" + describe(e) + ").");
        }
    }

    /**
     * Returns the single line used to store the given task.
     * <p>
     * Encoding lives here, next to {@link #decodeTask(String)}, so that the whole
     * file format can be read in one place. The alternative is to give every Task
     * subclass its own "encode yourself" method, which avoids the type tests below
     * but spreads the format over four files.
     *
     * @throws AmadeusException if the task is of a type that has no saved form yet
     */
    private static String encodeTask(Task task) throws AmadeusException {
        // "task instanceof Deadline deadline" tests the type and, when it matches,
        // declares "deadline" already converted to that type, so no cast is needed.
        // A Todo needs no extra fields, so that branch binds no variable.
        String head = FIELD_SEPARATOR + (task.isDone() ? "1" : "0")
                + FIELD_SEPARATOR + task.getDescription();

        if (task instanceof Todo) {
            return "T" + head;
        } else if (task instanceof Deadline deadline) {
            return "D" + head + FIELD_SEPARATOR + deadline.getBy().toStorageString();
        } else if (task instanceof Event event) {
            return "E" + head + FIELD_SEPARATOR + event.getStart().toStorageString()
                    + FIELD_SEPARATOR + event.getEnd().toStorageString();
        } else {
            throw new AmadeusException("A thousand apologies, I do not know how to save a "
                    + task.getClass().getSimpleName() + ".");
        }
    }
}
