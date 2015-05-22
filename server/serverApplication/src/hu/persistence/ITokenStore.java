package hu.persistence;

import hu.model.Video;
import hu.model.Token;

import java.util.List;

/**
 * This interface describes how {@link Token}s are stored in and retrieved from
 * the persistence layer.
 */
public interface ITokenStore {

    /**
     * Create a new {@link Token} for a {@link Video}.
     * 
     * All fields must be provided. The token field must be unique for all
     * {@link Token}s.
     * 
     * @param token
     *            to create.
     * @param videoId
     *            of the {@link Video} for which to create the
     *            {@link Token}.
     * @return the created {@link Token}.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public Token create(Token token, int videoId)
            throws InconsistencyException;

    /**
     * Delete a {@link Token} with a given identifier.
     * 
     * A {@link Token} cannot be deleted if an {@link AnsweredVideo}
     * exists for the related {@link Video}.
     * 
     * @param tokenIdentifier
     *            of the {@link Token} to delete.
     * @throws InconsistencyException
     *             it the constraints described above are not satisfied.
     */
    public void delete(String tokenIdentifier) throws InconsistencyException;

    /**
     * Delete a {@link Token}.
     * 
     * A {@link Token} cannot be deleted if an {@link AnsweredVideo}
     * exists for the related {@link Video}.
     * 
     * @param token
     *            to delete.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public void delete(Token token) throws InconsistencyException;

    /**
     * Retrieve a {@link Token} with a given identifier.
     * 
     * @param tokenIdentifier
     *            of the {@link Token} to fetch.
     * @return the {@link Token} with the specified identifier. Null, if no such
     *         {@link Token} exists.
     */
    public Token find(String tokenIdentifier);

    /**
     * Retrieve a {@link List} of {@link Token}s for a given
     * {@link Video}. The entries are sorted by their identifier in an
     * ascending order.
     * 
     * @param videoId
     *            of the {@link Video} whose {@link Token}s to retrieve.
     * @return a {@link List} of all {@link Token}s for the specified
     *         {@link Video}.
     * @throws NotFoundException
     *             if no such {@link Video} exists.
     */
    public List<Token> getForVideo(int videoId);
}