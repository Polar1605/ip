package amadeus.task;

import java.time.LocalDate;

/** A task that runs from one date and time to another. */
public class Event extends Task {

    protected TaskDateTime start;
    protected TaskDateTime end;

    public Event(String description, TaskDateTime start, TaskDateTime end) {
        super(description);
        this.start = start;
        this.end = end;
    }

    /** Returns the date the event starts. */
    public TaskDateTime getStart() {
        return start;
    }

    /** Returns the date the event ends. */
    public TaskDateTime getEnd() {
        return end;
    }

    /**
     * An event is relevant to every day it spans, not only the day it starts,
     * so a three-day event shows up on all three days.
     */
    @Override
    public boolean occursOn(LocalDate date) {
        return !date.isBefore(start.getDate()) && !date.isAfter(end.getDate());
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + start + " to: " + end + ")";
    }
}
