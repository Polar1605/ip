package amadeus.task;

import java.time.LocalDate;

public class Task {
    protected String description;
    protected Status status;

    public Task(String description) {
        this.description = description;
        this.status = Status.NOT_DONE;
    }

    /** Returns the task's description, e.g. "read book". */
    public String getDescription() {
        return description;
    }

    /**
     * Returns true if this task has been completed.
     * Used when saving, where the state is written as 1 or 0 rather than as an icon.
     */
    public boolean isDone() {
        return status == Status.DONE;
    }

    /**
     * Returns true if this task is relevant to the given day, which is what the
     * "on" command asks each task.
     * <p>
     * A plain todo carries no date, so the answer here is always false; the
     * subclasses that do have dates override this. Asking every task the same
     * question keeps the date logic inside the class that owns the date, instead
     * of a chain of type tests at the call site.
     */
    public boolean occursOn(LocalDate date) {
        return false;
    }

    public String getStatusIcon() {
        return status.getIcon(); // "X" marks a done task
    }

    public void markAsDone() {
        this.status = Status.DONE;
    }

    public void markAsNotDone() {
        this.status = Status.NOT_DONE;
    }

    @Override
    public String toString() {
        return "[" + this.getStatusIcon() + "] " + description;
    }
}
