package hu.util;

/**
 * This {@link RuntimeException} is thrown when an operation in the
 * {@link MailService} failed. This may happen because the mail server is not
 * reachable or the given userdata in the {@link Configuration} is not valid.
 */
public class MailServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private String message = null;
    private Throwable rootCause = null;

    /**
     * Create a {@link MailServiceException}.
     */
    public MailServiceException() {
    }

    /**
     * Create a {@link MailServiceException} with an explanation.
     * 
     * @param message
     *            to explain why the {@link MailServiceException} was thrown.
     */
    public MailServiceException(String message) {
        this.message = message;
    }

    /**
     * Create a {@link MailServiceException} with an explanation and a reference
     * to the {@link Exception} that initially caused the
     * {@link MailServiceException} .
     * 
     * @param message
     *            to explain why the {@link MailServiceException} was thrown.
     * @param rootCause
     *            of the {@link MailServiceException}.
     */
    public MailServiceException(String message, Throwable rootCause) {
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