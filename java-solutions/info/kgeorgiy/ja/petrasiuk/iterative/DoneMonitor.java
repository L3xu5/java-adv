package info.kgeorgiy.ja.petrasiuk.iterative;

/**
 * Monitors the progress of parallel tasks, allowing threads to wait until all tasks are complete
 * and aggregating any runtime exceptions that occur during execution.
 */
public class DoneMonitor {
    private int doneParts = 0;
    private final int totalParts;
    private RuntimeException exception;

    /**
     * Constructs a new {@code DoneMonitor} for tracking the specified number of tasks.
     *
     * @param totalParts the total number of tasks to monitor
     */
    public DoneMonitor(int totalParts) {
        this.totalParts = totalParts;
    }

    /**
     * Increments the count of completed tasks and notifies waiting threads.
     */
    public synchronized void addPart() {
        doneParts++;
        notify();
    }

    /**
     * Records a runtime exception, either as the primary exception or as a suppressed exception
     * if one already exists.
     *
     * @param e the runtime exception to record
     */
    public synchronized void addException(RuntimeException e) {
        if (exception == null) {
            exception = e;
        } else {
            exception.addSuppressed(e);
        }
    }

    /**
     * Blocks the calling thread until all tasks are complete.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public synchronized void waitUntilDone() throws InterruptedException {
        while (doneParts != totalParts) {
            wait();
        }
    }

    /**
     * Returns the aggregated runtime exception, if any, that occurred during task execution.
     *
     * @return the runtime exception, or {@code null} if no exceptions occurred
     */
    public RuntimeException getException() {
        return exception;
    }
}