package hu.persistence;

import hu.model.ESortDirection;
import hu.model.Group;
import hu.model.Video;
import hu.model.users.EUserType;
import hu.model.users.User;

import java.util.List;
import java.util.Map;

/**
 * This interface describes how {@link Group}s are stored in and retrieved from
 * the persistence layer.
 */
public interface IGroupStore {
    /**
     * Create a new {@link Group}.
     * 
     * All fields except the id have to be provided. If the id is provided it
     * has to be unique for all {@link Group}s. The initial owner must be
     * existent and of the type {@link EUserType#Tutor} or
     * {@link EUserType#Administrator}.
     * 
     * @param group
     *            to create.
     * @param initialOwner
     *            {@link User} that initially owns the {@link Group}.
     * @return the {@link Group} after it was saved, with its id populated.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public Group create(Group group, User initialOwner)
            throws InconsistencyException;

    /**
     * Create a new {@link Group}.
     * 
     * All fields except the id have to be provided. If the id is provided it
     * has to be unique for all {@link Group}s. The initial owner must be
     * existent and of the type {@link EUserType#Tutor} or
     * {@link EUserType#Administrator}.
     * 
     * @param group
     *            to create.
     * @param initialOwnerId
     *            of the {@link User} that initially owns the {@link Group}.
     * @return the {@link Group} after it was saved, with its id populated.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public Group create(Group group, int initialOwnerId)
            throws InconsistencyException;

    /**
     * Save changes made to an {@link Group}.
     * 
     * All fields have to be provided. The id is used to identify which
     * {@link Group} to change and can therefore not be changed.
     * 
     * @param group
     *            to save.
     * @return the {@link Group} after it was saved.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public Group save(Group group) throws InconsistencyException;

    /**
     * Delete an {@link Group}.
     * 
     * If a {@link Group} is deleted, all contained {@link Video}s are
     * also deleted.
     * 
     * A non-existent {@link Group} cannot be deleted.
     * 
     * @param groupId
     *            of the {@link Group} to delete.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public void delete(int groupId) throws InconsistencyException;

    /**
     * Delete an {@link Group}.
     * 
     * If a {@link Group} is deleted, all contained {@link Video}s are
     * also deleted.
     * 
     * A non-existent {@link Group} cannot be deleted.
     * 
     * @param group
     *            to delete.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public void delete(Group group) throws InconsistencyException;

    /**
     * Add an attendant to an {@link Group}.
     * 
     * The {@link User} must not already be an attendant or owner of the
     * specified {@link Group}. Both {@link User} and {@link Group} must be
     * existent.
     * 
     * @param group
     *            to add the attendant to.
     * @param user
     *            to add as the attendant.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public void addAttendant(Group group, User user)
            throws InconsistencyException;

    /**
     * Add an attendant to an {@link Group}.
     * 
     * The {@link User} must not already be an attendant or owner of the
     * specified {@link Group}. Both {@link User} and {@link Group} must be
     * existent.
     * 
     * @param groupId
     *            of the {@link Group} to add the attendant to.
     * @param userId
     *            of the {@link User} to add as the attendant.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public void addAttendant(int groupId, int userId)
            throws InconsistencyException;

    /**
     * Remove an attendant from an {@link Group}.
     * 
     * The {@link User} must be an attendant of the specified {@link Group}.
     * Both {@link User} and {@link Group} must be existent.
     * 
     * @param group
     *            to remove the attendant from.
     * @param user
     *            to remove from the group.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public void removeAttendant(Group group, User user)
            throws InconsistencyException;

    /**
     * Remove an attendant from an {@link Group}.
     * 
     * The {@link User} must be an attendant of the specified {@link Group}.
     * Both {@link User} and {@link Group} must be existent.
     * 
     * @param groupId
     *            of the {@link Group} to remove the attendant from.
     * @param userId
     *            of the {@link User} to remove from the group.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public void removeAttendant(int groupId, int userId)
            throws InconsistencyException;

    /**
     * Add an owner to an {@link Group}.
     * 
     * The {@link User} must not already be an owner or attendant of the
     * specified {@link Group}. The {@link User} must be of the type
     * {@link EUserType#Tutor} or {@link EUserType#Administrator}. Both
     * {@link User} and {@link Group} must be existent.
     * 
     * @param group
     *            to add the owner to.
     * @param user
     *            to add as the owner.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public void addOwner(Group group, User user) throws InconsistencyException;

    /**
     * Remove an owner from an {@link Group}.
     * 
     * The {@link User} must be an owner of the specified {@link Group}. Both
     * {@link User} and {@link Group} must be existent.
     * 
     * @param group
     *            to remove the owner from.
     * @param user
     *            to remove from the group.
     * @throws InconsistencyException
     *             if the constraints described above are not satisfied.
     */
    public void removeOwner(Group group, User user)
            throws InconsistencyException;

    /**
     * Retrieve an {@link Group} with a given id.
     * 
     * @param id
     *            of the {@link Group} to fetch.
     * @return the {@link Group} with the specified id.
     * @throws NotFoundException
     *             if no such {@link Group} exists.
     */
    public Group getById(int id);

    /**
     * Retrieve an {@link Group} with a given id.
     * 
     * @param id
     *            of the {@link Group} to fetch.
     * @return the {@link Group} with the specified id. Null, if no such
     *         {@link Group} exists.
     */
    public Group findById(int id);

    /**
     * Retrieve a {@link List} of all {@link Group}s. The entries are sorted by
     * each {@link Group}s title in an ascending order.
     * 
     * @return a {@link List} of all {@link Group}s.
     */
    public List<Group> getAll(User user);

    /**
     * @param user contains the user the list generated for.
     * @return the amount of all {@link Group}s.
     */
    public int getCountOfAll(User user);

    /**
     * Retrieve a {@link List} of all {@link Group}s. The entries are sorted by
     * each {@link Group}s title in an ascending order. The {@link List} may be
     * modified with the method arguments.
     * 
     * @param direction
     *            in which the list is sorted.
     * @param includeVisible
     *            in the list.
     * @param includeHidden
     *            in the list.
     * @param limit
     *            maximum number of list entries. Or zero for no limitation.
     * @param offset
     *            from the beginning of the list.
     * @param user contains the user the list generated for.
     * @return a {@link List} of {@link Group}s as specified by the method
     *         arguments.
     */
    public List<Group> getAll(ESortDirection direction, boolean includeVisible,
            boolean includeHidden, int limit, int offset, User user);

    /**
     * Retrieve the amount of {@link Group}s.
     * 
     * @param includeVisible
     *            in count.
     * @param includeHidden
     *            in count.
     * @param user contains the user the list generated for.
     * @return the amount of {@link Group}s that match the specified criteria.
     */
    public int getCountOfAll(boolean includeVisible, boolean includeHidden, User user);

    /**
     * Retrieve a {@link List} of all {@link Group}s owned by a given
     * {@link User}. The entries are sorted by each {@link Group}s title in an
     * ascending order.
     * 
     * @param userId
     *            of the {@link User} whose owned {@link Group}s to retrieve.
     * @return a {@link List} of the {@link Group}s owned by the specified
     *         {@link User}.
     * @throws NotFoundException
     *             if no such {@link User} exists.
     */
    public List<Group> getByOwner(int userId);

    /**
     * Retrieve a {@link List} of all {@link Group}s owned by a given
     * {@link User}. The entries are sorted by each {@link Group}s title in an
     * ascending order.
     * 
     * @param user
     *            whose owned {@link Group}s to retrieve.
     * @return a {@link List} of the {@link Group}s owned by the specified
     *         {@link User}.
     * @throws NotFoundException
     *             if no such {@link User} exists.
     */
    public List<Group> getByOwner(User user);

    /**
     * Retrieve a {@link List} of all {@link Group}s owned by a given
     * {@link User}. The entries are sorted by each {@link Group}s title in an
     * ascending order. The {@link List} may be modified with the method
     * arguments.
     * 
     * @param user
     *            whose owned {@link Group}s to retrieve.
     * @param direction
     *            in which the list is sorted.
     * @param limit
     *            maximum number of list entries. Or zero for no limitation.
     * @param offset
     *            from the beginning of the list.
     * @return a {@link List} of the {@link Group}s owned by the specified
     *         {@link User}.
     * @throws NotFoundException
     *             if no such {@link User} exists.
     */
    public List<Group> getByOwner(User user, ESortDirection direction, int limit,
            int offset);

    /**
     * Retrieve the amount of {@link Group}s owned by a given {@link User}.
     * 
     * @param user
     *            that owns the {@link Group}s.
     * @return the amount of {@link Group}s owned by the specified {@link User}.
     */
    public int getCountByOwner(User user);

    /**
     * Retrieve a {@link List} of all {@link Group}s attended by a certain
     * {@link User}. The entries are sorted by each {@link Group}s title in an
     * ascending order.
     * 
     * @param userId
     *            of the {@link User} whose attended {@link Group}s to retrieve.
     * @return a {@link List} of the {@link Group}s attended by the specified
     *         {@link User}.
     * @throws NotFoundException
     *             if no such {@link User} exists.
     */
    public List<Group> getByAttendant(int userId);

    /**
     * Retrieve a {@link List} of all {@link Group}s attended by a certain
     * {@link User}. The entries are sorted by each {@link Group}s title in an
     * ascending order.
     * 
     * @param user
     *            whose attended {@link Group}s to retrieve.
     * @return a {@link List} of the {@link Group}s attended by the specified
     *         {@link User}.
     * @throws NotFoundException
     *             if no such {@link User} exists.
     */
    public List<Group> getByAttendant(User user);

    /**
     * Retrieve a {@link List} of all {@link Group}s attended by a given
     * {@link User}. The entries are sorted by each {@link Group}s title in an
     * ascending order. The {@link List} may be modified with the method
     * arguments.
     * 
     * @param user
     *            whose attended {@link Group}s to retrieve.
     * @param direction
     *            in which the list is sorted.
     * @param limit
     *            maximum number of list entries. Or zero for no limitation.
     * @param offset
     *            from the beginning of the list.
     * @return a {@link List} of the {@link Group}s attended by the specified
     *         {@link User}.
     * @throws NotFoundException
     *             if no such {@link User} exists.
     */
    public List<Group> getByAttendant(User user, ESortDirection direction,
            int limit, int offset);

    /**
     * Retrieve the amount of {@link Group}s attended by a given {@link User}.
     * 
     * @param user
     *            that attends the {@link Group}s.
     * @return the amount of {@link Group}s attended by the specified
     *         {@link User}.
     */
    public int getCountByAttendant(User user);

    /**
     * Retrieve the {@link Group} which contains a given {@link Video}.
     * 
     * @param videoId
     *            of the {@link Video} whose {@link Group} to retrieve.
     * @return the {@link Group} that contains the given {@link Video}.
     * @throws NotFoundException
     *             if no such {@link Video} exists.
     */
    public Group getForVideo(int videoId);

    /**
     * Retrieve the {@link Group} which contains a given {@link Video}.
     * 
     * @param video
     *            whose {@link Group} to retrieve.
     * @return the {@link Group} that contains the given {@link Video}.
     * @throws NotFoundException
     *             if no such {@link Video} exists.
     */
    public Group getForVideo(Video video);

    /**
     * Retrieve the {@link Group} which contains a given {@link Video}.
     * Multiple {@link Video}s may be provided.
     * 
     * @param videoIds
     *            of the {@link Video}s whose {@link Group}s to
     *            retrieve.
     * @return a {@link Map} from each specified {@link Video} id to its
     *         related {@link Group}.
     * @throws NotFoundException
     *             if a {@link Video} does not exist.
     */
    public Map<Integer, Group> getForVideos(int[] videoIds);

    /**
     * Retrieve the {@link Group} which contains a given {@link Video}.
     * Multiple {@link Video}s may be provided.
     * 
     * @param videos
     *            whose {@link Group}s to retrieve.
     * @return a {@link Map} from each specified {@link Video} to its
     *         related {@link Group}.
     * @throws NotFoundException
     *             if a {@link Video} does not exist.
     */
    public Map<Video, Group> getForVideos(
            List<Video> videos);
}