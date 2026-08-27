package amadeus.task;

import java.time.LocalDate;

/** A task that has to be finished by a particular date and time. */
public class Deadline extends Task {

    protected TaskDateTime by;

    public Deadline(String description, TaskDateTime by) {
        super(description);
        this.by = by;
    }

    /** Returns the date the task is due. */
    public TaskDateTime getBy() {
        return by;
    }

    /** A deadline is relevant to the day it falls due on. */
    @Override
    public boolean occursOn(LocalDate date) {
        return by.isOn(date);
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
