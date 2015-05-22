package hu.model;

/**
 * This class represents a token. A unique identifier which may be used to
 * authorize access to a {@link Video}.
 * 
 * Note: One token may either grant access to a {@link Video} for
 * participation or for viewing results, but never for both functions.
 */
public class Token {
    private String token = null;
    private boolean forParticipation = true;

    /**
     * Note: to actually generate a token, please save the object
     */
    public Token() {
    }

    /**
     * Create a {@link Token}.
     * 
     * @param token
     *            to set.
     */
    public Token(String token) {
        this.token = token;
    }

    /**
     * 
     * @return true if the {@link Token} can be used to participate in a
     *         {@link Video}, false otherwise.
     */
    public boolean isForParticipation() {
        return this.forParticipation;
    }

    /**
     * Set whether the {@link Token} can be used for participating in a
     * {@link Video} or not. Note: if enabled, forEvalutation is
     * disabled.
     * 
     * @param forParticipation
     *            is true if the {@link Token} can be used to participate in a
     *            {@link Video}, false otherwise.
     */
    public void setForParticipation(boolean forParticipation) {
        this.forParticipation = forParticipation;
    }

    /**
     * 
     * @return true if the {@link Token} can be used to show the evaluation of a
     *         {@link Video}, false otherwise.
     */
    public boolean isForEvalutation() {
        return !this.forParticipation;
    }

    /**
     * Set whether the {@link Token} can be used to show a evaluation of a
     * {@link Video} or not. Note: if enabled, forParticipation is
     * disabled.
     * 
     * @param forEvalutation
     *            is true if the {@link Token} can be used to show the
     *            evaluation of a {@link Video}, false otherwise.
     */
    public void setForEvalutation(boolean forEvalutation) {
        this.forParticipation = !forEvalutation;
    }

    /**
     * 
     * @return the {@link String} representation of the {@link Token}.
     */
    public String getToken() {
        return this.token;
    }

    @Override
    public String toString() {
        return String.format("Token (%s)", this.token);
    }

    @Override
    public int hashCode() {
        return (this.token == null) ? 0 : this.token.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Token other = (Token) obj;
        if (this.token == null) {
            if (other.token != null) {
                return false;
            }
        } else if (!this.token.equals(other.token)) {
            return false;
        }
        return true;
    }
}