package hu.persistence;

import hu.model.EVideoType;
import hu.model.ESortColumnVideo;
import hu.model.ESortDirection;
import hu.model.Group;
import hu.model.Token;
import hu.model.Video;
import hu.model.users.EUserType;
import hu.model.users.User;

import java.util.List;

import org.mockito.stubbing.Answer;

/**
 * This interface describes how {@link Video}s are stored in and
 * retrieved from the persistence layer.
 */
public interface IVideoStore {
    /**
     * Create a new {@link Video} for a given {@link Group}.
     * 
     * The fields title, anonymous, earlyEvaluationAccess,
     * participationRestriction and evaluationRestriction must be provided. If
     * the id is provided, it must be unique for all {@link Video}s. A
     * start and stop time must not be set.
     * 
     * @param video
     *            to create.
     * @param group
     *            to contain the {@link Video}.
     * @return the created {@link Video} with its id populated.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public Video create(Video video, Group group)
            throws InconsistencyException;

    /**
     * Create a new {@link Video} for a given {@link Group}.
     * 
     * The fields title, anonymous, earlyEvaluationAccess,
     * participationRestriction and evaluationRestriction must be provided. If
     * the id is provided, it must be unique for all {@link Video}s. A
     * start and stop time must not be set.
     * 
     * @param video
     *            to create.
     * @param groupId
     *            of the {@link Group} to contain the {@link Video}.
     * @return the created {@link Video} with its id populated.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public Video create(Video video, int groupId)
            throws InconsistencyException;

    /**
     * Save changes made to a {@link Video}.
     * 
     * The fields title, anonymous, earlyEvaluationAccess,
     * participationRestriction and evaluationRestriction must be provided. The
     * id is used to identify which {@link Video} to change and
     * therefore cannot be changed. If a stop time is set, a start time must
     * also be provided. If both start and stop time are set, the start time
     * must be smaller than the stop time. If a {@link Video} is started
     * by setting a start time, it must adhere to some rules: The
     * {@link Video} must contain at least one {@link AQuestion}, and
     * all {@link ChoiceQuestion}s must contain at least one {@link Answer}. If
     * an {@link AnsweredVideo} exists for the given
     * {@link Video}, only its start and end time may be changed.
     * 
     * @param video
     *            to change.
     * @return the changed {@link Video}.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public Video save(Video video)
            throws InconsistencyException;

    /**
     * Delete a given {@link Video}.
     * 
     * This will also delete all {@link AQuestion}s, {@link Token}s and
     * {@link AnsweredVideo}s related to the {@link Video}.
     * 
     * It is not possible to delete a non-existent {@link Video}.
     * 
     * @param videoId
     *            of the {@link Video} to delete.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public void delete(int videoId) throws InconsistencyException;

    /**
     * Delete a given {@link Video}.
     * 
     * This will also delete all {@link AQuestion}s, {@link Token}s and
     * {@link AnsweredVideo}s related to the {@link Video}.
     * 
     * It is not possible to delete a non-existent {@link Video}.
     * 
     * @param video
     *            to delete.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public void delete(Video video)
            throws InconsistencyException;

    /**
     * Move a {@link Video} from one {@link Group} to another.
     * 
     * A {@link Video} cannot be moved if an
     * {@link AnsweredVideo} exists for the given {@link Video}.
     * A non-existent {@link Video} cannot be moved.
     * 
     * @param videoId
     *            of the {@link Video} to move.
     * @param oldGroupId
     *            of the {@link Group} that currently contains the
     *            {@link Video}.
     * @param newGroupId
     *            of the {@link Group} to contain the {@link Video}.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public void moveVideo(int videoId, int oldGroupId,
            int newGroupId) throws InconsistencyException;

    /**
     * Retrieve a {@link Video} with a given id.
     * 
     * @param id
     *            of the {@link Video} to fetch.
     * @return the {@link Video} with the specified id.
     * @throws NotFoundException
     *             if no such {@link Video} exists.
     */
    public Video getById(int id);

    /**
     * Retrieve a {@link Video} with a given id.
     * 
     * @param id
     *            of the {@link Video} to fetch.
     * @return the {@link Video} with the specified id. Null if no such
     *         {@link Video} exists.
     */
    public Video findById(int id);


    /**
     * Retrieve a {@link Video} containing a given {@link Token}.
     * 
     * @param token
     *            contained in the {@link Video} to fetch.
     * @return the {@link Video} containing the specified {@link Token}.
     * @throws NotFoundException
     *             if no such {@link Video} exists.
     */
    public Video getByToken(Token token);

    /**
     * Retrieve a {@link Video} containing a given {@link Token}.
     * 
     * @param tokenIdentifier
     *            of the {@link Token} contained in the {@link Video} to
     *            fetch.
     * @return the {@link Video} containing the specified {@link Token}.
     * @throws NotFoundException
     *             if no such {@link Video} exists.
     */
    public Video getByToken(String tokenIdentifier);
    
    /**
     * Retrieve a {@link Video} containing a given {@link Token}.
     * 
     * @param tokenIdentifier
     *            of the {@link Token} contained in the {@link Video} to
     *            fetch.
     * @return the {@link Video} containing the specified {@link Token}. Null
     * 				if no such {@link Video} exists.
     */
    public Video findByToken(String tokenIdentifier);
    
    /**
     * Retrieve the {@link Video} using a certain directory.
     * 
     * @param directory
     *            of the {@link Video} to fetch.
     * @return the {@link Video} using the specified directory. Null
     * 				if no such {@link Video} exists.
     */
    public Video findByDirectory(String directory);

    /**
     * Retrieve a {@link List} of {@link Video}s contained in a given
     * {@link Group}. The entries are sorted by their id in an ascending order.
     * 
     * @param group
     *            containing the {@link Video}s.
     * @return a {@link List} of {@link Video}s contained in the
     *         specified {@link Group}.
     * @throws NotFoundException
     *             if no such {@link Group} exists.
     */
    public List<Video> getForGroup(Group group);

    /**
     * Retrieve a {@link List} of {@link Video}s contained in a given
     * {@link Group}. The entries are sorted by their id in an ascending order.
     * 
     * @param groupId
     *            of the {@link Group} containing the {@link Video}s.
     * @return a {@link List} of {@link Video}s contained in the
     *         specified {@link Group}.
     * @throws NotFoundException
     *             if no such {@link Group} exists.
     */
    public List<Video> getForGroup(int groupId);
    
	/**
	 * Retrieve a {@link List} of {@link Video}s matching by the given
	 * combination of parameters. The entries are sorted by their id in an
	 * ascending order.
	 * 
	 * @param minimumRating
	 *            of the {@link Video}s.
	 * @param minimumViews
	 *            of the {@link Video}s.
	 * @param maxSize
	 *            of the {@link Video}s.
	 * @param author
	 *            of the {@link Video}s.
	 * @param user
	 *            allowed to watch {@link Video}s.
	 * @return a {@link List} of {@link Video}s matching the given combination
	 *         of parameters.
	 */
	public List<Video> getForApi(float minimumRating, int minimumViews,
			long maxSize, String author, User user);

    /**
     * Retrieve a {@link List} of all {@link Video}s with the specified
     * {@link EVideoType} from the specified user and group. The entries
     * are sorted by the specified {@link ESortColumnVideo} of the
     * {@link User} in {@link ESortDirection} order. It starts with the
     * specified first row.
     * 
     * @param user
     *            whose videos should be retrieved.
     * @param group
     *            whose videos should be retrieved.
     * @param sortColumn
     *            which is sorted.
     * @param sortDirection
     *            of the sort column.
     * @param type
     *            of the videos returned. Use null for all
     *            videos.
     * @param firstRow
     *            of the list retrieved.
     * @param rows
     *            of the list to retrieve. Set 0 if all rows should be 
     *            retrieved.
     * @return {@link List} of all {@link Video}s sorted by the
     *         specified {@link ESortColumnVideo} in
     *         {@link ESortDirection} order.
     */
    public List<Video> getAll(User user, Group group,
            ESortColumnVideo sortColumn, ESortDirection sortDirection,
            EVideoType type, int firstRow, int rows);

    /**
     * Retrieves the amount of all {@link Video}s with the specified
     * with the specified {@link EVideoType} from the specified user and
     * group.
     * 
     * @param user
     *            whose videos should be counted.
     * @param group
     *            whose videos should be counted.
     * @param type
     *            of the videos counted. Use null for all
     *            videos.
     * @return the amount of all {@link User}s with the specified
     *         {@link EUserType} whose properties match a given search term.
     */
    public int getCountOfAll(User user, Group group, EVideoType type);
    
    /**
     * Increase version of {@link Video}.
     * 
     * @param videoId
     *            of the {@link Video}.
     */
    public void increaseVersion(int videoId);
    
    /**
     * Increase views of {@link Video}.
     * 
     * @param videoId
     *            of the {@link Video}.
     */
    public void increaseViews(int videoId);
}
