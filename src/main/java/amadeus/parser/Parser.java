package amadeus.parser;

import java.time.LocalDate;

import amadeus.AmadeusException;
import amadeus.task.Deadline;
import amadeus.task.Event;
import amadeus.task.TaskDateTime;
import amadeus.task.Todo;

/**
 * Turns the lines the user types into the commands and objects the rest of the
 * app works with.
 * <p>
 * All the knowledge of the input format - where "/by" sits, that the user counts
 * tasks from 1, which date formats are allowed - lives here, so the main loop can
 * work with real objects instead of picking strings apart. Anything the user
 * types that cannot be understood is reported as an {@link AmadeusException} with
 * a message telling them the correct form, which means an invalid task can never
 * be built in the first place.
 * <p>
 * Every method is static because a Parser has nothing to remember between calls.
 */
public class Parser {

    /**
     * Returns the first word of the input, which tells us which command the user wants.
     */
    public static String parseCommandWord(String input) throws AmadeusException {
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            throw new AmadeusException("A thousand apologies, you haven't told me anything yet.");
        }
        // "\\s+" splits on any run of whitespace, and the limit of 2 stops the
        // split after the first word so the rest of the line stays in one piece.
        return trimmed.split("\\s+", 2)[0];
    }

    /**
     * Returns everything after the command word, trimmed.
     * Returns an empty string when the user typed the command word on its own,
     * which is what lets the callers below detect missing arguments instead of
     * crashing with a StringIndexOutOfBoundsException.
     */
    private static String parseArguments(String input) {
        String[] parts = input.trim().split("\\s+", 2);
        return parts.length < 2 ? "" : parts[1].trim();
    }

    /**
     * Returns the todo described by a line of the form {@code todo <description>}.
     *
     * @param input the whole line the user typed, e.g. "todo read book".
     * @return a new todo, not yet done.
     * @throws AmadeusException if the description is missing.
     */
    public static Todo parseTodo(String input) throws AmadeusException {
        String description = parseArguments(input);
        if (description.isEmpty()) {
            throw new AmadeusException("A hundred apologies, a todo needs a description.",
                    "Please use: todo <description>");
        }
        return new Todo(description);
    }

    /**
     * Parses a line of the form {@code deadline <description> /by <date>}.
     *
     * @throws AmadeusException if the description, the "/by" marker, or the date is
     *                          missing, or if the date cannot be understood
     */
    public static Deadline parseDeadline(String input) throws AmadeusException {
        String arguments = parseArguments(input);
        int byIdx = arguments.indexOf("/by");
        if (byIdx == -1) {
            throw new AmadeusException("Ten thousand apologies, a deadline needs a '/by'.",
                    "Please use: deadline <description> /by <date>");
        }

        // The description sits before "/by"; the due time is everything after it.
        String description = arguments.substring(0, byIdx).trim();
        String by = arguments.substring(byIdx + "/by".length()).trim();

        if (description.isEmpty()) {
            throw new AmadeusException("A hundred apologies, a deadline needs a description.",
                    "Please use: deadline <description> /by <date>");
        }
        if (by.isEmpty()) {
            throw new AmadeusException("A hundred apologies, you didn't say when it's due.",
                    "Please use: deadline <description> /by <date>");
        }
        // TaskDateTime.parse throws if the text is not a date it recognises, so a
        // Deadline can never be built with a due date the app cannot understand.
        return new Deadline(description, TaskDateTime.parse(by));
    }

    /**
     * Parses a line of the form {@code event <description> /from <start> /to <end>}.
     *
     * @throws AmadeusException if any of the three parts is missing, if "/to"
     *                          appears before "/from", or if a date cannot be understood
     */
    public static Event parseEvent(String input) throws AmadeusException {
        String arguments = parseArguments(input);
        int fromIdx = arguments.indexOf("/from");
        int toIdx = arguments.indexOf("/to");

        if (fromIdx == -1 || toIdx == -1) {
            throw new AmadeusException("A million apologies, an event needs both '/from' and '/to'.",
                    "Please use: event <description> /from <start> /to <end>");
        }
        // The three parts are read by position, so they have to be in this order.
        if (toIdx < fromIdx) {
            throw new AmadeusException("A million apologies, '/from' has to come before '/to'.",
                    "Please use: event <description> /from <start> /to <end>");
        }

        String description = arguments.substring(0, fromIdx).trim();
        String start = arguments.substring(fromIdx + "/from".length(), toIdx).trim();
        String end = arguments.substring(toIdx + "/to".length()).trim();

        if (description.isEmpty()) {
            throw new AmadeusException("A hundred apologies, an event needs a description.",
                    "Please use: event <description> /from <start> /to <end>");
        }
        if (start.isEmpty() || end.isEmpty()) {
            throw new AmadeusException("A hundred apologies, an event needs both a start and an end time.",
                    "Please use: event <description> /from <start> /to <end>");
        }
        return new Event(description, TaskDateTime.parse(start), TaskDateTime.parse(end));
    }

    /**
     * Reads the date given to the "on" command, e.g. "on 2019-12-02".
     * <p>
     * Any time of day the user adds is dropped, because "on" asks about a whole
     * day rather than a moment within it.
     *
     * @param input the whole line the user typed, e.g. "on 2019-12-02"
     * @return the day being asked about
     * @throws AmadeusException if the date is missing or not in a known format
     */
    public static LocalDate parseDate(String input) throws AmadeusException {
        String argument = parseArguments(input);
        if (argument.isEmpty()) {
            throw new AmadeusException("A hundred apologies, please tell me which date.",
                    "Please use: on <date>, for example: on 2019-12-02");
        }
        return TaskDateTime.parse(argument).getDate();
    }

    /**
     * Returns the keyword given to the "find" command, e.g. "book" from "find book".
     * <p>
     * Everything after the command word is taken as one keyword rather than
     * being split into several, so "find read book" searches for the whole
     * phrase "read book".
     *
     * @param input the whole line the user typed, e.g. "find book".
     * @return the text to search descriptions for.
     * @throws AmadeusException if no keyword was given.
     */
    public static String parseKeyword(String input) throws AmadeusException {
        String keyword = parseArguments(input);
        if (keyword.isEmpty()) {
            throw new AmadeusException("A hundred apologies, please tell me what to search for.",
                    "Please use: find <keyword>");
        }
        return keyword;
    }

    /**
     * Converts the number typed after "mark" or "unmark" into an array index.
     * The number shown to the user starts at 1, but arrays start at 0, hence the
     * subtraction at the end.
     *
     * @param input     the whole line the user typed, e.g. "mark 2"
     * @param taskCount how many tasks are currently stored
     * @return the 0-based index of the requested task
     * @throws AmadeusException if the number is missing, not a number, or out of range
     */
    public static int parseTaskIndex(String input, int taskCount) throws AmadeusException {
        String argument = parseArguments(input);
        if (argument.isEmpty()) {
            throw new AmadeusException("A hundred apologies, please tell me which task number.");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            // parseInt reports bad input by throwing rather than by returning a
            // value, so this has to be caught rather than tested with an if.
            throw new AmadeusException("Hundreds of apologies, '" + argument + "' is not a task number.");
        }

        if (taskCount == 0) {
            throw new AmadeusException("Thousands of apologies, your list is empty.");
        }
        if (taskNumber < 1 || taskNumber > taskCount) {
            throw new AmadeusException("Thousands of apologies, I only have " + taskCount + " task(s).");
        }
        return taskNumber - 1;
    }
}
