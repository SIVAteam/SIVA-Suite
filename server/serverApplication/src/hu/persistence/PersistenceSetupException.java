package hu.persistence;


/**
 * This {@link Exception} is thrown to indicate a failure in
 * {@link IPersistenceSetup}.
 * 
 */
public class PersistenceSetupException extends Exception {
    private static final long serialVersionUID = 1L;
    private String message = null;
    private Throwable rootCause = null;

    /**
     * Create an {@link PersistenceSetupException}.
     */
    public PersistenceSetupException() {
    }

    /**
     * Create an {@link PersistenceSetupException} with an explanation.
     * 
     * @param message
     *            to explain why the {@link PersistenceSetupException} was
     *            thrown.
     */
    public PersistenceSetupException(String message) {
        this.message = message;
    }

    /**
     * Create an {@link PersistenceSetupException} with an explanation and a
     * reference to the {@link Exception} that initially caused the
     * {@link PersistenceSetupException}.
     * 
     * @param message
     *            to explain why the {@link PersistenceSetupException} was
     *            thrown.
     * @param rootCause
     *            of the {@link PersistenceSetupException}.
     */
    public PersistenceSetupException(String message, Throwable rootCause) {
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
