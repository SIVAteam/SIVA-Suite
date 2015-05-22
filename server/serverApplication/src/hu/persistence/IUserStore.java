package hu.persistence;

import hu.model.ESortDirection;
import hu.model.Group;
import hu.model.Video;
import hu.model.users.ESortColumnUser;
import hu.model.users.EUserType;
import hu.model.users.User;

import java.util.List;
import java.util.Map;

/**
 * This interface describes how {@link User}s are stored in and retrieved from
 * the persistence layer.
 */
public interface IUserStore {
    /**
     * Create a new {@link User}.
     * 
     * All fields except title and id must be provided. If the id is provided,
     * it must be unique for all {@link User}s. The provided email must be
     * unique for all {@link User}s. If the {@link User} is marked as
     * non-deletable, he must be of the type {@link EUserType#Administrator}. If
     * the {@link User} is marked as banned he also must be marked as deletable.
     * 
     * @param user
     *            to create.
     * @return the created {@link User} with its id populated.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public User create(User user) throws InconsistencyException;

    /**
     * Save changes made to a {@link User}.
     * 
     * All fields except the title must be provided. The id is used to identify
     * which {@link User} to change and therefore cannot be changed. The
     * provided email must be unique for all {@link User}s. If the {@link User}
     * is marked as non-deletable, he must be of the type
     * {@link EUserType#Administrator}. If the {@link User} is marked as banned
     * he also must be marked as deletable.
     * 
     * @param user
     *            to change.
     * @return the changed {@link User}.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public User save(User user) throws InconsistencyException;

    /**
     * Delete a {@link User} with a given id.
     * 
     * This will also delete any {@link User} related data, such as
     * {@link Group} attendances and ownerships, as well as preset
     * {@link EEvaluationFunction}s. Any {@link AnsweredVideo}s that
     * belong to the {@link User} are not deleted.
     * 
     * It is not possible to delete a non-existent {@link User} or a
     * {@link User} that is marked as non-deletable.
     * 
     * @param userId
     *            of the {@link User} to deleted.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public void delete(int userId) throws InconsistencyException;

    /**
     * Delete a given {@link User}.
     * 
     * The id is used to identify which {@link User} is deleted and therefore
     * must be provided.
     * 
     * This will also delete any {@link User} related data, such as
     * {@link Group} attendances and ownerships, as well as preset
     * {@link EEvaluationFunction}s. Any {@link AnsweredVideo}s that
     * belong to the {@link User} are not deleted.
     * 
     * It is not possible to delete a non-existent {@link User} or a
     * {@link User} that is marked as non-deletable.
     * 
     * @param user
     *            to delete.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public void delete(User user) throws InconsistencyException;

    /**
     * Retrieve a {@link User} with a given id.
     * 
     * @param id
     *            of the {@link User} to fetch.
     * @return the {@link User} with the specified id.
     * @throws NotFoundException
     *             if no such {@link User} exists.
     */
    public User getById(int id);

    /**
     * Retrieve a {@link User} with a given id.
     * 
     * @param id
     *            of the {@link User} to fetch.
     * @return the {@link User} with the specified id. Null, if no such
     *         {@link User} exists.
     */
    public User findById(int id);

    /**
     * Retrieve a {@link User} with a given email.
     * 
     * @param email
     *            of the {@link User} to fetch.
     * @return the {@link User} with the specified email.
     * @throws NotFoundException
     *             if no such {@link User} exists.
     */
    public User getByEmail(String email);

    /**
     * Retrieve a {@link User} with a given email.
     * 
     * @param email
     *            of the {@link User} to fetch.
     * @return the {@link User} with the specified email. Null, if no such
     *         {@link User} exists.
     */
    public User findByEmail(String email);

    /**
     * Retrieve a {@link List} of {@link User}s with a given {@link EUserType}.
     * The entries are sorted by the id of the {@link User} in an ascending
     * order.
     * 
     * @param type
     *            of the {@link User}s to fetch.
     * @return a {@link List} of all {@link User}s with the specified
     *         {@link EUserType}.
     */
    public List<User> getByType(EUserType type);

    /**
     * Retrieve a {@link List} of all {@link User}s with the specified
     * {@link EUserType}. The entries are sorted by the specified
     * {@link ESortColumnUser} of the {@link User} in {@link ESortDirection}
     * order. It starts with the specified first row.
     * 
     * @param sortColumn
     *            which is sorted.
     * @param sortDirection
     *            of the sort column.
     * @param userType
     *            of the users returned. Use null for all users.
     * @param firstRow
     *            of the list retrieved.
     * @param rows
     *            of the list to retrieve. Set 0 for all rows.
     * @return {@link List} of all {@link User}s sorted by the specified
     *         {@link ESortColumnUser} in {@link ESortDirection} order.
     */
    public List<User> getAll(ESortColumnUser sortColumn,
            ESortDirection sortDirection, EUserType userType, int firstRow,
            int rows);

    /**
     * Retrieves the amount of all {@link User}s with the specified
     * {@link EUserType}.
     * 
     * @param sortColumn
     *            which is sorted.
     * @param sortDirection
     *            of the sort column.
     * @param userType
     *            of the users counted. Use null for all users.
     * @return the amount of all {@link User}s with the specified
     *         {@link EUserType} whose properties match a given search term.
     */
    public int getCountOfAll(EUserType userType);

    /**
     * Retrieve a {@link List} of all {@link User}s with the specified
     * {@link EUserType} whose properties match a given search term. The entries
     * are sorted by the specified {@link ESortColumnUser} of the {@link User}
     * in {@link ESortDirection} order. It starts with the specified first row.
     * 
     * @param searchTerm
     *            to search for.
     * @param sortColumn
     *            which is sorted.
     * @param sortDirection
     *            of the sort column.
     * @param userType
     *            of the users returned. Use null for all users.
     * @param firstRow
     *            of the list retrieved.
     * @param rows
     *            of the list to retrieve. Set 0 for all rows.
     * @return {@link List} of all {@link User}s sorted by the specified
     *         {@link ESortColumnUser} in {@link ESortDirection} order.
     */
    public List<User> search(final String searchTerm,
            ESortColumnUser sortColumn, ESortDirection sortDirection,
            EUserType userType, int firstRow, int rows);

    /**
     * Retrieve the amount of all {@link User}s with the specified
     * {@link EUserType} whose properties match a given search term.
     * 
     * @param searchTerm
     *            to search for.
     * @param sortColumn
     *            which is sorted.
     * @param sortDirection
     *            of the sort column.
     * @param userType
     *            of the users counted. Use null for all users.
     * @return the amount of all {@link User}s with the specified
     *         {@link EUserType}.
     */
    public int getCountOfSearch(final String searchTerm, EUserType userType);

    /**
     * Retrieve a {@link List} of {@link User}s that attend a given
     * {@link Group}. The entries are sorted by the id of the {@link User} in an
     * ascending order.
     * 
     * @param groupId
     *            of the {@link Group} whose attendants to retrieve.
     * @return a {@link List} of {@link User}s that attend the specified
     *         {@link Group}.
     * @throws NotFoundException
     *             if no such {@link Group} exists.
     */
    public List<User> getByAttendance(int groupId);

    /**
     * Retrieve a {@link List} of {@link User}s that attend a given
     * {@link Group}. The entries are sorted by the id of the {@link User} in an
     * ascending order.
     * 
     * @param group
     *            whose attendants to retrieve.
     * @return a {@link List} of {@link User}s that attend the specified
     *         {@link Group}.
     * @throws NotFoundException
     *             if no such {@link Group} exists.
     */
    public List<User> getByAttendance(Group group);

    /**
     * Retrieve a {@link List} of {@link User}s that own a given {@link Group}.
     * The entries are sorted by the id of the {@link User} in an ascending
     * order.
     * 
     * @param groupId
     *            of the {@link Group} whose owners to retrieve.
     * @return a {@link List} of {@link User}s that own the specified
     *         {@link Group}.
     * @throws NotFoundException
     *             if no such {@link Group} exists.
     */
    public List<User> getByOwnership(int groupId);

    /**
     * Retrieve a {@link List} of {@link User}s that own a given {@link Group}.
     * The entries are sorted by the id of the {@link User} in an ascending
     * order.
     * 
     * @param group
     *            whose owners to retrieve.
     * @return a {@link List} of {@link User}s that own the specified
     *         {@link Group}.
     * @throws NotFoundException
     *             if no such {@link Group} exists.
     */
    public List<User> getByOwnership(Group group);

    /**
     * Retrieve a {@link List} of {@link User}s that own a given {@link Group}.
     * Multiple {@link Group}s may be specified. The entries are sorted by the
     * id of the {@link User} in an ascending order.
     * 
     * @param groups
     *            whose owners to retrieve.
     * @return a {@link Map} from each specified {@link Group} to a {@link List}
     *         containing the {@link User}s that own the {@link Group}.
     * @throws NotFoundException
     *             if a {@link Group} does not exist.
     */
    public Map<Group, List<User>> getUsersOwningGroups(List<Group> groups);

    /**
     * Retrieve a {@link List} of {@link User}s that own a given {@link Group}.
     * Multiple {@link Group}s may be specified. The entries are sorted by the
     * id of the {@link User} in an ascending order.
     * 
     * @param groupIds
     *            of the {@link Group}s whose owners to retrieve.
     * @return a {@link Map} from the id of each specified {@link Group} to a
     *         {@link List} containing the {@link User}s that own the
     *         {@link Group}.
     * @throws NotFoundException
     *             if a {@link Group} does not exist.
     */
    public Map<Integer, List<User>> getUsersOwningGroups(int[] groupIds);

    /**
     * Retrieve a {@link List} of {@link User}s that own an {@link Group} which
     * contains a given {@link Video}. Multiple {@link Video}s
     * may be specified. The entries are sorted by the id of the {@link User} in
     * an ascending order.
     * 
     * @param videoIds
     *            of the {@link Video} whose {@link Group} owners to
     *            retrieve.
     * @return a {@link Map} from each specified {@link Video} id to a
     *         {@link List} containing the {@link User}s that own the
     *         {@link Group} which contains the {@link Video}.
     * @throws NotFoundException
     *             if a {@link Video} does not exist.
     */
    public Map<Integer, List<User>> getUsersOwningGroupsOfVideos(
            int[] videoIds);

    /**
     * Check if user is owner of a video.
     * 
     * @param userId
     *            of the {@link User} that is the potential owner.
     * @param videoId
     *            of the {@link Video}, the {@link User} is the
     *            potential owner.
     * @return true if {@link User} is owner of the {@link Video}, else
     *         false
     */
    public boolean isUserOwnerOfVideo(final int userId,
            final int videoId);

    /**
     * Check if user is attendant of a video.
     * 
     * @param userId
     *            of the {@link User} that is the potential attendant.
     * @param videoId
     *            of the {@link Video}, the {@link User} is the
     *            potential attendant.
     * @return true if {@link User} is attendant of the {@link Video},
     *         else false
     */
    public boolean isUserAttendantOfGroup(final int userId,
            final int videoId);

    /**
     * Retrieve a {@link User} that created a given
     * {@link AnsweredVideo}.
     * 
     * @param answeredVideoId
     *            of the {@link AnsweredVideo} whose creator to
     *            retrieve.
     * @return the {@link User} who created the specified
     *         {@link AnsweredVideo}. Null, if the {@link Video}
     *         was not created by any {@link User}.
     * @throws NotFoundException
     *             if no such {@link AnsweredVideo} exists.
     */
    public User getForAnsweredVideo(int answeredVideoId);

    /**
     * Retrieve the {@link User}s that created the given
     * {@link AnsweredVideo}s.
     * 
     * @param answeredVideoIds
     *            to get the creator of.
     * @return a map with the the associated {@link User}s of the
     *         {@link AnsweredVideo}s, empty map if none of the given
     *         ids exist.
     */
    public Map<Integer, User> getForAnsweredVideos(
            final int videoId, final int[] answeredVideoIds);
}