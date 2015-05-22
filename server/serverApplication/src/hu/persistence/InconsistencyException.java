package hu.persistence;

/**
 * This {@link Exception} is thrown when an operation in the persistence layer
 * was cancelled because it would cause inconsistencies in the stored data.
 */
public class InconsistencyException extends Exception {
    private static final long serialVersionUID = 1L;
    private String message = null;
    private Throwable rootCause = null;

    /**
     * Create an {@link InconsistencyException}.
     */
    public InconsistencyException() {
    }

    /**
     * Create an {@link InconsistencyException} with an explanation.
     * 
     * @param message
     *            to explain why the {@link InconsistencyException} was thrown.
     */
    public InconsistencyException(String message) {
        this.message = message;
    }

    /**
     * Create an {@link InconsistencyException} with an explanation and a
     * reference to the {@link Exception} that initially caused the
     * {@link InconsistencyException}.
     * 
     * @param message
     *            to explain why the {@link InconsistencyException} was thrown.
     * @param rootCause
     *            of the {@link InconsistencyException}.
     */
    public InconsistencyException(String message, Throwable rootCause) {
        this.message = message;
        this.rootCause = rootCause;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public synchronized Throwable getCause() {
        return this.rootCause;
    }
}