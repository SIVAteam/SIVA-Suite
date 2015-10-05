package hu.persistence.postgres;

import hu.model.ESortDirection;
import hu.model.Group;
import hu.model.users.EGender;
import hu.model.users.ESortColumnUser;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.IUserStore;
import hu.persistence.InconsistencyException;
import hu.persistence.NotFoundException;
import hu.util.ECountry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class implements a {@link IUserStore} using a PostgreSQL backend.
 */
public class PgUserStore implements IUserStore {

    private static final String NOT_FOUND_GROUPS = "Not all specified groups do exist.";
    private static final String GROUPS_EXIST = "SELECT COUNT(*) FROM \"group\" WHERE \"id\" IN (%s)";
    private static final String NOT_FOUND_VIDEOS = "Not all specified videos do exist.";
    private static final String VIDEOS_EXIST = "SELECT COUNT(*) FROM \"video\" WHERE \"id\" IN (%s)";
    private static final String SELECT_ANSWERED_VIDEO_BY_ID = "SELECT * FROM \"answeredVideo\" WHERE \"id\" = ?";
    private static final String NOT_FOUND_ANSWERED_VIDEO = "Answered video with id %d does not exist!";
    private static final String SELECT_GROUP_BY_ID = "SELECT * FROM \"group\" WHERE \"id\" = ?";
    private static final String NOT_FOUND_GROUP = "Group with id %d does not exist!";
    private static final String NOT_FOUND_USER_EMAIL = "User with email %s does not exist.";
    private static final String NOT_FOUND_USER_ID = "User with id %d does not exist.";
    private static final String SELECT_BY_TYPE = "SELECT * FROM \"user\" WHERE \"type\" = ?::\"enumUserType\" ORDER BY \"id\" ASC";
    private static final String SELECT_EMAIL_FOR_ANSWEREDVIDEO = "SELECT aq.\"id\", u.* FROM \"answeredVideo\" aq, \"user\" u WHERE aq.\"user\" = u.id AND aq.video = ? AND aq.id IN (%s)";
    private static final String SELECT_USERS_OWNING_GROUPS_OF_VIDEOS = "SELECT q.\"id\" AS \"video\", u.* FROM \"video\" q INNER JOIN \"group\" e ON (e.\"id\" = q.\"group\") INNER JOIN \"userToGroup\" ute ON (ute.\"group\" = e.\"id\" AND ute.\"role\" = 'owner') INNER JOIN \"user\" u ON (ute.\"user\" = u.\"id\") WHERE q.\"id\" IN (%s)";
    private static final String SELECT_GROUP_ATTENDANTS_BY_GROUP_ID = "SELECT u.* FROM \"userToGroup\" e INNER JOIN \"user\" u ON (u.\"id\" = e.\"user\") WHERE e.\"group\" = ? AND e.\"role\" = 'attendant' ORDER BY u.\"id\" ASC";
    private static final String DELETE_USER = "DELETE FROM \"user\" WHERE \"id\" = ?";
    private static final String UPDATE_USER_WITHOUT_PASSWORD = "UPDATE \"user\" SET \"title\" = ?, \"firstName\" = ?, \"lastName\" = ?, \"email\" = ?, \"gender\" = ?::\"enumGender\", \"birthday\" = ?, \"banned\" = ?, \"deletable\" = ?, \"type\" = ?::\"enumUserType\", \"street\" = ?, \"zip\" = ?, \"city\" = ?, \"country\" = ?, \"phone\" = ?, \"fax\" = ?, \"website\" = ?, \"visible\" = ?, \"photoAvailable\" = ?, \"externUserId\" = ? WHERE \"id\" = ?";
    private static final String UPDATE_USER = "UPDATE \"user\" SET \"title\" = ?, \"firstName\" = ?, \"lastName\" = ?, \"email\" = ?, \"gender\" = ?::\"enumGender\", \"birthday\" = ?, \"banned\" = ?, \"deletable\" = ?, \"type\" = ?::\"enumUserType\", \"street\" = ?, \"zip\" = ?, \"city\" = ?, \"country\" = ?, \"phone\" = ?, \"fax\" = ?, \"website\" = ?, \"visible\" = ?, \"photoAvailable\" = ?, \"passwordHash\" = ?, \"externUserId\" = ? WHERE \"id\" = ?";
    private static final String SELECT_BY_EMAIL = "SELECT * FROM \"user\" WHERE LOWER(\"email\") = LOWER(?)";
    private static final String SELECT_GROUP_OWNERS = "SELECT e.\"group\", u.* FROM \"userToGroup\" e INNER JOIN \"user\" u ON (u.\"id\" = e.\"user\") WHERE e.\"group\" IN (%s) AND e.\"role\" = 'owner' ORDER BY u.\"id\" ASC";
    private static final String SELECT_GROUP_OWNERS_BY_GROUP_ID = "SELECT u.* FROM \"userToGroup\" e INNER JOIN \"user\" u ON (u.\"id\" = e.\"user\") WHERE e.\"group\" = ? AND e.\"role\" = 'owner' ORDER BY u.\"id\" ASC";
    private static final String SELECT_BY_ID = "SELECT * FROM \"user\" WHERE id = ?";
    private static final String SELECT_BY_EXTERN_USER_ID = "SELECT * FROM \"user\" WHERE \"externUserId\" = ?";
    private static final String SELECT_ALL = "SELECT * FROM \"user\"";
    private static final String SELECT_BY_SEARCH_TERM = "SELECT * FROM \"user\" WHERE (LOWER(\"firstName\") = LOWER(?) OR LOWER(\"lastName\") = LOWER(?) OR LOWER(\"email\") = LOWER(?))";
    private static final String COUNT_ALL = "SELECT COUNT(\"id\") FROM \"user\"";
    private static final String COUNT_BY_SEARCH_TERM = "SELECT COUNT(\"id\") FROM \"user\" WHERE (LOWER(\"firstName\") = LOWER(?) OR LOWER(\"lastName\") = LOWER(?) OR LOWER(\"email\") = LOWER(?))";
    private static final String CREATE_USER = "INSERT INTO \"user\" (\"passwordHash\", \"email\", \"title\", \"firstName\", \"lastName\", \"type\", \"gender\", \"birthday\", \"banned\", \"deletable\", \"street\", \"zip\", \"city\", \"country\", \"phone\", \"fax\", \"website\", \"visible\", \"externUserId\") VALUES (?, ?, ?, ?, ?, ?::\"enumUserType\", ?::\"enumGender\", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String CREATE_USER_WITH_ID = "INSERT INTO \"user\" (\"id\", \"passwordHash\", \"email\", \"title\", \"firstName\", \"lastName\", \"type\", \"gender\", \"birthday\", \"banned\", \"deletable\", \"street\", \"zip\", \"city\", \"country\", \"phone\", \"fax\", \"website\", \"visible\", \"externUserId\") VALUES (?, ?, ?, ?, ?, ?, ?::\"enumUserType\", ?::\"enumGender\", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ANSWERED_VIDEO = "SELECT \"user\" FROM \"answeredVideo\" WHERE \"id\" = ?";
    private static final String GET_USER_IS_OWNER_OF_VIDEO = "SELECT u.\"user\" FROM \"userToGroup\" u, \"video\" v WHERE u.\"group\" = v.\"group\" AND u.\"user\" = ? AND u.\"role\" = \'owner\' AND v.\"id\" = ?";
    private static final String GET_USER_IS_ATTENDANT_OF_VIDEO = "SELECT u.\"user\" FROM \"userToGroup\" u, \"video\" v WHERE u.\"group\" = v.\"group\" AND u.\"user\" = ? AND u.\"role\" = \'attendant\' AND v.\"id\" = ?";

    private PgConnectionPool pool;

    /**
     * Initialize a new PgUserStore.
     * 
     * @param pool
     *            for database access.
     */
    PgUserStore(PgConnectionPool pool) {
        this.pool = pool;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public User getById(int id) {
        User user = this.findById(id);
        if (user == null) {
            throw new NotFoundException(String.format(NOT_FOUND_USER_ID, id));
        }
        return user;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public User getByEmail(String email) {
        User user = this.findByEmail(email);
        // does user exist
        if (user == null) {
            throw new NotFoundException(String.format(NOT_FOUND_USER_EMAIL,
                    email));
        }
        return user;
    }

    /**
     * Deserialize a given result set to a {@link User}.
     * 
     * @param user
     *            to deserialize.
     * @return the deserialized {@link User}.
     * @throws SQLException
     */
    private User deserialize(ResultSet user) throws SQLException {
        User resultUser = new User(user.getInt("id"));

        String title = user.getString("title");
        if (user.wasNull())
            title = null;
        resultUser.setTitle(title);

        resultUser.setFirstName(user.getString("firstname"));
        resultUser.setLastName(user.getString("lastname"));
        resultUser.setEmail(user.getString("email"));

        String gender = user.getString("gender");

	if (gender != null) {
	    if (gender.equals(EGender.Female.toString().toLowerCase())) {
		resultUser.setGender(EGender.Female);
	    } else if (gender.equals(EGender.Male.toString().toLowerCase())) {
		resultUser.setGender(EGender.Male);
	    }
	}

        if(user.getDate("birthday") != null)
        	resultUser.setBirthday(new java.util.Date(user.getDate("birthday")
                    .getTime()));
        else
        	resultUser.setBirthday(null);

        resultUser.setPasswordHash(user.getString("passwordHash"));
        resultUser.setStreet(user.getString("street"));
        resultUser.setZip(user.getString("zip"));
        resultUser.setCity(user.getString("city"));
        
	String country = user.getString("country");
	if (country != null) {
	    for (ECountry e : ECountry.values()) {
		if (country.equalsIgnoreCase(e.toString())) {
		    resultUser.setCountry(e);
		    break;
		}
	    }
	}
        
        resultUser.setPhone(user.getString("phone"));
        resultUser.setFax(user.getString("fax"));
        resultUser.setWebsite(user.getString("website"));
        resultUser.setVisible(user.getBoolean("visible"));
        resultUser.setPhotoAvailable(user.getBoolean("photoAvailable"));
        resultUser.setSecretKey(user.getString("secretKey").replaceAll("([^a-zA-Z0-9]+)", ""));
        resultUser.setBanned(user.getBoolean("banned"));
        resultUser.setDeletable(user.getBoolean("deletable"));
        
        resultUser.setExternUserId(user.getString("externUserId"));

        String type = user.getString("type");
        for (EUserType e : EUserType.values()) {
            if (type.equals(e.toString().toLowerCase())) {
                resultUser.setUserType(e);
            }
        }
        return resultUser;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<User> getByType(final EUserType type) {
        final List<User> users = new LinkedList<User>();

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(SELECT_BY_TYPE);
                stmt.setString(1, type.toString().toLowerCase());

                // Execute SQL query and add fetched users to list.
                ResultSet rset = stmt.executeQuery();
                while (rset.next()) {
                    users.add(deserialize(rset));
                }
            }
        }.execute();

        return users;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<User> getAll(ESortColumnUser sortColumn,
            ESortDirection sortDirection, EUserType userType, int firstRow,
            int rows) {
        final List<User> users = new LinkedList<User>();

        final StringBuilder query = new StringBuilder(SELECT_ALL);

        // Append WHERE to SQL query if a user type is specified
        if (userType != null) {
            query.append(" WHERE \"type\" =  '"
                    + userType.toString().toLowerCase() + "' ");
        }

        // Append ORDER BY to SQL query using the specified column and order
        if (sortColumn.equals(ESortColumnUser.Name)) {
            query.append(" ORDER BY \"lastName\" " + sortDirection
                    + ", \"firstName\" " + sortDirection + ", \"title\" "
                    + sortDirection);
            if (sortDirection.equals(ESortDirection.ASC)) {
                query.append(" NULLS FIRST");
            } else {
                query.append(" NULLS LAST");
            }
        } else if (sortColumn.equals(ESortColumnUser.Email)) {
            query.append(" ORDER BY \"email\" " + sortDirection);
        }    
        
        // Append OFFSET to SQL query if it is bigger than 0
        if(firstRow > 0) {
            query.append(" OFFSET " + firstRow);
        }
        
        // Append LIMIT to SQL query if greater than 0
        if(rows > 0){
            query.append(" LIMIT " + rows);
        }

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(query.toString());

                // Execute SQL query and add fetched users to list
                ResultSet rset = stmt.executeQuery();
                while (rset.next()) {
                    users.add(deserialize(rset));
                }
            }
        }.execute();

        return users;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public int getCountOfAll(EUserType userType) {
        final int[] count = new int[1];

        final StringBuilder query = new StringBuilder(COUNT_ALL);

        // Append WHERE to SQL query if a user type is specified
        if (userType != null) {
            query.append(" WHERE \"type\" =  '"
                    + userType.toString().toLowerCase() + "' ");
        }

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(query.toString());

                // Execute SQL query and set fetched amount of users
                ResultSet rset = stmt.executeQuery();
                rset.next();
                count[0] = rset.getInt(1);
            }
        }.execute();

        return count[0];
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<User> search(final String searchTerm,
            ESortColumnUser sortColumn, ESortDirection sortDirection,
            EUserType userType, int firstRow, int rows) {
        final List<User> users = new LinkedList<User>();

        final StringBuilder query = new StringBuilder(SELECT_BY_SEARCH_TERM);

        // Append WHERE to SQL query if a user type is specified
        if (userType != null) {
            query.append(" AND \"type\" =  '"
                    + userType.toString().toLowerCase() + "' ");
        }

        // Append ORDER BY to SQL query using the specified column and order
        if (sortColumn.equals(ESortColumnUser.Name)) {
            query.append(" ORDER BY \"lastName\" " + sortDirection
                    + ", \"firstName\" " + sortDirection + ", \"title\" "
                    + sortDirection);
            if (sortDirection.equals(ESortDirection.ASC)) {
                query.append(" NULLS FIRST");
            } else {
                query.append(" NULLS LAST");
            }
        } else if (sortColumn.equals(ESortColumnUser.Email)) {
            query.append(" ORDER BY \"email\" " + sortDirection);
        }

        // Append OFFSET to SQL query
        query.append(" OFFSET " + firstRow);
        
        // Append LIMIT to SQL query if greater than 0
        if(rows > 0){
            query.append(" LIMIT " + rows);
        }

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(query.toString());

                // Populate search term in SQL query
                stmt.setString(1, searchTerm);
                stmt.setString(2, searchTerm);
                stmt.setString(3, searchTerm);

                // Execute SQL query and add fetched users to list
                ResultSet rset = stmt.executeQuery();
                while (rset.next()) {
                    users.add(deserialize(rset));
                }
            }
        }.execute();

        return users;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public int getCountOfSearch(final String searchTerm, EUserType userType) {
        final int[] count = new int[1];

        final StringBuilder query = new StringBuilder(COUNT_BY_SEARCH_TERM);

        // Append WHERE to SQL query if a user type is specified
        if (userType != null) {
            query.append(" AND \"type\" =  '"
                    + userType.toString().toLowerCase() + "' ");
        }

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(query.toString());

                // Populate search term in SQL query
                stmt.setString(1, searchTerm);
                stmt.setString(2, searchTerm);
                stmt.setString(3, searchTerm);

                // Execute SQL query and set fetched amount of users
                ResultSet rset = stmt.executeQuery();
                rset.next();
                count[0] = rset.getInt(1);
            }
        }.execute();

        return count[0];
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public User save(final User user) throws InconsistencyException {

        new SQLRunner(this.pool) {
            public void runI() throws SQLException {
                // Decide if new Password should be set
                if (user.getPasswordHash() != null) {
                    stmt = conn.prepareStatement(UPDATE_USER);
                } else {
                    stmt = conn.prepareStatement(UPDATE_USER_WITHOUT_PASSWORD);
                }
                // Set title to SQL NULL if empty
                if (user.getTitle() == null || user.getTitle().length() == 0) {
                    user.setTitle(null);
                }

                stmt.setString(1, user.getTitle());
                stmt.setString(2, user.getFirstName());
                stmt.setString(3, user.getLastName());
                stmt.setString(4, user.getEmail());
                stmt.setString(5, ((user.getGender() != null) ? user.getGender().toString().toLowerCase() : null));
                if(user.getBirthday() != null)
                	stmt.setDate(6, (new java.sql.Date(user.getBirthday()
                        .getTime())));
                else
                	stmt.setDate(6, null);
                
                stmt.setBoolean(7, user.isBanned());
                stmt.setBoolean(8, user.isDeletable());
                stmt.setString(9, ((user.getUserType() != null) ? user.getUserType().toString()
                        .toLowerCase() : null));
                
                stmt.setString(10, user.getStreet());
                stmt.setString(11, user.getZip());
                stmt.setString(12, user.getCity());
                stmt.setString(13, ((user.getCountry() != null) ? user.getCountry().toString() : null));
                stmt.setString(14, user.getPhone());
                stmt.setString(15, user.getFax());
                stmt.setString(16, user.getWebsite());
                stmt.setBoolean(17, user.isVisible());
                stmt.setBoolean(18, user.isPhotoAvailable());
                
                if (user.getPasswordHash() != null) {
                    stmt.setString(19, user.getPasswordHash());
                    stmt.setString(20, user.getExternUserId());
					stmt.setInt(21, user.getId());
                } else {
					stmt.setString(19, user.getExternUserId());
                    stmt.setInt(20, user.getId());
                }
                
                stmt.executeUpdate();
            }
        }.executeI();
        return user;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void delete(final int userId) throws InconsistencyException {
        new SQLRunner(this.pool) {
            @Override
            public void runI() throws SQLException, InconsistencyException {
                stmt = conn.prepareStatement(DELETE_USER);
                stmt.setInt(1, userId);
                if (stmt.executeUpdate() != 1) {
                    throw new InconsistencyException(String.format(
                            NOT_FOUND_USER_ID, userId));
                }
            }
        }.executeI();
        return;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void delete(User user) throws InconsistencyException {
        this.delete(user.getId());
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<User> getByAttendance(final int groupId) {
        final List<User> attendants = new LinkedList<User>();

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(SELECT_GROUP_BY_ID);
                stmt.setInt(1, groupId);
                if (!stmt.executeQuery().next()) {
                    throw new NotFoundException(String.format(NOT_FOUND_GROUP,
                            groupId));
                }
                
                stmt = conn.prepareStatement(SELECT_GROUP_ATTENDANTS_BY_GROUP_ID);
                stmt.setInt(1, groupId);

                ResultSet rset = stmt.executeQuery();
                while (rset.next()) {
                    attendants.add(deserialize(rset));
                }
            }
        }.executeT();

        return attendants;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<User> getByAttendance(Group group) {
        return this.getByAttendance(group.getId());
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<User> getByOwnership(final int groupId) {
        final List<User> owners = new LinkedList<User>();

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(SELECT_GROUP_BY_ID);
                stmt.setInt(1, groupId);
                if (!stmt.executeQuery().next()) {
                    throw new NotFoundException(String.format(NOT_FOUND_GROUP,
                            groupId));
                }
                
                stmt = conn.prepareStatement(SELECT_GROUP_OWNERS_BY_GROUP_ID);

                // Populate search term in SQL query
                stmt.setInt(1, groupId);

                // Execute SQL query and add fetched owners to list
                ResultSet rset = stmt.executeQuery();
                while (rset.next()) {
                    owners.add(deserialize(rset));
                }
            }
        }.executeT();

        return owners;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<User> getByOwnership(Group group) {
        return this.getByOwnership(group.getId());
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Map<Integer, List<User>> getUsersOwningGroupsOfVideos(
            final int[] videoIds) {
        final Map<Integer, List<User>> owners = new HashMap<Integer, List<User>>();

        if (videoIds.length == 0) {
            return owners;
        }

        // Create markers for IN statement.
        final int qcount = videoIds.length;
        final StringBuilder markers = new StringBuilder();
        for (int i = 0; i < qcount; i++) {
            markers.append('?');
            if (i != qcount - 1) {
                markers.append(',');
            }
        }

        // Populate map of owners.
        for (int id : videoIds) {
            owners.put(id, new LinkedList<User>());
        }

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                // Verify that all specified videos do exist.
                stmt = conn.prepareStatement(String.format(VIDEOS_EXIST, markers));
                for (int i = 0; i < qcount; i++) {
                    stmt.setInt(i + 1, videoIds[i]);
                }
                ResultSet rset = stmt.executeQuery();
                if (!rset.next() || rset.getInt(1) != qcount) {
                    throw new NotFoundException(NOT_FOUND_VIDEOS);
                }
                stmt.close();

                stmt = conn.prepareStatement(String.format(
                        SELECT_USERS_OWNING_GROUPS_OF_VIDEOS,
                        markers));
                // Populate markers of IN statement.
                for (int i = 0; i < qcount; i++) {
                    stmt.setInt(i + 1, videoIds[i]);
                }

                // Fetch the owners of each video.
                rset = stmt.executeQuery();
                while (rset.next()) {
                    Integer qid = rset.getInt("video");
                    owners.get(qid).add(deserialize(rset));
                }
            }
        }.executeT();

        return owners;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public User getForAnsweredVideo(final int answeredVideoId) {
        final User[] returnUser = new User[1];
        
        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn
                        .prepareStatement(SELECT_ANSWERED_VIDEO_BY_ID);
                stmt.setInt(1, answeredVideoId);
                if (!stmt.executeQuery().next()) {
                    throw new NotFoundException(String.format(
                            NOT_FOUND_ANSWERED_VIDEO,
                            answeredVideoId));
                }
                
                stmt = conn.prepareStatement(SELECT_BY_ANSWERED_VIDEO);
                stmt.setInt(1, answeredVideoId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    returnUser[0] = findById(rs.getInt(1));
                }
            }
        }.executeT();
        return returnUser[0];
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Map<Integer, User> getForAnsweredVideos(
            final int videoId, final int[] answeredVideoIds) {
        final Map<Integer, User> creators = new HashMap<Integer, User>();

        if (answeredVideoIds.length == 0) {
            return creators;
        }

        // Create markers for IN statement.
        final StringBuilder markers = new StringBuilder();
        for (int i = 0; i < answeredVideoIds.length; i++) {
            markers.append('?');
            if (i != answeredVideoIds.length - 1) {
                markers.append(',');
            }
        }

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(String.format(
                        SELECT_EMAIL_FOR_ANSWEREDVIDEO, markers));
                stmt.setInt(1, videoId);

                // Populate markers of IN statement.
                for (int i = 0; i < answeredVideoIds.length; i++) {
                    stmt.setInt(i + 2, answeredVideoIds[i]);
                }

                // Execute query and write to map
                ResultSet rst = stmt.executeQuery();
                while (rst.next()) {
                    int questionId = rst.getInt("id");
                    creators.put(questionId, deserialize(rst));
                }
            }
        }.execute();

        return creators;
    }
    
    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public User create(final User user) throws InconsistencyException {

        final User[] cu = new User[1];
        new SQLRunner(this.pool) {
            @Override
            public void runI() throws SQLException, InconsistencyException {
                if (user.getId() != null) {
                    stmt = conn.prepareStatement(CREATE_USER_WITH_ID);
                    stmt.setInt(1, user.getId());
                    stmt.setString(2, user.getPasswordHash());
                    stmt.setString(3, user.getEmail());

                    // Set title to SQL NULL if empty
                    if(user.getTitle() == null || user.getTitle().length() == 0){
                        user.setTitle(null);
                    }
                    stmt.setString(4, user.getTitle());

                    stmt.setString(5, user.getFirstName());
                    stmt.setString(6, user.getLastName());
                    stmt.setString(7, ((user.getUserType() != null) ? user.getUserType().toString()
                            .toLowerCase() : null));
                    stmt.setString(8, ((user.getGender() != null) ? user.getGender().toString().toLowerCase() : null));
                    if(user.getBirthday() != null)
                    	stmt.setDate(9, (new java.sql.Date(user.getBirthday()
                            .getTime())));
                    else
                    	stmt.setDate(9, null);
                    stmt.setBoolean(10, user.isBanned());
                    stmt.setBoolean(11, user.isDeletable());
                    stmt.setString(12, user.getStreet());
                    stmt.setString(13, user.getZip());
                    stmt.setString(14, user.getCity());
                    stmt.setString(15, ((user.getCountry() != null) ? user.getCountry().toString() : null));
                    stmt.setString(16, user.getPhone());
                    stmt.setString(17, user.getFax());
                    stmt.setString(18, user.getWebsite());
                    stmt.setBoolean(19, user.isVisible());
                    stmt.setString(20, user.getExternUserId());
                    
                    // without user id (the common case!)
                } else {
                    stmt = conn.prepareStatement(CREATE_USER,
                            Statement.RETURN_GENERATED_KEYS);
                    stmt.setString(1, user.getPasswordHash());
                    stmt.setString(2, user.getEmail());

                    // Set title to SQL NULL if empty
                    if(user.getTitle() == null || user.getTitle().length() == 0){
                        user.setTitle(null);
                    }
                    stmt.setString(3, user.getTitle());

                    stmt.setString(4, user.getFirstName());
                    stmt.setString(5, user.getLastName());
                    stmt.setString(6, ((user.getUserType() != null) ? user.getUserType().toString()
                            .toLowerCase() : null));
                    stmt.setString(7, ((user.getGender() != null) ? user.getGender().toString().toLowerCase() : null));
                    if(user.getBirthday() != null)
                    	stmt.setDate(8, (new java.sql.Date(user.getBirthday()
                            .getTime())));
                    else
                    	stmt.setDate(8, null);
                    stmt.setBoolean(9, user.isBanned());
                    stmt.setBoolean(10, user.isDeletable());
                    stmt.setString(11, user.getStreet());
                    stmt.setString(12, user.getZip());
                    stmt.setString(13, user.getCity());
                    stmt.setString(14, ((user.getCountry() != null) ? user.getCountry().toString() : null));
                    stmt.setString(15, user.getPhone());
                    stmt.setString(16, user.getFax());
                    stmt.setString(17, user.getWebsite());
                    stmt.setBoolean(18, user.isVisible());   
                    stmt.setString(19, user.getExternUserId());
                }

                stmt.executeUpdate();

                if (user.getId() == null) {
                    Integer cuId = null;
                    ResultSet rset = stmt.getGeneratedKeys();

                    if (rset.next()) {
                        cuId = rset.getInt(1);
                    }
                    rset.close();
                    cu[0] = createNewUserObjectWithId(cuId, user);
                } else {
                    cu[0] = user;
                }
                stmt.close();
            }
        }.executeTI();
        return cu[0];
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public User findById(final int id) {
        final User[] user = new User[1];

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(SELECT_BY_ID);
                stmt.setInt(1, id);
                ResultSet rset = stmt.executeQuery();
                if (rset.next()) {
                    user[0] = deserialize(rset);
                }
            }
        }.execute();

        return user[0];
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public User findByEmail(final String email) {
        final User[] user = new User[1];
        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(SELECT_BY_EMAIL);
                stmt.setString(1, email);
                ResultSet rset = stmt.executeQuery();
                if (rset.next()) {
                    user[0] = deserialize(rset);
                }
            }
        }.execute();
        return user[0];
    }
    
    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public User findByExternUserId(final String externUserid) {
        final User[] user = new User[1];

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn.prepareStatement(SELECT_BY_EXTERN_USER_ID);
                stmt.setString(1, externUserid);
                ResultSet rset = stmt.executeQuery();
                if (rset.next()) {
                    user[0] = deserialize(rset);
                }
            }
        }.execute();

        return user[0];
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Map<Group, List<User>> getUsersOwningGroups(final List<Group> groups) {
        final Map<Group, List<User>> owners = new HashMap<Group, List<User>>();

        if (groups.size() == 0) {
            return owners;
        }

        // Create markers for IN statement.
        final int ecount = groups.size();
        final StringBuilder markers = new StringBuilder();
        for (int i = 0; i < ecount; i++) {
            markers.append('?');
            if (i != ecount - 1) {
                markers.append(',');
            }
        }

        // Provide group lookup by id and populate map of owners.
        final Map<Integer, Group> groupsById = new HashMap<Integer, Group>();
        for (Group e : groups) {
            groupsById.put(e.getId(), e);
            owners.put(e, new LinkedList<User>());
        }

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                // Verify that all specified groups do exist.
                stmt = conn.prepareStatement(String.format(
                        GROUPS_EXIST, markers));
                int i = 0;
                for (Group e : groups) {
                    stmt.setInt(i + 1, e.getId());
                    i++;
                }
                ResultSet rset = stmt.executeQuery();
                if (!rset.next() || rset.getInt(1) != ecount) {
                    throw new NotFoundException(NOT_FOUND_GROUPS);
                }
                stmt.close();

                stmt = conn.prepareStatement(String.format(SELECT_GROUP_OWNERS,
                        markers));
                // Populate markers of IN statement.
                i = 0;
                for (Group e : groups) {
                    stmt.setInt(i + 1, e.getId());
                    i++;
                }

                // Fetch the owners of each group.
                rset = stmt.executeQuery();
                while (rset.next()) {
                    Group group = groupsById.get(rset.getInt("group"));
                    owners.get(group).add(deserialize(rset));
                }
            }
        }.executeT();

        return owners;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Map<Integer, List<User>> getUsersOwningGroups(final int[] groupIds) {
        final Map<Integer, List<User>> owners = new HashMap<Integer, List<User>>();

        if (groupIds.length == 0) {
            return owners;
        }

        // Create markers for IN statement.
        final StringBuilder markers = new StringBuilder();
        for (int i = 0; i < groupIds.length; i++) {
            markers.append('?');
            if (i != groupIds.length - 1) {
                markers.append(',');
            }
        }

        // Populate map of owners with empty lists.
        for (int id : groupIds) {
            owners.put(id, new LinkedList<User>());
        }

        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                // Verify that all specified groups do exist.
                stmt = conn.prepareStatement(String.format(
                        GROUPS_EXIST, markers));
                for (int i = 0; i < groupIds.length; i++) {
                    stmt.setInt(i + 1, groupIds[i]);
                }
                ResultSet rset = stmt.executeQuery();
                if (!rset.next() || rset.getInt(1) != groupIds.length) {
                    throw new NotFoundException(NOT_FOUND_GROUPS);
                }
                stmt.close();

                stmt = conn.prepareStatement(String.format(SELECT_GROUP_OWNERS,
                        markers));
                // Populate markers of IN statement.
                for (int i = 0; i < groupIds.length; i++) {
                    stmt.setInt(i + 1, groupIds[i]);
                }

                // Fetch the owners of each group.
                rset = stmt.executeQuery();
                while (rset.next()) {
                    int groupId = rset.getInt("group");
                    owners.get(groupId).add(deserialize(rset));
                }
            }
        }.executeT();

        return owners;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public boolean isUserOwnerOfVideo(final int userId, final int videoId) {
        final boolean[] userIsOwner = new boolean[1];
        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn
                        .prepareStatement(GET_USER_IS_OWNER_OF_VIDEO);
                stmt.setInt(1, userId);
                stmt.setInt(2, videoId);
                ResultSet rset = stmt.executeQuery();
                
                userIsOwner[0] = rset.next();
            }

        }.execute();
        return userIsOwner[0];
    }
    

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public boolean isUserAttendantOfGroup(final int userId, final int videoId) {
        final boolean[] userIsOwner = new boolean[1];
        new SQLRunner(this.pool) {
            @Override
            public void run() throws SQLException {
                stmt = conn
                        .prepareStatement(GET_USER_IS_ATTENDANT_OF_VIDEO);
                stmt.setInt(1, userId);
                stmt.setInt(2, videoId);
                ResultSet rset = stmt.executeQuery();
                
                userIsOwner[0] = rset.next();
            }

        }.execute();
        return userIsOwner[0];
    }

    /**
     * Create an user object with an id.
     * 
     * @param uid
     *            the id that is "generated" from the Database
     * @param userData
     *            the old user object with all existing user data
     * @return the new user object with id uid and user data of userData
     * 
     */
    private User createNewUserObjectWithId(Integer uid, User userData) {
        User newUserObject = new User(uid);

        // If password is stored as plain text or hash (should only be stored as
        // hash...)
        if (userData.getPasswordHash() == null) {
            newUserObject.setPassword(userData.getPassword());
        } else {
            newUserObject.setPasswordHash(userData.getPasswordHash());
        }
        newUserObject.setEmail(userData.getEmail());
        newUserObject.setTitle(userData.getTitle());
        newUserObject.setFirstName(userData.getFirstName());
        newUserObject.setLastName(userData.getLastName());
        newUserObject.setUserType(userData.getUserType());
        newUserObject.setGender(userData.getGender());
        newUserObject.setBirthday(userData.getBirthday());
        newUserObject.setStreet(userData.getStreet());
        newUserObject.setZip(userData.getZip());
        newUserObject.setCity(userData.getCity());
        newUserObject.setCountry(userData.getCountry());
        newUserObject.setPhone(userData.getPhone());
        newUserObject.setFax(userData.getFax());
        newUserObject.setWebsite(userData.getWebsite());
        newUserObject.setVisible(userData.isVisible());
        newUserObject.setPhotoAvailable(userData.isPhotoAvailable());
        newUserObject.setBanned(userData.isBanned());
        newUserObject.setDeletable(userData.isDeletable());
        return newUserObject;
    }
}