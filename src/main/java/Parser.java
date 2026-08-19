
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
     * @throws AmadeusException if the description is missing
     */
    public static Todo parseTodo(String input) throws AmadeusException {
        String description = parseArguments(input);
        if (description.isEmpty()) {
            throw new AmadeusException("A hundred apologies, a todo needs a description."
                    + "\n Please use: todo <description>");
        }
        return new Todo(description);
    }

    /**
     * Parses a line of the form {@code deadline <description> /by <time>}.
     *
     * @throws AmadeusException if the description, the "/by" marker, or the time is missing
     */
    public static Deadline parseDeadline(String input) throws AmadeusException {
        String arguments = parseArguments(input);
        int byIdx = arguments.indexOf("/by");
        if (byIdx == -1) {
            throw new AmadeusException("Ten thousand apologies, a deadline needs a '/by'."
                    + "\n Please use: deadline <description> /by <time>");
        }

        // The description sits before "/by"; the due time is everything after it.
        String description = arguments.substring(0, byIdx).trim();
        String by = arguments.substring(byIdx + "/by".length()).trim();

        if (description.isEmpty()) {
            throw new AmadeusException("A hundred apologies, a deadline needs a description."
                    + "\n Please use: deadline <description> /by <time>");
        }
        if (by.isEmpty()) {
            throw new AmadeusException("A hundred apologies, you didn't say when it's due."
                    + "\n Please use: deadline <description> /by <time>");
        }
        return new Deadline(description, by);
    }

    /**
     * Parses a line of the form {@code event <description> /from <start> /to <end>}.
     *
     * @throws AmadeusException if any of the three parts is missing, or if "/to"
     *                          appears before "/from"
     */
    public static Event parseEvent(String input) throws AmadeusException {
        String arguments = parseArguments(input);
        int fromIdx = arguments.indexOf("/from");
        int toIdx = arguments.indexOf("/to");

        if (fromIdx == -1 || toIdx == -1) {
            throw new AmadeusException("A million apologies, an event needs both '/from' and '/to'."
                    + "\n Please use: event <description> /from <start> /to <end>");
        }
        // The three parts are read by position, so they have to be in this order.
        if (toIdx < fromIdx) {
            throw new AmadeusException("A million apologies, '/from' has to come before '/to'."
                    + "\n Please use: event <description> /from <start> /to <end>");
        }

        String description = arguments.substring(0, fromIdx).trim();
        String start = arguments.substring(fromIdx + "/from".length(), toIdx).trim();
        String end = arguments.substring(toIdx + "/to".length()).trim();

        if (description.isEmpty()) {
            throw new AmadeusException("A hundred apologies, an event needs a description."
                    + "\n Please use: event <description> /from <start> /to <end>");
        }
        if (start.isEmpty() || end.isEmpty()) {
            throw new AmadeusException("A hundred apologies, an event needs both a start and an end time."
                    + "\n Please use: event <description> /from <start> /to <end>");
        }
        return new Event(description, start, end);
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
