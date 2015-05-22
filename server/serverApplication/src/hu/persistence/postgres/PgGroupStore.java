package hu.persistence.postgres;

import hu.model.ESortDirection;
import hu.model.Group;
import hu.model.Video;
import hu.model.users.User;
import hu.persistence.IGroupStore;
import hu.persistence.InconsistencyException;
import hu.persistence.NotFoundException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class implements a {@link IGroupStore} using a PostgreSQL backend.
 */
public class PgGroupStore implements IGroupStore {
    private static final String NOT_FOUND_VIDEO = "Video with id %d does not exist.";
    private static final String NOT_FOUND_USER = "User with id %d does not exist.";
    private static final String USER_NO_OWNER_ATTENDANT = "User with id %d does not %s group with id %d.";
    private static final String NOT_FOUND_GROUP = "Group with id %d does not exist.";
    private static final String COUNT_BY_USER = "SELECT COUNT(\"group\") FROM \"userToGroup\" WHERE \"user\" = ? AND \"role\" = ?::\"enumUserGroupRole\"";
    private static final String SELECT_BY_USER_ORDER = " ORDER BY e.\"title\" %s";
    private static final String SELECT_BY_USER = "SELECT e.\"id\", e.\"title\", e.\"visible\" FROM \"userToGroup\" u INNER JOIN \"group\" e ON (e.\"id\" = u.\"group\") WHERE u.\"role\" = ?::\"enumUserGroupRole\" AND u.\"user\" = ?";
    private static final String SELECT_BY_USER_LIMIT = " LIMIT ? OFFSET ?";
    private static final String COUNT_ALL = "SELECT COUNT(distinct g.\"id\") FROM \"group\" g LEFT JOIN \"userToGroup\" t ON(g.\"id\" = t.\"group\") WHERE ((t.\"role\" = 'owner' AND t.\"user\" = ?)";
    private static final String SELECT_ALL = "SELECT distinct g.\"id\", g.\"title\", g.\"visible\" FROM \"group\" g LEFT JOIN \"userToGroup\" t ON(g.\"id\" = t.\"group\") WHERE ((t.\"role\" = 'owner' AND t.\"user\" = ?)";
    private static final String UPDATE_GROUP = "UPDATE \"group\" SET \"title\" = ?, \"visible\" = ? WHERE \"id\" = ?";
    private static final String INSERT_GROUP = "INSERT INTO \"group\" (\"title\", \"visible\") VALUES (?, ?)";
    private static final String INSERT_GROUP_WITH_ID = "INSERT INTO \"group\" (\"id\", \"title\", \"visible\") VALUES (?, ?, ?)";
    private static final String INSERT_USER = "INSERT INTO \"userToGroup\" (\"user\", \"group\", \"role\") VALUES (?, ?, ?::\"enumUserGroupRole\")";
    private static final String DELETE_USER = "DELETE FROM \"userToGroup\" WHERE \"user\" = ? AND \"group\" = ? AND \"role\" = ?::\"enumUserGroupRole\"";
    private static final String DELETE_GROUP = "DELETE FROM \"group\" WHERE \"id\" = ?";
    private static final String SELECT_BY_VIDEOS = "SELECT q.\"id\" AS \"video\", e.\"id\", e.\"title\", e.\"visible\" FROM \"video\" q INNER JOIN \"group\" e ON (e.\"id\" = q.\"group\") WHERE q.\"id\" IN (%s)";
    private static final String SELECT_BY_VIDEO = "SELECT e.\"id\", e.\"title\", e.\"visible\" FROM \"video\" q INNER JOIN \"group\" e ON (e.\"id\" = q.\"group\") WHERE q.\"id\" = ?";
    private static final String USER_EXISTS = "SELECT NULL FROM \"user\" WHERE \"id\" = ?";
    private static final String SELECT_BY_ID = "SELECT \"id\", \"title\", \"visible\" FROM \"group\" WHERE \"id\" = ?";

    private PgConnectionPool pool;

    /**
     * Initialize a new PgGroupStore.
     * @param pool
     *            for database access.
     */
    PgGroupStore(PgConnectionPool pool) {
        this.pool = pool;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group create(Group group, User initialOwner)
            throws InconsistencyException {
        return this.create(group, initialOwner.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group create(final Group group, final int initialOwnerId)
            throws InconsistencyException {
        final Group[] createdGroup = new Group[1];

        new SQLRunner(this.pool) {
            @Override
            public void runI() throws SQLException, InconsistencyException {
                if (group.getId() != null) {
                    stmt = conn.prepareStatement(INSERT_GROUP_WITH_ID);
                    stmt.setInt(1, group.getId());
                    stmt.setString(2, group.getTitle());
                    stmt.setBoolean(3, group.isVisible());
                } else {
                    stmt = conn.prepareStatement(INSERT_GROUP,
                            Statement.RETURN_GENERATED_KEYS);
                    stmt.setString(1, group.getTitle());
                    stmt.setBoolean(2, group.isVisible());
                }

                stmt.executeUpdate();

                Group ce;
                if (group.getId() == null) {
                    Integer ceId = null;
                    ResultSet rset = stmt.getGeneratedKeys();
                    if (rset.next()) {
                        ceId = rset.getInt(1);
                    }
                    rset.close();

                    ce = new Group(ceId);
                    ce.setTitle(group.getTitle());
                    ce.setVisible(group.isVisible());
                } else {
                    ce = group;
                }
                createdGroup[0] = ce;

                stmt.close();
                stmt = conn.prepareStatement(INSERT_USER);
                stmt.setInt(1, initialOwnerId);
                stmt.setInt(2, ce.getId());
                stmt.setString(3, "owner");
                stmt.executeUpdate();
            }
        }.executeTI();

        return createdGroup[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group save(final Group group) throws InconsistencyException {
        new SQLRunner(this.pool) {
            @Override
            public void runI() throws SQLException, InconsistencyException {
                stmt = conn.prepareStatement(UPDATE_GROUP);
                stmt.setString(1, group.getTitle());
                stmt.setBoolean(2, group.isVisible());
                stmt.setInt(3, group.getId());
                stmt.executeUpdate();
            }
        }.executeI();

        return group;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final int groupId) throws InconsistencyException {
        new SQLRunner(this.pool) {
            @Override
            public void runI() throws SQLException, InconsistencyException {
                stmt = conn.prepareStatement(DELETE_GROUP);
                stmt.setInt(1, groupId);
                if (stmt.executeUpdate() != 1) {
                    throw new InconsistencyException(String.format(
                            NOT_FOUND_GROUP, groupId));
                }
            }
        }.executeI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Group group) throws InconsistencyException {
        this.delete(group.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAttendant(Group group, User user)
            throws InconsistencyException {
        this.addUser(group.getId(), user.getId(), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAttendant(final int groupId, final int userId)
            throws InconsistencyException {
        this.addUser(groupId, userId, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addOwner(Group group, User user) throws InconsistencyException {
        this.addUser(group.getId(), user.getId(), true);
    }

    private void addUser(final int groupId, final int userId,
            final boolean owner) throws InconsistencyException {
        new SQLRunner(this.pool) {
            @Override
            public void runI() throws SQLException, InconsistencyException {
                stmt = conn.prepareStatement(INSERT_USER);
                stmt.setInt(1, userId);
                stmt.setInt(2, groupId);
                stmt.setString(3, (owner) ? "owner" : "attendant");
                stmt.executeUpdate();
            }
        }.executeI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAttendant(Group group, User user)
            throws InconsistencyException {
        this.removeUser(group.getId(), user.getId(), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAttendant(final int groupId, final int userId)
            throws InconsistencyException {
        this.removeUser(groupId, userId, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeOwner(Group group, User user)
            throws InconsistencyException {
        this.removeUser(group.getId(), user.getId(), true);
    }

    private void removeUser(final int groupId, final int userId,
            final boolean owner) throws InconsistencyException {
        new SQLRunner(this.pool) {
            @Override
            public void runI() throws SQLException, InconsistencyException {
                stmt = conn.prepareStatement(DELETE_USER);
                stmt.setInt(1, userId);
                stmt.setInt(2, groupId);
                stmt.setString(3, (owner) ? "owner" : "attendant");
                if (stmt.executeUpdate() != 1) {
                    throw new InconsistencyException(String.format(
                            USER_NO_OWNER_ATTENDANT, userId, (owner) ? "own"
                                    : "attend", groupId));
                }
            }
        }.executeI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group getById(int id) {
        Group group = this.findById(id);
        if (group == null) {
            throw new NotFoundException(String.format(
                    NOT_FOUND_GROUP, id));
        }
        return group;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group findById(final int id) {
        final Group[] group = new Group[1];

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(SELECT_BY_ID);
                stmt.setInt(1, id);
                ResultSet rset = stmt.executeQuery();
                if (rset.next()) {
                    group[0] = deserialize(rset);
                }
            }
        }.execute();

        return group[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Group> getAll(User user) {
        return this.getAll(ESortDirection.ASC, true, true, 0, 0, user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Group> getAll(ESortDirection direction, boolean includeVisible,
            boolean includeHidden, final int limit, final int offset, final User user) {
        final List<Group> groups = new LinkedList<Group>();
        
        final StringBuilder query = new StringBuilder(SELECT_ALL + " OR ");
        if (includeVisible != includeHidden) {
            query.append(" g.\"visible\" = " + ((includeVisible) ? "TRUE" : "FALSE"));
        } else if (!includeHidden && !includeVisible) {
            return groups;
        } else{
            query.append(" 1 = 1 ");
        }
        query.append(" ) ");
        query.append(" ORDER BY g.\"title\" " + ((direction == ESortDirection.DESC) ? "DESC" : "ASC"));
        if (limit > 0) {
            query.append(" LIMIT ? OFFSET ?");
        }
        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(query.toString());
                stmt.setInt(1, ((user != null) ? user.getId() : 0));
                if (limit > 0) {
                    stmt.setInt(2, limit);
                    stmt.setInt(3, offset);
                }
                ResultSet rset = stmt.executeQuery();
                while (rset.next()) {
                    groups.add(deserialize(rset));
                }
            }
        }.execute();

        return groups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCountOfAll(User user) {
        return this.getCountOfAll(true, true, user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCountOfAll(boolean includeVisible, boolean includeHidden, final User user) {
        final int[] count = new int[1];

        final StringBuilder query = new StringBuilder(COUNT_ALL + " OR ");
        if (includeVisible != includeHidden) {
            query.append(" g.\"visible\" = " + ((includeVisible) ? "TRUE" : "FALSE"));
        } else if (!includeHidden && !includeVisible) {
            return 0;
        } else{
            query.append(" 1 = 1 ");
        }
        query.append(" ) ");
        
        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(query.toString());
                stmt.setInt(1, ((user != null) ? user.getId() : 0));
                ResultSet rset = stmt.executeQuery();
                rset.next();
                count[0] = rset.getInt(1);
            }
        }.execute();

        return count[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Group> getByOwner(final int userId) {
        return this.getByUser(userId, true, ESortDirection.ASC, 0, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Group> getByOwner(User user) {
        return this.getByUser(user.getId(), true, ESortDirection.ASC, 0, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Group> getByOwner(User user, ESortDirection direction, int limit, int offset) {
        return this.getByUser(user.getId(), true, direction, limit, offset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCountByOwner(User user) {
        return this.getCountByUser(user.getId(), true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Group> getByAttendant(final int userId) {
        return this.getByUser(userId, false, ESortDirection.ASC, 0, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Group> getByAttendant(User user) {
        return this.getByUser(user.getId(), false, ESortDirection.ASC, 0, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Group> getByAttendant(User user, ESortDirection direction, int limit, int offset) {
        return this.getByUser(user.getId(), false, direction, limit, offset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCountByAttendant(User user) {
        return this.getCountByUser(user.getId(), false);
    }

    private int getCountByUser(final int userId, final boolean owner) {
        final int[] count = new int[1];

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(USER_EXISTS);
                stmt.setInt(1, userId);
                ResultSet rset = stmt.executeQuery();
                if (!rset.next()) {
                    throw new NotFoundException(String.format(NOT_FOUND_USER,
                            userId));
                }

                stmt.close();
                stmt = conn.prepareStatement(COUNT_BY_USER);
                stmt.setInt(1, userId);
                stmt.setString(2, (owner) ? "owner" : "attendant");
                rset = stmt.executeQuery();
                rset.next();
                count[0] = rset.getInt(1);
            }
        }.executeT();

        return count[0];
    }

    private List<Group> getByUser(final int userId, final boolean owner,
            ESortDirection direction, final int limit, final int offset) {
        final List<Group> groups = new LinkedList<Group>();

        final StringBuilder query = new StringBuilder();
        query.append(SELECT_BY_USER);
        query.append(String.format(SELECT_BY_USER_ORDER,
                (direction == ESortDirection.DESC) ? "DESC" : "ASC"));
        if (limit > 0) {
            query.append(SELECT_BY_USER_LIMIT);
        }

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(USER_EXISTS);
                stmt.setInt(1, userId);
                ResultSet rset = stmt.executeQuery();
                if (!rset.next()) {
                    throw new NotFoundException(String.format(NOT_FOUND_USER,
                            userId));
                }

                stmt.close();
                stmt = conn.prepareStatement(query.toString());
                stmt.setString(1, (owner) ? "owner" : "attendant");
                stmt.setInt(2, userId);
                if (limit > 0) {
                    stmt.setInt(3, limit);
                    stmt.setInt(4, offset);
                }

                rset = stmt.executeQuery();
                while (rset.next()) {
                    groups.add(deserialize(rset));
                }
            }
        }.executeT();

        return groups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group getForVideo(final int videoId) {
        final Group[] group = new Group[1];

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(SELECT_BY_VIDEO);
                stmt.setInt(1, videoId);
                ResultSet rset = stmt.executeQuery();
                if (rset.next()) {
                    group[0] = deserialize(rset);
                } else {
                    throw new NotFoundException(String.format(
                            NOT_FOUND_VIDEO, videoId));
                }
            }
        }.execute();

        return group[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group getForVideo(Video video) {
        return this.getForVideo(video.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, Group> getForVideos(final int[] videoIds) {
        final Map<Integer, Group> groups = new HashMap<Integer, Group>();

        if (videoIds.length == 0) {
            return groups;
        }

        final int qcount = videoIds.length;
        final StringBuilder markers = new StringBuilder();
        for (int i = 0; i < qcount; i++) {
            markers.append('?');
            if (i != qcount - 1) {
                markers.append(',');
            }
        }

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(String.format(
                        SELECT_BY_VIDEOS, markers));
                for (int i = 0; i < qcount; i++) {
                    stmt.setInt(i+1, videoIds[i]);
                }

                ResultSet rset = stmt.executeQuery();
                while (rset.next()) {
                    groups.put(rset.getInt("video"), deserialize(rset));
                }
            }
        }.execute();

        for (int id : videoIds) {
            if (!groups.containsKey(id)) {
                throw new NotFoundException(String.format(
                        NOT_FOUND_VIDEO, id));
            }
        }

        return groups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Video, Group> getForVideos(
            final List<Video> videos) {
        final Map<Video, Group> groups = new HashMap<Video, Group>();

        if (videos.size() == 0) {
            return groups;
        }

        final int qcount = videos.size();
        final StringBuilder markers = new StringBuilder();
        for (int i = 0; i < qcount; i++) {
            markers.append('?');
            if (i != qcount - 1) {
                markers.append(',');
            }
        }

        final Map<Integer, Video> qsById = new HashMap<Integer, Video>();
        for (Video q : videos) {
            qsById.put(q.getId(), q);
        }

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(String.format(
                        SELECT_BY_VIDEOS, markers));
                for (int i = 0; i < qcount; i++) {
                    stmt.setInt(i+1, videos.get(i).getId());
                }

                ResultSet rset = stmt.executeQuery();
                while (rset.next()) {
                    groups.put(qsById.get(rset.getInt("video")),
                            deserialize(rset));
                }
            }
        }.execute();

        for (Video q : videos) {
            if (!groups.containsKey(q)) {
                throw new NotFoundException(String.format(
                        NOT_FOUND_VIDEO, q.getId()));
            }
        }

        return groups;
    }

    private Group deserialize(ResultSet group) throws SQLException {
        Group result = new Group(group.getInt("id"));
        result.setTitle(group.getString("title"));
        result.setVisible(group.getBoolean("visible"));
        return result;
    }
}
