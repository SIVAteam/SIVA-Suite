package hu.persistence;

/**
 * This {@link RuntimeException} is thrown when an operation in the persistence
 * layer could not be performed, due to an internal, unrecoverable runtime
 * error.
 * 
 */
public class PersistenceRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String message = null;
    private Throwable rootCause = null;

    /**
     * Create a {@link PersistenceRuntimeException}.
     */
    public PersistenceRuntimeException() {
    }

    /**
     * Create a {@link PersistenceRuntimeException} with an explanation.
     * 
     * @param message
     *            to explain why the {@link PersistenceRuntimeException} was
     *            thrown.
     */
    public PersistenceRuntimeException(String message) {
        this.message = message;
    }

    /**
     * Create a {@link PersistenceRuntimeException} with an explanation and a
     * reference to an {@link Exception} that initially caused the
     * {@link PersistenceRuntimeException}.
     * 
     * @param message
     *            to explain why the {@link PersistenceRuntimeException} was
     *            thrown.
     * @param rootCause
     *            of the {@link PersistenceRuntimeException}.
     */
    public PersistenceRuntimeException(String message, Throwable rootCause) {
        this.message = message;
        this.rootCause = rootCause;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return this.message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Throwable getCause() {
        return this.rootCause;
    }
}
