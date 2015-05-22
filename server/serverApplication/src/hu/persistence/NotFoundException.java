package hu.persistence;

/**
 * This {@link RuntimeException} is thrown when an operation in the persistence
 * layer failed to locate a data record. This may happen because an invalid
 * identifier has been supplied by the method-caller or the record was recently
 * deleted.
 */
public class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private String message = null;
    private Throwable rootCause = null;

    /**
     * Create a {@link NotFoundException}.
     */
    public NotFoundException() {
    }

    /**
     * Create a {@link NotFoundException} with an explanation.
     * 
     * @param message
     *            to explain why the {@link NotFoundException} was thrown.
     */
    public NotFoundException(String message) {
        this.message = message;
    }

    /**
     * Create a {@link NotFoundException} with an explanation and a reference to
     * the {@link Exception} that initially caused the {@link NotFoundException}
     * .
     * 
     * @param message
     *            to explain why the {@link NotFoundException} was thrown.
     * @param rootCause
     *            of the {@link NotFoundException}.
     */
    public NotFoundException(String message, Throwable rootCause) {
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
