public class Task {
    protected String description;
    protected Status status;

    public Task(String description) {
        this.description = description;
        this.status = Status.NOT_DONE;
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
